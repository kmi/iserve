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

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.api.MatchTypes;
import uk.ac.open.kmi.iserve.discovery.api.Matcher;

import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * AbstractMatcher provides a reference implementation for most of the Matcher interface for convenience.
 * Concrete implementations could extend this and just implement the most basic methods in order to have a fully compliant
 * implementation. This reference implementation may well not be the best performing implementation depending on how
 * the matches are obtained. Notably it processes batch requests as a loop of single requests.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 18/09/2013
 */
public abstract class AbstractMatcher implements Matcher {

    private MatchTypes<MatchType> matchTypesSupported;

    public AbstractMatcher(MatchTypes<MatchType> matchTypesSupported) {
        this.matchTypesSupported = matchTypesSupported;
    }

    /**
     * Obtains the MatchTypes instance that contains the MatchTypes supported as well as their ordering information
     *
     * @return
     */
    @Override
    public MatchTypes<MatchType> getMatchTypesSupported() {
        return this.matchTypesSupported;
    }

    /**
     * Perform a match between two Sets of URIs (from {@code origin} to {@code destination})
     * and returns the result.
     *
     * @param origins      Set of URIs of the elements to match
     * @param destinations Set of URIs of the elements to match against
     * @return a {@link com.google.common.collect.Table} with the result of the matching indexed by origin URI and then destination URI.
     */
    @Override
    public Table<URI, URI, MatchResult> match(Set<URI> origins, Set<URI> destinations) {

        ImmutableTable.Builder<URI, URI, MatchResult> builder = ImmutableTable.builder();
        for (URI origin : origins) {
            for (URI destination : destinations) {
                builder.put(origin, destination, this.match(origin, destination));
            }
        }
        return builder.build();
    }

    /**
     * Obtains all the matching resources that have a precise MatchType with the URI of {@code origin}.
     *
     * @param origin URI to match
     * @param type   the MatchType we want to obtain
     * @return a Map containing indexed by the URI of the matching resource and containing the particular {@code MatchResult}. If no
     *         result is found the Map should be empty not null.
     */
    @Override
    public Map<URI, MatchResult> listMatchesOfType(URI origin, MatchType type) {
        return listMatchesWithinRange(origin, type, type);
    }

    /**
     * Obtains all the matching resources that have a MatchType with the URI of {@code origin} of the type provided (inclusive) or more.
     *
     * @param origin  URI to match
     * @param minType the minimum MatchType we want to obtain
     * @return a Map containing indexed by the URI of the matching resource and containing the particular {@code MatchResult}. If no
     *         result is found the Map should be empty not null.
     */
    @Override
    public Map<URI, MatchResult> listMatchesAtLeastOfType(URI origin, MatchType minType) {
        return listMatchesWithinRange(origin, minType, this.matchTypesSupported.getHighest());
    }

    /**
     * Obtain all the matching resources that have a MatchTyoe with the URI of {@code origin} of the type provided (inclusive) or less.
     *
     * @param origin  URI to match
     * @param maxType the maximum MatchType we want to obtain
     * @return a Map containing indexed by the URI of the matching resource and containing the particular {@code MatchResult}. If no
     *         result is found the Map should be empty not null.
     */
    @Override
    public Map<URI, MatchResult> listMatchesAtMostOfType(URI origin, MatchType maxType) {
        return listMatchesWithinRange(origin, this.matchTypesSupported.getLowest(), maxType);
    }

    /**
     * Obtains all the matching resources that have a MatchType with the URIs of {@code origin} of the type provided (inclusive) or more.
     *
     * @param origins URIs to match
     * @param minType the minimum MatchType we want to obtain
     * @return a {@link com.google.common.collect.Table} with the result of the matching indexed by origin URI and then destination URI.
     */
    @Override
    public Table<URI, URI, MatchResult> listMatchesAtLeastOfType(Set<URI> origins, MatchType minType) {
        return listMatchesWithinRange(origins, minType, this.matchTypesSupported.getHighest());
    }

    /**
     * Obtain all the matching resources that have a MatchTyoe with the URIs of {@code origin} of the type provided (inclusive) or less.
     *
     * @param origins URIs to match
     * @param maxType the maximum MatchType we want to obtain
     * @return a {@link com.google.common.collect.Table} with the result of the matching indexed by origin URI and then destination URI.
     */
    @Override
    public Table<URI, URI, MatchResult> listMatchesAtMostOfType(Set<URI> origins, MatchType maxType) {
        return listMatchesWithinRange(origins, this.matchTypesSupported.getLowest(), maxType);
    }

    /**
     * Obtain all the matching resources with the URIs of {@code origin} within the range of MatchTypes provided, both inclusive.
     *
     * @param origins URIs to match
     * @param minType the minimum MatchType we want to obtain
     * @param maxType the maximum MatchType we want to obtain
     * @return a {@link com.google.common.collect.Table} with the result of the matching indexed by origin URI and then destination URI.
     */
    @Override
    public Table<URI, URI, MatchResult> listMatchesWithinRange(Set<URI> origins, MatchType minType, MatchType maxType) {

        ImmutableTable.Builder<URI, URI, MatchResult> builder = ImmutableTable.builder();
        Map<URI, MatchResult> matches;
        for (URI origin : origins) {
            matches = this.listMatchesWithinRange(origin, minType, maxType);
            for (Map.Entry<URI, MatchResult> match : matches.entrySet()) {
                builder.put(origin, match.getKey(), match.getValue());
            }
        }
        return builder.build();
    }

    /**
     * Obtains all the matching resources that have a precise MatchType with the URIs of {@code origin}.
     *
     * @param origins URIs to match
     * @param type    the MatchType we want to obtain
     * @return a {@link com.google.common.collect.Table} with the result of the matching indexed by origin URI and then destination URI.
     */
    @Override
    public Table<URI, URI, MatchResult> listMatchesOfType(Set<URI> origins, MatchType type) {
        return listMatchesWithinRange(origins, type, type);
    }
}
