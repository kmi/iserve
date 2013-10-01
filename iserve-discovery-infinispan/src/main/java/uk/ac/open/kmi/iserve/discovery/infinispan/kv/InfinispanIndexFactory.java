package uk.ac.open.kmi.iserve.discovery.infinispan.kv;

import com.google.common.collect.Table;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.impl.MatchersFactory;

import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.discovery.disco.index.IndexFactory;
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
public class InfinispanIndexFactory implements IndexFactory<URI, Map<URI, String>> {
    private iServeFacade facade;
    private ConceptMatcher delegatedMatcher;

    public InfinispanIndexFactory(){
        // Get the default iServe facade
        facade = iServeFacade.getInstance();
        // Get the default concept matcher to populate the index
        delegatedMatcher = MatchersFactory.createConceptMatcher();
    }

    private void populate(Cache<URI, Map<URI, String>> cache) {
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
        Cache<URI, Map<URI, String>> cache = new DefaultCacheManager().getCache();
        populate(cache);
        return cache;
    }
}
