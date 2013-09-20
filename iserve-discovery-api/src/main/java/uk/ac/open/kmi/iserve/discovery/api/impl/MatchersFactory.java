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

import com.google.inject.*;
import uk.ac.open.kmi.iserve.core.PluginModuleLoader;
import uk.ac.open.kmi.iserve.core.SystemConfiguration;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatcherPluginModule;

import javax.inject.Named;
import java.util.Map;

/**
 * MatchersFactory provides a factory for all known Matcher implementations.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 18/09/2013
 */
@Singleton
public class MatchersFactory {

    private static Injector injector = null;

    private static MatchersFactory instance = null;

    // Bind to the provider for lazy instance creation
    private Map<String, Provider<ConceptMatcher>> conceptMatcherBindings = null;

    @Inject(optional = true)
    @Named(SystemConfiguration.DEFAULT_CONCEPT_MATCHER_PROP)
    private final String defaultConceptMatcher = null;

    @Inject
    protected MatchersFactory(Map<String, Provider<ConceptMatcher>> conceptMatcherBindings) {
        this.conceptMatcherBindings = conceptMatcherBindings;
    }

    private static void init() {
        // Load all matcher plugins
        PluginModuleLoader<MatcherPluginModule> matcherPlugin = PluginModuleLoader.of(MatcherPluginModule.class);
        injector = Guice.createInjector(matcherPlugin);
        instance = injector.getInstance(MatchersFactory.class);
    }

    public static ConceptMatcher createConceptMatcher() {
        if (instance == null) {
            init();
        }

        if (instance.conceptMatcherBindings != null && !instance.conceptMatcherBindings.isEmpty()) {
            // Get the default one
            if (instance.defaultConceptMatcher != null) {
                return instance.conceptMatcherBindings.get(instance.defaultConceptMatcher).get();
            }
            // Just return the first one otherwise
            return instance.conceptMatcherBindings.values().iterator().next().get();
        }

        return null;
    }

    public static ConceptMatcher createConceptMatcher(String className) {
        if (instance == null) {
            init();
        }

        if (instance.conceptMatcherBindings != null && !instance.conceptMatcherBindings.isEmpty()) {
            return instance.conceptMatcherBindings.get(className).get();
        }

        return null;
    }

}
