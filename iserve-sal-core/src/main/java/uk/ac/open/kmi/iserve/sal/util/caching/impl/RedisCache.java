package uk.ac.open.kmi.iserve.sal.util.caching.impl;

import biz.source_code.base64Coder.Base64Coder;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.core.RMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import uk.ac.open.kmi.iserve.core.ConfigurationProperty;
import uk.ac.open.kmi.iserve.core.iServeProperty;
import uk.ac.open.kmi.iserve.sal.util.caching.Cache;
import uk.ac.open.kmi.iserve.sal.util.caching.CacheException;

import java.io.*;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by Luca Panziera on 04/02/15.
 */
public class RedisCache<K, V> implements Cache<K, V> {
    private Logger log = LoggerFactory.getLogger(RedisCache.class);
    private RMap<K, String> rMap;
    private Redisson redisson;

    @Inject
    public RedisCache(@iServeProperty(ConfigurationProperty.REDIS_HOST) String host, @iServeProperty(ConfigurationProperty.REDIS_PORT) String port, @Assisted String name) throws CacheException {
        log.debug("Creating Redis-based cache named {}...", name);
        // TODO remove use of jedis
        try {
            Jedis jedis = new Jedis(host, Integer.valueOf(port));
            jedis.randomKey();
            jedis.close();
        } catch (Exception e) {
            log.debug("Unable to connect to the Redis server");
            throw new CacheException("Unable to connect to the Redis server");
        }

        Config config = new Config();
        config.useSingleServer().setAddress(host + ":" + port);

        redisson = Redisson.create(config);
        rMap = redisson.getMap(name);
        log.debug("Cache created");
    }

    /**
     * Read the object from Base64 string.
     */
    private static Object fromString(String s) {
        if (s != null) {
            try {
                byte[] data = Base64Coder.decode(s);
                ObjectInputStream ois = new ObjectInputStream(
                        new ByteArrayInputStream(data));
                Object o = ois.readObject();
                ois.close();
                return o;

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Write the object to a Base64 string.
     */
    private static String toString(Serializable o) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
            oos.close();
            return new String(Base64Coder.encode(baos.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int size() {
        return rMap.size();
    }

    @Override
    public boolean isEmpty() {
        return rMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return rMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return rMap.containsValue(value);
    }

    @Override
    public V get(Object key) {
        String serializedV = rMap.get(key);
        return (V) fromString(serializedV);
    }

    @Override
    public Object put(Object key, Object value) {
        String serializedValue = toString((Serializable) value);
        return rMap.put((K) key, serializedValue);
    }

    @Override
    public V remove(Object key) {
        return (V) fromString(rMap.remove(key));
    }

    @Override
    public void putAll(Map m) {
        Map<K, String> r = Maps.transformValues(m, new ToStringFunction());
        rMap.putAll(r);
    }

    @Override
    public void clear() {
        rMap.clear();
    }

    @Override
    public Set keySet() {
        return rMap.keySet();
    }

    @Override
    public Collection values() {
        return rMap.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> r = Sets.newHashSet();
        for (Entry<K, String> e : rMap.entrySet()) {
            r.add(new AbstractMap.SimpleEntry<K, V>(e.getKey(), (V) fromString(e.getValue())));
        }
        return r;
    }

    @Override
    public V putIfAbsent(K key, V value) {
        String serializedValue = toString((Serializable) value);
        String r = rMap.putIfAbsent(key, serializedValue);
        if (r == null) {
            return null;
        } else {
            return (V) fromString(r);
        }
    }

    @Override
    public boolean remove(Object key, Object value) {
        return rMap.remove(key, toString((Serializable) value));
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return rMap.replace(key, toString((Serializable) oldValue), toString((Serializable) newValue));
    }

    @Override
    public V replace(K key, V value) {
        if (rMap.containsKey(key)) {
            return (V) fromString(rMap.put(key, toString((Serializable) value)));
        } else return null;
    }

    private class ToStringFunction implements Function<V, String> {

        @Override
        public String apply(V v) {
            return RedisCache.toString((Serializable) v);
        }
    }

}
