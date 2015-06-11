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
import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.ConfigurationProperty;
import uk.ac.open.kmi.iserve.core.iServeProperty;
import uk.ac.open.kmi.iserve.sal.events.ServiceCreatedEvent;
import uk.ac.open.kmi.iserve.sal.events.ServiceDeletedEvent;
import uk.ac.open.kmi.iserve.sal.events.ServicesClearedEvent;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.manager.IntegratedComponent;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;
import uk.ac.open.kmi.iserve.sal.manager.SparqlGraphStoreManager;
import uk.ac.open.kmi.iserve.sal.util.MonitoredQueryExecution;
import uk.ac.open.kmi.iserve.sal.util.UriUtil;
import uk.ac.open.kmi.msm4j.*;
import uk.ac.open.kmi.msm4j.io.impl.ServiceReaderImpl;
import uk.ac.open.kmi.msm4j.io.impl.ServiceWriterImpl;
import uk.ac.open.kmi.msm4j.io.util.URIUtil;
import uk.ac.open.kmi.msm4j.vocabulary.MSM;
import uk.ac.open.kmi.msm4j.vocabulary.SAWSDL;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Singleton
public class ServiceManagerSparql extends IntegratedComponent implements ServiceManager {

    private static final Logger log = LoggerFactory.getLogger(ServiceManagerSparql.class);

    // Default path for service and documents URIs.
    // Note that any change here should also affect the REST API
    // Keep the trailing slash
    private static final String SERVICES_URL_PATH = "id/services/";

    // To load
    private static final URI WSMO_LITE_URI = URI.create("http://www.wsmo.org/ns/wsmo-lite");
    private static final URI MSM_URI = URI.create("http://iserve.kmi.open.ac.uk/ns/msm");
    private static final URI HRESTS_URI = URI.create("http://iserve.kmi.open.ac.uk/ns/hrests");
    private static final URI MSM_WSDL_URI = URI.create("http://iserve.kmi.open.ac.uk/ns/msm-wsdl");
    private static final URI MSM_SWAGGER_URI = URI.create("http://iserve.kmi.open.ac.uk/ns/msm-swagger");
    private static final URI SAWSDL_URI = URI.create("http://www.w3.org/ns/sawsdl");
    private static final URI HTTP_VOCAB_URI = URI.create("http://www.w3.org/2011/http");
    private static final URI HTTP_METHODS_URI = URI.create("http://www.w3.org/2011/http-methods");
    private static final URI FOAF_0_1_URI = URI.create("http://xmlns.com/foaf/0.1/");
    private static final URI CONTENT_VOCAB_URI = URI.create("http://www.w3.org/2011/content#");
    private static final URI DCTERMS_URI = URI.create("http://purl.org/dc/terms/");
    private static final URI HTTP_STATUS_URI = URI.create("http://www.w3.org/2011/http-statusCodes");
    private static final URI MEDIA_TYPES_URI = URI.create("http://purl.org/NET/mediatype");
    private static final URI MSM_NFP_URI = URI.create("http://iserve.kmi.open.ac.uk/ns/msm-nfp");
    private static final URI SIOC_URI = URI.create("http://rdfs.org/sioc/ns");

//    private static final URI WSDL_EXTENSIONS_URI = URI.create("http://www.w3.org/ns/wsdl-extensions#");

    private final URI servicesUri;
    private final SparqlGraphStoreManager graphStoreManager;


