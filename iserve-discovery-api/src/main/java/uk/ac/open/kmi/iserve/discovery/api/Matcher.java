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

}
