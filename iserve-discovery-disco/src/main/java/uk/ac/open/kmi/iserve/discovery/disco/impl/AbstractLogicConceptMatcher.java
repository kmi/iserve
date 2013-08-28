package uk.ac.open.kmi.iserve.discovery.disco.impl;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.google.common.collect.Table;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.api.MatchTypes;
import uk.ac.open.kmi.iserve.discovery.api.impl.EnumMatchTypes;
import uk.ac.open.kmi.iserve.discovery.disco.DiscoMatchType;
import uk.ac.open.kmi.iserve.discovery.util.MatchComparator;

import java.net.URI;
import java.util.Map;
import java.util.Set;


public abstract class AbstractLogicConceptMatcher implements ConceptMatcher {

    public static final MatchTypes<MatchType> matchTypes = EnumMatchTypes.of(DiscoMatchType.class);

    // Function to getMatchResult from a Map
    protected final Function<Map.Entry<URI, MatchResult>, MatchResult> getMatchResult =
            new Function<Map.Entry<URI, MatchResult>, MatchResult>() {
                public MatchResult apply(Map.Entry<URI, MatchResult> entry) {
                    return entry.getValue();
                }
            };

    // Order the results by score and then by url
    protected final Ordering<Map.Entry<URI, MatchResult>> entryOrdering =
            Ordering.from(MatchComparator.BY_TYPE).onResultOf(getMatchResult).reverse().
                    compound(Ordering.from(MatchComparator.BY_URI).onResultOf(getMatchResult));


    @Override
    public MatchTypes<MatchType> getMatchTypesSupported() {
        return matchTypes;
    }
}
