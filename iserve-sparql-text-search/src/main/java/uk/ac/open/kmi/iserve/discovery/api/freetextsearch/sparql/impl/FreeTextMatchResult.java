package uk.ac.open.kmi.iserve.discovery.api.freetextsearch.sparql.impl;

import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.api.Matcher;

import java.net.URI;

/**
 * Created by Luca Panziera on 10/11/14.
 */
public class FreeTextMatchResult implements MatchResult {

    private URI resourceToMatch;
    private URI matchedResource;
    private MatchType matchType;

    public FreeTextMatchResult(URI resourceToMatch, URI matchedResource) {
        this.resourceToMatch = resourceToMatch;
        this.matchedResource = matchedResource;
        this.matchType = new FreeTextMatchType();
    }

    @Override
    public URI getResourceToMatch() {
        return resourceToMatch;
    }

    @Override
    public URI getMatchedResource() {
        return matchedResource;
    }

    @Override
    public MatchType getMatchType() {
        return matchType;
    }

    @Override
    public Matcher getMatcher() {
        return null;
    }

    @Override
    public String getExplanation() {
        return null;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }


}
