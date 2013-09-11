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
import java.util.Set;

/**
 * RangedMultiMatcher defines the interface for matchers that can provide cross-matches across sets within certain
 * match result boundaries.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @author <a href="mailto:pablo.rodriguez.mier@usc.es">Pablo Rodriguez Mier</a> (CITIUS - Universidad de Santiago de Compostela)
 * @since 11/09/2013
 */
public interface RangedMultiMatcher extends MultiMatcher, RangedMatcher {

    /**
     * Obtains all the matching resources that have a MatchType with the URIs of {@code origin} of the type provided (inclusive) or more.
     *
     * @param origins URIs to match
     * @param minType the minimum MatchType we want to obtain
     * @return a {@link com.google.common.collect.Table} with the result of the matching indexed by origin URI and then destination URI.
     */
    Table<URI, URI, MatchResult> listMatchesAtLeastOfType(Set<URI> origins, MatchType minType);

    /**
     * Obtain all the matching resources that have a MatchTyoe with the URIs of {@code origin} of the type provided (inclusive) or less.
     *
     * @param origins URIs to match
     * @param maxType the maximum MatchType we want to obtain
     * @return a {@link Table} with the result of the matching indexed by origin URI and then destination URI.
     */
    Table<URI, URI, MatchResult> listMatchesAtMostOfType(Set<URI> origins, MatchType maxType);


    /**
     * Obtain all the matching resources with the URIs of {@code origin} within the range of MatchTypes provided, both inclusive.
     *
     * @param origins URIs to match
     * @param minType the minimum MatchType we want to obtain
     * @param maxType the maximum MatchType we want to obtain
     * @return a {@link Table} with the result of the matching indexed by origin URI and then destination URI.
     */
    Table<URI, URI, MatchResult> listMatchesWithinRange(Set<URI> origins, MatchType minType, MatchType maxType);

    /**
     * Obtains all the matching resources that have a precise MatchType with the URIs of {@code origin}.
     *
     * @param origins URIs to match
     * @param type    the MatchType we want to obtain
     * @return a {@link Table} with the result of the matching indexed by origin URI and then destination URI.
     */
    Table<URI, URI, MatchResult> listMatchesOfType(Set<URI> origins, MatchType type);

}
