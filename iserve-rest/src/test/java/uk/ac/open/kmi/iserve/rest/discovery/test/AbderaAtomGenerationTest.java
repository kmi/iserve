package uk.ac.open.kmi.iserve.rest.discovery.test;

import com.google.common.base.Stopwatch;
import com.google.gson.JsonParser;
import org.apache.abdera.model.Feed;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.discovery.api.DiscoveryEngine;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.util.Pair;
import uk.ac.open.kmi.iserve.rest.RestModule;
import uk.ac.open.kmi.iserve.rest.discovery.DiscoveryResult;
import uk.ac.open.kmi.iserve.rest.discovery.DiscoveryResultsBuilder;
import uk.ac.open.kmi.iserve.rest.util.AbderaAtomFeedProvider;

import java.net.URI;
import java.util.Map;

/**
 * Created by Luca Panziera on 01/05/2014.
 */
@RunWith(JukitoRunner.class)
public class AbderaAtomGenerationTest {
    private static Logger logger = LoggerFactory.getLogger(AbderaAtomGenerationTest.class);

    @Test
    public void discoveryTest(DiscoveryEngine discoveryEngine, DiscoveryResultsBuilder discoveryResultsBuilder) {

        URI modelA = URI.create("http://schema.org/InformAction");

        String query = "{ \"discovery\": { \"func-rdfs\": { \"classes\": \"" + modelA + "\", \"type\":\"svc\" } }, \"ranking\":\"standard\" }";

        Map<URI, Pair<Double, MatchResult>> result = discoveryEngine.discover(new JsonParser().parse(query));

        Stopwatch stopwatch = new Stopwatch().start();
        Map<URI, DiscoveryResult> discoveryResults = discoveryResultsBuilder.build(result, "standard");
        stopwatch.stop();

        Feed feed = new AbderaAtomFeedProvider().generateDiscoveryFeed("test", discoveryEngine.toString(), discoveryResults);

        logger.info("Discovery Result building complete in {}", stopwatch);
        logger.info(feed.toString());

    }

    /**
     * JukitoModule.
     */
    public static class InnerModule extends JukitoModule {
        @Override
        protected void configureTest() {
            install(new RestModule());
        }

    }

}
