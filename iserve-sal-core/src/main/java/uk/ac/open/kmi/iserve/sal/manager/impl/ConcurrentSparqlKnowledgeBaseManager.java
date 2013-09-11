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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.ServiceWriterImpl;
import uk.ac.open.kmi.iserve.commons.io.util.URIUtil;
import uk.ac.open.kmi.iserve.commons.model.Service;
import uk.ac.open.kmi.iserve.commons.vocabulary.*;
import uk.ac.open.kmi.iserve.sal.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.events.OntologyCreatedEvent;
import uk.ac.open.kmi.iserve.sal.events.ServiceCreatedEvent;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.manager.KnowledgeBaseManager;

import javax.inject.Named;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.*;

/**
 * This class is a parametric tool for crawling for RDF data.
 * <p/>
 * todo parameters: timeout, max allowed file size, max allowed files, number of
 * threads, request throttling (global, per-host) todo add logging with
 * ScutterVocab?
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 */
class ConcurrentSparqlKnowledgeBaseManager extends SparqlGraphStoreManager implements KnowledgeBaseManager {

    private static final Logger log = LoggerFactory.getLogger(ConcurrentSparqlKnowledgeBaseManager.class);
    private static final String DIRECT_SUBCLASS = "http://www.openrdf.org/schema/sesame#directSubClassOf";
    private static final String JAVA_PROXY_HOST_PROP = "http.proxyHost";
    private static final String JAVA_PROXY_PORT_PROP = "http.proxyPort";

    // Set backed by a ConcurrentHashMap to avoid race conditions
    private Set<URI> loadedModels;
    private Set<URI> unreachableModels = new HashSet<URI>();
    private int NUM_THREADS = 4;

    private ExecutorService executor;

    /**
     * Static class used for Optional constructor arguments.
     * For more information check http://code.google.com/p/google-guice/wiki/FrequentlyAskedQuestions#How_can_I_inject_optional_parameters_into_a_constructor
     */
    static class ProxyConfiguration {
        @Inject(optional = true)
        @Named(SystemConfiguration.PROXY_HOST_NAME_PROP)
        private String proxyHost = null;
        @Inject(optional = true)
        @Named(SystemConfiguration.PROXY_PORT_PROP)
        private String proxyPort = null;
    }

    ConcurrentSparqlKnowledgeBaseManager(EventBus eventBus,
                                         String iServeUri,
                                         String sparqlQueryEndpoint,
                                         String sparqlUpdateEndpoint,
                                         String sparqlServiceEndpoint) throws SalException {

        this(eventBus, iServeUri, sparqlQueryEndpoint, sparqlUpdateEndpoint, sparqlServiceEndpoint, new ProxyConfiguration());
    }


    /**
     * default constructor
     */
    @Inject
    ConcurrentSparqlKnowledgeBaseManager(EventBus eventBus,
                                         @Named(SystemConfiguration.ISERVE_URL_PROP) String iServeUri,
                                         @Named(SystemConfiguration.SERVICES_REPOSITORY_SPARQL_PROP) String sparqlQueryEndpoint,
                                         @Named(SystemConfiguration.SERVICES_REPOSITORY_SPARQL_UPDATE_PROP) String sparqlUpdateEndpoint,
                                         @Named(SystemConfiguration.SERVICES_REPOSITORY_SPARQL_SERVICE_PROP) String sparqlServiceEndpoint,
                                         ProxyConfiguration proxyCfg) throws SalException {

        super(eventBus, iServeUri, sparqlQueryEndpoint, sparqlUpdateEndpoint, sparqlServiceEndpoint);

        this.loadedModels = Collections.newSetFromMap(new ConcurrentHashMap<URI, Boolean>());
        this.loadedModels.add(URI.create(RDF.getURI()));
        this.loadedModels.add(URI.create(RDFS.getURI()));
        this.loadedModels.add(URI.create(OWL.NS));
        this.loadedModels.add(URI.create(WSMO_LITE.NS));
        this.loadedModels.add(URI.create(SAWSDL.NS));
        this.loadedModels.add(URI.create(MSM.NS));
        this.loadedModels.add(URI.create(HRESTS.NS));
        this.loadedModels.add(URI.create(MSM_WSDL.NS));
        this.loadedModels.add(URI.create("http://www.w3.org/ns/wsdl-extensions#"));  // for WSDLX safety

        // Configure proxy if necessary
        configureProxy(proxyCfg);

        // Set default values for Document Manager
        OntDocumentManager dm = OntDocumentManager.getInstance();
        dm.setProcessImports(true);
        dm.setCacheModels(true);

        // set the executor
//        executor = Executors.newFixedThreadPool(NUM_THREADS);
        executor = Executors.newSingleThreadExecutor();
    }

