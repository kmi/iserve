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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;

import java.net.URL;
import java.util.Map;

/**
 * Functions for merging a Multimap of Match Results into a Map of Match Results
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 */
public enum MultimapToMapMerger implements Function<Multimap<URL, MatchResult>, Map<URL, MatchResult>> {

    /**
     * Perform the UNION of the results.
     * The resulting type of the Composite results is determined by the best match
     * {@see MatchResultsMerger}
     */
    UNION {
        @Override
        public Map<URL, MatchResult> apply(Multimap<URL, MatchResult> input) {
            return ImmutableMap.copyOf(Maps.transformValues(input.asMap(),
                    MatchResultsMerger.UNION));
        }

    },

    /**
     * Perform the INTERSECTION of the results.
     * The resulting type of the Composite results is determined by the worst match
     * except for the FAIL cases where we may have Partial Matches
     * {@see MatchResultsMerger}
     */
    INTERSECTION {
        @Override
        public Map<URL, MatchResult> apply(Multimap<URL, MatchResult> input) {
            return ImmutableMap.copyOf(Maps.transformValues(input.asMap(),
                    MatchResultsMerger.INTERSECTION));
        }

    }

}
