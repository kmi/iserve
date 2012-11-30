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

import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Syntax;

import uk.ac.open.kmi.iserve.sal.ServiceFormat;
import uk.ac.open.kmi.iserve.sal.ServiceImporter;
import uk.ac.open.kmi.iserve.sal.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.model.service.Service;
import uk.ac.open.kmi.iserve.sal.model.user.User;

/**
 * General Purpose Manager for iServe. 
 * 
 * TODO: Registering import mechanisms should also be defined somewhere
 * 
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public interface iServeManager extends LogManager, KeyManager,
		ReviewManager, TaxonomyManager, UserManager {

	public abstract SystemConfiguration getConfiguration();
	
	/**
	 * Register a new importer for a given file format
	 * 
	 * @param format
	 * @param importer
	 * @return
	 */
	public abstract ServiceImporter registerImporter(ServiceFormat format, 
			ServiceImporter importer);
	
	/**
	 * Unregister a new importer for a given file format
	 * 
	 * @param format
	 * @return
	 */
	public abstract ServiceImporter unregisterImporter(ServiceFormat format);
	
	// Service Management Operations
	
	/**
	 * Register a new service. This operation takes care of performing the 
	 * appropriate format transformation. The source document is not stored on 
	 * the server. For keeping a copy of the source document within the server 
	 * use @see importService instead.
	 * 
	 * @param sourceDocumentUri
	 * @param contentType
	 * @return
	 */
	public abstract URI registerService(URI sourceDocumentUri, 
			String contentType);
	
	/**
	 * Imports a new service within iServe. The original document is stored 
	 * in the server and the transformed version registered within iServe.
	 * 
	 * @param serviceContent
	 * @param contentType
	 * @return
	 */
	public abstract URI importService(InputStream serviceContent, 
			String contentType);
	
	/**
	 * Unregisters the service from iServe. Any related documents are also 
	 * deleted in the process.
	 * 
	 * @param serviceUri
	 * @return
	 */
	public abstract boolean unregisterService(URI serviceUri);

	public abstract String getService(URI serviceUri, Syntax syntax);

	public abstract Model getServiceAsModel(URI serviceUri);

	public abstract Service getService(URI serviceUri);
	
	public abstract List<URI> listServices();
	

	// Document Management Operations
	public abstract List<URI> listDocuments();
	
	public abstract List<URI> listDocumentsForService(URI serviceUri);

	public abstract URI addDocumentToService(String fileName, 
			InputStream docContent, URI serviceUri);

	public abstract URI deleteDocument(URI documentUri);

	public abstract String getDocument(URI documentUri);
	
	// Log Management Operations 
	// TODO: Check these. 
//	public abstract Map<String, LogItem> getAllLogItems()
//			throws DatatypeConfigurationException;
//	
//	public abstract Map<String, LogItem> getAllLogItemsForUser(URI userUri)
//			throws DatatypeConfigurationException;
//	
//	public abstract Map<String, LogItem> getAllLogItemsAboutResource(URI resourceUri)
//			throws DatatypeConfigurationException;

	
	// Review Manamgenet Methods
	// TODO: Check these. Extend or redefine?
//	// Create
//	public abstract boolean addRating(URI agentUri, URI resourceUri, String rating);
//
//	public abstract boolean addComment(URI agentUri, URI resourceUri, String comment);
//
//	public abstract boolean addTag(URI agentUri, URI resourceUri, String tag);
//	
//	public abstract boolean addTags(URI agentUri, URI resourceUri, List<String> tags);
//	
//	// Read
//	public abstract List<String> getRatings(URI resourceUri);
//	
//	public abstract List<String> getComments(URI resourceUri);
//	
//	public abstract List<String> getTags(URI resourceUri);
//	
//	//Update (only seems to make sense for rating)
//	public abstract boolean updateRating(URI agentUri, URI resourceUri, String rating);
//	
//	// Delete
//	public abstract boolean deleteRating(URI agentUri, URI resourceUri);
//	
//	public abstract boolean deleteComment(URI agentUri, URI resourceUri);
//	
//	public abstract boolean deleteTag(URI agentUri, URI resourceUri, String tag);
	
	// User Managemet Methods
	// TODO: Check these.
//	public abstract User getUser(URI openId);
//
//	public abstract User getUser(String userName);
//
//	public abstract URI addUser(URI foafId, URI openId, String userName,
//			String password);
//
//	public abstract URI addUser(User user);
//
//	public abstract void removeUser(URI foafId);
//
//	public abstract void removeUser(String userName);
//
//	public abstract URI updateUser(User user);
	
	
}
