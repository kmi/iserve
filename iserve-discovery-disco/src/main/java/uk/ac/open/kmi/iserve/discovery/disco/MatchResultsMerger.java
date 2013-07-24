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
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import uk.ac.open.kmi.iserve.discovery.api.CompositeMatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

/**
 * This Function supports merging two Match Results over the same item into one
 * match. This is necessary when combining different separate matches into one.
 * The resulting match would be a composite one when all the inner matches are.
 * <p/>
 * TODO: Refactor it: there is considerable redundancy in the code here.
 *
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public enum MatchResultsMerger implements Function<Collection<MatchResult>, MatchResult> {

    /**
     * Merge the results obtained from different matches by UNION.
     * This will basically merge all the matches into a composite one.
     * The type of the composite match will be the maximum of the matches found
     *
     * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
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
            URL matchUrl = match.getMatchedResource();
            String matchLabel = match.getMatchLabel();

            CompositeMatchResult result = new CompositeMatchResultImpl(matchUrl, matchLabel);
            result.addInnerMatch(match);

            while (it.hasNext()) {
                match = it.next();
                if (match.getMatchedResource().equals(matchUrl)) {
                    // Just in case check the matches are for the same item
                    result.addInnerMatch(match);
                }
            }

            Function<MatchResult, DiscoMatchType> getMatchType =
                    new Function<MatchResult, DiscoMatchType>() {
                        public DiscoMatchType apply(MatchResult match) {
                            return (DiscoMatchType) match.getMatchType();
                        }
                    };

            MatchType maxType = Ordering.natural().max(Iterables.transform(result.getInnerMatches(), getMatchType));
            result.setMatchType(maxType);

            return result;
        }
    },

    /**
     * Merge the different inner matches into a Composite Match by INTERSECTION.
     * The composite match type is determined by the worst case except when it is
     * a FAIL. In this case, if the best is EXACT or PLUGIN the composite match
     * will be PARTIAL_PLUGIN. If the best is SUBSUMES it is PARTIAL_SUBSUMES.
     * {@see Util}
     *
     * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
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
            URL matchUrl = match.getMatchedResource();
            String matchLabel = match.getMatchLabel();

            CompositeMatchResult result = new CompositeMatchResultImpl(matchUrl, matchLabel);
            result.addInnerMatch(match);

            while (it.hasNext()) {
                match = it.next();
                if (match.getMatchedResource().equals(matchUrl)) {
                    // Just in case check the matches are for the same item
                    result.addInnerMatch(match);
                }
            }

            Function<MatchResult, DiscoMatchType> getMatchType =
                    new Function<MatchResult, DiscoMatchType>() {
                        public DiscoMatchType apply(MatchResult match) {
                            return (DiscoMatchType) match.getMatchType();
                        }
                    };


            Iterable<DiscoMatchType> iter = Iterables.transform(result.getInnerMatches(), getMatchType);
            DiscoMatchType maxType = Ordering.natural().max(iter);
            DiscoMatchType minType = Ordering.natural().min(iter);

            result.setMatchType(Util.calculateCompositeMatchType(maxType, minType));
            return result;

        }

    }
}
