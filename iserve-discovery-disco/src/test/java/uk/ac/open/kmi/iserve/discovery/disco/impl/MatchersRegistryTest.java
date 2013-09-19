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

package uk.ac.open.kmi.iserve.discovery.disco.impl;

import com.google.inject.Guice;
import com.google.inject.Injector;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.PluginModuleLoader;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatcherPluginModule;
import uk.ac.open.kmi.iserve.discovery.api.MatchersRegistry;
import uk.ac.open.kmi.iserve.discovery.api.ServiceMatcher;
import uk.ac.open.kmi.iserve.sal.manager.impl.iServeManagementModule;

import java.util.Map;

/**
 * MatchersRegistryTest
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 18/09/2013
 */
public class MatchersRegistryTest {

    private static final Logger log = LoggerFactory.getLogger(MatchersRegistryTest.class);

    private MatchersRegistry registry;

    @Before
    public void setUp() throws Exception {
        // Load all matcher plugins
        PluginModuleLoader<MatcherPluginModule> matcherPlugin = PluginModuleLoader.of(MatcherPluginModule.class);
        Injector inj = Guice.createInjector(matcherPlugin, new iServeManagementModule());
        registry = inj.getInstance(MatchersRegistry.class);
    }

    @Test
    public void testPluginLoading() {
        log.info("Loaded Concept Matchers: {}", registry.getConceptMatchers().size());
        Assert.assertEquals(2, registry.getConceptMatchers().size());

        for (Map.Entry<String, ConceptMatcher> matcher : registry.getConceptMatchers().entrySet()) {
            log.info("Concept Matcher: Name {}", matcher.getKey());
        }

        log.info("Loaded Service Matchers: {}", registry.getServiceMatchers().size());
        Assert.assertEquals(0, registry.getServiceMatchers().size());

        for (Map.Entry<String, ServiceMatcher> matcher : registry.getServiceMatchers().entrySet()) {
            log.info("Service Matcher registered: {}", matcher.getKey());
        }

    }

}
