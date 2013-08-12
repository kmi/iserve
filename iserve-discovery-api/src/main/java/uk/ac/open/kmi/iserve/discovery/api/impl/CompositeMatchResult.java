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

import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;

import java.net.URI;
import java.util.Set;
import java.util.SortedSet;

/**
 * Composite Match Results capture detailed match results within inner matches.
 * This allows capturing both complex match results as well as the traces of
 * these results for ulterior processing (scoring, ranking, explanation, etc)
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @author <a href="mailto:pablo.rodriguez.mier@usc.es">Pablo Rodriguez Mier</a> (CITIUS - Universidad de Santiago de Compostela)
 */
public class CompositeMatchResult extends AtomicMatchResult {

    private SortedSet<MatchResult> innerMatches;

    public CompositeMatchResult(URI resourceToMatch, URI matchedResource, MatchType matchType, ConceptMatcher matcher,
                                SortedSet<MatchResult> innerMatches) {
        super(resourceToMatch, matchedResource, matchType, matcher);
        this.innerMatches = innerMatches;
    }

    /**
     * Get inner matches for this composite object
     *
     * @return the inner matches
     */
    public Set<MatchResult> getInnerMatches() {
        return this.innerMatches;
    }

    @Override
    public String getExplanation() {
        StringBuilder result = new StringBuilder();
        result.append("Composite Match of type: ")
                .append(this.getMatchType().name())
                .append(". ");

        for (MatchResult match : innerMatches) {
            result.append("Inner match - ")
                    .append(match.getExplanation())
                    .append(". ");
        }
        return result.toString();
    }
}
