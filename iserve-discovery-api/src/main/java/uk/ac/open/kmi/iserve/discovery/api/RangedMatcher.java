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

import java.net.URI;
import java.util.Map;

/**
 * RangedMatcher defines the interface for Matchers that can provide match results within certain ranges.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @author <a href="mailto:pablo.rodriguez.mier@usc.es">Pablo Rodriguez Mier</a> (CITIUS - Universidad de Santiago de Compostela)
 * @since 11/09/2013
 */
public interface RangedMatcher extends Matcher {

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
