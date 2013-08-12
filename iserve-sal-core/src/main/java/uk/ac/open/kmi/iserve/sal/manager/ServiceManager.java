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

import uk.ac.open.kmi.iserve.commons.model.Service;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;

import java.net.URI;
import java.util.List;

/**
 * Interface that defines the operations a Service Manager should offer
 * If other implementations backed by different types of storage systems are
 * needed we should update this.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 */
public interface ServiceManager {

    // Create Methods

    /**
     * Creates a Service Description in the system.
     * Only needs to be fed with an MSM Service description.
     *
     * @param service the input service description in terms of MSM
     * @return the URI this service description was saved to
     * @throws ServiceException
     */
    public URI addService(Service service) throws ServiceException;

    // Update Methods

    /**
     * Relates a given document to a service. The relationship is just a generic one.
     *
     * @param serviceUri      the service URI
     * @param relatedDocument the related document URI
     * @return True if successful or False otherwise
     * @throws uk.ac.open.kmi.iserve.sal.exception.SalException
     *
     */
    public abstract boolean addRelatedDocumentToService(URI serviceUri, URI relatedDocument) throws ServiceException;

    // Read Methods

    /**
     * Obtains the service description of the service identified by the URI
     *
     * @param serviceUri the URI of the service to obtain
     * @return the service description if it exists or null otherwise
     * @throws ServiceException
     */
    public abstract Service getService(URI serviceUri)
            throws ServiceException;

    /**
     * Obtains the service descriptions for all the services identified by the URI List
     *
     * @param serviceUris the URIs of the service to obtain
     * @return the list of all services that could be obtained. If none could be obtained the list will be empty.
     * @throws ServiceException
     */
    public abstract List<Service> getServices(List<URI> serviceUris)
            throws ServiceException;

    // Listing Methods

    /**
     * Obtains a list of service URIs with all the services known to the system
     *
     * @return list of URIs with all the services in the registry
     */
    public abstract List<URI> listServices();

    /**
     * Obtains the list of operation URIs for a given Operation
     *
     * @param serviceUri the service URI
     * @return a List of URIs with the operations provided by the service. If there are no operations, the List should be empty NOT null.
     */
    public abstract List<URI> listOperations(URI serviceUri);

    /**
     * Obtains the list of input URIs for a given Operation
     *
     * @param operationUri the operation URI
     * @return a List of URIs with the inputs of the operation. If no input is necessary the List should be empty NOT null.
     */
    public abstract List<URI> listInputs(URI operationUri);

    /**
     * Obtains the list of output URIs for a given Operation
     *
     * @param operationUri the operation URI
     * @return a List of URIs with the outputs of the operation. If no output is provided the List should be empty NOT null.
     */
    public abstract List<URI> listOutputs(URI operationUri);

    /**
     * Lists all documents related to a given service
     *
     * @param serviceUri the service URI
     * @return the List of the URIs of all the documents related to the service
     * @throws ServiceException
     */
    public abstract List<URI> listDocumentsForService(URI serviceUri) throws ServiceException;

    // Delete Methods

    /**
     * Deletes the given service
     *
     * @param serviceUri the URI of the service to delete
     * @return True if it was deleted or false otherwise
     * @throws ServiceException
     */
    public abstract boolean deleteService(URI serviceUri)
            throws ServiceException;

    /**
     * Deletes the given service
     *
     * @param service the service to delete
     * @return True if it was deleted or false otherwise
     * @throws ServiceException
     */
    public abstract boolean deleteService(Service service)
            throws ServiceException;

    // General Management Methods

    /**
     * Deletes all the services on the registry.
     * This operation cannot be undone. Use with care.
     *
     * @return
     * @throws ServiceException
     */
    public abstract boolean clearServices()
            throws ServiceException;


    /**
     * Determines whether a service is known to the registry
     *
     * @param serviceUri the URI of the service being looked up
     * @return True if it is registered in the server
     * @throws ServiceException
     */
    public abstract boolean serviceExists(URI serviceUri) throws ServiceException;

}