package uk.ac.open.kmi.iserve.discovery.api.experimental;


public class SingleMatch<E> implements Comparable<SingleMatch<E>> {
    private E resourceToMatch;
    private E matchedResource;
    private MatchTypes.ValuedType matchValuedType;

    public SingleMatch(E resourceToMatch, E matchedResource, MatchTypes.ValuedType matchType) {
        this.resourceToMatch = resourceToMatch;
        this.matchedResource = matchedResource;
        this.matchValuedType = matchType;
    }

    @Override
    public int compareTo(SingleMatch<E> o) {
        return matchValuedType.compareTo(o.matchValuedType);
    }

    public MatchTypes.ValuedType getMatchValuedType() {
        return matchValuedType;
    }
}
