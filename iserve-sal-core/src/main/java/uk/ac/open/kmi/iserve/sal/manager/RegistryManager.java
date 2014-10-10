/*
 * Copyright (c) 2014. Knowledge Media Institute - The Open University
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

import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.msm4j.io.impl.ServiceTransformationEngine;

import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * iServe Manager provides the main entry point to the entire Storage and Access Layer of a given registry.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 17/10/2013
 */
public interface RegistryManager {


    /**
     * Register the given object as an Observer for this iServe instance.
     * All events created within iServe would be notified. Currently based on Guava's EventBus.
     * In order to process them, the corresponding observer should only implement a method that takes as sole parameter
     * the event wanted to process. This method should additionally be annotated with {@code @Subscribe} for the system
     * to work.
     *
     * @param obj The observer
     */
    void registerAsObserver(Object obj);

    /**
     * Unregister observer. Events won't be notified to this observer any longer.
     *
     * @param obj The observer
     */
    void unregisterObserver(Object obj);

    /**
     * Obtains the Service Manager for this instance of iServe
     *
     * @return the Service Manager
     */
    ServiceManager getServiceManager();

    /**
     * Obtains the Knowledge Base Manager for this instance of iServe
     *
     * @return the Knowledge Base Manager
     */
    KnowledgeBaseManager getKnowledgeBaseManager();

    /**
     * Obtains the Document Manager for this instance of iServe
     *
     * @return the Document Manager
     */
    DocumentManager getDocumentManager();

    /**
     * Obtains the Service Transformation Engine for this instance
     *
     * @return the ServiceTransformationEngine
     */
    ServiceTransformationEngine getServiceTransformationEngine();

    /**
     * This method will be called when the server is being shutdown.
     * Ensure a clean shutdown.
     */
    void shutdown();

    /**
     * Obtains the list of supported media types the engine can import
     *
     * @return the Set of media types
     */
    Set<String> getSupportedInputMediaTypes();

    /**
     * Checks if the given media type can be imported
     *
     * @param mediaType the media type we wish to check if it can be imported
     * @return true if it is supported or false otherwise.
     */
    boolean canImport(String mediaType);

    /**
     * Gets the corresponding filename filter to a given media type by checking both native formats
     * and those supported through transformation
     *
     * @param mediaType the media type for which to obtain the file extension
     * @return the filename filter or null if it is not supported. Callers are advised to check
     * first that the media type is supported {@see canTransform} .
     */
    public FilenameFilter getFilenameFilter(String mediaType);

    /**
     * Imports a new service within iServe. The original document is stored
     * in the server and the transformed version registered within iServe.
     *
     * @param servicesContentStream
     * @param mediaType
     * @return the List of URIs of the services imported
     * @throws SalException
     */
    List<URI> importServices(InputStream servicesContentStream,
                             String mediaType) throws SalException;

    /**
     * Imports a new service from a remote description within iServe. The original document is stored
     * in the server and the transformed version registered within iServe.
     *
     * @param servicesContentLocation
     * @param mediaType
     * @return the List of URIs of the services imported
     * @throws SalException
     */
    List<URI> importServices(URI servicesContentLocation,
                             String mediaType) throws SalException;


    /**
     * Clears the registry entirely: all documents and services are deleted
     * This operation cannot be undone. Use with care.
     *
     * @return true if the registry was cleared.
     * @throws SalException
     */
    boolean clearRegistry() throws SalException;

    /**
     * Registers all the services found on a given source document. This operation takes care of performing the
     * appropriate format transformation. The source document is not stored on the server. For keeping a copy of the
     * source document within the server use @see importServices instead.
     *
     * @param sourceDocumentUri
     * @param mediaType
     * @return the List of URIs of the services registered
     * @throws SalException
     */
    List<URI> registerServices(URI sourceDocumentUri, String mediaType) throws SalException;

    /**
     * Unregisters a service from the registry. Effectively this will delete the service description and remove any
     * related documents on the server.
     *
     * @param serviceUri the URI of the service to unregister
     * @return true if it was properly unregistered, false otherwise.
     * @throws SalException
     */
    boolean unregisterService(URI serviceUri) throws SalException;

    /**
     * Exports a given service in the media type requested
     *
     * @param serviceUri the URI of the service to export
     * @param mediaType  the media type to use for the export
     * @return the String representation of the service in the given format
     * @throws ServiceException
     */
    String exportService(URI serviceUri, String mediaType)
            throws ServiceException;
}
