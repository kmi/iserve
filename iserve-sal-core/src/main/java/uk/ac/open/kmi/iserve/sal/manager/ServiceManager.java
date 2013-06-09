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
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
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

    /**
     * Deletes all the services on the registry.
     * This operation cannot be undone. Use with care.
     *
     * @return
     * @throws ServiceException
     */
    public abstract boolean clearServices()
            throws ServiceException;

    // General Management Methods

    /**
     * Obtains a list of service URIs with all the services known to the system
     *
     * @return list of URIs with all the services in the registry
     */
    public abstract List<URI> listServices();

    /**
     * Determines whether a service is known to the registry
     *
     * @param serviceUri the URI of the service being looked up
     * @return True if it is registered in the server
     * @throws ServiceException
     */
    public abstract boolean serviceExists(URI serviceUri) throws ServiceException;

}