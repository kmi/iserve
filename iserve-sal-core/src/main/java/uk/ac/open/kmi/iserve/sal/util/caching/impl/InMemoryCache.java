package uk.ac.open.kmi.iserve.sal.util.caching.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.sal.util.caching.Cache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Luca Panziera on 12/02/15.
 */
public class InMemoryCache<K, V> implements Cache<K, V> {
    private Logger log = LoggerFactory.getLogger(InMemoryCache.class);
    private ConcurrentMap<K, V> cacheMap;

    @Inject
    public InMemoryCache(@Assisted String name) {
        log.debug("Creating in-memory cache named {}...", name);
        cacheMap = new ConcurrentHashMap<K, V>();
    }

    @Override
    public int size() {
        return cacheMap.size();
    }

    @Override
    public boolean isEmpty() {
        return cacheMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return cacheMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return cacheMap.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return cacheMap.get(key);
    }

    @Override
    public V put(K key, V value) {
        return cacheMap.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return cacheMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        cacheMap.putAll(m);
    }

    @Override
    public void clear() {
        cacheMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return cacheMap.keySet();
    }

    @Override
    public Collection<V> values() {
        return cacheMap.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return cacheMap.entrySet();
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return cacheMap.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return cacheMap.remove(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return cacheMap.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        return cacheMap.replace(key, value);
    }
}