    private void configureProxy(ProxyConfiguration proxyCfg) {
        Properties prop = System.getProperties();
        if (proxyCfg != null && proxyCfg.proxyHost != null && proxyCfg.proxyPort != null) {
            log.info("Configuring proxy: Host - {} - Port {} .", proxyCfg.proxyHost, proxyCfg.proxyPort);
            prop.put(JAVA_PROXY_HOST_PROP, proxyCfg.proxyHost);
            prop.put(JAVA_PROXY_PORT_PROP, proxyCfg.proxyPort);
        } else {
            prop.remove(JAVA_PROXY_HOST_PROP);
            prop.remove(JAVA_PROXY_PORT_PROP);
        }
    }

    /**
     * This method will be called when the server is initialised.
     * If necessary it should take care of updating any indexes on boot time.
     */
    @Override
    public void initialise() {
        // TODO: implement
    }

    /**
     * This method will be called when the server is being shutdown.
     * Ensure a clean shutdown.
     */
    public void shutdown() {
        log.info("Shutting down Knowledge Base crawlers.");
        this.executor.shutdown();
        // waiting 2 seconds
        try {
            this.executor.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for threads to conclude crawling.", e);
        }
    }

    @Override
    public boolean containsModel(URI modelUri) {
        return this.loadedModels.contains(modelUri);
    }

    /**
     * Obtains a set with all the models loaded  into this Knowledge Base Manager
     *
     * @return the set of loaded models
     */
    @Override
    public Set<URI> getLoadedModels() {
        return this.loadedModels;
    }

    /**
     * Obtains a set with all the models that have not been reachable at some point. These are models that should be
     * loaded but could not possibly due to the model being temporarily unavailable of the link being broken.
     *
     * @return the set of unreachable models
     */
    @Override
    public Set<URI> getUnreachableModels() {
        return this.unreachableModels;
    }

