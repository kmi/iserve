package uk.ac.open.kmi.iserve.discovery.index;

import com.google.common.cache.CacheBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Simple factory implementation to create {@link java.util.concurrent.ConcurrentHashMap}
 * @see MapFactory
 * @author Pablo Rodríguez Mier
 */
public class ConcurrentHashMapFactory implements MapFactory {
    private Map<String, ConcurrentMap> factory;

    @Override
    public <K, V> ConcurrentMap<K, V> createMap() {
        return new ConcurrentHashMap<K, V>();
    }

    @Override
    public <K, V> ConcurrentMap<K, V> createMap(String name) {
        ConcurrentMap<K,V> map = factory.get(name);
        if (map == null){
            map = createMap();
            factory.put(name, map);
        }
        return map;
    }
}
