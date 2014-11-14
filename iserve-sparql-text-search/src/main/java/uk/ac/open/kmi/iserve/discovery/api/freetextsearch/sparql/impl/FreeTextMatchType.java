package uk.ac.open.kmi.iserve.discovery.api.freetextsearch.sparql.impl;

import uk.ac.open.kmi.iserve.discovery.api.MatchType;

/**
 * Created by Luca Panziera on 10/11/14.
 */
public class FreeTextMatchType implements MatchType {
    @Override
    public String name() {
        return "Free text match";
    }

    @Override
    public String description() {
        return "This is the result of a free text search";
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
