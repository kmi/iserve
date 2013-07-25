package uk.ac.open.kmi.iserve.discovery.api.experimental;


import uk.ac.open.kmi.iserve.discovery.api.MatchResult;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

public class ComplexMatch<E> {
    // Maps Matched resource -> Set (sorted) of Elements to match
    private Map<E, SortedSet<SingleMatch<E>>> matchMap = new HashMap<E, SortedSet<SingleMatch<E>>>();

    // Methods to obtain the global match
    public MatchTypes.ValuedType getGlobalMatchType(){
        MatchTypes.ValuedType bestType = null; // TODO Change to FAIL > ValuedType bestType = MatchTypes.FAIL;
        for(SortedSet<SingleMatch<E>> matched : matchMap.values()){
            if (bestType == null || matched.first().getMatchValuedType().compareTo(bestType) > 0){
                bestType = matched.first().getMatchValuedType();
            }
        }
        return bestType;
    }

    public void addMatch(MatchResult result){

    }
}