    @Override
    public void uploadModel(URI modelUri, Model model, boolean forceUpdate) {

        if (modelUri != null && model != null && (!this.loadedModels.contains(modelUri) || forceUpdate)) {
            this.loadedModels.add(modelUri);
            // We are assuming here that uploading won't fail!
            // There is no way to figure this out at the moment with Jena (other than querying after)
            putGraph(modelUri, model);

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
        return false;  // TODO: implement
    }

    /**
     * Given a model, this method will fetch an upload of the models referred to by the service.
     * This is a synchronous implementation that will therefore wait until its fetched and uploaded.
     *
     * @param svc the service to be checked for referred models.
     * @return True if all the models were propertly fetched, false otherwise
     */
    @Override
    public boolean fetchModelsForService(Service svc) {
        boolean result = true;
        Map<URI, Future<Boolean>> concurrentTasks = launchFetchingTasks(svc);

        Boolean fetched;
        for (URI modelUri : concurrentTasks.keySet()) {
            Future<Boolean> f = concurrentTasks.get(modelUri);
            try {
                fetched = f.get();
                result = result && fetched;

                if (!fetched) {
                    this.unreachableModels.add(modelUri);
                    log.error("Cannot load " + modelUri + ". Marked as invalid");
                }
            } catch (Exception e) {
                // Mark as invalid
                log.error("There was an error while trying to fetch a remote model", e);
                this.unreachableModels.add(modelUri);
                log.info("Added {} to the unreachable models list.", modelUri);
                result = false;
            }
        }

        return result;
    }

    /**
     * Given a model, this method will fetch an upload of the models referred to by the service.
     * This is an asynchronous implementation that will not wait until its fetched and uploaded.
     *
     * @param svc the service to be checked for referred models.
     * @return a map with a Future<Boolean> per model to be fetched. True will indicate if the model was properly obtained.
     */
    @Override
    public Map<URI, Future<Boolean>> asyncFetchModelsForService(Service svc) {
        return launchFetchingTasks(svc);
    }

    private Map<URI, Future<Boolean>> launchFetchingTasks(Service svc) {
        Set<URI> modelUris = obtainReferencedModelUris(svc);
        Map<URI, Future<Boolean>> concurrentTasks = new HashMap<URI, Future<Boolean>>();

        for (URI modelUri : modelUris) {
            if (!this.loadedModels.contains(modelUri) && !this.unreachableModels.contains(modelUri)) {
                Callable<Boolean> task = new CrawlCallable(this, modelUri);
                concurrentTasks.put(modelUri, this.executor.submit(task));
                log.debug("Fetching model - {}", modelUri);
            }
        }

        return concurrentTasks;
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
    public List<URI> listEquivalentClasses(URI classUri) {
        List<URI> result = new ArrayList<URI>();
        if (classUri == null)
            return result;

        StringBuilder strBuilder = new StringBuilder()
                .append("select DISTINCT ?class where { \n")
                .append("?class").append(" <").append(OWL.equivalentClass.getURI()).append("> ").append("<").append(classUri.toASCIIString()).append("> .")
                .append(" }");

        return listResourcesByQuery(strBuilder.toString(), "class");
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
    public List<URI> listSubClasses(URI classUri, boolean direct) {
        List<URI> result = new ArrayList<URI>();
        if (classUri == null)
            return result;

        StringBuilder strBuilder = new StringBuilder()
                .append("select DISTINCT ?class where { \n");

        if (direct) {
            strBuilder.append("?class").append(" <").append(DIRECT_SUBCLASS).append("> ").append("<").append(classUri.toASCIIString()).append("> .");
        } else {
            strBuilder.append("?class").append(" <").append(RDFS.subClassOf.getURI()).append("> ").append("<").append(classUri.toASCIIString()).append("> .");
        }

        strBuilder.append(" }");
        return listResourcesByQuery(strBuilder.toString(), "class");
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
    public List<URI> listSuperClasses(URI classUri, boolean direct) {
        List<URI> result = new ArrayList<URI>();
        if (classUri == null)
            return result;

        StringBuilder strBuilder = new StringBuilder()
                .append("select DISTINCT ?class where { \n");

        if (direct) {
            strBuilder.append("<").append(classUri.toASCIIString()).append("> ").append("<").append(DIRECT_SUBCLASS).append("> ").append("?class .");
        } else {
            strBuilder.append("<").append(classUri.toASCIIString()).append("> ").append("<").append(RDFS.subClassOf.getURI()).append("> ").append("?class .");
        }
        strBuilder.append(" }");

        return listResourcesByQuery(strBuilder.toString(), "class");
    }

    @Override
    public List<URI> listConcepts(URI graphID) {

        StringBuilder strBuilder = new StringBuilder()
                .append("select DISTINCT ?class where { \n");

        if (graphID != null) {
            strBuilder.append("GRAPH <").append(graphID).append("> {");
            strBuilder.append("?class ").append("a <").append(RDFS.Class.getURI()).append("> .");
            strBuilder.append(" } ");
        } else {
            strBuilder.append("?class ").append("a <").append(RDFS.Class.getURI()).append("> .");
        }

        strBuilder.append(" }");

        return listResourcesByQuery(strBuilder.toString(), "class");
    }

    private List<URI> listResourcesByQuery(String queryStr, String variableName) {

        List<URI> result = new ArrayList<URI>();
        // If the SPARQL endpoint does not exist return immediately.
        if (this.getSparqlQueryEndpoint() == null) {
            return result;
        }

        // Query the engine
        Query query = QueryFactory.create(queryStr);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(this.getSparqlQueryEndpoint().toASCIIString(), query);

        try {
            // TODO: Remove profiling
            long startTime = System.currentTimeMillis();

            ResultSet qResults = qexec.execSelect();

            // TODO: Remove profiling
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            log.info("Time taken for querying the registry: " + duration);

            Resource resource;
            URI matchUri;
            // Iterate over the results obtained
            while (qResults.hasNext()) {
                QuerySolution soln = qResults.nextSolution();

                // Get the match URL
                resource = soln.getResource(variableName);

                if (resource != null && resource.isURIResource()) {
                    matchUri = new URI(resource.getURI());
                    result.add(matchUri);
                } else {
                    log.warn("Skipping result as the URL is null");
                    break;
                }
            }
        } catch (URISyntaxException e) {
            log.error("Error obtaining match result. Expected a correct URI", e);
        } finally {
            qexec.close();
        }
        return result;
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

    @Subscribe
    public void handleServiceCreated(ServiceCreatedEvent event) {
        log.debug("Processing Service Created Event {}", event);
        this.fetchModelsForService(event.getService());
    }
}
