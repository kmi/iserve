/*
 * Created by IntelliJ IDEA.
 * User: cp3982
 * Date: 30/10/2013
 * Time: 13:50
 */
package uk.ac.open.kmi.iserve.discovery.engine.rest;

import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.ServletModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.PluginModuleLoader;
import uk.ac.open.kmi.iserve.discovery.api.MatcherPluginModule;
import uk.ac.open.kmi.iserve.discovery.api.ranking.Ranker;
import uk.ac.open.kmi.iserve.discovery.api.ranking.ScoreComposer;
import uk.ac.open.kmi.iserve.discovery.api.ranking.Scorer;
import uk.ac.open.kmi.iserve.discovery.api.ranking.impl.BasicScoreComposer;
import uk.ac.open.kmi.iserve.discovery.api.ranking.impl.StandardRanker;
import uk.ac.open.kmi.iserve.discovery.freetextsearch.FreeTextSearchProvider;
import uk.ac.open.kmi.iserve.sal.manager.impl.RegistryManagementModule;

public class DiscoveryRestModule extends ServletModule {

    private Logger logger = LoggerFactory.getLogger(DiscoveryRestModule.class);

    @Override
    protected void configureServlets() {
        logger.debug("Loading Discovery Rest module...");
        install(new RegistryManagementModule());
        // Load all matcher plugins
        install(PluginModuleLoader.of(MatcherPluginModule.class));

        // ServletModule defines the "request" scope. GuiceFilter creates/destroys the scope on each incoming request.
        install(new ServletModule());

        //Scorers configuration
        Multibinder<uk.ac.open.kmi.iserve.discovery.api.ranking.Filter> filterBinder = Multibinder.newSetBinder(binder(), uk.ac.open.kmi.iserve.discovery.api.ranking.Filter.class);

        //Scorers configuration
        Multibinder<Scorer> scorerBinder = Multibinder.newSetBinder(binder(), Scorer.class);
        bind(ScoreComposer.class).to(BasicScoreComposer.class);
        bind(Ranker.class).to(StandardRanker.class);
        bind(DiscoveryResultsBuilderPlugin.class).to(DiscoveryResultsBuilder.class);

        //Free text search
        install(new FreeTextSearchProvider());

    }

}
