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

package uk.ac.open.kmi.iserve.discovery.util;

import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;

import java.util.Comparator;

/**
 * Reference implementation of basic Comparators methods for Match Results
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @author <a href="mailto:pablo.rodriguez.mier@usc.es">Pablo Rodriguez Mier</a> (CITIUS - Universidad de Santiago de Compostela)
 */
public enum MatchResultComparators implements Comparator<MatchResult> {

    /**
     * Simple comparator for Match Results based on the URIs of the match results
     *
     * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
     */
    BY_URI {
        @Override
        public int compare(MatchResult arg0, MatchResult arg1) {
            return arg0.getMatchedResource().toString().compareTo(arg1.getMatchedResource().toString());
        }
    },

    /**
     * Use the natural ordering of match types.
     *
     * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
     */
    BY_TYPE {
        @Override
        public int compare(MatchResult arg0, MatchResult arg1) {
            // Deal with null values for the Match Results
            if (arg0 == null && arg1 == null) return 0;
            if (arg0 == null) return -1;
            if (arg1 == null) return 1;

            // Deal with null values for the Match Types
            MatchType type0 = arg0.getMatchType();
            MatchType type1 = arg1.getMatchType();
            if (type0 == null && type1 == null) return 0;
            if (type0 == null) return -1;
            if (type1 == null) return 1;

            return type0.compareTo(type1);

        }

    }

}
