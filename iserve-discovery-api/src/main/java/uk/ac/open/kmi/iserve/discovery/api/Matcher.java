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

/**
 * The interface {@code Matcher} defines the basic methods to perform a match
 * between two elements.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @author <a href="mailto:pablo.rodriguez.mier@usc.es">Pablo Rodriguez Mier</a> (CITIUS - Universidad de Santiago de Compostela)
 */
public interface Matcher<E> {

    /**
     * Perform a match between two elements (from {@code origin} to {@code destination})
     * and returns the result.
     *
     * @param origin      Element to match
     * @param destination Matched element
     * @return {@link MatchResult} with the result of the matching.
     */
    MatchResult match(E origin, E destination);

    // Information about the matcher
    String getMatcherDescription();

    String getMatcherVersion();
}
