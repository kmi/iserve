package uk.ac.open.kmi.iserve.discovery.hazelcast;

import com.google.inject.multibindings.MapBinder;
import uk.ac.open.kmi.iserve.core.ConfiguredModule;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatcherPluginModule;


import javax.inject.Singleton;

/**
 * @author Pablo Rodríguez Mier
 */
public class HazelcastPluginModule extends ConfiguredModule implements MatcherPluginModule {

    @Override
    protected void configure() {
        super.configure();
        MapBinder<String, ConceptMatcher> conceptBinder = MapBinder.newMapBinder(binder(), String.class, ConceptMatcher.class);
        conceptBinder.addBinding(HazelcastIndexedConceptMatcher.class.getName()).to(HazelcastIndexedConceptMatcher.class).in(Singleton.class);

    }
}
