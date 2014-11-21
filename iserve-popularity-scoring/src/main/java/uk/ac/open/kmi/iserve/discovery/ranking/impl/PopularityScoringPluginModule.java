package uk.ac.open.kmi.iserve.discovery.ranking.impl;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import uk.ac.open.kmi.iserve.discovery.api.ranking.RecommendationPluginModule;
import uk.ac.open.kmi.iserve.discovery.api.ranking.Scorer;

/**
 * Created by Luca Panziera on 21/11/14.
 */
public class PopularityScoringPluginModule extends AbstractModule implements RecommendationPluginModule {
    @Override
    public void configure() {
        Multibinder<Scorer> scorerBinder = Multibinder.newSetBinder(binder(), Scorer.class);
        scorerBinder.addBinding().to(CommunityVitalityScorer.class);
        scorerBinder.addBinding().to(ProviderPopularityScorer.class);
    }
}
