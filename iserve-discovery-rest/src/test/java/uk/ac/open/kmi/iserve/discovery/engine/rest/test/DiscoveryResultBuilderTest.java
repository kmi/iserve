package uk.ac.open.kmi.iserve.discovery.engine.rest.test;

import com.google.common.base.Stopwatch;
import com.google.gson.JsonParser;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.discovery.api.DiscoveryEngine;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.engine.rest.DiscoveryRestModule;
import uk.ac.open.kmi.iserve.discovery.engine.rest.DiscoveryResult;
import uk.ac.open.kmi.iserve.discovery.engine.rest.DiscoveryResultsBuilder;
import uk.ac.open.kmi.iserve.discovery.util.Pair;

import java.net.URI;
import java.util.Map;

/**
 * Created by Luca Panziera on 01/05/2014.
 */
@RunWith(JukitoRunner.class)
public class DiscoveryResultBuilderTest {
    private static Logger logger = LoggerFactory.getLogger(DiscoveryResultBuilderTest.class);

    @Test
    public void discoveryTest(DiscoveryEngine discoveryEngine, DiscoveryResultsBuilder discoveryResultsBuilder) {

        URI modelA = URI.create("http://schema.org/InformAction");

        String query = "{ \"discovery\": { \"func-rdfs\": { \"classes\": \"" + modelA + "\", \"type\":\"svc\" } }, \"ranking\":\"standard\" }";

        Map<URI, Pair<Double, MatchResult>> result = discoveryEngine.discover(new JsonParser().parse(query));

        Stopwatch stopwatch = new Stopwatch().start();
        Map<URI, DiscoveryResult> discoveryResults = discoveryResultsBuilder.build(result, "standard");
        stopwatch.stop();

        logger.info("Discovery Result building complete in {}", stopwatch);
        logger.info(discoveryResults.toString());

    }

    /**
     * JukitoModule.
     */
    public static class InnerModule extends JukitoModule {
        @Override
        protected void configureTest() {
            install(new DiscoveryRestModule());
        }

    }

}
