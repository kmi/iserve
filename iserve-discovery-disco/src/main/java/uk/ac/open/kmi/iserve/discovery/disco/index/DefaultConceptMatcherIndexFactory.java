package uk.ac.open.kmi.iserve.discovery.disco.index;

import com.google.common.collect.Table;
import com.google.inject.Inject;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.impl.MatchersFactory;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.sal.manager.impl.iServeFacade;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Pablo Rodríguez Mier
 */
public class DefaultConceptMatcherIndexFactory implements IndexFactory<URI, Map<URI, String>> {

    private iServeFacade facade;
    private ConceptMatcher delegatedMatcher;
    private MapFactory factory;

    public DefaultConceptMatcherIndexFactory(){
        // Get the default iServe facade
        facade = iServeFacade.getInstance();
        // Get the default concept matcher to populate the index
        delegatedMatcher = MatchersFactory.createConceptMatcher();
    }

    @Inject
    public DefaultConceptMatcherIndexFactory(MapFactory factory){
        // Get the default iServe facade
        facade = iServeFacade.getInstance();
        // Get the default concept matcher to populate the index
        this.delegatedMatcher = MatchersFactory.createConceptMatcher();
        this.factory = factory;
    }


    private void populate(Map<URI, Map<URI, String>> cache) {
        Set<URI> classes = new HashSet<URI>(facade.getKnowledgeBaseManager().listConcepts(null));
        Table<URI, URI, MatchResult> table = delegatedMatcher.listMatchesAtLeastOfType(classes, LogicConceptMatchType.Plugin);
        for (URI origin : table.rowKeySet()) {
            Map<URI, String> destMatch = new HashMap<URI, String>();
            Map<URI, MatchResult> dest = table.row(origin);
            for (URI destUri : dest.keySet()) {
                destMatch.put(destUri, dest.get(destUri).getMatchType().name());
            }
            cache.put(origin, destMatch);
        }

    }

    @Override
    public ConcurrentMap<URI, Map<URI, String>> createIndex() {
        ConcurrentMap<URI, Map<URI, String>> index = this.factory.createMap();
        populate(index);
        return index;
    }
}