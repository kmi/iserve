/*
 * Copyright (c) 2014. Knowledge Media Institute - The Open University
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

package uk.ac.open.kmi.iserve.discovery.api.ranking.impl;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;
import uk.ac.open.kmi.iserve.discovery.api.ranking.Ranker;

import java.net.URI;
import java.util.Map;
import java.util.SortedMap;

/**
 * Created by Luca Panziera on 22/04/2014.
 */
public class StandardRanker implements Ranker {

    @Override
    public SortedMap<URI, Double> rank(Map<URI, Double> map) {

        Ordering<URI> byScoreOrdering = Ordering.natural().reverse().onResultOf(Functions.forMap(map)).compound(Ordering.usingToString());

        return ImmutableSortedMap.copyOf(map, byScoreOrdering);
    }

}
