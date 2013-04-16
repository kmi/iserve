/*
   Copyright 2012  Knowledge Media Institute - The Open University

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
package uk.ac.open.kmi.iserve.sal.manager;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import uk.ac.open.kmi.iserve.sal.exception.DocumentException;

/**
 * Class Description
 * 
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public interface DocumentManager {

	// Create Methods
	public abstract URI addDocument(String fileName,
			String documentContent, String serviceId) throws DocumentException;
	
	public abstract URI addDocument(String fileName,
			InputStream docContent, URI serviceUri) throws DocumentException;
	
	// Read Methods

	public abstract String getDocument(URI documentUri)
			throws DocumentException;
	
	// Delete Methods
	public abstract URI deleteDocument(URI documentUri)
			throws DocumentException;

	public abstract URI deleteDocumentById(String documentId)
			throws DocumentException;

	
	// General Management Methods
	
	public abstract List<URI> listDocuments();
	
	public abstract List<URI> listDocumentsForService(String serviceId);
	
	public abstract List<URI> listDocumentsForService(URI serviceURI);
	
	public abstract boolean documentExists(String serviceId, String fileName)
			throws DocumentException;

	public abstract boolean documentExists(URI documentUri)
			throws DocumentException;
	
}