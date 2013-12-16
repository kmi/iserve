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

package uk.ac.open.kmi.iserve.api.impl;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.api.MatchersFactory;
import uk.ac.open.kmi.iserve.api.iServeEngine;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.sal.manager.RegistryManager;

import java.util.Set;

/**
 * iServe Facade provides the main entry point for client applications to use iServe.
 *
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public class iServeEngineImpl implements iServeEngine {

    private static final Logger log = LoggerFactory.getLogger(iServeEngineImpl.class);

    private final RegistryManager registryManager;
    private final MatchersFactory matchersFactory;

    @Inject
    private iServeEngineImpl(RegistryManager registryManager, MatchersFactory matchersFactory) {
        this.registryManager = registryManager;
        this.matchersFactory = matchersFactory;
    }

    /**
     * Registry Manager getter
     *
     * @return the Registry Manager
     */
    @Override
    public RegistryManager getRegistryManager() {
        return registryManager;
    }

    /**
     * Obtains the default Concept Matcher available.
     *
     * @return the Concept Matcher
     */
    @Override
    public ConceptMatcher getDefaultConceptMatcher() {
        return matchersFactory.getDefaultConceptMatcher();
    }

    /**
     * Obtains a concrete Concept Matcher given the className
     *
     * @param className the class name of the matcher we want to obtain
     * @return the concept matcher
     */
    @Override
    public ConceptMatcher getConceptMatcher(String className) {
        return matchersFactory.getConceptMatcher(className);
    }

    /**
     * Lists all the known concept matchers available in the engine
     *
     * @return a Set with all the concept matcher implementations available
     */
    @Override
    public Set<String> listAvailableMatchers() {
        return matchersFactory.listAvailableMatchers();
    }

    @Override
    public void shutdown() {
        registryManager.shutdown();
    }

}
