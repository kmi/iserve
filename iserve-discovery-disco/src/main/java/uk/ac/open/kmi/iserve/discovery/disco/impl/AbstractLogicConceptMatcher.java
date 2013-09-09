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

package uk.ac.open.kmi.iserve.discovery.disco.impl;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.api.MatchTypes;
import uk.ac.open.kmi.iserve.discovery.api.Matcher;
import uk.ac.open.kmi.iserve.discovery.api.impl.EnumMatchTypes;
import uk.ac.open.kmi.iserve.discovery.disco.DiscoMatchType;
import uk.ac.open.kmi.iserve.discovery.util.MatchComparator;

import java.net.URI;
import java.util.Map;


public abstract class AbstractLogicConceptMatcher implements Matcher {

    public static final MatchTypes<MatchType> matchTypes = EnumMatchTypes.of(DiscoMatchType.class);

    // Function to getMatchResult from a Map
    protected final Function<Map.Entry<URI, MatchResult>, MatchResult> getMatchResult =
            new Function<Map.Entry<URI, MatchResult>, MatchResult>() {
                public MatchResult apply(Map.Entry<URI, MatchResult> entry) {
                    return entry.getValue();
                }
            };

    // Order the results by score and then by url
    protected final Ordering<Map.Entry<URI, MatchResult>> entryOrdering =
            Ordering.from(MatchComparator.BY_TYPE).onResultOf(getMatchResult).reverse().
                    compound(Ordering.from(MatchComparator.BY_URI).onResultOf(getMatchResult));


    @Override
    public MatchTypes<MatchType> getMatchTypesSupported() {
        return matchTypes;
    }
}
