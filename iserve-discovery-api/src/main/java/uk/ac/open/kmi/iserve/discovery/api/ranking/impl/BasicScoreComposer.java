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

import com.google.common.collect.Maps;
import uk.ac.open.kmi.iserve.discovery.api.ranking.ScoreComposer;
import uk.ac.open.kmi.iserve.discovery.api.ranking.Scorer;

import java.net.URI;
import java.util.Map;

/**
 * Created by Luca Panziera on 23/04/2014.
 */
public class BasicScoreComposer implements ScoreComposer {

    @Override
    public Map<URI, Double> compose(Map<Scorer, Map<URI, Double>> localScoresMap) {
        Map<URI, Double> globalScores = Maps.newHashMap();
        double weight = 1 / ((double) localScoresMap.size());
        for (Scorer scorer : localScoresMap.keySet()) {
            for (URI resource : localScoresMap.get(scorer).keySet()) {
                if (!globalScores.containsKey(resource)) {
                    globalScores.put(resource, localScoresMap.get(scorer).get(resource) * weight);
                } else {
                    Double partialSum = globalScores.get(resource);
                    globalScores.put(resource, partialSum + (localScoresMap.get(scorer).get(resource) * weight));
                }
            }
        }
        return globalScores;
    }
}
