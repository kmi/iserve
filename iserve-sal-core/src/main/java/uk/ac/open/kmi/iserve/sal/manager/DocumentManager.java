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

import uk.ac.open.kmi.iserve.sal.ServiceFormat;
import uk.ac.open.kmi.iserve.sal.exception.DocumentException;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

/**
 * Class Description
 * TODO: Distinguish between the metadata and the actual document and provide
 * methods for accessing both
 *
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public interface DocumentManager {

    // Create Methods
    public abstract URI createDocument(InputStream docContent, ServiceFormat format)
            throws DocumentException;

    // Read Methods
    public abstract InputStream getDocument(URI documentUri)
            throws DocumentException;

    // Delete Methods
    public abstract boolean deleteDocument(URI documentUri)
            throws DocumentException;

    /**
     * Deletes all the documents in iServe
     *
     * @return
     * @throws DocumentException
     */
    public abstract boolean clearDocuments()
            throws DocumentException;

    // General Management Methods
    public abstract List<URI> listDocuments() throws DocumentException;

    public abstract List<URI> listDocumentsForService(URI serviceURI)
            throws DocumentException;

    public abstract boolean documentExists(URI documentUri)
            throws DocumentException;

}