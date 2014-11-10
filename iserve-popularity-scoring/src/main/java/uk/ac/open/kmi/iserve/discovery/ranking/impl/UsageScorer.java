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

import java.net.URI;

/**
 * Created by Luca Panziera on 10/03/2014.
 */
public class UsageScorer extends PopularityScorer {

    @Inject
    public UsageScorer(NfpManager nfpManager) {
        super(nfpManager);
    }

    @Override
    public Double apply(URI serviceId) {

        Double r = (Double) getNfpManager().getPropertyValue(serviceId, URI.create(MSM_NFP.hasRecentMashups.getURI()), Double.class);
        if (r != null && r > 0) {
            return r / 110;
        }
        return Double.valueOf(0);
    }

    @Override
    public Double apply(URI resource, String parameter) {
        return apply(resource);
    }


}
