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
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
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
     * Registers all the services found on a given source document. This operation takes care of performing the
     * appropriate format transformation. The source document is not stored on the server. For keeping a copy of the
     * source document within the server use @see importServices instead.
     *
     * @param sourceDocumentUri
     * @param mediaType
     * @return
     * @throws SalException
     */
    public abstract List<URI> registerServices(URI sourceDocumentUri,
                                               String mediaType) throws SalException;

    /**
     * Register a new service. Given a service already expressed in terms of MSM, this method takes care of registering
     * it within the server. This method makes no guarantee about the availability of the source document or any other
     * related document. The calling application should ensure this is correct.
     *
     * @param service the MSM service to register
     * @return the resulting URI of the service within the server.
     * @throws SalException
     */
    public abstract URI registerService(Service service) throws SalException;

    /**
     * Imports a new service within iServe. The original document is stored
     * in the server and the transformed version registered within iServe.
     *
     * @param servicesContentStream
     * @param mediaType
     * @return
     * @throws SalException
     */
    public abstract List<URI> importServices(InputStream servicesContentStream,
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
     * Obtains the list of operation URIs for a given Operation
     *
     * @param serviceUri the service URI
     * @return a List of URIs with the operations provided by the service. If there are no operations, the List should be empty NOT null.
     */
    public abstract List<URI> listOperations(URI serviceUri) throws SalException;

    /**
     * Obtains the list of input URIs for a given Operation
     *
     * @param operationUri the operation URI
     * @return a List of URIs with the inputs of the operation. If no input is necessary the List should be empty NOT null.
     */
    public abstract List<URI> listInputs(URI operationUri) throws SalException;

    /**
     * Obtains the list of output URIs for a given Operation
     *
     * @param operationUri the operation URI
     * @return a List of URIs with the outputs of the operation. If no output is provided the List should be empty NOT null.
     */
    public abstract List<URI> listOutputs(URI operationUri) throws SalException;

    /**
     * Obtains the list of mandatory parts for a given Message Content
     *
     * @param messageContent the message content URI
     * @return a List of URIs with the mandatory parts of the message content. If there are no parts the List should be empty NOT null.
     */
    public abstract List<URI> listMandatoryParts(URI messageContent);

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

    // Knowledge Base Management

    /**
     * Answers a List all of URIs of the classes that are known to be equivalent to this class. Equivalence may be
     * asserted in the model (using, for example, owl:equivalentClass, or may be inferred by the reasoner attached to
     * the model. Note that the OWL semantics entails that every class is equivalent to itself, so when using a
     * reasoning model clients should expect that this class will appear as a member of its own equivalent classes.
     *
     * @param classUri the URI of the class for which to list the equivalent classes
     * @return the List of equivalent classes
     */
    public List<URI> listEquivalentClasses(URI classUri);

    /**
     * Answers a List of all the URIs of the classes that are declared to be sub-classes of this class.
     *
     * @param classUri the URI of the class for which to list the subclasses.
     * @param direct   if true only the direct subclasses will be listed.
     * @return the list of subclasses
     */
    public List<URI> listSubClasses(URI classUri, boolean direct);

    /**
     * Answers a List of all the URIs of the classes that are declared to be super-classes of this class.
     *
     * @param classUri the URI of the class for which to list the super-classes.
     * @param direct   if true only the direct super-classes will be listed.
     * @return the list of super-classes
     */
    public List<URI> listSuperClasses(URI classUri, boolean direct);

}

