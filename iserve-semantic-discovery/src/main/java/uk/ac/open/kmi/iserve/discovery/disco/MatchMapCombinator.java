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
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

/**
 * Enum containing the main combinators for Maps of MatchResults
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 */
public enum MatchMapCombinator implements
        Function<Collection<Map<URI, MatchResult>>, Multimap<URI, MatchResult>> {

    /**
     * Perform the UNION of two Maps of results and store it into a Multimap.
     * This multimap should subsequently be post-processed and merged into a
     * Map.
     */
    UNION {
        /* (non-Javadoc)
         * @see com.google.common.base.Function#apply(java.lang.Object)
         */
        @Override
        public Multimap<URI, MatchResult> apply(Collection<Map<URI, MatchResult>> input) {

            Multimap<URI, MatchResult> result = HashMultimap.create();

            if (input == null || input.isEmpty()) {
                return result;
            }

            // Add all elements from the collections
            for (Map<URI, MatchResult> map : input) {
                result.putAll(Multimaps.forMap(map));
            }

            return result;
        }
    },

    /**
     * Perform the INTERSECTION of two Maps of results and store it into a
     * Multimap. This multimap should subsequently be post-processed and merged into a
     * Map.
     */
    INTERSECTION {
        /* (non-Javadoc)
         * @see com.google.common.base.Function#apply(java.lang.Object)
         */
        @Override
        public Multimap<URI, MatchResult> apply(
                Collection<Map<URI, MatchResult>> input) {

            Multimap<URI, MatchResult> result = HashMultimap.create();
            // Check input and return if empty
            if (input == null || input.isEmpty()) {
                return result;
            }

            // Generate a composite predicate that returns true when an element
            // is present in all the collections in input
            Predicate<URI> filter = Predicates.alwaysTrue();
            for (Map<URI, MatchResult> map : input) {
                filter = Predicates.and(filter, Predicates.in(map.keySet()));
            }

            // Add all the unfiltered elements from each collection
            for (Map<URI, MatchResult> map : input) {
                result.putAll(Multimaps.filterKeys(Multimaps.forMap(map), filter));
            }

            return result;
        }

    }

}
