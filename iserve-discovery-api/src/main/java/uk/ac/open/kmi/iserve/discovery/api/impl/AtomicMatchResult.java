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

package uk.ac.open.kmi.iserve.discovery.api.impl;

import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.api.Matcher;

import java.net.URI;

/**
 * AtomicMatchResult is a basic implementation of MatchResult for atomic matches.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @author <a href="mailto:pablo.rodriguez.mier@usc.es">Pablo Rodriguez Mier</a> (CITIUS - Universidad de Santiago de Compostela)
 * @since 26/07/2013
 */
public class AtomicMatchResult<T extends MatchType> implements MatchResult<MatchType> {

    private URI resourceToMatch;
    private URI matchedResource;
    private Matcher matcher;
    private MatchType matchType;

    public AtomicMatchResult(URI resourceToMatch, URI matchedResource, MatchType matchType, Matcher matcher) {
        this.resourceToMatch = resourceToMatch;
        this.matchedResource = matchedResource;
        this.matcher = matcher;
        this.matchType = matchType;
    }

    /**
     * The resource we wanted to find matches for, i.e., the origin
     *
     * @return the URI of the resource
     */
    @Override
    public URI getResourceToMatch() {
        return resourceToMatch;
    }

    /**
     * The resource that was matched, i.e., the destination
     *
     * @return the URI of the resource matched to
     */
    @Override
    public URI getMatchedResource() {
        return matchedResource;
    }

    /**
     * Gets the matcher that was used to generate this match
     *
     * @return
     */
    @Override
    public Matcher getMatcher() {
        return this.matcher;
    }

    @Override
    public String getExplanation() {
        return new StringBuilder()
                .append(resourceToMatch.toASCIIString())
                .append(" matched to ")
                .append(matchedResource.toASCIIString())
                .append(" with match type ")
                .append(matchType.name())
                .toString();
    }

    /**
     * Get the match type that was assigned to this match result
     *
     * @return
     */
    @Override
    public MatchType getMatchType() {
        return this.matchType;
    }

    @Override
    public int compareTo(MatchType matchType) {
        return 0;  // TODO: implement
    }
}
