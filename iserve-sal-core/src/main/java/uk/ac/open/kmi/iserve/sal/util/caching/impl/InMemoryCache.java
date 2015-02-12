package uk.ac.open.kmi.iserve.sal.util.caching.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import uk.ac.open.kmi.iserve.sal.util.caching.Cache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Luca Panziera on 12/02/15.
 */
public class InMemoryCache<K, V> implements Cache<K, V> {

    private Map<K, V> cacheMap;

    @Inject
    public InMemoryCache(@Assisted String name) {
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
}
