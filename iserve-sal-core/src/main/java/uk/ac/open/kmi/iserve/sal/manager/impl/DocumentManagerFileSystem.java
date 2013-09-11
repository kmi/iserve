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

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.util.FileUtil;
import uk.ac.open.kmi.iserve.sal.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.events.DocumentCreatedEvent;
import uk.ac.open.kmi.iserve.sal.events.DocumentDeletedEvent;
import uk.ac.open.kmi.iserve.sal.events.DocumentsClearedEvent;
import uk.ac.open.kmi.iserve.sal.exception.DocumentException;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.manager.DocumentManager;
import uk.ac.open.kmi.iserve.sal.manager.IntegratedComponent;
import uk.ac.open.kmi.iserve.sal.util.UriUtil;

import javax.inject.Named;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class DocumentManagerFileSystem extends IntegratedComponent implements DocumentManager {

    private static final Logger log = LoggerFactory.getLogger(DocumentManagerFileSystem.class);

    // Default path for service and documents URIs.
    // Note that any change here should also affect the REST API
    // Keep the trailing slash
    private static final String DEFAULT_DOC_URL_PATH = "id/documents/";

    private static final String DEFAULT_DOC_FOLDER_PATH = "/tmp/";

    private URI documentsInternalPath;
    private URI documentsPublicUri;

    @Inject
    DocumentManagerFileSystem(EventBus eventBus,
                              @Named(SystemConfiguration.ISERVE_URL_PROP) String iServeUri,
                              @Named(SystemConfiguration.DOC_FOLDER_PATH_PROP) String documentsFolderPath) throws SalException {
        super(eventBus, iServeUri);

        if (documentsFolderPath == null) {
            documentsFolderPath = DEFAULT_DOC_FOLDER_PATH;
        }

        this.documentsPublicUri = this.getIserveUri().resolve(DEFAULT_DOC_URL_PATH);

        try {
            // Set the internal URI to the docs folder
            this.documentsInternalPath = new URI(documentsFolderPath + (documentsFolderPath.endsWith("/") ? "" : "/"));  // Ensure it has a final slash

            if (!this.documentsInternalPath.isAbsolute()) {
                // Obtain absolute URI
                log.warn("Configuring document manager with relative path. Documents may be deleted when the application is redeployed.");
                this.documentsInternalPath = this.getClass().getResource(".").toURI().resolve(this.documentsInternalPath);
            }

        } catch (URISyntaxException e) {
            log.error("Incorrect iServe URI while initialising " + this.getClass().getSimpleName(), e);
            throw new SalException("Incorrect iServe URI while initialising " + this.getClass().getSimpleName(), e);
        }

        File file = new File(this.documentsInternalPath);
        if (!file.exists()) {
            if (file.mkdirs()) {
                log.info("Created documents folder.");
            } else {
                throw new DocumentException("Unable to create documents folder.");
            }
        } else {
            if (!file.isDirectory()) {
                throw new DocumentException("There already exists a file with the documents folder URI.");
            }
        }
    }

    /**
     * @return
     */
    private URI getDocumentsInternalPath() {
        return this.documentsInternalPath;
    }

    /**
     * Obtain the relative URI of a document
     *
     * @param documentUri
     * @return
     */
    private URI getDocumentRelativeUri(URI documentUri) {
        return documentsPublicUri.relativize(documentUri);
    }

    /**
     * Obtain the Internal URI for a given document.
     *
     * @param documentUri
     * @return
     */
    private URI getDocumentInternalUri(URI documentUri) {
        return this.getDocumentsInternalPath().
                resolve(this.getDocumentRelativeUri(documentUri));
    }

    /**
     * @param file
     * @return
     */
    private URI getDocumentPublicUri(File file) {
        URI relativeUri = this.getDocumentsInternalPath().
                relativize(file.toURI());
        return documentsPublicUri.resolve(relativeUri);
    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#listDocument()
     */
    @Override
    public List<URI> listDocuments() throws DocumentException {
        List<URI> result = new ArrayList<URI>();
        File documentsFolder = new File(this.getDocumentsInternalPath());
        File[] docsList = documentsFolder.listFiles();
        for (File doc : docsList) {
            if (doc.isFile())
                result.add(this.getDocumentPublicUri(doc));
        }
        return result;
    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#getDocument(java.io.InputStream)
     */
    @Override
    public InputStream getDocument(URI documentUri) throws DocumentException {

        if (!documentExists(documentUri)) {
            return null;
        }

        File file = new File(this.getDocumentInternalUri(documentUri));
        InputStream result = null;
        try {
            result = new FileInputStream(file);
        } catch (IOException e) {
            throw new DocumentException(e);
        }
        return result;
    }

//	/* (non-Javadoc)
//	 * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#addDocument(java.lang.String, java.lang.String)
//	 */
//	@Override
//	public URI addDocumentToService(String fileName, String docContent, String serviceId) throws DocumentException {
//		
//		URI folder = this.getDocumentsInternalPath().resolve(serviceId); 
//		URI fileUri = folder.resolve(fileName);
//		URI result = null;
//		try {
//			FileUtil.createDirIfNotExists(new File(folder));
//			File file = new File(fileUri);
//			IOUtil.writeString(docContent, file);
//			result = this.getDocumentPublicUri(file);
//		} catch (IOException e) {
//			throw new DocumentException(e);
//		}
//		return result;
//	}

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#createDocument(java.io.InputStream, java.lang.String )
     */
    @Override
    public URI createDocument(InputStream docContent, String fileExtension) throws DocumentException {

        URI newDocUri = this.generateUniqueDocumentUri(fileExtension);
        try {
            URI internalDocUri = this.getDocumentInternalUri(newDocUri);
            File file = new File(internalDocUri);
            FileOutputStream out = new FileOutputStream(file);
            FileUtil.createDirIfNotExists(file.getParentFile());
            int size = IOUtils.copy(docContent, out);
            log.info("Document internal URI - " + internalDocUri.toASCIIString() + " - " + size + " bytes.");
        } catch (IOException e) {
            throw new DocumentException("Unable to add document to service.", e);
        }

        // Generate Event
        this.getEventBus().post(new DocumentCreatedEvent(new Date(), newDocUri));

        return newDocUri;
    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#deleteDocument(java.net.URI)
     */
    @Override
    public boolean deleteDocument(URI documentUri) throws DocumentException {
        // delete from hard disk
        boolean result = false;
        URI fileUri = this.getDocumentInternalUri(documentUri);
        File file = new File(fileUri);
        if (file.exists()) {
            result = file.delete();
            if (result) {
                log.info("File deleted: " + file.getName());
                // Generate Event
                this.getEventBus().post(new DocumentDeletedEvent(new Date(), documentUri));
            } else {
                log.warn("File does not exist. Unable to delete it: " + file.getAbsolutePath());
            }
        }
        return result;
    }

    /**
     * Deletes all the documents in iServe
     *
     * @return
     * @throws uk.ac.open.kmi.iserve.sal.exception.DocumentException
     *
     */
    @Override
    public boolean clearDocuments() throws DocumentException {
        File internalFolder = new File(this.getDocumentsInternalPath());
        File[] files = internalFolder.listFiles();
        for (File file : files) {
            file.delete();
            log.info("File deleted: " + file.getAbsolutePath());
        }

        // Generate Event
        this.getEventBus().post(new DocumentsClearedEvent(new Date()));

        return true;
    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#documentExists(java.net.URI)
     */
    @Override
    public boolean documentExists(URI documentUri) throws DocumentException {
        URI fileUri = getDocumentInternalUri(documentUri);
        File file = new File(fileUri);
        return file.exists();
    }

    private URI generateUniqueDocumentUri(String fileExtension) {
        String uid = UriUtil.generateUniqueId();
        return documentsPublicUri.resolve(uid + "." + fileExtension);
    }

    /**
     * This method will be called when the server is initialised.
     * If necessary it should take care of updating any indexes on boot time.
     */
    @Override
    public void initialise() {
        // TODO: implement
    }

    /**
     * This method will be called when the server is being shutdown.
     * Ensure a clean shutdown.
     */
    @Override
    public void shutdown() {
        // TODO: implement
    }
}
