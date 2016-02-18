package uk.ac.open.kmi.iserve.api;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.disco.impl.SparqlIndexedLogicConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.disco.impl.SparqlLogicConceptMatcher;
import uk.ac.open.kmi.iserve.sal.manager.RegistryManager;

import java.net.URI;
import java.util.Set;

/**
 * iServeEngineTest
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 29/10/2013
 */
public class iServeEngineTest {

    private static final Logger log = LoggerFactory.getLogger(iServeEngineTest.class);

    @Test
    public void testCreationMethods() throws Exception {

        iServeEngine engine;
        RegistryManager registryManager;
        Set<URI> servicesList1;
        Set<URI> servicesList2;

        // Get default config
        engine = iServeEngineFactory.createEngine();
        Assert.assertNotNull(engine);
        log.info("Obtained an iServe Engine with default configuration - {}", engine);
        registryManager = engine.getRegistryManager();
        log.info("Obtained a Registry Manager - {}", registryManager);
        Assert.assertNotNull(registryManager);
        servicesList1 = registryManager.getServiceManager().listServices();
        Assert.assertNotNull(servicesList1);

//        // Get one specifying the configuration
//        engine = iServeEngineFactory.createEngine("config.properties");
//        Assert.assertNotNull(engine);
//        log.info("Obtained an iServe Engine with specific configuration file - {}", engine);
//        registryManager = engine.getRegistryManager();
//        log.info("Obtained a Registry Manager - {}", registryManager);
//        Assert.assertNotNull(registryManager);
//        servicesList2 = registryManager.getServiceManager().listServices();
//        Assert.assertNotNull(servicesList1);
//
//        Assert.assertEquals(servicesList1.size(), servicesList2.size());
//
//        Sets.SetView<URI> setsDiff = Sets.difference(servicesList1, servicesList2);
//        Assert.assertTrue(setsDiff.isEmpty());
    }

    @Test
    public void testGetRegistryManager() throws Exception {

        RegistryManager registryManager;

        iServeEngine engine = iServeEngineFactory.createEngine();

        registryManager = engine.getRegistryManager();
        log.info("Obtained a Registry Manager - {}", registryManager);
        Assert.assertNotNull(registryManager);
        Set<URI> servicesList1 = registryManager.getServiceManager().listServices();
        Assert.assertNotNull(servicesList1);

        registryManager = engine.getRegistryManager();
        log.info("Obtained a Registry Manager - {}", registryManager);
        Assert.assertNotNull(registryManager);
        Set<URI> servicesList2 = registryManager.getServiceManager().listServices();
        Assert.assertNotNull(servicesList2);

        Assert.assertEquals(servicesList1.size(), servicesList2.size());

        Sets.SetView<URI> setsDiff = Sets.difference(servicesList1, servicesList2);
        Assert.assertTrue(setsDiff.isEmpty());
    }

    @Test
    public void testGetConceptMatcher() throws Exception {

        ConceptMatcher matcher;

        iServeEngine engine = iServeEngineFactory.createEngine();

        // check we get one
        matcher = engine.getDefaultConceptMatcher();
        Assert.assertNotNull(matcher);
        log.info("Obtained a concept matcher - {}", matcher.toString());

        // Check we get one of the class requested
        matcher = engine.getConceptMatcher(SparqlLogicConceptMatcher.class.getName());
        Assert.assertNotNull(matcher);
        log.info("Obtained a SparqlLogicConceptMatcher - {}", matcher.toString());
        Assert.assertEquals(matcher.getClass(), SparqlLogicConceptMatcher.class);

        // Check we get one of the class requested
        matcher = engine.getConceptMatcher(SparqlIndexedLogicConceptMatcher.class
                .getName());
        Assert.assertNotNull(matcher);
        log.info("Obtained a SparqlIndexedLogicConceptMatcher - {}", matcher.toString());
        Assert.assertEquals(matcher.getClass(), SparqlIndexedLogicConceptMatcher.class);

    }

    @Test
    public void testListAvailableMatchers() throws Exception {

        iServeEngine engine = iServeEngineFactory.createEngine();

        Set<String> matchers = engine.listAvailableMatchers();
        log.info("Obtained matchers available - {}", matchers);
        Assert.assertNotNull(matchers);
        Assert.assertTrue(matchers.size() >= 2);

    }
}
