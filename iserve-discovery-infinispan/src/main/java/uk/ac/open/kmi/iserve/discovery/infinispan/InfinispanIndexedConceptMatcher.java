package uk.ac.open.kmi.iserve.discovery.infinispan;

import com.google.common.collect.Table;
import com.google.inject.Inject;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.api.MatchTypes;
import uk.ac.open.kmi.iserve.discovery.api.impl.AbstractMatcher;
import uk.ac.open.kmi.iserve.discovery.api.impl.AtomicMatchResult;
import uk.ac.open.kmi.iserve.discovery.api.impl.EnumMatchTypes;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.discovery.disco.impl.SparqlLogicConceptMatcher;
import uk.ac.open.kmi.iserve.sal.manager.impl.iServeFacade;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Pablo Rodríguez Mier
 */
public class InfinispanIndexedConceptMatcher extends AbstractMatcher implements ConceptMatcher {

    private Cache<URI, Map<URI, String>> map;
    private SparqlLogicConceptMatcher matcher;
    private iServeFacade facade;

    @Inject
    public InfinispanIndexedConceptMatcher(SparqlLogicConceptMatcher matcher) {
        super(EnumMatchTypes.of(LogicConceptMatchType.class));
        // Configure infinispan to obtain a cache
        this.facade = iServeFacade.getInstance();
        this.map = new DefaultCacheManager().getCache();
        this.matcher = matcher;
        populate(map);
        warmup(map);
    }

    // TODO: This should not be here. A separate crawler is required to update the index
    private void populate(Cache<URI,Map<URI, String>> map) {
        Set<URI> classes = new HashSet<URI>(this.facade.getKnowledgeBaseManager().listConcepts(null));
        Table<URI, URI, MatchResult> table = matcher.listMatchesAtLeastOfType(classes, LogicConceptMatchType.Plugin);
        for(URI origin : table.rowKeySet()){
            Map<URI, String> destMatch = new HashMap<URI, String>();
            Map<URI, MatchResult> dest = table.row(origin);
            for(URI destUri : dest.keySet()){
                destMatch.put(destUri, dest.get(destUri).getMatchType().name());
            }
            map.put(origin, destMatch);
        }
    }

    private void warmup(Cache<URI,Map<URI, String>> map) {
        for(URI key : map.keySet()){
            System.out.println(key + " -> " + map.get(key));
        }
    }

    @Override
    public String getMatcherDescription() {
        return "Infinispan matcher";
    }

    @Override
    public String getMatcherVersion() {
        return "Infinispan 0.1v";
    }

    @Override
    public MatchTypes<MatchType> getMatchTypesSupported() {
        return EnumMatchTypes.of(LogicConceptMatchType.class);
    }

    @Override
    public MatchResult match(URI origin, URI destination) {
        // Get match by origin
        String matchType = this.map.get(origin).get(destination);
        if (matchType!=null){
            // Disco match type
            LogicConceptMatchType type = LogicConceptMatchType.valueOf(matchType);
            return new AtomicMatchResult(origin, destination, type, this);

        }
        // Return fail
        return new AtomicMatchResult(origin, destination, LogicConceptMatchType.Fail, this);
    }

    @Override
    public Map<URI, MatchResult> listMatchesWithinRange(URI origin, MatchType minType, MatchType maxType) {
        throw new UnsupportedOperationException();
    }

}
