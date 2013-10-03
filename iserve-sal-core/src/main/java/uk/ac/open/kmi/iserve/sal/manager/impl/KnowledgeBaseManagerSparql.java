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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.ServiceWriterImpl;
import uk.ac.open.kmi.iserve.commons.io.util.URIUtil;
import uk.ac.open.kmi.iserve.commons.model.Service;
import uk.ac.open.kmi.iserve.commons.vocabulary.*;
import uk.ac.open.kmi.iserve.core.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.events.OntologyCreatedEvent;
import uk.ac.open.kmi.iserve.sal.events.ServiceCreatedEvent;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.manager.IntegratedComponent;
import uk.ac.open.kmi.iserve.sal.manager.KnowledgeBaseManager;
import uk.ac.open.kmi.iserve.sal.manager.SparqlGraphStoreManager;

import javax.inject.Named;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * This class is a parametric tool for crawling for RDF data.
 * <p/>
 * todo parameters: timeout, max allowed file size, max allowed files, number of
 * threads, request throttling (global, per-host) todo add logging with
 * ScutterVocab?
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 */
public class KnowledgeBaseManagerSparql extends IntegratedComponent implements KnowledgeBaseManager {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseManagerSparql.class);
    private static final String DIRECT_SUBCLASS = "http://www.openrdf.org/schema/sesame#directSubClassOf";
    private static final String JAVA_PROXY_HOST_PROP = "http.proxyHost";
    private static final String JAVA_PROXY_PORT_PROP = "http.proxyPort";

    // To load
    private static final URI WSMO_LITE_URI = URI.create(WSMO_LITE.NS);
    private static final URI MSM_URI = URI.create(MSM.NS);
    private static final URI HRESTS_URI = URI.create(HRESTS.NS);
    private static final URI MSM_WSDL_URI = URI.create(MSM_WSDL.NS);
//    private static final URI WSDL_EXTENSIONS_URI = URI.create("http://www.w3.org/ns/wsdl-extensions#");


    // Set backed by a ConcurrentHashMap to avoid race conditions
    // Tracks unreachability and the moment this was last attempted
    private Map<URI, Date> unreachableModels;

    private int NUM_THREADS = 4;

    private SparqlGraphStoreManager graphStoreManager;

