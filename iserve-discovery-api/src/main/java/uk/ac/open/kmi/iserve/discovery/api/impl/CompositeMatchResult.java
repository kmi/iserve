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

import java.net.URL;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Composite Match Results capture detailed match results within inner matches.
 * This allows capturing both complex match results as well as the traces of
 * these results for ulterior processing (scoring, ranking, explanation, etc)
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @author <a href="mailto:pablo.rodriguez.mier@usc.es">Pablo Rodriguez Mier</a> (CITIUS - Universidad de Santiago de Compostela)
 */
public class CompositeMatchResult extends MatchResultImpl {

    private SortedSet<MatchResult> innerMatches = new TreeSet<MatchResult>();

    public CompositeMatchResult(URL resourceToMatch, URL matchedResource, Matcher matcher, MatchType matchType, String matchLabel, Float score) {
        super(resourceToMatch, matchedResource, matcher, matchType, matchLabel, score);
    }

    /**
     * Adds an inner match to this composite object
     *
     * @param innerMatch the match to add
     */
    public void addInnerMatch(MatchResult innerMatch) {
        if (innerMatch != null) {
            this.innerMatches.add(innerMatch);
        }
    }

    /**
     * Get inner matches for this composite object
     *
     * @return the inner matches
     */
    public Set<MatchResult> getInnerMatches() {
        return this.innerMatches;
    }

    /**
     * Set the inner matches for this composite object
     *
     * @param innerMatches
     */
    public void setInnerMatches(SortedSet<MatchResult> innerMatches) {
        this.innerMatches = innerMatches;
    }

    /**
     * Removes an inner match from this composite object
     *
     * @param innerMatch the match to remove
     */
    public void removeInnerMatch(MatchResult innerMatch) {
        if (innerMatch != null) {
            this.innerMatches.remove(innerMatch);
        }
    }

    @Override
    public String getExplanation() {
        StringBuilder result = new StringBuilder();
        result.append("Composite Match of type: ")
                .append(this.getMatchType().name())
                .append(". ").
                append("Total Score: ")
                .append(this.getScore())
                .append(". ");

        for (MatchResult match : innerMatches) {
            result.append("Inner match - ")
                    .append(match.getExplanation())
                    .append(". ");
        }
        return result.toString();
    }
}
