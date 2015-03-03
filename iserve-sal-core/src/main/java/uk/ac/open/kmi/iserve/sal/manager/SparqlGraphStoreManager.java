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

package uk.ac.open.kmi.iserve.sal.manager;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;

import java.net.URI;
import java.util.Set;

/**
 * SparqlGraphStoreManager
 * TODO: Provide Description
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 02/10/2013
 */
public interface SparqlGraphStoreManager extends iServeComponent {

    URI getSparqlQueryEndpoint();

    URI getSparqlUpdateEndpoint();

    URI getSparqlServiceEndpoint();

    boolean canBeModified();

    void addModelToGraph(URI graphUri, Model data);

    boolean containsGraph(URI graphUri);

    void deleteGraph(URI graphUri);

    void putGraph(Model data);

    void putGraph(URI graphUri, Model data);

    void clearDataset();

    Set<URI> listStoredGraphs();

    Set<URI> listResourcesByQuery(String queryStr, String variableName);

    boolean fetchAndStore(URI modelUri);

    boolean fetchAndStore(URI modelUri, String syntax);

    OntModel getGraph(URI graphUri);

    URI getGraphUriByResource(URI resource);

    void deleteStatement(Statement triple);
}
