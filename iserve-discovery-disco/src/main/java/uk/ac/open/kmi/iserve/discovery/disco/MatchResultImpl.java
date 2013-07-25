/*
 * Copyright (c) 2013. Knowledge Media Institute - The Open University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.open.kmi.iserve.discovery.disco;

import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.api.Matcher;

import java.net.URL;

/**
 * Class Description
 *
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public class MatchResultImpl implements MatchResult {

    private URL matchUrl;
    private String matchLabel;
    private MatchType matchType;
    private Float score;
    private URL request;
    private URL engineUrl;


    /**
     * @param url
     * @param label
     */
    public MatchResultImpl(URL url, String label) {
        super();
        this.matchUrl = url;
        this.matchLabel = label;
    }

    /**
     * @param matchUrl
     * @param matchLabel
     * @param matchType
     * @param score
     * @param request
     * @param engineUrl
     */
    public MatchResultImpl(URL matchUrl, String matchLabel,
                           MatchType matchType, float score, URL request, URL engineUrl) {
        super();
        this.matchUrl = matchUrl;
        this.matchLabel = matchLabel;
        this.matchType = matchType;
        this.score = score;
        this.request = request;
        this.engineUrl = engineUrl;
    }


    @Override
    public URL getResourceToMatch() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /* (non-Javadoc)
         * @see SingleMatch#getMatchedResource()
         */
    public URL getMatchedResource() {
        return this.matchUrl;
    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.discovery.api.SingleMatch#getMatchLabel()
     */
    public String getMatchLabel() {
        return this.matchLabel;
    }

    /**
     * @return the matchType
     */
    public MatchType getMatchType() {
        return this.matchType;
    }

    /* (non-Javadoc)
     * @see SingleMatch#getScore()
     */
    public Float getScore() {
        return this.score;
    }

    /* (non-Javadoc)
     * @see SingleMatch#getRequest()
     */
    public URL getRequest() {
        return this.request;
    }

    /* (non-Javadoc)
     * @see SingleMatch#getEngineUrl()
     */
    public URL getEngineUrl() {
        return this.engineUrl;
    }

    /* (non-Javadoc)
     * @see SingleMatch#getExplanation()
     */
    public String getExplanation() {
        return this.matchType.getLongName() + " match with a score of: " +
                this.score;
    }

    /* (non-Javadoc)
     * @see SingleMatch#setScore()
     */
    public void setScore(Float score) {
        this.score = score;
    }

    @Override
    public Matcher getMatcher() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * @param matchType the matchType to set
     */
    public void setMatchType(MatchType matchType) {
        this.matchType = matchType;
    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.discovery.api.SingleMatch#setRequest(java.net.URL)
     */
    public void setRequest(URL request) {
        this.request = request;
    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.discovery.api.SingleMatch#setEngineUrl(java.net.URL)
     */
    public void setEngineUrl(URL engineUrl) {
        this.engineUrl = engineUrl;
    }

}