    @Inject
    ServiceManagerSparql(EventBus eventBus,
                         SparqlGraphStoreFactory graphStoreFactory,
                         @iServeProperty(ConfigurationProperty.ISERVE_URL) String iServeUri,
                         @iServeProperty(ConfigurationProperty.SERVICES_SPARQL_QUERY) String sparqlQueryEndpoint,
                         @iServeProperty(ConfigurationProperty.SERVICES_SPARQL_UPDATE) String sparqlUpdateEndpoint,
                         @iServeProperty(ConfigurationProperty.SERVICES_SPARQL_SERVICE) String sparqlServiceEndpoint) throws SalException {

        super(eventBus, iServeUri);
        this.servicesUri = this.getIserveUri().resolve(SERVICES_URL_PATH);

        log.debug("Creating Service Manager SPARQL. Services URL Path - {}\n SPARQL Query - {}\n, SPARQL Update - {}\n, SPARQL Service - {}",
                servicesUri, sparqlQueryEndpoint, sparqlUpdateEndpoint, sparqlServiceEndpoint);
        //TODO Figure out which ontology breaks sesame/owlim inferences (it seems media types, but not sure)
        // Configuration of base models to be loaded
//        Set<URI> defaultModelsToLoad = ImmutableSet.of(FOAF_0_1_URI, HRESTS_URI, MSM_URI, MSM_WSDL_URI,
//                MSM_SWAGGER_URI, WSMO_LITE_URI, HTTP_VOCAB_URI, HTTP_METHODS_URI, CONTENT_VOCAB_URI,
//                DCTERMS_URI, HTTP_STATUS_URI, MEDIA_TYPES_URI, MSM_NFP_URI, SIOC_URI);

        Set<URI> defaultModelsToLoad = ImmutableSet.of(MSM_URI, DCTERMS_URI);


        // Configuration for quick retrieval of ontologies by resolving them to local files.
        ImmutableMap.Builder<String, String> mappingsBuilder = ImmutableMap.builder();
        //mappingsBuilder.put(FOAF_0_1_URI.toASCIIString(), this.getClass().getResource("/foaf-2010-08-09.ttl").toString());
        //mappingsBuilder.put(HRESTS_URI.toASCIIString(), this.getClass().getResource("/hrests-2013-10-03.ttl").toString());
        mappingsBuilder.put(MSM_URI.toASCIIString(), this.getClass().getResource("/msm-2014-09-03.ttl").toString());
        //mappingsBuilder.put(MSM_WSDL_URI.toASCIIString(), this.getClass().getResource("/msm-wsdl-2014-09-03.ttl").toString());
        //mappingsBuilder.put(MSM_SWAGGER_URI.toASCIIString(), this.getClass().getResource("/msm-swagger-2014-09-03.ttl").toString());
        mappingsBuilder.put(SAWSDL_URI.toASCIIString(), this.getClass().getResource("/sawsdl-2007-08-28.ttl").toString());
        mappingsBuilder.put(WSMO_LITE_URI.toASCIIString(), this.getClass().getResource("/wsmo-lite-2013-05-03.ttl").toString());
        //mappingsBuilder.put(HTTP_VOCAB_URI.toASCIIString(), this.getClass().getResource("/http-2011-04-29.ttl").toString());
        //mappingsBuilder.put(HTTP_METHODS_URI.toASCIIString(), this.getClass().getResource("/http-methods-2011-04-29.ttl").toString());
        //mappingsBuilder.put(CONTENT_VOCAB_URI.toASCIIString(), this.getClass().getResource("/content-2011-04-29.ttl").toString());
        mappingsBuilder.put(DCTERMS_URI.toASCIIString(), this.getClass().getResource("/dcterms-2012-06-14.ttl").toString());
        //mappingsBuilder.put(HTTP_STATUS_URI.toASCIIString(), this.getClass().getResource("/http-statusCodes-2014-09-03.ttl").toString());
        //mappingsBuilder.put(MEDIA_TYPES_URI.toASCIIString(), this.getClass().getResource("/media-types-2014-09-03.ttl").toString());
        //mappingsBuilder.put(MSM_NFP_URI.toASCIIString(), this.getClass().getResource("/msm-nfp-2014-07-10.ttl").toString());
        //mappingsBuilder.put(SIOC_URI.toASCIIString(), this.getClass().getResource("/sioc-2010-03-25.ttl").toString());

        // Configuration for avoiding the import of certain files
        ImmutableSet<String> ignoredImports = ImmutableSet.of();

        this.graphStoreManager = graphStoreFactory.create(sparqlQueryEndpoint, sparqlUpdateEndpoint,
                sparqlServiceEndpoint, defaultModelsToLoad, mappingsBuilder.build(), ignoredImports);
    }

