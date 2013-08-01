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

package uk.ac.open.kmi.iserve.discovery.api;

import com.google.common.collect.Table;

import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * The interface {@code Matcher} defines the basic methods to perform a match
 * between two elements.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @author <a href="mailto:pablo.rodriguez.mier@usc.es">Pablo Rodriguez Mier</a> (CITIUS - Universidad de Santiago de Compostela)
 */
public interface Matcher {

    // Information about the matcher

    /**
     * Obtains a brief textual description of the particular Matcher to be presented to the user.
     *
     * @return the textual description
     */
    String getMatcherDescription();

    /**
     * Obtains the particular version of the matcher being used.
     *
     * @return the version
     */
    String getMatcherVersion();

    /**
     * Obtains the MatchTypes instance that contains the MatchTypes supported as well as their ordering information
     *
     * @return
     */
    MatchTypes<MatchType> getMatchTypesSupported();


    /**
     * Perform a match between two URIs (from {@code origin} to {@code destination})
     * and returns the result.
     *
     * @param origin      URI of the element to match
     * @param destination URI of the element to match against
     * @return {@link MatchResult} with the result of the matching.
     */
    MatchResult match(URI origin, URI destination);

    /**
     * Perform a match between two Sets of URIs (from {@code origin} to {@code destination})
     * and returns the result.
     *
     * @param origin      Set of URIs of the elements to match
     * @param destination Set of URIs of the elements to match against
     * @return a {@link Table} with the result of the matching indexed by origin URI and then destination URI.
     */
    Table<URI, URI, MatchResult> match(Set<URI> origin, Set<URI> destination);

    /**
     * Obtains all the matching resources that have a precise MatchType with the URI of {@code origin}.
     *
     * @param origin URI to match
     * @param type   the MatchType we want to obtain
     * @return a Map containing indexed by the URI of the matching resource and containing the particular {@code MatchResult}. If no
     *         result is found the Map should be empty not null.
     */
    Map<URI, MatchResult> listMatchesOfType(URI origin, MatchType type);

    /**
     * Obtains all the matching resources that have a MatchType with the URI of {@code origin} of the type provided (inclusive) or more.
     *
     * @param origin  URI to match
     * @param minType the minimum MatchType we want to obtain
     * @return a Map containing indexed by the URI of the matching resource and containing the particular {@code MatchResult}. If no
     *         result is found the Map should be empty not null.
     */
    Map<URI, MatchResult> listMatchesAtLeastOfType(URI origin, MatchType minType);

    /**
     * Obtain all the matching resources that have a MatchTyoe with the URI of {@code origin} of the type provided (inclusive) or less.
     *
     * @param origin  URI to match
     * @param maxType the maximum MatchType we want to obtain
     * @return a Map containing indexed by the URI of the matching resource and containing the particular {@code MatchResult}. If no
     *         result is found the Map should be empty not null.
     */
    Map<URI, MatchResult> listMatchesAtMostOfType(URI origin, MatchType maxType);

    /**
     * Obtain all the matching resources with the URI of {@code origin} within the range of MatchTypes provided, both inclusive.
     *
     * @param origin  URI to match
     * @param minType the minimum MatchType we want to obtain
     * @param maxType the maximum MatchType we want to obtain
     * @return a Map containing indexed by the URI of the matching resource and containing the particular {@code MatchResult}. If no
     *         result is found the Map should be empty not null.
     */
    Map<URI, MatchResult> listMatchesWithinRange(URI origin, MatchType minType, MatchType maxType);

}
