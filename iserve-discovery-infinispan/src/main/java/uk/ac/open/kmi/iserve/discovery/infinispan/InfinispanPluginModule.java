package uk.ac.open.kmi.iserve.discovery.infinispan;

import com.google.inject.multibindings.MapBinder;
import uk.ac.open.kmi.iserve.core.ConfiguredModule;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatcherPluginModule;
import uk.ac.open.kmi.iserve.discovery.disco.index.IndexFactory;
import uk.ac.open.kmi.iserve.discovery.infinispan.index.InfinispanIndexFactory;

import javax.inject.Singleton;

/**
 * @author Pablo Rodríguez Mier
 */
public class InfinispanPluginModule extends ConfiguredModule implements MatcherPluginModule {

    @Override
    protected void configure() {
        super.configure();
        MapBinder<String, ConceptMatcher> conceptBinder = MapBinder.newMapBinder(binder(), String.class, ConceptMatcher.class);
        conceptBinder.addBinding(InfinispanIndexedConceptMatcher.class.getName()).to(InfinispanIndexedConceptMatcher.class).in(Singleton.class);
    }
}
