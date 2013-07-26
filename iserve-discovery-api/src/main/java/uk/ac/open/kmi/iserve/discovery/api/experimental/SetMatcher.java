package uk.ac.open.kmi.iserve.discovery.api.experimental;


import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.Matcher;

import java.util.Set;


public class SetMatcher<E> implements Matcher<E> {

    private Matcher<E> matcher;

    public SetMatcher(Matcher<E> matcher){
        this.matcher = matcher;
    }

    /*
    public ComplexMatch match(Set<E> source, Set<E> destination) {
        ComplexMatch<E> result = new ComplexMatch<E>();
        for(E e1 : source){
            for (E e2 : destination){
                MatchResult matchResult = match(e1,e2);
                //result.add()
            }
        }
    }*/

    @Override
    public MatchResult match(E origin, E destination) {
        return this.matcher.match(origin, destination);
    }

    @Override
    public String getMatcherDescription() {
        return matcher.getMatcherDescription() + " (Wrapped)";
    }

    @Override
    public String getMatcherVersion() {
        return matcher.getMatcherVersion();
    }
}
