package uk.ac.open.kmi.iserve.discovery.disco.index;

import java.util.concurrent.ConcurrentMap;

/**
 * @author Pablo Rodríguez Mier
 */
public interface IndexFactory<K,V> {
    /**
     * Create a typed ready-to-use k/v index
     * @return new index.
     */
    ConcurrentMap<K,V> createIndex();
}
