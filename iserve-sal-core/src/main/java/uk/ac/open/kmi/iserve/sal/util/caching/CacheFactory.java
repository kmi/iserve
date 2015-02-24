package uk.ac.open.kmi.iserve.sal.util.caching;

import com.google.inject.name.Named;

/**
 * Created by Luca Panziera on 04/02/15.
 */
public interface CacheFactory {
    @Named("persistent")
    public Cache createPersistentCache(String name) throws CacheException;

    @Named("in-memory")
    public Cache createInMemoryCache(String name);
}
