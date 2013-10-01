package uk.ac.open.kmi.iserve.discovery.infinispan.kv;

import org.infinispan.manager.DefaultCacheManager;
import uk.ac.open.kmi.iserve.discovery.disco.index.MapFactory;

import java.util.concurrent.ConcurrentMap;

/**
 * @author Pablo Rodríguez Mier
 */
public class InfinispanMapFactory implements MapFactory {
    // TODO; This is not a proper configuration for a cache manager. Improve this
    private static final DefaultCacheManager manager = new DefaultCacheManager();

    @Override
    public <K, V> ConcurrentMap<K, V> createMap() {
        return manager.getCache();
    }

    @Override
    public <K, V> ConcurrentMap<K, V> createMap(String name) {
        return manager.getCache(name);
    }

    public static InfinispanMapFactory newFactory(){
        return new InfinispanMapFactory();
    }
}
