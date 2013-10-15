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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.ServiceReaderImpl;
import uk.ac.open.kmi.iserve.commons.io.ServiceWriterImpl;
import uk.ac.open.kmi.iserve.commons.io.util.URIUtil;
import uk.ac.open.kmi.iserve.commons.model.*;
import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.core.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.events.ServiceCreatedEvent;
import uk.ac.open.kmi.iserve.sal.events.ServiceDeletedEvent;
import uk.ac.open.kmi.iserve.sal.events.ServicesClearedEvent;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.manager.IntegratedComponent;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;
import uk.ac.open.kmi.iserve.sal.manager.SparqlGraphStoreManager;
import uk.ac.open.kmi.iserve.sal.util.UriUtil;

import javax.inject.Named;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ServiceManagerIndexRdf extends IntegratedComponent implements ServiceManager {

    private static final Logger log = LoggerFactory.getLogger(ServiceManagerIndexRdf.class);

    // Default path for service and documents URIs.
    // Note that any change here should also affect the REST API
    // Keep the trailing slash
    private static final String SERVICES_URL_PATH = "id/services/";

    private final URI servicesUri;

    private ConcurrentMap<URI, Set<URI>> svcOpMap;
    private ConcurrentMap<URI, Set<URI>> opInputMap;
    private ConcurrentMap<URI, Set<URI>> messageMandatoryPartsMap;
    private ConcurrentMap<URI, Set<URI>> messageOptionalPartsMap;
    private ConcurrentMap<URI, Set<URI>> opOutputMap;
    private ConcurrentMap<URI, Set<URI>> modelReferencesMap;

    // To load
    private static final URI WSMO_LITE_URI = URI.create("http://www.wsmo.org/ns/wsmo-lite");
    private static final URI MSM_URI = URI.create("http://iserve.kmi.open.ac.uk/ns/msm");
    private static final URI HRESTS_URI = URI.create("http://iserve.kmi.open.ac.uk/ns/hrests");
    private static final URI MSM_WSDL_URI = URI.create("http://iserve.kmi.open.ac.uk/ns/msm-wsdl");
    private static final URI SAWSDL_URI = URI.create("http://www.w3.org/ns/sawsdl");
    private static final URI HTTP_VOCAB_URI = URI.create("http://www.w3.org/2011/http");
    private static final URI HTTP_METHODS_URI = URI.create("http://www.w3.org/2011/http-methods");
    private static final URI FOAF_0_1_URI = URI.create("http://xmlns.com/foaf/0.1/");
    private static final URI CONTENT_VOCAB_URI = URI.create("http://www.w3.org/2011/content#");
    private static final URI DCTERMS_URI = URI.create("http://purl.org/dc/terms/");

    private final SparqlGraphStoreManager graphStoreManager;

    @Inject
    ServiceManagerIndexRdf(EventBus eventBus,
                           SparqlGraphStoreFactory graphStoreFactory,
                           @Named(SystemConfiguration.ISERVE_URL_PROP) String iServeUri,
                           @Named(SystemConfiguration.SERVICES_REPOSITORY_SPARQL_PROP) String sparqlQueryEndpoint,
                           @Named(SystemConfiguration.SERVICES_REPOSITORY_SPARQL_UPDATE_PROP) String sparqlUpdateEndpoint,
                           @Named(SystemConfiguration.SERVICES_REPOSITORY_SPARQL_SERVICE_PROP) String sparqlServiceEndpoint) throws SalException {

        super(eventBus, iServeUri);
        this.servicesUri = this.getIserveUri().resolve(SERVICES_URL_PATH);

        // Configuration of base models to be loaded
        Set<URI> defaultModelsToLoad = ImmutableSet.of(WSMO_LITE_URI, MSM_URI, HRESTS_URI, MSM_WSDL_URI, HTTP_VOCAB_URI, HTTP_METHODS_URI);

        // Configuration for quick retrieval of ontologies by resolving them to local files.
        ImmutableMap.Builder<String, String> mappingsBuilder = ImmutableMap.builder();
        mappingsBuilder.put(FOAF_0_1_URI.toASCIIString(), this.getClass().getResource("/foaf-2010-08-09.rdf").toString());
        mappingsBuilder.put(HRESTS_URI.toASCIIString(), this.getClass().getResource("/hrests-2013-10-03.ttl").toString());
        mappingsBuilder.put(MSM_URI.toASCIIString(), this.getClass().getResource("/msm-2013-05-03.ttl").toString());
        mappingsBuilder.put(MSM_WSDL_URI.toASCIIString(), this.getClass().getResource("/msm-wsdl-2013-05-30.ttl").toString());
        mappingsBuilder.put(SAWSDL_URI.toASCIIString(), this.getClass().getResource("/sawsdl-2007-08-28.ttl").toString());
        mappingsBuilder.put(WSMO_LITE_URI.toASCIIString(), this.getClass().getResource("/wsmo-lite-2013-05-03.ttl").toString());
        mappingsBuilder.put(HTTP_VOCAB_URI.toASCIIString(), this.getClass().getResource("/http-2011-04-29.ttl").toString());
        mappingsBuilder.put(HTTP_METHODS_URI.toASCIIString(), this.getClass().getResource("/http-methods-2011-04-29.ttl").toString());
        mappingsBuilder.put(CONTENT_VOCAB_URI.toASCIIString(), this.getClass().getResource("/content-2011-04-29.ttl").toString());
        mappingsBuilder.put(DCTERMS_URI.toASCIIString(), this.getClass().getResource("/dcterms-2012-06-14.ttl").toString());

        // Configuration for avoiding the import of certain files
        ImmutableSet<String> ignoredImports = ImmutableSet.of();

        this.graphStoreManager = graphStoreFactory.create(sparqlQueryEndpoint, sparqlUpdateEndpoint, sparqlServiceEndpoint, defaultModelsToLoad, mappingsBuilder.build(), ignoredImports);

        this.initialiseCache();
    }

    /**
     * Obtains a list of service URIs with all the services known to the system
     *
     * @return list of URIs with all the services in the registry
     */
    @Override
    public Set<URI> listServices() {
        return this.svcOpMap.keySet();
    }

    private Set<URI> loadListServices() {

        String queryStr = new StringBuilder()
                .append("select DISTINCT ?svc where { \n")
                .append("?svc ").append("<").append(RDF.type.getURI()).append(">").append(" ").append("<").append(MSM.Service.getURI()).append(">").append(" . }")
                .toString();

        return this.graphStoreManager.listResourcesByQuery(queryStr, "svc");
    }

    /**
     * Obtains the list of operation URIs for a given Operation
     *
     * @param serviceUri the service URI
     * @return a List of URIs with the operations provided by the service. If there are no operations, the List should be empty NOT null.
     */
    @Override
    public Set<URI> listOperations(URI serviceUri) {
        if (serviceUri == null) {
            return ImmutableSet.of();
        }

        ImmutableSet.Builder<URI> result = ImmutableSet.builder();
        for (URI operation : this.svcOpMap.get(serviceUri)) {
            result.add(operation);
        }

        return result.build();
    }

    /**
     * Obtains the list of input URIs for a given Operation
     *
     * @param operationUri the operation URI
     * @return a List of URIs with the inputs of the operation. If no input is necessary the List should be empty NOT null.
     */
    @Override
    public Set<URI> listInputs(URI operationUri) {
        if (operationUri == null) {
            return ImmutableSet.of();
        }

        return ImmutableSet.copyOf(this.opInputMap.get(operationUri));
    }

    /**
     * Given an operation, this method obtains the list of input URIs mapped to their annotated types (i.e., modelReferences).
     * Note that the same input may have several annotations indicating the type.
     *
     * @param operationUri the URI for which we want to obtain the inputs and their annotated types
     * @return a Multimap with the inputs and their corresponding types.
     */
    @Override
    public Multimap<URI, URI> listTypedInputs(URI operationUri) {
        return null;  // TODO: implement
    }

    /**
     * Obtains the list of output URIs for a given Operation
     *
     * @param operationUri the operation URI
     * @return a List of URIs with the outputs of the operation. If no output is provided the List should be empty NOT null.
     */
    @Override
    public Set<URI> listOutputs(URI operationUri) {
        if (operationUri == null) {
            return ImmutableSet.of();
        }

        return ImmutableSet.<URI>copyOf(this.opOutputMap.get(operationUri));
    }

    /**
     * Given an operation, this method obtains the list of output URIs mapped to their annotated types (i.e., modelReferences).
     * Note that the same input may have several annotations indicating the type.
     *
     * @param operationUri the URI for which we want to obtain the outputs and their annotated types
     * @return a Multimap with the outputs and their corresponding types.
     */
    @Override
    public Multimap<URI, URI> listTypedOutputs(URI operationUri) {
        return null;  // TODO: implement
    }

    /**
     * Obtains the list of mandatory parts for a given Message Content
     *
     * @param messageContent the message content URI
     * @return a List of URIs with the mandatory parts of the message content. If there are no parts the List should be empty NOT null.
     */
    @Override
    public Set<URI> listMandatoryParts(URI messageContent) {
        if (messageContent == null) {
            return ImmutableSet.of();
        }

        return ImmutableSet.<URI>copyOf(this.messageMandatoryPartsMap.get(messageContent));
    }

    /**
     * Obtains the list of optional parts for a given Message Content
     *
     * @param messageContent the message content URI
     * @return a Set of URIs with the optional parts of the message content. If there are no parts the Set should be empty NOT null.
     */
    @Override
    public Set<URI> listOptionalParts(URI messageContent) {
        if (messageContent == null) {
            return ImmutableSet.of();
        }

        return ImmutableSet.<URI>copyOf(this.messageOptionalPartsMap.get(messageContent));
    }

    /**
     * Obtains the service description of the service identified by the URI
     *
     * @param serviceUri the URI of the service to obtain
     * @return the service description if it exists or null otherwise
     * @throws uk.ac.open.kmi.iserve.sal.exception.ServiceException
     *
     */
    @Override
    public Service getService(URI serviceUri) throws ServiceException {
        if (serviceUri == null || !serviceUri.isAbsolute()) {
            log.warn("The Service URI is either absent or relative. Provide an absolute URI");
            return null;
        }

        OntModel model = null;
        try {
            model = this.graphStoreManager.getGraph(URIUtil.getNameSpace(serviceUri));
        } catch (URISyntaxException e) {
            log.error("The namespace of the service is not a correct URI", e);
            return null;
        }

        // Parse the service. There should only be one in the response
        ServiceReaderImpl reader = new ServiceReaderImpl();
        List<Service> services = reader.parseService(model);
        if (services != null && !services.isEmpty()) {
            return services.get(0);
        }

        return null;
    }

    /**
     * Obtains the service descriptions for all the services identified by the URI Set
     *
     * @param serviceUris the URIs of the service to obtain
     * @return the list of all services that could be obtained. If none could be obtained the list will be empty.
     * @throws uk.ac.open.kmi.iserve.sal.exception.ServiceException
     *
     */
    @Override
    public Set<Service> getServices(Set<URI> serviceUris) throws ServiceException {
        ImmutableSet.Builder<Service> result = ImmutableSet.builder();
        Service svc;
        for (URI svcUri : serviceUris) {
            svc = this.getService(svcUri);
            if (svc != null) {
                result.add(svc);
            }
        }
        return result.build();
    }

    /**
     * Lists all documents related to a given service
     *
     * @param serviceUri the service URI
     * @return the List of the URIs of all the documents related to the service
     * @throws uk.ac.open.kmi.iserve.sal.exception.ServiceException
     *
     */
    @Override
    public Set<URI> listDocumentsForService(URI serviceUri) throws ServiceException {
        return ImmutableSet.of(); // TODO: Implement
    }


    public URI addService(Service service) throws ServiceException {

        // Check input and exit early
        if (service == null)
            throw new ServiceException("No service provided.");

        if (!this.graphStoreManager.canBeModified()) {
            log.warn("The dataset cannot be modified. Unable to store the service.");
            return null;
        }

        URI newBaseServiceUri = this.generateUniqueServiceUri();
        // Replace URIs in service description
        try {
            replaceUris(service, newBaseServiceUri);
        } catch (URISyntaxException e) {
            log.error("Unable to create URI for service", e);
            throw new ServiceException("Unable to create URI for service", e);
        }

        // Store in backend
        ServiceWriterImpl writer = new ServiceWriterImpl();
        Model svcModel = writer.generateModel(service);
        log.info("Adding service - {}", service.getUri().toASCIIString());
        // The graph id corresponds is the base id
        this.graphStoreManager.putGraph(newBaseServiceUri, svcModel);

        // Index service
        this.indexService(service);

        // Generate Event
        this.getEventBus().post(new ServiceCreatedEvent(new Date(), service));

        return service.getUri();
    }

    /**
     * Relates a given document to a service. The relationship is just a generic one.
     *
     * @param serviceUri      the service URI
     * @param relatedDocument the related document URI
     * @return True if successful or False otherwise
     * @throws uk.ac.open.kmi.iserve.sal.exception.SalException
     *
     */
    @Override
    public boolean addRelatedDocumentToService(URI serviceUri, URI relatedDocument) throws ServiceException {
        return false;  //TODO: Implement
    }

    private URI generateUniqueServiceUri() {
        return this.servicesUri.resolve(UriUtil.generateUniqueId());
    }

    /**
     * Given a Resource and the new Uri base to use, modify each and every URL accordingly.
     * This methods maintains the naming used by the service already.
     * Recursive implementation that traverses the entire graph
     *
     * @param resource   The service that will be modified
     * @param newUriBase The new URI base
     */
    private void replaceUris(uk.ac.open.kmi.iserve.commons.model.Resource resource, URI newUriBase) throws URISyntaxException {

        // Exit early
        if (resource == null || newUriBase == null || !newUriBase.isAbsolute())
            return;

        resource.setUri(URIUtil.replaceNamespace(resource.getUri(), newUriBase));

        // Replace recursively each of the URLs of the inner elements
        // Handling of Service
        if (resource instanceof Service) {
            // Replace Operations
            List<Operation> operations = ((Service) resource).getOperations();
            for (Operation op : operations) {
                replaceUris(op, resource.getUri());
            }
        }

        // Handling of Operation
        if (resource instanceof Operation) {
            List<MessageContent> mcs;
            // Replace Inputs, Outputs, InFaults, and Outfaults
            mcs = ((Operation) resource).getInputs();
            for (MessageContent mc : mcs) {
                replaceUris(mc, resource.getUri());
            }

            mcs = ((Operation) resource).getInputFaults();
            for (MessageContent mc : mcs) {
                replaceUris(mc, resource.getUri());
            }

            mcs = ((Operation) resource).getOutputs();
            for (MessageContent mc : mcs) {
                replaceUris(mc, resource.getUri());
            }

            mcs = ((Operation) resource).getOutputFaults();
            for (MessageContent mc : mcs) {
                replaceUris(mc, resource.getUri());
            }
        }

        // Handling for MessageParts
        if (resource instanceof MessagePart) {
            // Deal with optional and mandatory parts
            List<MessagePart> mps;
            mps = ((MessagePart) resource).getMandatoryParts();
            for (MessagePart mp : mps) {
                replaceUris(mp, resource.getUri());
            }

            mps = ((MessagePart) resource).getOptionalParts();
            for (MessagePart mp : mps) {
                replaceUris(mp, resource.getUri());
            }
        }

        // Handling for Invocable Entities
        if (resource instanceof InvocableEntity) {
            // Replace Effects
            List<Effect> effects = ((InvocableEntity) resource).getEffects();
            for (Effect effect : effects) {
                replaceUris(effect, resource.getUri());
            }

            // Replace Conditions
            List<Condition> conditions = ((InvocableEntity) resource).getConditions();
            for (Condition condition : conditions) {
                replaceUris(condition, resource.getUri());
            }
        }

        // Handling for Annotable Resources
        if (resource instanceof AnnotableResource) {
            // Replace NFPs
            List<NonFunctionalProperty> nfps = ((AnnotableResource) resource).getNfps();
            for (NonFunctionalProperty nfp : nfps) {
                replaceUris(nfp, resource.getUri());
            }
        }
    }

    /**
     * Returns the URI of the document defining the service, i.e., without the
     * fragment.
     *
     * @param serviceId the unique id of the service
     * @return the URI of the service document
     */
    private URI getServiceBaseUri(String serviceId) {
        return this.servicesUri.resolve(serviceId);
    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#deleteService(java.lang.String)
     */
    @Override
    public boolean deleteService(URI serviceUri) throws ServiceException {
        if (serviceUri == null || !serviceUri.isAbsolute()) {
            log.warn("The Service URI is either absent or relative. Provide an absolute URI");
            return false;
        }

        if (!this.graphStoreManager.canBeModified()) {
            log.warn("The dataset cannot be modified.");
            return false;
        }

        log.info("Deleting service - {}", serviceUri.toASCIIString());
        try {
            this.graphStoreManager.deleteGraph(URIUtil.getNameSpace(serviceUri));
        } catch (URISyntaxException e) {
            log.error("The namespace of the service is an incorrect URI", e);
            throw new ServiceException("The namespace of the service is an incorrect URI", e);
        }

        // Update cache
        this.removeServiceFromCache(serviceUri);

        // Generate Event
        this.getEventBus().post(new ServiceDeletedEvent(new Date(), new Service(serviceUri)));

        return true;
    }

    @Override
    public boolean deleteService(Service service) throws ServiceException {
        return deleteService(service.getUri());
    }

    /**
     * Deletes all the services on the registry.
     * This operation cannot be undone. Use with care.
     *
     * @return
     * @throws uk.ac.open.kmi.iserve.sal.exception.ServiceException
     *
     */
    @Override
    public boolean clearServices() throws ServiceException {


        if (!this.graphStoreManager.canBeModified()) {
            log.warn("The dataset cannot be modified.");
            return false;
        }

        log.info("Clearing services registry.");
        this.graphStoreManager.clearDataset();

        // Initialise cache
        this.initialiseCache();
        // Generate Event
        this.getEventBus().post(new ServicesClearedEvent(new Date()));

        return true;

    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#serviceExists(java.net.URI)
     */
    @Override
    public boolean serviceExists(URI serviceUri) throws ServiceException {
        if (serviceUri == null || !serviceUri.isAbsolute()) {
            log.warn("The Service URI is either absent or relative. Provide an absolute URI");
            return false;
        }

        String queryStr = null;
        try {
            queryStr = new StringBuilder()
                    .append("ASK { \n")
                    .append("GRAPH <").append(URIUtil.getNameSpace(serviceUri).toASCIIString()).append("> {")
                    .append("<").append(serviceUri.toASCIIString()).append("> <").append(RDF.type.getURI()).append("> <").append(MSM.Service).append("> }\n}").toString();
        } catch (URISyntaxException e) {
            log.error("The namespace of the URI of the service is incorrect.", e);
            throw new ServiceException("The namespace of the URI of the service is incorrect.", e);
        }

        Query query = QueryFactory.create(queryStr);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(this.graphStoreManager.getSparqlQueryEndpoint().toASCIIString(), query);
        try {
            return qexec.execAsk();
        } finally {
            qexec.close();
        }
    }


    /**
     * Given the URI of an element, this method returns the URI of the element from the Minimal Service Model it
     * corresponds to. That is, it will say if it is a service, operation, etc.
     * This method uses SPARQL 1.1 to avoid using regexs for performance.
     *
     * @param elementUri the URI to get the element type for.
     * @return the URI of the MSM type it corresponds to.
     */
    @Override
    public Set<URI> getMsmType(URI elementUri) {
        if (elementUri == null || !elementUri.isAbsolute()) {
            log.warn("The Element URI is either absent or relative. Provide an absolute URI");
            return null;
        }

        String queryStr = null;
        queryStr = new StringBuilder()
                .append("SELECT * WHERE { \n")
                .append("<").append(elementUri.toASCIIString()).append("> <").append(RDF.type.getURI()).append("> ?type .")
                .append("FILTER(STRSTARTS(STR(?type), \"").append(MSM.NAMESPACE.getURI()).append("\"))")
                .append("\n }")
                .toString();

        return this.graphStoreManager.listResourcesByQuery(queryStr, "type");
    }

    /**
     * Obtains the list of model references for a given element.
     *
     * @param elementUri the URI of the element for which we want to obtain the model references
     * @return a List of URIs with the model references of the given element. If there are no model references the List should be empty NOT null.
     */
    @Override
    public Set<URI> listModelReferences(URI elementUri) {
        if (elementUri == null) {
            return ImmutableSet.of();
        }

        return ImmutableSet.<URI>copyOf(this.modelReferencesMap.get(elementUri));
    }

    /**
     * Given a modelReference, this method finds all the elements that have been annotated with it. This method finds
     * exact annotations. Note that the elements can be services, operations, inputs, etc.
     *
     * @param modelReference the actual annotation we are looking for
     * @return a set of URIs for elements that have been annotated with the requested modelReferences.
     */
    @Override
    public Set<URI> listElementsAnnotatedWith(URI modelReference) {
        return null;  // TODO: implement
    }

    /**
     * Given the URI of a type (i.e., a modelReference), this method figures out all the operations
     * that have this as part of their inputs.
     *
     * @param modelReference the type of input sought for
     * @return a Set of URIs of operations that can take this as input type.
     */
    @Override
    public Set<URI> listOperationsWithInputType(URI modelReference) {
        return null;  // TODO: implement
    }

    /**
     * Given the URI of a type (i.e., a modelReference), this method figures out all the operations
     * that have this as part of their inputs.
     *
     * @param modelReference the type of output sought for
     * @return a Set of URIs of operations that generate this output type.
     */
    @Override
    public Set<URI> listOperationsWithOutputType(URI modelReference) {
        return null;  // TODO: implement
    }

    /**
     * Given the URI of a type (i.e., a modelReference), this method figures out all the services
     * that have this as part of their inputs.
     *
     * @param modelReference the type of input sought for
     * @return a Set of URIs of services that can take this as input type.
     */
    @Override
    public Set<URI> listServicesWithInputType(URI modelReference) {
        return null;  // TODO: implement
    }

    /**
     * Given the URI of a type (i.e., a modelReference), this method figures out all the services
     * that have this as part of their inputs.
     *
     * @param modelReference the type of output sought for
     * @return a Set of URIs of services that generate this output type.
     */
    @Override
    public Set<URI> listServicesWithOutputType(URI modelReference) {
        return null;  // TODO: implement
    }

    /**
     * This method will be called when the server is initialised.
     * If necessary it should take care of updating any indexes on boot time.
     */
    @Override
    public void initialise() {

        initialiseCache();
        log.info("Populating cache.");
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();
        populateCache();
        stopwatch.stop();
        log.info("Cache populated. Time taken {}", stopwatch);
    }

    private void initialiseCache() {
        this.svcOpMap = new ConcurrentHashMap<URI, Set<URI>>();
        this.opInputMap = new ConcurrentHashMap<URI, Set<URI>>();
        this.messageMandatoryPartsMap = new ConcurrentHashMap<URI, Set<URI>>();
        this.messageOptionalPartsMap = new ConcurrentHashMap<URI, Set<URI>>();
        this.opOutputMap = new ConcurrentHashMap<URI, Set<URI>>();
        this.modelReferencesMap = new ConcurrentHashMap<URI, Set<URI>>();
    }

    private void populateCache() {
        Set<URI> services = this.loadListServices();
        for (URI svc : services) {
            Service service = null;
            try {
                service = this.getService(svc);
                indexService(service);
            } catch (ServiceException e) {
                log.error("Error populating cache. Cannot retrieve service.", e);
            }
        }
    }

    private void indexService(Service service) {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();
        List<Operation> operations = service.getOperations();
        Set<URI> svcOps = new HashSet<URI>();
        for (Operation operation : operations) {
            svcOps.add(operation.getUri());
            indexOperation(operation);
        }
        // Set the svcOp map
        this.svcOpMap.put(service.getUri(), svcOps);
        // Index the modelReferences
        indexModelReferences(service);
        stopwatch.stop();
        log.info("Service - {} - indexed. Time taken {}", service.getUri(), stopwatch);
    }

    private void indexModelReferences(AnnotableResource annotableResource) {
        List<uk.ac.open.kmi.iserve.commons.model.Resource> modelRefs = annotableResource.getModelReferences();
        Set<URI> modelRefsUris = new HashSet<URI>();
        for (uk.ac.open.kmi.iserve.commons.model.Resource modelRef : modelRefs) {
            modelRefsUris.add(modelRef.getUri());
        }
        this.modelReferencesMap.put(annotableResource.getUri(), modelRefsUris);
    }

    private void indexOperation(Operation operation) {

        // Note this method assumes only one input per operation
        // If there are more only the last will be mapped
        Set<URI> partsUris;
        Set<URI> messagesUris;

        messagesUris = new HashSet<URI>();
        List<MessageContent> inputs = operation.getInputs();
        for (MessageContent input : inputs) {
            messagesUris.add(input.getUri());
            indexModelReferences(input);
            indexMandatoryParts(input);
            indexOptionalParts(input);
        }
        // Index inputs
        this.opInputMap.put(operation.getUri(), messagesUris);

        // Process Output
        messagesUris = new HashSet<URI>();
        List<MessageContent> outputs = operation.getInputs();
        for (MessageContent output : outputs) {
            messagesUris.add(output.getUri());
            indexModelReferences(output);
            indexMandatoryParts(output);
            indexOptionalParts(output);
        }
        // Index outputs
        this.opOutputMap.put(operation.getUri(), messagesUris);

        // Add model refs
        indexModelReferences(operation);
    }

    private void indexOptionalParts(MessageContent messageContent) {
        Set<URI> partsUris;// Index optional parts
        partsUris = new HashSet<URI>();
        List<MessagePart> optionalParts = messageContent.getOptionalParts();
        for (MessagePart optionalPart : optionalParts) {
            partsUris.add(optionalPart.getUri());
            indexModelReferences(optionalPart);
        }
        this.messageOptionalPartsMap.put(messageContent.getUri(), partsUris);
    }

    private void indexMandatoryParts(MessageContent messageContent) {
        Set<URI> partsUris;// Index mandatory parts
        partsUris = new HashSet<URI>();
        List<MessagePart> mandatoryParts = messageContent.getMandatoryParts();
        for (MessagePart mandatoryPart : mandatoryParts) {
            partsUris.add(mandatoryPart.getUri());
            indexModelReferences(mandatoryPart);
        }
        this.messageMandatoryPartsMap.put(messageContent.getUri(), partsUris);
    }


    private void removeServiceFromCache(URI serviceUri) {

        Set<URI> operations = this.svcOpMap.remove(serviceUri);
        for (URI operation : operations) {
            removeOperationFromCache(operation);
        }
        this.modelReferencesMap.remove(serviceUri);
    }

    private void removeOperationFromCache(URI operation) {

        // Clean input
        Set<URI> parts;
        Set<URI> inputUris = this.opInputMap.remove(operation);
        for (URI inputUri : inputUris) {
            this.modelReferencesMap.remove(inputUri);
            parts = this.messageMandatoryPartsMap.remove(inputUri);
            for (URI part : parts) {
                this.modelReferencesMap.remove(part);
            }
            parts = this.messageOptionalPartsMap.remove(inputUri);
            for (URI part : parts) {
                this.modelReferencesMap.remove(part);
            }
        }

        // Clean output
        Set<URI> outputUris = this.opOutputMap.remove(operation);
        for (URI outputUri : outputUris) {
            this.modelReferencesMap.remove(outputUri);
            parts = this.messageMandatoryPartsMap.remove(outputUri);
            for (URI part : parts) {
                this.modelReferencesMap.remove(part);
            }
            parts = this.messageOptionalPartsMap.remove(outputUri);
            for (URI part : parts) {
                this.modelReferencesMap.remove(part);
            }
        }

        this.modelReferencesMap.remove(operation);
    }

    /**
     * This method will be called when the server is being shutdown.
     * Ensure a clean shutdown.
     */
    @Override
    public void shutdown() {
        this.graphStoreManager.shutdown();
    }
}
