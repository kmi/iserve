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
import uk.ac.open.kmi.iserve.discovery.api.ranking.AtomicScorer;
import uk.ac.open.kmi.iserve.sal.manager.NfpManager;

import java.net.URI;

/**
 * Created by Luca Panziera on 10/03/2014.
 */
public abstract class PopularityScorer implements AtomicScorer {

    private NfpManager nfpManager;

    @Inject
    public PopularityScorer(NfpManager nfpManager) {
        this.nfpManager = nfpManager;
    }

    public abstract Double apply(URI serviceId);

    public NfpManager getNfpManager() {
        return nfpManager;
    }

}
