package uk.ac.open.kmi.iserve.index;

import java.util.Map;

/**
 * Factory of generic concurrent maps
 *
 * @author Pablo Rodríguez Mier
 */
public interface MapFactory {
    /**
     * Create a new generic and empty {@link java.util.Map}.
     * @param <K> key type.
     * @param <V> value type.
     * @return new empty map.
     */
    <K,V> Map<K,V> createMap();

    /**
     * Retrieve a map identified by a name. Each time
     * {@code createMap} is invoked with a name, the associated
     * map is returned. If there is no map for the name, a new one
     * is created.
     *
     * @param name name that identifies the map
     * @param <K> key type.
     * @param <V> value type.
     * @return new empty map.
     */
    <K,V> Map<K,V> createMap(String name);
}
