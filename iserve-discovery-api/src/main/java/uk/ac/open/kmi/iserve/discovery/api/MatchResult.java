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
 * Interface that match results obtained from discovery plugins should implement
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @author <a href="mailto:pablo.rodriguez.mier@usc.es">Pablo Rodriguez Mier</a> (CITIUS - Universidad de Santiago de Compostela)
 */
public interface MatchResult {

    /**
     * The resource we wanted to find matches for, i.e., the origin
     *
     * @return the URI of the resource
     */
    public URI getResourceToMatch();

    /**
     * The resource that was matched, i.e., the destination
     *
     * @return the URI of the resource matched to
     */
    public URI getMatchedResource();

    /**
     * Get the match type that was assigned to this match result
     *
     * @return
     */
    public MatchType getMatchType();

    /**
     * Gets the matcher that was used to generate this match
     *
     * @return
     */
    public ConceptMatcher getMatcher();

    public String getExplanation();



}
