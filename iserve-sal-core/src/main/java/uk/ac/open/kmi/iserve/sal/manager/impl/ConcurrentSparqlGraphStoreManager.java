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

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.modify.request.Target;
import com.hp.hpl.jena.sparql.modify.request.UpdateCreate;
import com.hp.hpl.jena.sparql.modify.request.UpdateDrop;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.manager.SparqlGraphStoreManager;
import uk.ac.open.kmi.msm4j.io.Syntax;
import uk.ac.open.kmi.msm4j.util.Vocabularies;

import javax.inject.Named;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.*;

public class ConcurrentSparqlGraphStoreManager implements SparqlGraphStoreManager {

    private static final Logger log = LoggerFactory.getLogger(ConcurrentSparqlGraphStoreManager.class);

    // Default Models that every Graph Store should have in iServe
    private static final URI RDF_URI = URI.create(RDF.getURI());
    private static final URI RDFS_URI = URI.create(RDFS.getURI());
    private static final URI OWL_URI = URI.create(OWL.NS);
    private static final Set<URI> CORE_MODELS = ImmutableSet.of(RDF_URI, RDFS_URI, OWL_URI);

    private static final String JAVA_PROXY_HOST_PROP = "http.proxyHost";
    private static final String JAVA_PROXY_PORT_PROP = "http.proxyPort";
    private static final int NUM_THREADS = 2;

    // Set backed by a ConcurrentHashMap to avoid race conditions
    private Set<URI> loadedModels;

    // Default model that this Graph Store should have even when it is empty
    // You can see this as default initialisation graphs that should be pre-loaded
    private Set<URI> baseModels;

    // Executor for doing the fetching of remote models
    private ExecutorService executor;

    private DatasetAccessor datasetAccessor;

    private URI sparqlQueryEndpoint;
    // To use SPARQL Update for modification
    private URI sparqlUpdateEndpoint;
    // To use SPARQL HTTP protocol for graph modification
    private URI sparqlServiceEndpoint;

    // Ontology Model Specification to use with Jena
    private OntModelSpec modelSpec;

    // Keeps track of currently ongoing fetching tasks
    private Map<URI, Future<Boolean>> fetchingMap;

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

    @Inject
    ConcurrentSparqlGraphStoreManager(
            @Assisted("queryEndpoint") String sparqlQueryEndpoint,
            @Assisted("updateEndpoint") String sparqlUpdateEndpoint,
            @Assisted("serviceEndpoint") String sparqlServiceEndpoint,
            @Assisted("baseModels") Set<URI> baseModels,
            @Assisted("locationMappings") Map<String, String> locationMappings,
            @Assisted("ignoredImports") Set<String> ignoredImports,
            ProxyConfiguration proxyCfg) throws SalException {

        // Setup backend access

        if (sparqlQueryEndpoint == null) {
            log.error(ConcurrentSparqlGraphStoreManager.class.getSimpleName() + " requires a SPARQL Query endpoint.");
            throw new SalException(ConcurrentSparqlGraphStoreManager.class.getSimpleName() + " requires a SPARQL Query endpoint.");
        }

        if (sparqlUpdateEndpoint == null && sparqlServiceEndpoint == null) {
            log.warn(ConcurrentSparqlGraphStoreManager.class.getSimpleName() + " requires a SPARQL Update endpoint to modify data.");
        }

        try {
            this.sparqlQueryEndpoint = new URI(sparqlQueryEndpoint);
            this.sparqlUpdateEndpoint = new URI(sparqlUpdateEndpoint);
            this.sparqlServiceEndpoint = new URI(sparqlServiceEndpoint);

            if (this.sparqlServiceEndpoint != null) {
                this.datasetAccessor = DatasetAccessorFactory.createHTTP(this.sparqlServiceEndpoint.toASCIIString());
            }
        } catch (URISyntaxException e) {
            log.error("URI error configuring SPARQL Graph Store Manager", e);
        }

        this.loadedModels = Collections.newSetFromMap(new ConcurrentHashMap<URI, Boolean>());
        this.loadedModels.addAll(CORE_MODELS);
        this.baseModels = baseModels;
        this.fetchingMap = new ConcurrentHashMap<URI, Future<Boolean>>();

        // Configure proxy if necessary
        configureProxy(proxyCfg);

        setupModelSpecification(locationMappings, ignoredImports);

        // Initialise the thread pool
        this.executor = Executors.newFixedThreadPool(NUM_THREADS);
        // this.executor = Executors.newSingleThreadExecutor();

        // Finally initialise indexes and other structures
        initialise();
    }