//    /**
//     * Static class used for Optional constructor arguments.
//     * For more information check http://code.google.com/p/google-guice/wiki/FrequentlyAskedQuestions#How_can_I_inject_optional_parameters_into_a_constructor
//     */
//    static class ProxyConfiguration {
//        @Inject(optional = true)
//        @Named(SystemConfiguration.PROXY_HOST_NAME_PROP)
//        private String proxyHost = null;
//        @Inject(optional = true)
//        @Named(SystemConfiguration.PROXY_PORT_PROP)
//        private String proxyPort = null;
//    }
//
//    ConcurrentSparqlKnowledgeBaseManager(EventBus eventBus,
//                                         String iServeUri,
//                                         String sparqlQueryEndpoint,
//                                         String sparqlUpdateEndpoint,
//                                         String sparqlServiceEndpoint) throws SalException {
//
//        this(eventBus, iServeUri, sparqlQueryEndpoint, sparqlUpdateEndpoint, sparqlServiceEndpoint, new ProxyConfiguration());
//    }
//
//
//    /**
//     * default constructor
//     */
//    @Inject
//    ConcurrentSparqlKnowledgeBaseManager(EventBus eventBus,
//                                         @Named(SystemConfiguration.ISERVE_URL_PROP) String iServeUri,
//                                         @Named(SystemConfiguration.SERVICES_REPOSITORY_SPARQL_PROP) String sparqlQueryEndpoint,
//                                         @Named(SystemConfiguration.SERVICES_REPOSITORY_SPARQL_UPDATE_PROP) String sparqlUpdateEndpoint,
//                                         @Named(SystemConfiguration.SERVICES_REPOSITORY_SPARQL_SERVICE_PROP) String sparqlServiceEndpoint,
//                                         ProxyConfiguration proxyCfg) throws SalException {

    /**
     * default constructor
     */
    @Inject
    KnowledgeBaseManagerSparql(EventBus eventBus,
                               SparqlGraphStoreFactory graphStoreFactory,
                               @Named(SystemConfiguration.ISERVE_URL_PROP) String iServeUri,
                               @Named(SystemConfiguration.SERVICES_REPOSITORY_SPARQL_PROP) String sparqlQueryEndpoint,
                               @Named(SystemConfiguration.SERVICES_REPOSITORY_SPARQL_UPDATE_PROP) String sparqlUpdateEndpoint,
                               @Named(SystemConfiguration.SERVICES_REPOSITORY_SPARQL_SERVICE_PROP) String sparqlServiceEndpoint) throws SalException {

        super(eventBus, iServeUri);

        Set<URI> defaultModels = ImmutableSet.of();
        Map<String, String> locationMappings = ImmutableMap.of();
        Set<String> ignoredImports = ImmutableSet.of();

        this.graphStoreManager = graphStoreFactory.create(sparqlQueryEndpoint, sparqlUpdateEndpoint, sparqlServiceEndpoint, defaultModels, locationMappings, ignoredImports);
        this.unreachableModels = new ConcurrentHashMap<URI, Date>();
    }

    /**
     * This method will be called when the server is initialised.
     * If necessary it should take care of updating any indexes on boot time.
     */
    @Override
    public void initialise() {
        this.graphStoreManager.initialise();
    }

    /**
     * This method will be called when the server is being shutdown.
     * Ensure a clean shutdown.
     */
    public void shutdown() {
        this.graphStoreManager.shutdown();
    }

    @Override
    public boolean containsModel(URI modelUri) {
        return this.graphStoreManager.containsGraph(modelUri);
    }

    /**
     * Obtains a set with all the models loaded  into this Knowledge Base Manager
     *
     * @return the set of loaded models
     */
    @Override
    public Set<URI> getLoadedModels() {
        return this.graphStoreManager.listStoredGraphs();
    }

    /**
     * Obtains a set with all the models that have not been reachable at some point. These are models that should be
     * loaded but could not possibly due to the model being temporarily unavailable of the link being broken.
     *
     * @return the set of unreachable models
     */
    @Override
    public Set<URI> getUnreachableModels() {
        return this.unreachableModels.keySet();
    }

    @Override
    public void uploadModel(URI modelUri, Model model, boolean forceUpdate) {

        if (modelUri != null && model != null && (!this.containsModel(modelUri) || forceUpdate)) {
            // We are assuming here that uploading won't fail!
            // There is no way to figure this out at the moment with Jena (other than querying after)
            this.graphStoreManager.putGraph(modelUri, model);

            // Generate Event
            this.getEventBus().post(new OntologyCreatedEvent(new Date(), modelUri));
        }
    }

    /**
     * Deletes a model from the Knowledge Base Manager
     * <p/>
     * After successfully deleting a model, implementations of this method should raise a {@code OntologyDeletedEvent}
     *
     * @param modelUri the URI of the model to remove
     * @return true if it was correctly deleted, false otherwise.
     */
    @Override
    public boolean deleteModel(URI modelUri) {
        // We are assuming here that uploading won't fail!
        // There is no way to figure this out at the moment with Jena (other than querying after)
        this.graphStoreManager.deleteGraph(modelUri);
        return true;
    }

    /**
     * Given a service, this method will fetch an upload of the models referred to by the service.
     * This is a synchronous implementation that will therefore wait until its fetched and uploaded.
     *
     * @param svc the service to be checked for referred models.
     * @return True if all the models were properly fetched, false otherwise
     */
    private boolean fetchModelsForService(Service svc) {

        boolean result = true;

        Set<URI> modelUris = obtainReferencedModelUris(svc);
        for (URI modelUri : modelUris) {
            // Only fetch those that are not there and have not been unsucessfully fetched in the last 24 hours
            if (!this.graphStoreManager.containsGraph(modelUri)) {
                boolean fetch = true;
                // If it was previously unreachable we need to check how long ago we tried
                if (this.unreachableModels.containsKey(modelUri)) {
                    Date now = new Date();
                    Date lastAttempt = this.unreachableModels.get(modelUri);
                    long diffHours = (now.getTime() - lastAttempt.getTime()) / (60 * 60 * 1000) % 24;
                    fetch = (diffHours >= 24 ? true : false);
                }

                if (fetch) {
                    boolean isStored = this.graphStoreManager.fetchAndStore(modelUri);
                    if (!isStored) {
                        this.unreachableModels.put(modelUri, new Date());
                    }
                    result = result & isStored;
                }
            }
        }

        return result;
    }

    /**
     * This method checks a set of fetching tasks that are pending to validate their adequate conclusion.
     * For each of the fetching tasks launched we will track the models that were not adequately loaded for ulterior
     * uploading.
     *
     * @param concurrentTasks the Map with the models to fetch and the Future providing the state of the uploading
     * @return True if all the models were adequately uploaded. False otherwise.
     */
    private boolean checkFetchingTasks(Map<URI, Future<Boolean>> concurrentTasks) {
        boolean result = true;
        Boolean fetched;
        for (URI modelUri : concurrentTasks.keySet()) {
            Future<Boolean> f = concurrentTasks.get(modelUri);
            try {
                fetched = f.get();
                result = result && fetched;
                // Track unreachability
                if (fetched && this.unreachableModels.containsKey(modelUri)) {
                    this.unreachableModels.remove(modelUri);
                    log.info("A previously unreachable model has finally been obtained - {}", modelUri);
                }

                if (!fetched) {
                    this.unreachableModels.put(modelUri, new Date());
                    log.error("Cannot load " + modelUri + ". Marked as invalid");
                }
            } catch (Exception e) {
                // Mark as invalid
                log.error("There was an error while trying to fetch a remote model", e);
                this.unreachableModels.put(modelUri, new Date());
                log.info("Added {} to the unreachable models list.", modelUri);
                result = false;
            }
        }

        return result;
    }

    /**
     * Answers a List all of URIs of the classes that are known to be equivalent to this class. Equivalence may be
     * asserted in the model (using, for example, owl:equivalentClass, or may be inferred by the reasoner attached to
     * the model. Note that the OWL semantics entails that every class is equivalent to itself, so when using a
     * reasoning model clients should expect that this class will appear as a member of its own equivalent classes.
     *
     * @param classUri the URI of the class for which to list the equivalent classes
     * @return the List of equivalent classes
     */
    @Override
    public Set<URI> listEquivalentClasses(URI classUri) {

        if (classUri == null)
            return ImmutableSet.of();

        StringBuilder strBuilder = new StringBuilder()
                .append("select DISTINCT ?class where { \n")
                .append("?class").append(" <").append(OWL.equivalentClass.getURI()).append("> ").append("<").append(classUri.toASCIIString()).append("> .")
                .append(" }");

        return this.graphStoreManager.listResourcesByQuery(strBuilder.toString(), "class");
    }

    /**
     * Answers a List of all the URIs of the classes that are declared to be sub-classes of this class.
     * Note: this is currently dependent on Sesame's specific property for direct subclasses
     *
     * @param classUri the URI of the class for which to list the subclasses.
     * @param direct   if true only the direct subclasses will be listed.
     * @return the list of subclasses
     */
    @Override
    public Set<URI> listSubClasses(URI classUri, boolean direct) {

        if (classUri == null)
            return ImmutableSet.of();

        StringBuilder strBuilder = new StringBuilder()
                .append("select DISTINCT ?class where { \n");

        if (direct) {
            strBuilder.append("?class").append(" <").append(DIRECT_SUBCLASS).append("> ").append("<").append(classUri.toASCIIString()).append("> .");
        } else {
            strBuilder.append("?class").append(" <").append(RDFS.subClassOf.getURI()).append("> ").append("<").append(classUri.toASCIIString()).append("> .");
        }

        strBuilder.append(" }");
        return this.graphStoreManager.listResourcesByQuery(strBuilder.toString(), "class");
    }

    /**
     * Answers a List of all the URIs of the classes that are declared to be super-classes of this class.
     * Note: this is currently dependent on Sesame's specific property for direct subclasses
     *
     * @param classUri the URI of the class for which to list the super-classes.
     * @param direct   if true only the direct super-classes will be listed.
     * @return the list of super-classes
     */
    @Override
    public Set<URI> listSuperClasses(URI classUri, boolean direct) {

        if (classUri == null)
            return ImmutableSet.of();

        StringBuilder strBuilder = new StringBuilder()
                .append("select DISTINCT ?class where { \n");

        if (direct) {
            strBuilder.append("<").append(classUri.toASCIIString()).append("> ").append("<").append(DIRECT_SUBCLASS).append("> ").append("?class .");
        } else {
            strBuilder.append("<").append(classUri.toASCIIString()).append("> ").append("<").append(RDFS.subClassOf.getURI()).append("> ").append("?class .");
        }
        strBuilder.append(" }");

        return this.graphStoreManager.listResourcesByQuery(strBuilder.toString(), "class");
    }

    @Override
    public Set<URI> listConcepts(URI graphID) {

        StringBuilder strBuilder = new StringBuilder()
                .append("select DISTINCT ?class where { \n");

        // Add the graph wrapper if necessary
        if (graphID != null) {
            strBuilder.append("GRAPH <").append(graphID).append("> { ");
        }
        // Pattern for both OWL and RDFS (note that this is only inferred and would not pop up in a named graph (at least in OWLIM)
        strBuilder.append(" { ")
                .append("?class <").append(RDF.type.getURI()).append("> <").append(RDFS.Class.getURI()).append("> .")
                .append(" } UNION { ")
                .append("?class <").append(RDF.type.getURI()).append("> <").append(OWL.Class.getURI()).append("> .")
                .append(" } ");

        // Close the graph wrapper if necessary
        if (graphID != null) {
            strBuilder.append(" } ");
        }

        strBuilder.append(" } ");

        return this.graphStoreManager.listResourcesByQuery(strBuilder.toString(), "class");
    }

    private Set<URI> obtainReferencedModelUris(Service svc) {
        Set<URI> result = new HashSet<URI>();
        if (svc != null) {
            ServiceWriterImpl writer = new ServiceWriterImpl();
            Model svcModel = writer.generateModel(svc);

            // Get all model refs
            RDFNode node;
            NodeIterator modelRefs = svcModel.listObjectsOfProperty(SAWSDL.modelReference);
            while (modelRefs.hasNext()) {
                node = modelRefs.next();
                if (!node.isAnon()) {
                    try {
                        result.add(URIUtil.getNameSpace(node.asResource().getURI()));
                    } catch (URISyntaxException e) {
                        log.error("The namespace from the resource is not a correct URI. Skipping node.", e);
                    }
                }
            }
        }

        return result;
    }

    // Event Processing of internal changes

    @Override
    @Subscribe
    public void handleServiceCreated(ServiceCreatedEvent event) {
        log.debug("Processing Service Created Event {}", event);
        this.fetchModelsForService(event.getService());
    }
}
