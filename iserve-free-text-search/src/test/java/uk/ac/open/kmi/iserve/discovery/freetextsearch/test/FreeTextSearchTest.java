package uk.ac.open.kmi.iserve.discovery.freetextsearch.test;

import com.google.common.base.Stopwatch;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.discovery.freetextsearch.FreeTextSearchPlugin;
import uk.ac.open.kmi.iserve.discovery.freetextsearch.FreeTextSearchProvider;
import uk.ac.open.kmi.iserve.discovery.freetextsearch.FreeTextSearchResult;
import uk.ac.open.kmi.msm4j.vocabulary.MSM;

import java.net.URI;
import java.util.Set;

/**
 * Created by Luca Panziera on 01/05/2014.
 */
@RunWith(JukitoRunner.class)
public class FreeTextSearchTest {
    private static Logger logger = LoggerFactory.getLogger(FreeTextSearchTest.class);

    @Test
    public void searchTest(FreeTextSearchPlugin freeTextSearchPlugin) {

        Stopwatch stopwatch = new Stopwatch().start();
        Set<FreeTextSearchResult> result = freeTextSearchPlugin.search("facebook");
        stopwatch.stop();
        logger.info("Search complete in {}", stopwatch);
        logger.info(result.toString());

        stopwatch = new Stopwatch().start();
        result = freeTextSearchPlugin.search("facebook", URI.create(MSM.Service.getURI()));
        stopwatch.stop();
        logger.info("Search complete in {}", stopwatch);
        logger.info(result.toString());

    }

    /**
     * JukitoModule.
     */
    public static class InnerModule extends JukitoModule {
        @Override
        protected void configureTest() {
            install(new FreeTextSearchProvider());
        }

    }

}