    /**
     * Obtains a list of service URIs with all the services known to the system
     *
     * @return list of URIs with all the services in the registry
     */
    @Override
    public Set<URI> listServices() {
        String queryStr = new StringBuilder()
                .append("SELECT DISTINCT ?svc WHERE { \n")
                .append(" GRAPH ?g { \n")
                .append("?svc ").append("<").append(RDF.type.getURI()).append(">").append(" ").append("<").append(MSM.Service.getURI()).append("> . \n")
                .append(" } \n")
                .append("} \n")
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

        if (serviceUri == null || !serviceUri.isAbsolute()) {
            log.warn("The Service URI is either absent or relative. Provide an absolute URI");
            return ImmutableSet.of();
        }

        URI graphUri;
        try {
            graphUri = getGraphUriForElement(serviceUri);
        } catch (URISyntaxException e) {
            log.warn("The namespace of the URI of the service is incorrect.", e);
            return ImmutableSet.of();
        }

        if (graphUri == null) {
            log.warn("Could not obtain a graph URI for the element. The URI may not be managed by the server - " + serviceUri);
            return ImmutableSet.of();
        }

        String queryStr = new StringBuilder()
                .append("SELECT DISTINCT ?op WHERE { \n")
                .append(" GRAPH <").append(graphUri.toASCIIString()).append("> { \n")
                .append(" <").append(serviceUri.toASCIIString()).append("> ").append("<").append(MSM.hasOperation.getURI()).append(">").append(" ?op . ")
                .append(" ?op ").append("<").append(RDF.type.getURI()).append(">").append(" ").append("<").append(MSM.Operation.getURI()).append("> . \n")
                .append(" } \n ")
                .append("} \n ")
                .toString();

        return this.graphStoreManager.listResourcesByQuery(queryStr, "op");
    }


