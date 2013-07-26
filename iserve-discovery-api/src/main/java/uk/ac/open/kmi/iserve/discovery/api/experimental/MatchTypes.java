package uk.ac.open.kmi.iserve.discovery.api.experimental;


import java.util.SortedSet;

// TODO change to an interface ?
public class MatchTypes {

    public static class ValuedType implements Comparable<ValuedType> {
        private MatchType type;
        private int value;

        public ValuedType(MatchType type, int value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public int compareTo(ValuedType o) {
            return Integer.compare(this.value, o.value);
        }
    }

    private SortedSet<ValuedType> types;

    public void addMatchType(MatchType type){
        types.add(new ValuedType(type,types.last().value+1));
    }

    public void addMatchType(MatchType type, int value){
        types.add(new ValuedType(type, value));
    }

    public static ValuedType failType(){
        return null;
    }
}
