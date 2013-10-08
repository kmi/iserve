package uk.ac.open.kmi.iserve.discovery.disco.index;

import java.util.concurrent.ConcurrentMap;

/**
 * Factory of generic concurrent maps
 *
 * @author Pablo Rodríguez Mier
 */
public interface MapFactory {
    /**
     * Create a new generic and empty {@link ConcurrentMap}.
     * @param <K> key type.
     * @param <V> value type.
     * @return new concurrent and empty map.
     */
    <K,V> ConcurrentMap<K,V> createMap();

    /**
     * Retrieve a map identified by a name. Each time
     * {@code createMap} is invoked with a name, the associated
     * map is returned. If there is no map for the name, a new one
     * is created.
     *
     * @param name name that identifies the map
     * @param <K> key type.
     * @param <V> value type.
     * @return concurrent map.
     */
    <K,V> ConcurrentMap<K,V> createMap(String name);
}