    /**
     * Obtains the list of input URIs for a given Operation
     *
     * @param operationUri the operation URI
     * @return a List of URIs with the inputs of the operation. If no input is necessary the List should be empty NOT null.
     */
    @Override
    public Set<URI> listInputs(URI operationUri) {

        if (operationUri == null || !operationUri.isAbsolute()) {
            log.warn("The Operation URI is either absent or relative. Provide an absolute URI");
            return ImmutableSet.of();
        }

        URI graphUri;
        try {
            graphUri = getGraphUriForElement(operationUri);
        } catch (URISyntaxException e) {
            log.warn("The namespace of the URI of the operation is incorrect.", e);
            return ImmutableSet.of();
        }

        if (graphUri == null) {
            log.warn("Could not obtain a graph URI for the element. The URI may not be managed by the server - " + operationUri);
            return ImmutableSet.of();
        }

        String queryStr = new StringBuilder()
                .append("SELECT DISTINCT ?input WHERE { \n")
                .append(" GRAPH <").append(graphUri.toASCIIString()).append("> { \n")
                .append("<").append(operationUri.toASCIIString()).append("> ").append("<").append(MSM.hasInput.getURI()).append(">").append(" ?input .")
                .append("?input ").append("<").append(RDF.type.getURI()).append(">").append(" ").append("<").append(MSM.MessageContent.getURI()).append("> .")
                .append(" } \n ")
                .append("} \n ")
                .toString();


        return this.graphStoreManager.listResourcesByQuery(queryStr, "input");
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

        if (operationUri == null || !operationUri.isAbsolute()) {
            log.warn("The Operation URI is either absent or relative. Provide an absolute URI");
            return ImmutableSet.of();
        }

        URI graphUri;
        try {
            graphUri = getGraphUriForElement(operationUri);
        } catch (URISyntaxException e) {
            log.warn("The namespace of the URI of the operation is incorrect.", e);
            return ImmutableSet.of();
        }

        if (graphUri == null) {
            log.warn("Could not obtain a graph URI for the element. The URI may not be managed by the server - " + operationUri);
            return ImmutableSet.of();
        }

        String queryStr = new StringBuilder()
                .append("SELECT DISTINCT ?output WHERE { \n")
                .append(" GRAPH <").append(graphUri.toASCIIString()).append("> { \n")
                .append("<").append(operationUri.toASCIIString()).append("> ").append("<").append(MSM.hasOutput.getURI()).append(">").append(" ?output .")
                .append("?output ").append("<").append(RDF.type.getURI()).append(">").append(" ").append("<").append(MSM.MessageContent.getURI()).append("> .")
                .append(" } \n ")
                .append("} \n ")
                .toString();

        return this.graphStoreManager.listResourcesByQuery(queryStr, "output");
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
     * @return a Set of URIs with the mandatory parts of the message content. If there are no parts the Set should be empty NOT null.
     */
    @Override
    public Set<URI> listMandatoryParts(URI messageContent) {
        if (messageContent == null || !messageContent.isAbsolute()) {
            log.warn("The Message Content URI is either absent or relative. Provide an absolute URI");
            return ImmutableSet.of();
        }

        URI graphUri;
        try {
            graphUri = getGraphUriForElement(messageContent);
        } catch (URISyntaxException e) {
            log.warn("The namespace of the URI of the message content is incorrect.", e);
            return ImmutableSet.of();
        }

        if (graphUri == null) {
            log.warn("Could not obtain a graph URI for the element. The URI may not be managed by the server - " + messageContent);
            return ImmutableSet.of();
        }

        String queryStr = new StringBuilder()
                .append("SELECT DISTINCT ?part WHERE { \n")
                .append(" GRAPH <").append(graphUri.toASCIIString()).append("> { \n")
                .append("<").append(messageContent.toASCIIString()).append("> ").append("<").append(MSM.hasMandatoryPart.getURI()).append(">").append(" ?part .")
                .append("?part ").append("<").append(RDF.type.getURI()).append(">").append(" ").append("<").append(MSM.MessagePart.getURI()).append("> .")
                .append(" } \n ")
                .append("} \n ")
                .toString();

        return this.graphStoreManager.listResourcesByQuery(queryStr, "part");
    }

    /**
     * Obtains the list of optional parts for a given Message Content
     *
     * @param messageContent the message content URI
     * @return a Set of URIs with the optional parts of the message content. If there are no parts the Set should be empty NOT null.
     */
    @Override
    public Set<URI> listOptionalParts(URI messageContent) {
        if (messageContent == null || !messageContent.isAbsolute()) {
            log.warn("The Message Content URI is either absent or relative. Provide an absolute URI");
            return ImmutableSet.of();
        }

        URI graphUri;
        try {
            graphUri = getGraphUriForElement(messageContent);
        } catch (URISyntaxException e) {
            log.warn("The namespace of the URI of the message content is incorrect.", e);
            return ImmutableSet.of();
        }

        if (graphUri == null) {
            log.warn("Could not obtain a graph URI for the element. The URI may not be managed by the server - " + messageContent);
            return ImmutableSet.of();
        }

        String queryStr = new StringBuilder()
                .append("SELECT DISTINCT ?part WHERE { \n")
                .append(" GRAPH <").append(graphUri.toASCIIString()).append("> { \n")
                .append("<").append(messageContent.toASCIIString()).append("> ").append("<").append(MSM.hasOptionalPart.getURI()).append(">").append(" ?part .")
                .append("?part ").append("<").append(RDF.type.getURI()).append(">").append(" ").append("<").append(MSM.MessagePart.getURI()).append("> .")
                .append(" } \n ")
                .append("} \n ")
                .toString();

        return this.graphStoreManager.listResourcesByQuery(queryStr, "part");
    }

    /**
     * Obtains the service description of the service identified by the URI
     *
     * @param serviceUri the URI of the service to obtain
     * @return the service description if it exists or null otherwise
     * @throws uk.ac.open.kmi.iserve.sal.exception.ServiceException
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
     */
    @Override
    public Set<URI> listDocumentsForService(URI serviceUri) throws ServiceException {
        return ImmutableSet.of(); // TODO: Implement
    }

    /**
     * Creates a Service Description in the system.
     * Only needs to be fed with an MSM Service description.
     *
     * After successfully adding a service, implementations of this method should raise a {@code ServiceCreatedEvent}
     *
     * @param service the input service description in terms of MSM
     * @return the URI this service description was saved to
     * @throws uk.ac.open.kmi.iserve.sal.exception.ServiceException
     */
    @Override
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
     * @throws uk.ac.open.kmi.iserve.sal.exception.ServiceException
     */
    @Override
    public boolean addRelatedDocumentToService(URI serviceUri, URI relatedDocument) throws ServiceException {
        return false;  //TODO: Implement
    }

    /**
     * Deletes the given service
     *
     * After successfully deleting a service, implementations of this method should raise a {@code ServiceDeletedEvent}
     *
     * @param serviceUri the URI of the service to delete
     * @return True if it was deleted or false otherwise
     * @throws uk.ac.open.kmi.iserve.sal.exception.ServiceException
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
            this.graphStoreManager.deleteGraph(getGraphUriForElement(serviceUri));
        } catch (URISyntaxException e) {
            log.error("The namespace of the service is an incorrect URI", e);
            throw new ServiceException("The namespace of the service is an incorrect URI", e);
        }

        // Generate Event
        this.getEventBus().post(new ServiceDeletedEvent(new Date(), new Service(serviceUri)));

        return true;
    }

    /**
     * Deletes the given service
     *
     * After successfully deleting a service, implementations of this method should raise a {@code ServiceDeletedEvent}
     *
     * @param service the service to delete
     * @return True if it was deleted or false otherwise
     * @throws uk.ac.open.kmi.iserve.sal.exception.ServiceException
     */
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
     */
    @Override
    public boolean clearServices() throws ServiceException {

        if (!this.graphStoreManager.canBeModified()) {
            log.warn("The dataset cannot be modified.");
            return false;
        }

        log.info("Clearing services registry.");
        this.graphStoreManager.clearDataset();

        // Generate Event
        this.getEventBus().post(new ServicesClearedEvent(new Date()));

        return true;
    }

    /**
     * Determines whether a service is known to the registry
     *
     * @param serviceUri the URI of the service being looked up
     * @return True if it is registered in the server
     * @throws uk.ac.open.kmi.iserve.sal.exception.ServiceException
     */
    @Override
    public boolean serviceExists(URI serviceUri) throws ServiceException {
        if (serviceUri == null || !serviceUri.isAbsolute()) {
            log.warn("The Service URI is either absent or relative. Provide an absolute URI");
            return false;
        }

        URI graphUri;
        try {
            graphUri = getGraphUriForElement(serviceUri);
        } catch (URISyntaxException e) {
            log.warn("The namespace of the URI of the message content is incorrect.", e);
            return Boolean.FALSE;
        }

        if (graphUri == null) {
            log.warn("Could not obtain a graph URI for the element. The URI may not be managed by the server - " + serviceUri);
            return Boolean.FALSE;
        }

        String queryStr = new StringBuilder()
                .append("ASK { \n")
                .append("GRAPH <").append(graphUri.toASCIIString()).append("> {")
                .append("<").append(serviceUri.toASCIIString()).append("> <").append(RDF.type.getURI()).append("> <").append(MSM.Service).append("> }\n}").toString();

        Query query = QueryFactory.create(queryStr);
        QueryExecution qe = QueryExecutionFactory.sparqlService(this.graphStoreManager.getSparqlQueryEndpoint().toASCIIString(), query);
        MonitoredQueryExecution qexec = new MonitoredQueryExecution(qe);
        try {
            return qexec.execAsk();
        } finally {
            qexec.close();
        }
    }

    /**
     * Given the URI of a modelReference, this method figures out all the services
     * that have this as model references.
     * This method uses SPARQL 1.1 to avoid using regexs for performance.
     *
     * @param modelReference the type of output sought for
     * @return a Set of URIs of operations that generate this output type.
     */

    @Override
    public Set<URI> listServicesWithModelReference(URI modelReference) {
        if (modelReference == null) {
            return ImmutableSet.of();
        }

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder
                .append("PREFIX msm: <").append(MSM.getURI()).append("> ")
                .append("PREFIX rdf: <").append(RDF.getURI()).append("> ")
                .append("PREFIX sawsdl: <").append(SAWSDL.getURI()).append("> ")
                .append("SELECT ?service WHERE { ?service rdf:type msm:Service . ?service sawsdl:modelReference <").append(modelReference).append("> . }");
        String query = queryBuilder.toString();
        return this.graphStoreManager.listResourcesByQuery(query, "service");
    }

    /**
     * Given the URI of a modelReference, this method figures out all the operations
     * of which services have this as model references.
     * This method uses SPARQL 1.1 to avoid using regexs for performance.
     *
     * @param modelReference the type of output sought for
     * @return a Set of URIs of operations that generate this output type.
     */

    @Override
    public Set<URI> listOperationsWithModelReference(URI modelReference) {
        if (modelReference == null) {
            return ImmutableSet.of();
        }

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder
                .append("PREFIX msm: <").append(MSM.getURI()).append("> ")
                .append("PREFIX rdf: <").append(RDF.getURI()).append("> ")
                .append("PREFIX sawsdl: <").append(SAWSDL.getURI()).append("> ")
                .append("SELECT ?operation WHERE { ?service msm:hasOperation ?operation . ?service sawsdl:modelReference <").append(modelReference).append("> . }");
        String query = queryBuilder.toString();
        return this.graphStoreManager.listResourcesByQuery(query, "operation");
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
            return ImmutableSet.of();
        }

        URI graphUri;
        try {
            graphUri = getGraphUriForElement(elementUri);
        } catch (URISyntaxException e) {
            log.warn("The namespace of the URI of the message content is incorrect.", e);
            return ImmutableSet.of();
        }

        if (graphUri == null) {
            log.warn("Could not obtain a graph URI for the element. The URI may not be managed by the server - " + elementUri);
            return ImmutableSet.of();
        }

        String queryStr = new StringBuilder()
                .append("SELECT * WHERE { \n")
                .append(" GRAPH <").append(graphUri.toASCIIString()).append("> { \n")
                .append("<").append(elementUri.toASCIIString()).append("> <").append(RDF.type.getURI()).append("> ?type .")
                .append("FILTER(STRSTARTS(STR(?type), \"").append(MSM.NAMESPACE.getURI()).append("\"))")
                .append(" } \n ")
                .append("} \n ")
                .toString();

        return this.graphStoreManager.listResourcesByQuery(queryStr, "type");
    }

