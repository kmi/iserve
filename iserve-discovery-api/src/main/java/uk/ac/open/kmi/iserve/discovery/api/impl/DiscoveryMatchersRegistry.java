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

package uk.ac.open.kmi.iserve.discovery.api.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchersRegistry;
import uk.ac.open.kmi.iserve.discovery.api.ServiceMatcher;

import java.util.HashMap;
import java.util.Map;

/**
 * MatchersRegistry provides a registry were all known Matcher implementations are registered and made available.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 18/09/2013
 */
@Singleton
public class DiscoveryMatchersRegistry implements MatchersRegistry {

    @Inject(optional = true)
    private Map<String, ConceptMatcher> conceptMatchers = new HashMap<String, ConceptMatcher>();

    private ConceptMatcher defaultConceptMatcher = null;

    @Inject(optional = true)
    private Map<String, ServiceMatcher> serviceMatchers = new HashMap<String, ServiceMatcher>();

    private ServiceMatcher defaultServiceMatcher = null;

    protected DiscoveryMatchersRegistry() {
    }

    @Override
    public Map<String, ConceptMatcher> getConceptMatchers() {
        return conceptMatchers;
    }

    @Override
    public Map<String, ServiceMatcher> getServiceMatchers() {
        return serviceMatchers;
    }

    @Override
    public ConceptMatcher getDefaultConceptMatcher() {
        if (defaultConceptMatcher == null && !conceptMatchers.isEmpty()) {
            defaultConceptMatcher = conceptMatchers.values().iterator().next();
        }
        return defaultConceptMatcher;
    }

    @Override
    public ServiceMatcher getDefaultServiceMatcher() {
        if (defaultServiceMatcher == null && !serviceMatchers.isEmpty()) {
            defaultServiceMatcher = serviceMatchers.values().iterator().next();
        }
        return defaultServiceMatcher;
    }

    @Override
    public void setDefaultConceptMatcher(ConceptMatcher defaultConceptMatcher) {
        this.defaultConceptMatcher = defaultConceptMatcher;
    }

    @Override
    public void setDefaultServiceMatcher(ServiceMatcher defaultServiceMatcher) {
        this.defaultServiceMatcher = defaultServiceMatcher;
    }
}
