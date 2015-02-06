package uk.ac.open.kmi.iserve.sal.util.caching.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.core.RMap;
import uk.ac.open.kmi.iserve.core.ConfigurationProperty;
import uk.ac.open.kmi.iserve.core.iServeProperty;
import uk.ac.open.kmi.iserve.sal.util.caching.Cache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by Luca Panziera on 04/02/15.
 */
public class RedisCache<K, V> implements Cache<K, V> {
    private RMap<K, V> rMap;

    @Inject
    public RedisCache(@iServeProperty(ConfigurationProperty.REDIS_ADDRESS) String address, @Assisted String name) {
        Config config = new Config();
        config.useSingleServer().setAddress(address);

        Redisson redisson = Redisson.create(config);
        rMap = redisson.getMap(name);
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
        return rMap.get(key);
    }

    @Override
    public Object put(Object key, Object value) {
        return rMap.put((K) key, (V) value);
    }

    @Override
    public V remove(Object key) {
        return rMap.remove(key);
    }

    @Override
    public void putAll(Map m) {
        rMap.putAll(m);
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
        return rMap.entrySet();
    }

}
