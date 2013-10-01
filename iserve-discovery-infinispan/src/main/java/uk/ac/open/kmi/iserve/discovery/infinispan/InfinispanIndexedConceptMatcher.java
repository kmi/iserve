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
import uk.ac.open.kmi.iserve.discovery.infinispan.index.InfinispanIndexFactory;
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
public class InfinispanIndexedConceptMatcher extends AbstractMatcher implements ConceptMatcher {

    private ConcurrentMap<URI, Map<URI, String>> map;

    public InfinispanIndexedConceptMatcher() {
        super(EnumMatchTypes.of(LogicConceptMatchType.class));
        this.map = new InfinispanIndexFactory().createIndex();
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
