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

import uk.ac.open.kmi.iserve.sal.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.exception.SalException;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

/**
 * General Purpose Manager for iServe.
 * <p/>
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 */

public interface iServeManager extends iServeComponent {

    // TODO: Implement the Observer pattern so that we can update other software about changes

    public abstract SystemConfiguration getConfiguration();

    /**
     * Obtains the Service Manager for this instance of iServe
     *
     * @return the Service Manager
     */
    public ServiceManager getServiceManager();

    /**
     * Obtains the Knowledge Base Manager for this instance of iServe
     *
     * @return the Knowledge Base Manager
     */
    public KnowledgeBaseManager getKnowledgeBaseManager();


    /**
     * Obtains the Document Manager for this instance of iServe
     *
     * @return the Document Manager
     */
    public DocumentManager getDocumentManager();

	/*
     * Registry Management Methods
	 */

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
    public abstract List<URI> registerServices(URI sourceDocumentUri,
                                               String mediaType) throws SalException;

    /**
     * Unregisters the service from iServe. Any related documents are also
     * deleted in the process.
     *
     * @param serviceUri
     * @return true if the service was successfully unregistered
     * @throws SalException
     */
    public abstract boolean unregisterService(URI serviceUri) throws SalException;

    /**
     * Imports a new service within iServe. The original document is stored
     * in the server and the transformed version registered within iServe.
     *
     * @param servicesContentStream
     * @param mediaType
     * @return the List of URIs of the services imported
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

    /**
     * Obtains the Service in a particular syntax
     *
     * @param serviceUri the URI of the service to retrieve
     * @param mediaType  the format to use for the serialisation
     * @return the string representation of the service in the format requested
     * @throws SalException
     */
    public abstract String exportService(URI serviceUri, String mediaType) throws SalException;

}

