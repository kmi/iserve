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

import com.hp.hpl.jena.rdf.model.Model;
import uk.ac.open.kmi.iserve.commons.model.Service;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * KnowledgeBaseManager is in charge of managing ontologies within iServe.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 29/07/2013
 */
public interface KnowledgeBaseManager extends iServeComponent {

    /**
     * Checks whether the model has already been uploaded
     *
     * @param modelUri URI of the model to be checked
     * @return true if the model has already been uploaded, false otherwise.
     */
    public boolean containsModel(URI modelUri);

    /**
     * Obtains a set with all the models loaded  into this Knowledge Base Manager
     *
     * @return the set of loaded models
     */
    public Set<URI> getLoadedModels();

    /**
     * Obtains a set with all the models that have not been reachable at some point. These are models that should be
     * loaded but could not possibly due to the model being temporarily unavailable of the link being broken.
     *
     * @return the set of unreachable models
     */
    public Set<URI> getUnreachableModels();

    /**
     * Uploads a model into the Knowledge Base Manager
     * <p/>
     * After successfully uploading a model, implementations of this method should raise a {@code OntologyCreatedEvent}
     *
     * @param modelUri    URI of the model to upload
     * @param model       the actual model to upload
     * @param forceUpdate if true the model will be updated even if already is there
     */
    public void uploadModel(URI modelUri, Model model, boolean forceUpdate);

    /**
     * Deletes a model from the Knowledge Base Manager
     * <p/>
     * After successfully deleting a model, implementations of this method should raise a {@code OntologyDeletedEvent}
     *
     * @param modelUri the URI of the model to remove
     * @return true if it was correctly deleted, false otherwise.
     */
    public boolean deleteModel(URI modelUri);

    /**
     * Given a model, this method will fetch an upload of the models referred to by the service.
     * This is a synchronous implementation that will therefore wait until its fetched and uploaded.
     *
     * @param svc the service to be checked for referred models.
     * @return True if all the models were propertly fetched, false otherwise
     */
    public boolean fetchModelsForService(Service svc);

    /**
     * Given a model, this method will fetch an upload of the models referred to by the service.
     * This is an asynchronous implementation that will not wait until its fetched and uploaded.
     *
     * @param svc the service to be checked for referred models.
     * @return a map with a Future<Boolean> per model to be fetched. True will indicate if the model was properly obtained.
     */
    public Map<URI, Future<Boolean>> asyncFetchModelsForService(Service svc);

    /**
     * Answers a List all of URIs of the classes that are known to be equivalent to this class. Equivalence may be
     * asserted in the model (using, for example, owl:equivalentClass, or may be inferred by the reasoner attached to
     * the model. Note that the OWL semantics entails that every class is equivalent to itself, so when using a
     * reasoning model clients should expect that this class will appear as a member of its own equivalent classes.
     *
     * @param classUri the URI of the class for which to list the equivalent classes
     * @return the List of equivalent classes
     */
    public Set<URI> listEquivalentClasses(URI classUri);

    /**
     * Answers a List of all the URIs of the classes that are declared to be sub-classes of this class.
     *
     * @param classUri the URI of the class for which to list the subclasses.
     * @param direct   if true only the direct subclasses will be listed.
     * @return the list of subclasses
     */
    public Set<URI> listSubClasses(URI classUri, boolean direct);

    /**
     * Answers a List of all the URIs of the classes that are declared to be super-classes of this class.
     *
     * @param classUri the URI of the class for which to list the super-classes.
     * @param direct   if true only the direct super-classes will be listed.
     * @return the list of super-classes
     */
    public Set<URI> listSuperClasses(URI classUri, boolean direct);


    /**
     * List all concepts managed by the KnowledgeBaseManager.
     *
     * @param graphID Graph URI for filtering the concepts within the specified graphID, null for all concepts.
     * @return List of concepts in the KnowledgeBaseManager
     */
    Set<URI> listConcepts(URI graphID);
}
