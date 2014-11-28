package uk.ac.open.kmi.iserve.discovery.ranking.impl;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import uk.ac.open.kmi.iserve.discovery.api.ranking.Filter;
import uk.ac.open.kmi.iserve.discovery.api.ranking.RecommendationPluginModule;

/**
 * Created by Luca Panziera on 28/11/14.
 */
public class SwaggerServiceFilterPlugin extends AbstractModule implements RecommendationPluginModule {

    @Override
    protected void configure() {
        Multibinder<Filter> filterBinder = Multibinder.newSetBinder(binder(), Filter.class);
        filterBinder.addBinding().to(SwaggerServiceFilter.class);
    }
}
