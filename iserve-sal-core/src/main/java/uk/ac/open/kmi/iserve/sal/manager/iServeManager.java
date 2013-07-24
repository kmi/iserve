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
import uk.ac.open.kmi.iserve.sal.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.exception.SalException;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

/**
 * General Purpose Manager for iServe.
 * Higher Level Access to Data management within iServe. Only a controlled subset
 * of the inner methods for data management are exposed at this level
 * <p/>
 *
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */

public interface iServeManager {

    // TODO: Implement the Observer pattern so that we can update other software about changes

    public abstract SystemConfiguration getConfiguration();

    /**
     * This method will be called when the server is being shutdown.
     * Ensure a clean shutdown.
     */
    public abstract void shutdown();

	/*
     * Service Management Methods
	 */

    // Create

    /**
     * Register a new service. This operation takes care of performing the
     * appropriate format transformation. The source document is not stored on
     * the server. For keeping a copy of the source document within the server
     * use @see importService instead.
     *
     * @param sourceDocumentUri
     * @param mediaType
     * @return
     * @throws SalException
     */
    public abstract URI registerService(URI sourceDocumentUri,
                                        String mediaType) throws SalException;

    /**
     * Imports a new service within iServe. The original document is stored
     * in the server and the transformed version registered within iServe.
     *
     * @param serviceContent
     * @param mediaType
     * @return
     * @throws SalException
     */
    public abstract URI importService(InputStream serviceContent,
                                      String mediaType) throws SalException;

    /**
     * Clears the registry entirely: all documents and services are deleted
     * This operation cannot be undone. Use with care.
     *
     * @return true if the registry was cleared.
     * @throws SalException
     */
    public abstract boolean clearRegistry() throws SalException;

    // Read

    // TODO: Syntax and ServiceFormat are sort overlapping here

    /**
     * Obtains the Service in a particular syntax
     *
     * @param serviceUri the URI of the service to retrieve
     * @param mediaType  the format to use for the serialisation
     * @return the string representation of the service in the format requested
     * @throws SalException
     */
    public abstract String exportService(URI serviceUri, String mediaType) throws SalException;

    /**
     * Obtains the Service
     *
     * @param serviceUri the URI of the service to retrieve
     * @return the service instance or null if it could not be obtained
     * @throws SalException
     */
    public abstract Service getService(URI serviceUri) throws SalException;

    /**
     * Obtains the Services requested
     *
     * @param serviceUris the list of URIs to retrieve
     * @return the list of services or an empty list if none could be obtained
     * @throws SalException
     */
    public abstract List<Service> getServices(List<URI> serviceUris) throws SalException;

    /**
     * List all the known services
     *
     * @return list with the URIs of all known services
     * @throws SalException
     */
    public abstract List<URI> listServices() throws SalException;

    /**
     * Checks if a service exists in the repository
     *
     * @param serviceUri the URI of the service
     * @return true if it exists in the registry, false otherwise
     * @throws SalException
     */
    public abstract boolean serviceExists(URI serviceUri) throws SalException;

    /**
     * Lists all documents related to a given service
     *
     * @param serviceUri the service URI
     * @return the List of the URIs of all the documents related to the service
     * @throws SalException
     */
    public abstract List<URI> listDocumentsForService(URI serviceUri) throws SalException;

    // Delete

    /**
     * Unregisters the service from iServe. Any related documents are also
     * deleted in the process.
     *
     * @param serviceUri
     * @return
     * @throws SalException
     */
    public abstract boolean unregisterService(URI serviceUri) throws SalException;

    /**
     * Delete all services from the server
     * This operation cannot be undone. Use with care.
     *
     * @return true if all the services could be deleted.
     * @throws SalException
     */
    public abstract boolean clearServices() throws SalException;

    // Update Operations

    /**
     * Relates a given document to a service. The relationship is just a generic one.
     *
     * @param serviceUri      the service URI
     * @param relatedDocument the related document URI
     * @return True if successful or False otherwise
     * @throws SalException
     */
    public abstract boolean addRelatedDocumentToService(URI serviceUri, URI relatedDocument) throws SalException;

	/*
     *  Document Management Operations
	 */

    // Create

    /**
     * Adds a document to the system. Documents are resources on their own
     * but are expected to be linked to services.
     *
     * @param docContent    content of the document
     * @param fileExtension the file extension to use for the document
     * @return
     * @throws SalException
     * @see
     */
    public URI createDocument(InputStream docContent, String fileExtension) throws SalException;

    // Read

    /**
     * Lists all known documents
     *
     * @return the list of the URIs of all the documents known to this server
     * @throws SalException
     */
    public abstract List<URI> listDocuments() throws SalException;

    /**
     * Obtains a particular document
     *
     * @param documentUri the URI of the document to get
     * @return the InputStream with the content of the document
     * @throws SalException
     */
    public abstract InputStream getDocument(URI documentUri) throws SalException;

    // Delete

    /**
     * Deletes a document from the server
     *
     * @param documentUri the URI of the document to delete
     * @return true if it was successful or null otherwise
     * @throws SalException
     */
    public abstract boolean deleteDocument(URI documentUri) throws SalException;

    /**
     * Deletes all documents from the server
     * This operation cannot be undone. Use with care.
     *
     * @return true if all the documents could be deleted.
     * @throws SalException
     */
    public abstract boolean clearDocuments() throws SalException;

}

