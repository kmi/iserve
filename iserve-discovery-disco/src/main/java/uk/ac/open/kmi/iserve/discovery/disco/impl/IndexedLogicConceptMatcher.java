package uk.ac.open.kmi.iserve.discovery.disco.impl;

import com.google.inject.Inject;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.api.impl.AbstractMatcher;
import uk.ac.open.kmi.iserve.discovery.api.impl.AtomicMatchResult;
import uk.ac.open.kmi.iserve.discovery.api.impl.EnumMatchTypes;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.discovery.disco.index.IndexFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Pablo Rodríguez Mier
 */
public class IndexedLogicConceptMatcher extends AbstractMatcher implements ConceptMatcher {
    ConcurrentMap<URI, Map<URI, String>> matchIndex;

    @Inject
    public IndexedLogicConceptMatcher(IndexFactory matchIndexFactory){
        super(EnumMatchTypes.of(LogicConceptMatchType.class));
        this.matchIndex = matchIndexFactory.createIndex();
    }

    @Override
    public String getMatcherDescription() {
        return "Logic Concept Matcher backed to a custom index";
    }

    @Override
    public String getMatcherVersion() {
        return null;
    }

    @Override
    public MatchResult match(URI origin, URI destination) {
        // Get match by origin
        String matchType = this.matchIndex.get(origin).get(destination);
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
        Map<URI, MatchResult> rangeMatches = new HashMap<URI, MatchResult>();
        Map<URI, String> matches = matchIndex.get(origin);
        // TODO; Revise the matchType model. This should be changed in the future to avoid hard-coded type assumptions.
        if (!(minType instanceof LogicConceptMatchType)){
            throw new IllegalArgumentException("The minType provided is not a valid LogicConceptMatchType");
        }
        if (!(maxType instanceof LogicConceptMatchType)){
            throw new IllegalArgumentException("The maxType provided is not a valid LogicConceptMatchType");
        }
        LogicConceptMatchType logicMaxType = (LogicConceptMatchType)minType;
        LogicConceptMatchType logicMinType = (LogicConceptMatchType)maxType;

        for(Map.Entry<URI, String> entry : matches.entrySet()){
            // Check if is in range
            String mtype = entry.getValue();
            // Logic match type assumed
            LogicConceptMatchType matchType = LogicConceptMatchType.valueOf(mtype);
            if (matchType.compareTo(logicMinType)<=0 && matchType.compareTo(logicMaxType)>=0){
                rangeMatches.put(entry.getKey(), new AtomicMatchResult(origin, entry.getKey(), matchType, this));
            }
        }
        return rangeMatches;
    }
}