    /**
     * Obtains the list of model references for a given element.
     *
     * @param elementUri the URI of the element for which we want to obtain the model references
     * @return a Set of URIs with the model references of the given element. If there are no model references the Set should be empty NOT null.
     */
    @Override
    public Set<URI> listModelReferences(URI elementUri) {
        if (elementUri == null) {
            return ImmutableSet.of();
        }

        URI graphUri;
        try {
            graphUri = getGraphUriForElement(elementUri);
        } catch (URISyntaxException e) {
            log.warn("The namespace of the URI of the message content is incorrect.", e);
            return ImmutableSet.of();
        }

        if (graphUri == null) {
            log.warn("Could not obtain a graph URI for the element. The URI may not be managed by the server - " + elementUri);
            return ImmutableSet.of();
        }

        String queryStr = new StringBuilder()
                .append("SELECT DISTINCT ?model WHERE { \n")
                .append(" GRAPH <").append(graphUri.toASCIIString()).append("> { \n")
                .append("<").append(elementUri.toASCIIString()).append("> <").append(SAWSDL.modelReference.getURI()).append("> ?model")
                .append(" } \n ")
                .append("} \n ")
                .toString();

        return this.graphStoreManager.listResourcesByQuery(queryStr, "model");
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
        if (modelReference == null) {
            return ImmutableSet.of();
        }

        String queryStr = new StringBuilder()
                .append("SELECT DISTINCT ?element WHERE { \n")
                .append(" GRAPH ?g { \n")
                .append(" ?element <").append(SAWSDL.modelReference.getURI()).append("> <").append(modelReference.toASCIIString()).append("> .")
                .append(" } \n ")
                .append("} \n ")
                .toString();

        return this.graphStoreManager.listResourcesByQuery(queryStr, "element");
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
        return this.listEntitiesByDataModel(MSM.Operation, MSM.hasInput, modelReference);
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
        return this.listEntitiesByDataModel(MSM.Operation, MSM.hasOutput, modelReference);
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
        return this.listEntitiesByDataModel(MSM.Service, MSM.hasInput, modelReference);
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
        return this.listEntitiesByDataModel(MSM.Service, MSM.hasOutput, modelReference);
    }

