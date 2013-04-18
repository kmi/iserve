/*
   Copyright ${year}  Knowledge Media Institute - The Open University

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package uk.ac.open.kmi.iserve.sal.manager.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.utils.IOUtils;

import uk.ac.open.kmi.iserve.commons.io.FileUtil;
import uk.ac.open.kmi.iserve.commons.io.IOUtil;
import uk.ac.open.kmi.iserve.commons.io.StringUtil;
import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.sal.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.exception.DocumentException;
import uk.ac.open.kmi.iserve.sal.manager.DocumentManager;
import uk.ac.open.kmi.iserve.sal.util.UriUtil;

public class DocumentManagerFileSystem implements DocumentManager {
	
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
	private URI getDocumentsPublicPath() {
		return URI.create(configuration.getIserveUrl() + MSM.DOCUMENT_INFIX);
	}

	/**
	 * @return
	 */
	private URI getDocumentsInternalPath() {
		return URI.create(configuration.getDocumentsFolder());
	}
	
	/**
	 * Obtain the relative internal URI of a document 
	 * 
	 * @param documentUri
	 * @return
	 */
	private URI getRelativeInternalUri(URI documentUri) {
		URI documentsPath = getDocumentsPublicPath();
		URI result = documentsPath.relativize(documentUri);
		return result;
	}
	
	/**
	 * Obtain the Internal URI for a given document.
	 * 
	 * @param documentUri
	 * @return
	 */
	private URI getDocumentInternalUri(URI documentUri) {
		URI relativeUri = this.getRelativeInternalUri(documentUri);
		URI docsFolder = getDocumentsInternalPath();
		return relativeUri.relativize(docsFolder); 
	}
	
	/**
	 * Obtain the Internal URI for a given document.
	 * 
	 * @param serviceId
	 * @param fileName
	 * @return
	 */
	private URI getDocumentInternalUri(String serviceId, String fileName) {
		URI docsFolder = getDocumentsInternalPath();
		return docsFolder.resolve(serviceId + "/" + fileName);
	}
	
	/**
	 * Obtain the Internal URI for a given document.
	 * 
	 * @param serviceURI
	 * @param fileName
	 * @return
	 * @throws URISyntaxException 
	 */
	private URI getDocumentInternalUri(URI serviceUri, String fileName) throws URISyntaxException {
		URI docsFolder = getDocumentsInternalPath();
		return docsFolder.resolve(
				UriUtil.getUniqueId(serviceUri, 
						configuration.getIserveUrl().toURI()) + "/" + fileName);
	}

	/**
	 * @param file
	 * @return
	 */
	private URI getDocumentPublicUri(File file) {
		//TODO: Implement
		return null; 
	}
	
	// TODO: The constant doesn't belong to MSM but rather to iServe Config
	// TODO: Move the entire method to the configuration class? (needs aligning servlets definitions and configuration)
	private URI getServicesPublicPath() {
		return URI.create(configuration.getIserveUrl() + MSM.SERVICE_INFIX);
	} 
	
	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#listDocument()
	 */
	@Override
	public List<URI> listDocuments() throws DocumentException {
		List<URI> result = new ArrayList<URI>();
		File wsdlFolder = new File(configuration.getDocumentsFolder());
		File[] folderList = wsdlFolder.listFiles();
		for ( int i = 0; i < folderList.length; i++ ) {
			if ( folderList[i].isDirectory() == true ) {
				String[] fileList = folderList[i].list();
				if ( fileList.length > 0 ) {
					// TODO: This assumes only one document per folder/service.
					URI docPath = this.getDocumentsPublicPath();
					result.add(docPath.resolve(folderList[i].getName() + "/" + fileList[0]));
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
		List<URI> result = new ArrayList<URI>();
		if (serviceURI == null) {
			return result;
		}
		
		URI folderUri = this.getDocumentInternalUri(serviceURI);
		File serviceFolder = new File(folderUri);
		if (serviceFolder.exists() && serviceFolder.isDirectory()) {
			File[] files = serviceFolder.listFiles();
			for (File file : files) {
				if (file.isFile()) {
					result.add(getDocumentPublicUri(file));
				}
			}
		}
		return null;
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
	 * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#addDocument(java.lang.String, java.io.InputStream, java.net.URI)
	 */
	@Override
	public URI addDocumentToService(String fileName, InputStream docContent,
			URI serviceUri) throws DocumentException {
				
		URI result = null;
		try {
			URI internalDocUri = this.getDocumentInternalUri(serviceUri, fileName);
			File file = new File(internalDocUri);
			FileUtil.createDirIfNotExists(file.getParentFile());	
			IOUtil.writeStream(docContent, file);
			result = this.getDocumentPublicUri(file);
		} catch (IOException e) {
			throw new DocumentException("Unable to add document to service.", e);
		} catch (URISyntaxException e) {
			throw new DocumentException("Unable to add document to service.", e);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#deleteDocument(java.net.URI)
	 */
	@Override
	public boolean deleteDocument(URI documentUri) throws DocumentException {
		// delete from hard disk
		URI fileUri = this.getDocumentInternalUri(documentUri);
		File file = new File(fileUri);
		if (file.exists()) {
			return file.delete();
		}
		return false;
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

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#deleteServiceDocuments(java.net.URI)
	 */
	@Override
	public boolean deleteServiceDocuments(URI serviceURI)
			throws DocumentException {
		
		if (serviceURI == null) {
			return false;
		}
		
		URI folderUri = this.getDocumentInternalUri(serviceURI);
		File serviceFolder = new File(folderUri);
		if (serviceFolder.exists() && serviceFolder.isDirectory()) {
			return FileUtil.deltree(serviceFolder);
		}
		return false;
	}

}
