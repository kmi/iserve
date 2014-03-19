package uk.ac.open.kmi.iserve.discovery.infinispan;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatcherPluginModule;

/**
 * @author Pablo Rodríguez Mier
 */
public class InfinispanPluginModule extends AbstractModule implements MatcherPluginModule {

    @Override
    protected void configure() {
        MapBinder<String, ConceptMatcher> conceptBinder = MapBinder.newMapBinder(binder(), String.class, ConceptMatcher.class);
        conceptBinder.addBinding(InfinispanIndexedConceptMatcher.class.getName()).to(InfinispanIndexedConceptMatcher.class);
        //bind(ConceptMatcher.class).to(SparqlLogicConceptMatcher.class);
        //bind(KnowledgeBaseManager.class).to(KnowledgeBaseManagerSparql.class);
    }
}