    private void configureProxy(ProxyConfiguration proxyCfg) {
        Properties prop = System.getProperties();
        if (proxyCfg != null &&
                proxyCfg.proxyHost != null && !proxyCfg.proxyHost.isEmpty() &&
                proxyCfg.proxyPort != null && !proxyCfg.proxyPort.isEmpty()) {
            log.info("Configuring proxy: Host - {} - Port {} .", proxyCfg.proxyHost, proxyCfg.proxyPort);
            prop.put(JAVA_PROXY_HOST_PROP, proxyCfg.proxyHost);
            prop.put(JAVA_PROXY_PORT_PROP, proxyCfg.proxyPort);
        } else {
            log.info("No proxy required.");
            prop.remove(JAVA_PROXY_HOST_PROP);
            prop.remove(JAVA_PROXY_PORT_PROP);
        }
    }

    /**
     * Setup a the OntModelSpec to be used when parsing services.
     * In particular, setup import redirections, etc.
     *
     * @param locationMappings
     * @param ignoredImports
     * @return
     */
    private void setupModelSpecification(Map<String, String> locationMappings, Set<String> ignoredImports) {

        OntDocumentManager documentManager = new OntDocumentManager();

        // Add mapping specific to OWLS TC 4 tests.
        // Add mappings to local files to save time and avoid connection issues
        for (Map.Entry<String, String> mapping : locationMappings.entrySet()) {
            documentManager.addAltEntry(mapping.getKey(), mapping.getValue());
        }

        // Ignore the imports indicated
        for (String ignoreUri : ignoredImports) {
            documentManager.addIgnoreImport(ignoreUri);
        }

        // Don't follow imports for now until we find a way to control this well
//        documentManager.setProcessImports(true);
        documentManager.setProcessImports(false);

        documentManager.setCacheModels(false);

        // No inferencing here. Leaving this for the backend
        this.modelSpec = new OntModelSpec(OntModelSpec.OWL_MEM);
//        this.modelSpec = new OntModelSpec(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
        this.modelSpec.setDocumentManager(documentManager);
    }

    /**
     * This method will be called when the server is initialised.
     * If necessary it should take care of updating any indexes on boot time.
     */
    private void initialise() {
        // Figure out models loaded
        this.loadedModels.addAll(listStoredGraphs());
        // Check and load default models
        loadDefaultModels();
    }

