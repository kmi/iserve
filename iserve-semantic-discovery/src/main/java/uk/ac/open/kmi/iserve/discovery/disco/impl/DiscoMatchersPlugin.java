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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.multibindings.MapBinder;
import uk.ac.open.kmi.iserve.discovery.api.*;
import uk.ac.open.kmi.iserve.discovery.disco.util.impl.StringToLogicConceptMatchTypeConverter;
import uk.ac.open.kmi.iserve.discovery.util.StringToMatchTypeConverter;
import uk.ac.open.kmi.iserve.sal.manager.impl.RegistryManagementModule;

/**
 * DiscoMatchersPlugin is a Guice module providing a set of Matchers implementation for concept and services matching
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 18/09/2013
 */
public class DiscoMatchersPlugin extends AbstractModule implements MatcherPluginModule {


    @Override
    protected void configure() {
        // Bind Concept Matchers (necessary for composIT-iServe integration)
        MapBinder<String, ConceptMatcher> conceptBinder = MapBinder.newMapBinder(binder(), String.class, ConceptMatcher.class);
        conceptBinder.addBinding(SparqlLogicConceptMatcher.class.getName()).to(SparqlLogicConceptMatcher.class);
        conceptBinder.addBinding(SparqlIndexedLogicConceptMatcher.class.getName()).to(SparqlIndexedLogicConceptMatcher.class);

        RegistryManagementModule rmm = new RegistryManagementModule();
        Injector injector = Guice.createInjector(rmm);
        install(injector.getInstance(ConceptMatcherModule.class));

        // Bind DataflowMatcher
        bind(DataflowMatcher.class).to(OperationDataflowMatcher.class);

        // Single bind discoverers for now
        bind(OperationDiscoverer.class).to(GenericLogicDiscoverer.class);
        bind(ServiceDiscoverer.class).to(GenericLogicDiscoverer.class);
        bind(StringToMatchTypeConverter.class).to(StringToLogicConceptMatchTypeConverter.class);
    }
}
