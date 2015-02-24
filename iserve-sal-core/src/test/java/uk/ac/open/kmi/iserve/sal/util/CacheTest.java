package uk.ac.open.kmi.iserve.sal.util;

import junit.framework.Assert;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.ac.open.kmi.iserve.sal.manager.impl.RegistryManagementModule;
import uk.ac.open.kmi.iserve.sal.util.caching.Cache;
import uk.ac.open.kmi.iserve.sal.util.caching.CacheException;
import uk.ac.open.kmi.iserve.sal.util.caching.CacheFactory;

/**
 * Created by Luca Panziera on 24/02/15.
 */
@RunWith(JukitoRunner.class)
public class CacheTest {
    @Test
    public void crateCacheTest(CacheFactory cacheFactory) {

        Cache<String, String> cache;
        try {
            cache = cacheFactory.createPersistentCache("cache-test");
        } catch (CacheException e) {
            cache = cacheFactory.createInMemoryCache("cache-test");
        }
        cache.put("x", "y");
        Assert.assertEquals("y", cache.get("x"));
    }

    public static class InnerModule extends JukitoModule {
        @Override
        protected void configureTest() {
            // Get configuration
            install(new RegistryManagementModule());
        }
    }
}
