package uk.ac.open.kmi.iserve.discovery.disco.impl;


import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.disco.DiscoMatchType;

import java.net.URI;
import java.net.URL;

/**
 * MatchResult default implementation, created for the sole purpose of being able to compile the project.
 * MatchResultImpl was removed in commit b552d8686bb2a1df800eb5b6e46254d4d4a415a5
 * @author Pablo Rodriguez Mier
 */
public class MatchResultImpl implements MatchResult {
    public MatchResultImpl(URL matchUrl, String matchLabel) {

    }

    @Override
    public URI getResourceToMatch() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public URI getMatchedResource() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public MatchType getMatchType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ConceptMatcher getMatcher() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getExplanation() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setMatchType(DiscoMatchType matchType) {
        //this.matchType = matchType;
    }
}
