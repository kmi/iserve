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


import com.google.inject.Inject;
import uk.ac.open.kmi.iserve.sal.manager.NfpManager;
import uk.ac.open.kmi.msm4j.vocabulary.MSM_NFP;
import uk.ac.open.kmi.msm4j.vocabulary.SCHEMA;

import java.net.URI;

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

    public Double apply(URI serviceId) {

        // Get provider
        URI provider = (URI) getNfpManager().getPropertyValue(serviceId, URI.create(SCHEMA.provider.getURI()), URI.class);

        // Get popularity
        if (provider != null) {
            Double r = (Double) getNfpManager().getPropertyValue(provider, URI.create(MSM_NFP.hasPopularity.getURI()), Double.class);
            if (r != null && r >= 0) {
                return r / 100;
            }
        }
        return new Double(0);
    }

}
