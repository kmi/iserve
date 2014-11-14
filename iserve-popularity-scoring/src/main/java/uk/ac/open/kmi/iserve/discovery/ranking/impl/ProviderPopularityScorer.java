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
import uk.ac.open.kmi.msm4j.vocabulary.SCHEMA;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Luca Panziera
 * Date: 07/02/2014
 * Time: 17:27
 */
public class ProviderPopularityScorer extends PopularityScorer {

    @Inject
    public ProviderPopularityScorer(NfpManager nfpManager) {
        super(nfpManager);
    }

    @Override
    public Map<URI, Double> apply(Set<URI> resources) {
        Map<URI, Object> providerObjectMap = getNfpManager().getPropertyValueOfResources(resources, URI.create(SCHEMA.provider.getURI()), URI.class);
        Map<URI, URI> providerMap = Maps.transformValues(providerObjectMap, new Function<Object, URI>() {
            @Override
            public URI apply(Object input) {
                return (URI) input;
            }
        });

        Map<URI, Object> popObjectsMap = getNfpManager().getPropertyValueOfResources(new HashSet<URI>(providerMap.values()), URI.create(MSM_NFP.hasPopularity.getURI()), Double.class);
        Map<URI, Double> popMap = Maps.transformValues(popObjectsMap, new Function<Object, Double>() {
            @Override
            public Double apply(Object input) {
                Double r = (Double) input;
                if (r != null && r > 0) {
                    return r / 100;
                }
                return Double.valueOf(0);
            }
        });
        Map<URI, Double> result = Maps.newHashMap();
        for (URI resource : resources) {
            if (providerMap.get(resource) == null) {
                result.put(resource, new Double(0));
            } else {
                result.put(resource, popMap.get(providerMap.get(resource)));
            }
        }

        return result;
    }

    @Override
    public Map<URI, Double> apply(Set<URI> resources, String parameters) {
        return apply(resources);
    }
}
