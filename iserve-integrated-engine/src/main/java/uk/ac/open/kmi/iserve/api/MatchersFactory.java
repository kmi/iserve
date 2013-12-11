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

package uk.ac.open.kmi.iserve.api;

import com.google.inject.Inject;
import com.google.inject.Provider;
import uk.ac.open.kmi.iserve.core.SystemConfiguration;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;

import javax.inject.Named;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * MatchersFactory provides a factory for all known Matcher implementations.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 18/09/2013
 */
public class MatchersFactory {

    // Bind to the provider for lazy instance creation
    private final Map<String, Provider<ConceptMatcher>> conceptMatcherProviders;

    // TODO: Add configuration details
    @Inject(optional = true)
    @Named(SystemConfiguration.DEFAULT_CONCEPT_MATCHER_PROP)
    private final String defaultConceptMatcher = null;

    @Inject
    protected MatchersFactory(Map<String, Provider<ConceptMatcher>> conceptMatcherProviders) {
        this.conceptMatcherProviders = conceptMatcherProviders;
    }

    public ConceptMatcher getDefaultConceptMatcher() {

        if (this.conceptMatcherProviders != null && !this.conceptMatcherProviders.isEmpty()) {
            // Get the default one
            if (this.defaultConceptMatcher != null) {
                return this.conceptMatcherProviders.get(this.defaultConceptMatcher).get();
            }
            // Just return the first one otherwise
            return this.conceptMatcherProviders.values().iterator().next().get();
        }

        return null;
    }

    public ConceptMatcher getConceptMatcher(String className) {

        if (this.conceptMatcherProviders != null && !this.conceptMatcherProviders.isEmpty()) {
            return this.conceptMatcherProviders.get(className).get();
        }

        return null;
    }

    public Set<String> listAvailableMatchers() {
        return new HashSet<String>(this.conceptMatcherProviders.keySet());
    }
}
