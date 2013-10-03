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

package uk.ac.open.kmi.iserve.sal.manager.impl;

import com.google.inject.assistedinject.Assisted;
import uk.ac.open.kmi.iserve.sal.manager.SparqlGraphStoreManager;

import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * SparqlGraphStoreFactory defines an interface for creating SPARQL Graphs Store Managers.
 * This is based on the Assisted Injection support provided by Guice.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 02/10/2013
 */
public interface SparqlGraphStoreFactory {

    SparqlGraphStoreManager create(@Assisted("queryEndpoint") String sparqlQueryEndpoint,
                                   @Assisted("updateEndpoint") String sparqlUpdateEndpoint,
                                   @Assisted("serviceEndpoint") String sparqlServiceEndpoint,
                                   @Assisted("baseModels") Set<URI> baseModels,
                                   @Assisted("locationMappings") Map<String, String> locationMappings,
                                   @Assisted("ignoredImports") Set<String> ignoredImports);
}
