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
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.model.*;
import uk.ac.open.kmi.iserve.core.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;

import javax.inject.Named;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ServiceManagerIndexRdf extends ServiceManagerSparql implements ServiceManager {

    private static final Logger log = LoggerFactory.getLogger(ServiceManagerIndexRdf.class);

    // Service -> Operations
    private ConcurrentMap<URI, Set<URI>> svcOpMap;
    // Operation -> Inputs
    private ConcurrentMap<URI, Set<URI>> opInputMap;
    // Inputs - > Mandatory Parts
    private ConcurrentMap<URI, Set<URI>> messageMandatoryPartsMap;
    // Inputs - > Optional Parts
    private ConcurrentMap<URI, Set<URI>> messageOptionalPartsMap;
    // Operation -> Output
    private ConcurrentMap<URI, Set<URI>> opOutputMap;
    // Element -> Model Refs
    private ConcurrentMap<URI, Set<URI>> modelReferencesMap;

    @Inject
    ServiceManagerIndexRdf(EventBus eventBus,
                           SparqlGraphStoreFactory graphStoreFactory,
                           @Named(SystemConfiguration.ISERVE_URL_PROP) String iServeUri,
                           @Named(SystemConfiguration.SERVICES_REPOSITORY_SPARQL_PROP) String sparqlQueryEndpoint,
                           @Named(SystemConfiguration.SERVICES_REPOSITORY_SPARQL_UPDATE_PROP) String sparqlUpdateEndpoint,
                           @Named(SystemConfiguration.SERVICES_REPOSITORY_SPARQL_SERVICE_PROP) String sparqlServiceEndpoint) throws SalException {

        super(eventBus, graphStoreFactory, iServeUri, sparqlQueryEndpoint, sparqlUpdateEndpoint, sparqlServiceEndpoint);

        this.svcOpMap = new ConcurrentHashMap<URI, Set<URI>>();
        this.opInputMap = new ConcurrentHashMap<URI, Set<URI>>();
        this.messageMandatoryPartsMap = new ConcurrentHashMap<URI, Set<URI>>();
        this.messageOptionalPartsMap = new ConcurrentHashMap<URI, Set<URI>>();
        this.opOutputMap = new ConcurrentHashMap<URI, Set<URI>>();
        this.modelReferencesMap = new ConcurrentHashMap<URI, Set<URI>>();

        initialise();
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
        if (operationUri == null) {
            return ImmutableMultimap.of();
        }

        ImmutableMultimap.Builder<URI, URI> result = ImmutableMultimap.builder();
        for (URI input : this.opInputMap.get(operationUri)) {
            result.putAll(input, this.modelReferencesMap.get(input));
        }

        return result.build();
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

        return ImmutableSet.copyOf(this.opOutputMap.get(operationUri));
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
        if (operationUri == null) {
            return ImmutableMultimap.of();
        }

        ImmutableMultimap.Builder<URI, URI> result = ImmutableMultimap.builder();
        for (URI output : this.opOutputMap.get(operationUri)) {
            result.putAll(output, this.modelReferencesMap.get(output));
        }

        return result.build();
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

        return ImmutableSet.copyOf(this.messageMandatoryPartsMap.get(messageContent));
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

        return ImmutableSet.copyOf(this.messageOptionalPartsMap.get(messageContent));
    }

    /**
     * Creates a Service Description in the system.
     * Only needs to be fed with an MSM Service description.
     * <p/>
     * After successfully adding a service, implementations of this method should raise a {@code ServiceCreatedEvent}
     *
     * @param service the input service description in terms of MSM
     * @return the URI this service description was saved to
     * @throws ServiceException
     */
    @Override
    public URI addService(Service service) throws ServiceException {

        URI serviceUri = super.addService(service);
        if (serviceUri != null) {
            // Index service
            this.indexService(service);
        }

        return service.getUri();
    }

    /**
     * Deletes the given service
     * <p/>
     * After successfully deleting a service, implementations of this method should raise a {@code ServiceDeletedEvent}
     *
     * @param serviceUri the URI of the service to delete
     * @return True if it was deleted or false otherwise
     * @throws ServiceException
     */
    @Override
    public boolean deleteService(URI serviceUri) throws ServiceException {

        if (super.deleteService(serviceUri)) {
            // Update cache
            this.removeServiceFromCache(serviceUri);
            return true;
        }

        return false;
    }

    /**
     * Deletes the given service
     * <p/>
     * After successfully deleting a service, implementations of this method should raise a {@code ServiceDeletedEvent}
     *
     * @param service the service to delete
     * @return True if it was deleted or false otherwise
     * @throws ServiceException
     */
    @Override
    public boolean deleteService(Service service) throws ServiceException {
        if (super.deleteService(service)) {
            // Update cache
            this.removeServiceFromCache(service.getUri());
            return true;
        }

        return false;
    }

    /**
     * Deletes all the services on the registry.
     * This operation cannot be undone. Use with care.
     * <p/>
     * After successfully clearing the services, implementations of this method should raise a {@code ServicesClearedEvent}
     *
     * @return true if the service registry was cleared.
     * @throws ServiceException
     */
    @Override
    public boolean clearServices() throws ServiceException {

        if (super.clearServices()) {
            // Initialise
            initialise();
            return true;
        }

        return true;

    }

    /**
     * Determines whether a service is known to the registry
     *
     * @param serviceUri the URI of the service being looked up
     * @return True if it is registered in the server
     * @throws ServiceException
     */
    @Override
    public boolean serviceExists(URI serviceUri) throws ServiceException {
        return this.svcOpMap.containsKey(serviceUri);
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

        return ImmutableSet.copyOf(this.modelReferencesMap.get(elementUri));
    }

    /**
     * This method will be called when the server is initialised.
     * If necessary it should take care of updating any indexes on boot time.
     */
    private void initialise() {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();
        populateCache();
        stopwatch.stop();
        log.info("Cache populated. Time taken {}", stopwatch);
    }

    private void populateCache() {
        // clear
        this.svcOpMap.clear();
        this.opInputMap.clear();
        this.messageMandatoryPartsMap.clear();
        this.messageOptionalPartsMap.clear();
        this.opOutputMap.clear();
        this.modelReferencesMap.clear();

        // fill up
        Set<URI> services = super.listServices();
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
        super.shutdown();
    }
}