    /**
     * This method will be called when the server is being shutdown.
     * Ensure a clean shutdown.
     */
    @Override
    public void shutdown() {
        super.shutdown();
        this.graphStoreManager.shutdown();
    }

    /**
     * Given the URI of a type (i.e., a modelReference), this method figures out all the entities of a type
     * (Service or Operation are the ones that are expected) that have this as part of their inputs or outputs. What
     * data relationship should be used is also parameterised.
     *
     * @param entityType       the type of entity we are looking for (i.e., service or operation)
     * @param dataPropertyType the kind of data property we are interested in (e.g., inputs, outputs, fault)
     * @param modelReference   the type of input sought for
     * @return a Set of URIs of the entities that matched the request or the empty Set otherwise.
     */
    private Set<URI> listEntitiesByDataModel(com.hp.hpl.jena.rdf.model.Resource entityType, Property dataPropertyType, URI modelReference) {

        if (modelReference == null) {
            return ImmutableSet.of();
        }

        StringBuilder queryBuilder = new StringBuilder()
                .append("SELECT DISTINCT ?entity WHERE { \n");

        // Deal with engines that store the inference in the default graph (e.g., OWLIM)
        queryBuilder.append("{").append("\n")
                .append(" ?entity <").append(RDF.type.getURI()).append("> <").append(entityType.getURI()).append("> .").append("\n");

        // Deal with the difference between Services and Operations
        if (entityType.equals(MSM.Service)) {
            queryBuilder.append(" ?entity <").append(MSM.hasOperation.getURI()).append("> / ").append("\n")
                    .append(" <").append(dataPropertyType.getURI()).append("> / ").append("\n");
        } else {
            queryBuilder.append(" ?entity <").append(dataPropertyType.getURI()).append("> / ").append("\n");
        }

        queryBuilder.append(" <").append(SAWSDL.modelReference.getURI()).append("> <").append(modelReference.toASCIIString()).append("> .").append("\n");

        // UNION
        queryBuilder.append("\n } UNION { \n");

        queryBuilder.append(" ?entity <").append(RDF.type.getURI()).append("> <").append(entityType.getURI()).append("> .").append("\n");

        // Deal with the difference between Services and Operations
        if (entityType.equals(MSM.Service)) {
            queryBuilder.append(" ?entity <").append(MSM.hasOperation.getURI()).append("> / ").append("\n")
                    .append(" <").append(dataPropertyType.getURI()).append("> ?message .").append("\n");
        } else {
            queryBuilder.append(" ?entity <").append(dataPropertyType.getURI()).append("> ?message .").append("\n");
        }

        queryBuilder.append("{ ?message <").append(MSM.hasPartTransitive.getURI()).append("> ?part . }").append("\n");
        queryBuilder.append(" UNION \n");
        queryBuilder.append("{ ?message <").append(MSM.hasPart.getURI()).append("> ?part . }").append("\n");
        queryBuilder.append(" UNION \n");
        queryBuilder.append("{ ?message <").append(MSM.hasOptionalPart.getURI()).append("> ?part . }").append("\n");
        queryBuilder.append(" UNION \n");
        queryBuilder.append("{ ?message <").append(MSM.hasMandatoryPart.getURI()).append("> ?part . }").append("\n");
        queryBuilder.append(" ?part <").append(SAWSDL.modelReference.getURI()).append("> <").append(modelReference.toASCIIString()).append("> .").append("\n } } ");

        return this.graphStoreManager.listResourcesByQuery(queryBuilder.toString(), "entity");
    }

    /**
     * Given a Resource and the new Uri base to use, modify each and every URL accordingly.
     * This methods maintains the naming used by the service already.
     * Recursive implementation that traverses the entire graph
     *
     * @param resource   The service that will be modified
     * @param newUriBase The new URI base
     */
    private void replaceUris(uk.ac.open.kmi.msm4j.Resource resource,
                             URI newUriBase) throws URISyntaxException {

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

            mcs = ((Operation) resource).getFaults();
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


    private URI getGraphUriForElement(URI elementUri) throws URISyntaxException {
        // Exit early if necessary
        if (elementUri == null || !elementUri.isAbsolute())
            return null;

        URI elmtPath = this.servicesUri.relativize(elementUri);
        // Exit if not relative to server
        if (elmtPath.isAbsolute())
            return null;

        String graphId = elmtPath.toASCIIString().substring(0, elmtPath.toASCIIString().indexOf('/'));
        return this.servicesUri.resolve(graphId);
    }

    private URI generateUniqueServiceUri() {
        return this.servicesUri.resolve(UriUtil.generateUniqueId());
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
}
