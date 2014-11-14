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

package uk.ac.open.kmi.iserve.discovery.ranking.impl;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import uk.ac.open.kmi.iserve.sal.manager.NfpManager;
import uk.ac.open.kmi.msm4j.vocabulary.MSM_NFP;

import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * Created by Luca Panziera on 10/03/2014.
 */
public class UsageScorer extends PopularityScorer {

    @Inject
    public UsageScorer(NfpManager nfpManager) {
        super(nfpManager);
    }

    @Override
    public Map<URI, Double> apply(Set<URI> resources) {

        Map<URI, Object> popObjectsMap = getNfpManager().getPropertyValueOfResources(resources, URI.create(MSM_NFP.hasPopularity.getURI()), Double.class);
        Map<URI, Double> result = Maps.transformValues(popObjectsMap, new Function<Object, Double>() {
            @Override
            public Double apply(Object input) {
                Double r = (Double) input;
                if (r != null && r > 0) {
                    return r / 100;
                }
                return Double.valueOf(0);
            }
        });

        return result;
    }

    @Override
    public Map<URI, Double> apply(Set<URI> resources, String parameters) {
        return apply(resources);
    }
}