    /**
     * This method will be called when the server is being shutdown.
     * Ensure a clean shutdown.
     */
    @Override
    public void shutdown() {
        log.info("Shutting down Ontology crawlers.");
        this.executor.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!this.executor.awaitTermination(5, TimeUnit.SECONDS)) {
                this.executor.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!this.executor.awaitTermination(2, TimeUnit.SECONDS))
                    log.error("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            this.executor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Loads the default models for this Graph Store and verifies they were correctly retrieved
     * NOTE: For now models with local mappings cannot be TTL for Jena does not guess the format properly in that case.
     * We need to specify the format while loading in this case.
     *
     * @return True if they were all added correctly. False otherwise.
     */
    private boolean loadDefaultModels() {

        Map<URI, Future<Boolean>> concurrentTasks = new HashMap<URI, Future<Boolean>>();
        for (URI modelUri : baseModels) {
            if (modelUri != null && modelUri.isAbsolute()) {
                concurrentTasks.put(modelUri, this.asyncFetchModel(modelUri, Syntax.N3.getName()));
            }
        }

        boolean result = true;
        for (URI modelUri : concurrentTasks.keySet()) {
            Future<Boolean> f = concurrentTasks.get(modelUri);
            try {
                boolean fetched = f.get();
                result = result && fetched;

                if (!fetched) {
                    log.error("Cannot load default model - {} - The software may not behave correctly. ", modelUri);
                }
            } catch (Exception e) {
                // Mark as invalid
                log.error("There was an error while trying to fetch a remote model - {}", modelUri, e);
                result = false;
            }
        }

        return result;
    }


    /**
     * @return the sparqlQueryEndpoint
     */
    @Override
    public URI getSparqlQueryEndpoint() {
        return this.sparqlQueryEndpoint;
    }

    /**
     * @return the SPARQL update endpoint
     */
    @Override
    public URI getSparqlUpdateEndpoint() {
        return sparqlUpdateEndpoint;
    }

    /**
     * @return the URI of the SPARQL Service
     */
    @Override
    public URI getSparqlServiceEndpoint() {
        return sparqlServiceEndpoint;
    }

    @Override
    public boolean canBeModified() {
        return (sparqlServiceEndpoint != null || sparqlUpdateEndpoint != null);
    }

    /**
     * Add statements to a named model
     *
     * @param graphUri
     * @param data
     */
    @Override
    public void addModelToGraph(URI graphUri, Model data) {
        if (graphUri == null || data == null)
            return;

        // Use HTTP protocol if possible
        if (this.sparqlServiceEndpoint != null) {
            datasetAccessor.add(graphUri.toASCIIString(), data);
        } else {
            this.addModelToGraphSparqlQuery(graphUri, data);
        }
    }

    protected void addModelToGraphSparqlQuery(URI graphUri, Model data) {
        // TODO: Implement
    }

    /**
     * Does the Dataset contain a named graph?
     *
     * @param graphUri
     * @return
     */
    @Override
    public boolean containsGraph(URI graphUri) {

        if (graphUri == null)
            return false;

        return this.loadedModels.contains(graphUri);
    }

    private boolean containsGraphSparqlQuery(URI graphUri) {

        if (graphUri == null)
            return true; // Default graph always exists

        StringBuilder queryStr = new StringBuilder("ASK { GRAPH <").append(graphUri).append("> {?s a ?o} } \n");

        Query query = QueryFactory.create(queryStr.toString());
        QueryExecution qexec = QueryExecutionFactory.sparqlService(this.getSparqlQueryEndpoint().toASCIIString(), query);
        try {
            return qexec.execAsk();
        } finally {
            qexec.close();
        }
    }

    /**
     * Delete a named model of a Dataset
     *
     * @param graphUri
     */
    @Override
    public void deleteGraph(URI graphUri) {

        if (graphUri == null || !this.canBeModified())
            return;

        // Use HTTP protocol if possible
        if (this.sparqlServiceEndpoint != null) {
            this.datasetAccessor.deleteModel(graphUri.toASCIIString());
        } else {
            deleteGraphSparqlUpdate(graphUri);
        }

        this.loadedModels.remove(graphUri);
        log.debug("Graph deleted: {}", graphUri.toASCIIString());
    }

    /**
     * Delete Graph using SPARQL Update
     *
     * @param graphUri
     */
    private void deleteGraphSparqlUpdate(URI graphUri) {
        UpdateRequest request = UpdateFactory.create();
        request.setPrefixMapping(PrefixMapping.Factory.create().setNsPrefixes(Vocabularies.prefixes));
        request.add(new UpdateDrop(graphUri.toASCIIString()));

        // Use create form for Sesame-based engines. TODO: Generalise and push to config.
        UpdateProcessor processor = UpdateExecutionFactory.createRemoteForm(request, this.getSparqlUpdateEndpoint().toASCIIString());
        processor.execute(); // TODO: anyway to know if things went ok?
    }

    /**
     * Gets the default model of a Dataset
     *
     * @return
     */
    protected OntModel getGraph() {

        // Use HTTP protocol if possible
        if (this.sparqlServiceEndpoint != null) {
            return ModelFactory.createOntologyModel(this.modelSpec, datasetAccessor.getModel());
        } else {
            return this.getGraphSparqlQuery();
        }
    }

    private OntModel getGraphSparqlQuery() {
        return getGraphSparqlQuery(null);

    }

    /**
     * Get a named model of a Dataset
     *
     * @param graphUri
     * @return the Ontology Model
     */
    @Override
    public OntModel getGraph(URI graphUri) {
        log.debug("Obtaining graph: {}", graphUri.toASCIIString());

        if (graphUri == null)
            return null;

        // FIXME: For now Jena and Sesame can't talk to each other
        // Jena will ignore a named graph as the output.
        // Use HTTP protocol if possible
//        if (this.sparqlServiceEndpoint != null) {
//            Model model = datasetAccessor.getModel(graphUri);
//            return ModelFactory.createOntologyModel(this.modelSpec, model);
//
//        } else {
        return this.getGraphSparqlQuery(graphUri);
//        }
    }

    /**
     * Obtains the OntModel within a named graph or the default graph if no graphUri is provided
     *
     * @param graphUri
     * @return
     */
    private OntModel getGraphSparqlQuery(URI graphUri) {

        StringBuilder queryStr = new StringBuilder("CONSTRUCT { ?s ?p ?o } \n")
                .append("WHERE {\n");

        // Add named graph if necessary
        if (graphUri != null) {
            queryStr.append("GRAPH <").append(graphUri.toASCIIString()).append("> ");
        }
        queryStr.append("{ ?s ?p ?o } } \n");

        log.debug("Querying graph store: {}", queryStr.toString());

        Query query = QueryFactory.create(queryStr.toString());
        QueryExecution qexec = QueryExecutionFactory.sparqlService(this.getSparqlQueryEndpoint().toASCIIString(), query);
        // Note that we are not using the store specification here to avoid retrieving remote models
//        OntModel resultModel = ModelFactory.createOntologyModel();
        OntModel resultModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
        try {
            qexec.execConstruct(resultModel);
            return resultModel;
        } finally {
            qexec.close();
        }
    }

    /**
     * Put (create/replace) the default graph of a Dataset
     *
     * @param data
     */
    @Override
    public void putGraph(Model data) {

        if (data == null)
            return;

        // Use HTTP protocol if possible
        if (this.sparqlServiceEndpoint != null) {
            datasetAccessor.putModel(data);
        } else {
            this.putGraphSparqlQuery(data);
        }

    }

    private void putGraphSparqlQuery(Model data) {
        this.putGraphSparqlQuery(null, data);
    }

    /**
     * Put (create/replace) a named graph of a Dataset
     *
     * @param graphUri
     * @param data
     */
    @Override
    public void putGraph(URI graphUri, Model data) {
        if (graphUri == null || data == null)
            return;

        // Use HTTP protocol if possible
        if (this.sparqlServiceEndpoint != null) {
            datasetAccessor.putModel(graphUri.toASCIIString(), data);
        } else {
            this.putGraphSparqlQuery(graphUri, data);
        }

        this.loadedModels.add(graphUri);
        log.info("Graph added to store: {}", graphUri.toASCIIString());
    }

    private void putGraphSparqlQuery(URI graphUri, Model data) {

        UpdateRequest request = UpdateFactory.create();
        request.setPrefixMapping(PrefixMapping.Factory.create().setNsPrefixes(Vocabularies.prefixes));
        request.add(new UpdateCreate(graphUri.toASCIIString()));
        request.add(generateInsertRequest(graphUri, data));
        log.debug("Sparql Update Query issued: {}", request.toString());

        // Use create form for Sesame-based engines. TODO: Generalise and push to config.
        UpdateProcessor processor = UpdateExecutionFactory.createRemoteForm(request, this.getSparqlUpdateEndpoint().toASCIIString());
        processor.execute(); // TODO: anyway to know if things went ok?
    }

    private String generateInsertRequest(URI graphUri, Model data) {

        StringWriter out = new StringWriter();
        data.write(out, uk.ac.open.kmi.msm4j.io.Syntax.TTL.getName());
        StringBuilder updateSB = new StringBuilder();
        updateSB.append("INSERT DATA { \n");
        // If a graph is given use it, otherwise insert in the default graph.
        if (graphUri != null) {
            updateSB.append("GRAPH <").append(graphUri.toASCIIString()).append(">");
        }
        updateSB.append(" { \n");
        updateSB.append(out.getBuffer());
        updateSB.append("}\n");
        updateSB.append("}\n");

        String result = updateSB.toString();

        return result;
    }

    /**
     * Empties the entire Dataset
     */
    @Override
    public void clearDataset() {

        // Deleting via graph store protocol does not work apparently.
        // Use HTTP protocol if possible
//        if (this.sparqlServiceEndpoint != null) {
//            datasetAccessor.deleteDefault();
//        } else {
        clearDatasetSparqlUpdate();
//        }
        initialise();
    }

    private void clearDatasetSparqlUpdate() {
        UpdateRequest request = UpdateFactory.create();
        request.add(new UpdateDrop(Target.ALL));
        UpdateProcessor processor = UpdateExecutionFactory.createRemoteForm(request,
                this.getSparqlUpdateEndpoint().toASCIIString());
        processor.execute(); // TODO: any way to know if things went ok?
        log.debug("Dataset cleared.");
    }

    /**
     * Figure out the models that are already in the Knowledge Base
     *
     * @return the Set of URIs of the models loaded
     */
    @Override
    public Set<URI> listStoredGraphs() {

        StringBuilder strBuilder = new StringBuilder()
                .append("SELECT DISTINCT ?graph WHERE {").append("\n")
                .append("GRAPH ?graph {").append("?s ?p ?o").append(" }").append("\n")
                .append("}").append("\n");

        return listResourcesByQuery(strBuilder.toString(), "graph");
    }

    @Override
    public Set<URI> listResourcesByQuery(String queryStr, String variableName) {

        ImmutableSet.Builder<URI> result = ImmutableSet.builder();
        // If the SPARQL endpoint does not exist return immediately.
        if (this.getSparqlQueryEndpoint() == null || queryStr == null || queryStr.isEmpty()) {
            return result.build();
        }

        // Query the engine
        log.debug("Evaluating SPARQL query in Knowledge Base: \n {}", queryStr);
        Query query = QueryFactory.create(queryStr);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(this.getSparqlQueryEndpoint().toASCIIString(), query);

        try {
            Stopwatch stopwatch = new Stopwatch();
            stopwatch.start();

            ResultSet qResults = qexec.execSelect();

            stopwatch.stop();
            log.info("Time taken for querying the registry: {}", stopwatch);

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
        return result.build();
    }

    @Override
    public boolean fetchAndStore(URI modelUri) {
        return fetchAndStore(modelUri, Syntax.RDFXML.getName());
    }

    public boolean fetchAndStore(URI modelUri, String syntax) {
        boolean fetched;
        Future<Boolean> future = asyncFetchModel(modelUri, syntax);

        try {
            fetched = future.get();
            // remove from the fetching map
            this.fetchingMap.remove(modelUri);
            if (!fetched) {
                log.warn("Could not store model - {}", modelUri);
            }

        } catch (InterruptedException e) {
            log.error("The system was interrupted while trying to fetch and store a remote model - " + modelUri, e);
            return false;
        } catch (ExecutionException e) {
            log.error("There was an error while trying to fetch and store a remote model - " + modelUri, e);
            return false;
        }
        return fetched;
    }

    private Future<Boolean> asyncFetchModel(URI modelUri, String syntax) {

        // Check we are not downloading it already
        if (this.fetchingMap.containsKey(modelUri)) {
            return this.fetchingMap.get(modelUri);
        }

        Callable<Boolean> task = new CrawlCallable(this, modelSpec, modelUri, syntax);
        log.debug("Fetching model - {}", modelUri);
        return this.executor.submit(task);
    }

}
