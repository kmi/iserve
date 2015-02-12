package uk.ac.open.kmi.iserve.discovery.index;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Pablo Rodríguez Mier
 */
public abstract class MapLoader<K,V> {

    public Map<K,V> loadAll(Iterable<K> keys){
        Map<K,V> map = new HashMap<K, V>();
        for(K key : keys){
            map.put(key, load(key));
        }
        return map;
    }

    public abstract V load(K key);
}
