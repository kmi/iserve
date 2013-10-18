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

package uk.ac.open.kmi.iserve.discovery.disco;

import com.google.common.base.Function;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.api.Matcher;
import uk.ac.open.kmi.iserve.discovery.api.impl.CompositeMatchResult;
import uk.ac.open.kmi.iserve.discovery.util.MatchResultComparators;

import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This Function supports merging two Match Results over the same item into one
 * match. This is necessary when combining different separate matches into one.
 * The resulting match would be a composite one when all the inner matches are.
 * <p/>
 * TODO: Refactor it: there is considerable redundancy in the code here.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 */
public enum MatchResultsMerger implements Function<Collection<MatchResult>, MatchResult> {

    /**
     * Merge the results obtained from different matches by UNION.
     * This will basically merge all the matches into a composite one.
     * The type of the composite match will be the maximum of the matches found
     */
    UNION {
        /* (non-Javadoc)
         * @see com.google.common.base.Function#apply(java.lang.Object)
         */
        @Override
        public MatchResult apply(Collection<MatchResult> input) {

            // Filter out empty or null collections
            if (input == null || input.isEmpty()) {
                return null;
            }

            Iterator<MatchResult> it = input.iterator();
            MatchResult match = it.next();

            if (input.size() == 1) {
                return match;
            }

            // There are at least 2 results
            // Use the first one as reference and ignore matches for different items
            URI matchUri = match.getMatchedResource();
            URI resourceToMatch = match.getResourceToMatch();
            Matcher matcher = match.getMatcher();

            SortedSet<MatchResult> innerMatches = new TreeSet<MatchResult>(MatchResultComparators.BY_TYPE);
            innerMatches.add(match);

            while (it.hasNext()) {
                match = it.next();
                if (match.getResourceToMatch().equals(resourceToMatch) && match.getMatchedResource().equals(matchUri)) {
                    // Just in case check the matches are for the same origin and resource items
                    innerMatches.add(match);
                }
            }

            MatchType maxType = innerMatches.first().getMatchType();
            CompositeMatchResult result = new CompositeMatchResult(resourceToMatch, matchUri, maxType, matcher, innerMatches);
            return result;
        }
    },

    /**
     * Merge the different inner matches into a Composite Match by INTERSECTION.
     * The composite match type is determined by the worst case except when it is
     * a FAIL. In this case, if the best is EXACT or PLUGIN the composite match
     * will be PARTIAL_PLUGIN. If the best is SUBSUMES it is PARTIAL_SUBSUMES.
     * {@see Util}
     */
    INTERSECTION {
        @Override
        public MatchResult apply(Collection<MatchResult> input) {
            // Filter out empty or null collections
            if (input == null || input.isEmpty()) {
                return null;
            }

            Iterator<MatchResult> it = input.iterator();
            MatchResult match = it.next();

            if (input.size() == 1) {
                return match;
            }

            // There are at least 2 results
            // Use the first one as reference and ignore matches for different items
            URI matchUri = match.getMatchedResource();
            URI resourceToMatch = match.getResourceToMatch();
            Matcher matcher = match.getMatcher();

            SortedSet<MatchResult> innerMatches = new TreeSet<MatchResult>(MatchResultComparators.BY_TYPE);
            innerMatches.add(match);

            while (it.hasNext()) {
                match = it.next();
                if (match.getResourceToMatch().equals(resourceToMatch) && match.getMatchedResource().equals(matchUri)) {
                    // Just in case check the matches are for the same item
                    innerMatches.add(match);
                }
            }

            MatchType maxType = innerMatches.first().getMatchType();
            MatchType minType = innerMatches.last().getMatchType();
            MatchType type = Util.calculateCompositeMatchType(maxType, minType);
            CompositeMatchResult result = new CompositeMatchResult(resourceToMatch, matchUri, type, matcher, innerMatches);
            return result;

        }

    }
}
