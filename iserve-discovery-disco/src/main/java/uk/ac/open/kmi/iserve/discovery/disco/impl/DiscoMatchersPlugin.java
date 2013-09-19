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

import com.google.inject.multibindings.MapBinder;
import uk.ac.open.kmi.iserve.core.ConfiguredModule;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatcherPluginModule;

import javax.inject.Singleton;

/**
 * DiscoMatchersPlugin is a Guice module providing a set of Matchers implementation for concept and services matching
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 18/09/2013
 */
public class DiscoMatchersPlugin extends ConfiguredModule implements MatcherPluginModule {

    @Override
    protected void configure() {
        // Ensure we configure it
        super.configure();

        MapBinder<String, ConceptMatcher> conceptBinder = MapBinder.newMapBinder(binder(), String.class, ConceptMatcher.class);
        conceptBinder.addBinding(SparqlLogicConceptMatcher.class.getName()).to(SparqlLogicConceptMatcher.class).in(Singleton.class);
        conceptBinder.addBinding(SparqlIndexedLogicConceptMatcher.class.getName()).to(SparqlIndexedLogicConceptMatcher.class).in(Singleton.class);

//        Multibinder<ServiceMatcher> serviceBinder = Multibinder.newSetBinder(binder(), ServiceMatcher.class);
    }
}
