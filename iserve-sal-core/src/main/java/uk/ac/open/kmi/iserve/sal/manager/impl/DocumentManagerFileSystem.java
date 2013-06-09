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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.FileUtil;
import uk.ac.open.kmi.iserve.commons.io.IOUtil;
import uk.ac.open.kmi.iserve.sal.ServiceFormat;
import uk.ac.open.kmi.iserve.sal.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.exception.DocumentException;
import uk.ac.open.kmi.iserve.sal.manager.DocumentManager;
import uk.ac.open.kmi.iserve.sal.util.UriUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class DocumentManagerFileSystem implements DocumentManager {

    private static final Logger log = LoggerFactory.getLogger(ManagerSingleton.class);

    private SystemConfiguration configuration;

    /**
     * Constructor for the Document Manager. Protected to avoid external access.
     * Any access to this should take place through the iServeManager
     *
     * @param configuration
     */
    protected DocumentManagerFileSystem(SystemConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * @return
     */
    private URI getDocumentsInternalPath() {
        return URI.create(configuration.getDocumentsFolder());
    }

    /**
     * Obtain the relative URI of a document
     *
     * @param documentUri
     * @return
     */
    private URI getDocumentRelativeUri(URI documentUri) {
        return configuration.getDocumentsUri().relativize(documentUri);
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
        return configuration.getDocumentsUri().resolve(relativeUri);
    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#listDocument()
     */
    @Override
    public List<URI> listDocuments() throws DocumentException {
        List<URI> result = new ArrayList<URI>();
        File wsdlFolder = new File(configuration.getDocumentsFolder());
        File[] folderList = wsdlFolder.listFiles();
        for (int i = 0; i < folderList.length; i++) {
            if (folderList[i].isDirectory() == true) {
                String[] fileList = folderList[i].list();
                if (fileList.length > 0) {
                    // TODO: This assumes only one document per folder/service.
                    URI docsBaseUri = configuration.getDocumentsUri();
                    result.add(docsBaseUri.resolve(folderList[i].getName() + "/" + fileList[0]));
                }
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#listDocumentsForService(java.net.URI)
     */
    @Override
    public List<URI> listDocumentsForService(URI serviceURI) throws DocumentException {
        //TODO: Implement
        throw new DocumentException("Not yet implemented");
    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#getDocument(java.lang.String)
     */
    @Override
    public String getDocument(URI documentUri) throws DocumentException {

        if (!documentExists(documentUri)) {
            return null;
        }

        File file = new File(this.getDocumentInternalUri(documentUri));
        String result = null;
        try {
            result = IOUtil.readString(file);
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
     * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#createDocument(java.io.InputStream, uk.ac.open.kmi.iserve.sal.ServiceFormat )
     */
    @Override
    public URI createDocument(InputStream docContent, ServiceFormat format) throws DocumentException {

        URI newDocUri = this.generateUniqueDocumentUri(format);
        try {
            URI internalDocUri = this.getDocumentInternalUri(newDocUri);
            File file = new File(internalDocUri);
            FileUtil.createDirIfNotExists(file.getParentFile());
            IOUtil.writeStream(docContent, file);
            log.info("Document created - " + file.getName());
            log.info("Document URI - " + internalDocUri.toASCIIString());
        } catch (IOException e) {
            throw new DocumentException("Unable to add document to service.", e);
        }
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
            } else {
                log.info("File does not exist. Unable to delete it: " + file.getName());
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
            log.info("File deleted: " + file.getName());
        }
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

    private URI generateUniqueDocumentUri(ServiceFormat format) {
        String uid = UriUtil.generateUniqueId();
        return configuration.getDocumentsUri().resolve(uid + "." + format.getFileExtension());
    }

}
