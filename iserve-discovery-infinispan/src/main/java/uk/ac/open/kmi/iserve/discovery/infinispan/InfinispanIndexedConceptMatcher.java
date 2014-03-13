package uk.ac.open.kmi.iserve.discovery.infinispan;

import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.api.MatchTypes;
import uk.ac.open.kmi.iserve.discovery.api.impl.AbstractMatcher;
import uk.ac.open.kmi.iserve.discovery.api.impl.AtomicMatchResult;
import uk.ac.open.kmi.iserve.discovery.api.impl.EnumMatchTypes;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.discovery.infinispan.index.InfinispanIndexFactory;
import uk.ac.open.kmi.iserve.discovery.util.MatchResultPredicates;
import uk.ac.open.kmi.iserve.sal.manager.KnowledgeBaseManager;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Pablo Rodríguez Mier
 */
public class InfinispanIndexedConceptMatcher extends AbstractMatcher implements ConceptMatcher {

    private ConcurrentMap<URI, Map<URI, MatchResult>> map;

    @Inject
    public InfinispanIndexedConceptMatcher(ConceptMatcher delegatedMatcher, KnowledgeBaseManager kb) {
        super(EnumMatchTypes.of(LogicConceptMatchType.class));
        this.map = new InfinispanIndexFactory(delegatedMatcher, kb).createIndex();
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
        MatchResult result = this.map.get(origin).get(destination);
        if (result!=null){
            return result;
        }
        // Return fail
        return new AtomicMatchResult(origin, destination, LogicConceptMatchType.Fail, this);
    }

    @Override
    public Map<URI, MatchResult> listMatchesWithinRange(URI origin, MatchType minType, MatchType maxType) {
        if (origin == null || minType == null | maxType == null) {
            return ImmutableMap.of();
        }

        // Return an immutable map out of the filtered view. Drop copyOf to obtain a live view
        return ImmutableMap.copyOf(Maps.filterValues(this.map.get(origin), MatchResultPredicates.withinRange(minType, BoundType.CLOSED, maxType, BoundType.CLOSED)));

    }

}
