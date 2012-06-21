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
package uk.ac.open.kmi.iserve.sal.manager.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.commons.configuration.ConfigurationException;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Syntax;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.iserve.commons.io.RDFRepositoryConnector;
import uk.ac.open.kmi.iserve.sal.ServiceFormat;
import uk.ac.open.kmi.iserve.sal.ServiceFormatDetector;
import uk.ac.open.kmi.iserve.sal.ServiceImporter;
import uk.ac.open.kmi.iserve.sal.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.exception.DocumentException;
import uk.ac.open.kmi.iserve.sal.exception.ImporterException;
import uk.ac.open.kmi.iserve.sal.exception.LogException;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.exception.TaxonomyException;
import uk.ac.open.kmi.iserve.sal.exception.UserException;
import uk.ac.open.kmi.iserve.sal.manager.DocumentManager;
import uk.ac.open.kmi.iserve.sal.manager.KeyManager;
import uk.ac.open.kmi.iserve.sal.manager.LogManager;
import uk.ac.open.kmi.iserve.sal.manager.ReviewManager;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;
import uk.ac.open.kmi.iserve.sal.manager.TaxonomyManager;
import uk.ac.open.kmi.iserve.sal.manager.UserManager;
import uk.ac.open.kmi.iserve.sal.manager.iServeManager;
import uk.ac.open.kmi.iserve.sal.model.common.URI;
import uk.ac.open.kmi.iserve.sal.model.log.LogItem;
import uk.ac.open.kmi.iserve.sal.model.oauth.AccessToken;
import uk.ac.open.kmi.iserve.sal.model.oauth.Consumer;
import uk.ac.open.kmi.iserve.sal.model.oauth.RequestToken;
import uk.ac.open.kmi.iserve.sal.model.service.Service;
import uk.ac.open.kmi.iserve.sal.model.taxonomy.Category;
import uk.ac.open.kmi.iserve.sal.model.user.User;

/**
 * Singleton Class providing a Facade to the entire Storage and Access Layer
 * Delegates the actual implementation to each of the specific managers configured
 * 
 * TODO: Can we homogenise the use of URI to java.net? At least at this level this 
 * looks like it is more appropriate.
 * 
 * TODO: Most URIs are here passed as simple Strings!
 * 
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public class ManagerSingleton implements iServeManager {
	
	private static final String CONFIG_PROPERTIES_FILENAME = "config.properties";
	
	private SystemConfiguration configuration;
	private DocumentManager docManager;
	private LogManager logManager;
	private ReviewManager reviewManager;
	private ServiceManager serviceManager;
	private TaxonomyManager taxonomyManager;
	private UserManager userManager;
	private KeyManager keyManager;
	private ServiceFormatDetector formatDetector;
	private Map<ServiceFormat, ServiceImporter> importerMap;
	
	private static ManagerSingleton _instance;
	
	private ManagerSingleton() throws RepositoryException, ConfigurationException {
		
		configuration = new SystemConfiguration(CONFIG_PROPERTIES_FILENAME);
		docManager = new DocumentManagerFileSystem(configuration);
		logManager = new LogManagerRdf(configuration);
		reviewManager = new ReviewManagerLuf(configuration);
		serviceManager = new ServiceManagerRdf(configuration);
		taxonomyManager = new TaxonomyManagerRdf(configuration);
		userManager = new UserManagerRdf(configuration);
		keyManager = new KeyManagerRdf(configuration);
		
		formatDetector = new ServiceFormatDetector();
		setProxy(configuration.getProxyHostName(), configuration.getProxyPort());
		
		// TODO: Automatically locate and index the existing service importers
		importerMap = new HashMap<ServiceFormat, ServiceImporter>();
	}
	
	private void setProxy(String proxyHost, String proxyPort) {
		if ( proxyHost != null && proxyPort != null ) {
			Properties prop = System.getProperties();
			prop.put("http.proxyHost", proxyHost);
			prop.put("http.proxyPort", proxyPort);
		}
	}

	public static ManagerSingleton getInstance() {
		if (_instance == null) {
			try {
				_instance = new ManagerSingleton();
			} catch (RepositoryException e) {
				throw(new IllegalStateException("iServe's storage and access layer is not properly configured", e));
			} catch (ConfigurationException e) {
				throw(new IllegalStateException("iServe's storage and access layer is not properly configured", e));
			}
		}
		return _instance;
	}
	
	/**
	 * @return the configuration
	 */
	public SystemConfiguration getConfiguration() {
		return this.configuration;
	}

	/**
	 * Registers the given importer for that format so that it is used when saving
	 * a service 
	 * 
	 * @param format the format handled by the importer
	 * @param importer the previous value associated with key, or null if there 
	 * was no mapping for key. (A null return can also indicate that the map 
	 * previously associated null with key, if the implementation supports null 
	 * values.)
	 * @return 
	 */
	public ServiceImporter registerImporter(ServiceFormat format, ServiceImporter importer) {
		return this.importerMap.put(format, importer);
	}
	
	/**
	 * Unregisters the importer for the given format
	 * @param format the format to remove the importer for
	 * @return the previous value associated with key, or null if there was 
	 * no mapping for key.
	 */
	public ServiceImporter unregisterImporter(ServiceFormat format) {
		return this.importerMap.remove(format);
	}
	
	// Delegate Methods
	
	/**
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#listDocument()
	 */
	public List<String> listDocument() {
		return this.docManager.listDocument();
	}

	/**
	 * @param fileName
	 * @param serviceDescription
	 * @return
	 * @throws DocumentException
	 * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#addDocument(java.lang.String, java.lang.String)
	 */
	public String addDocument(String fileName, String serviceDescription)
			throws DocumentException {
		return this.docManager.addDocument(fileName, serviceDescription);
	}

	/**
	 * @param documentUri
	 * @return
	 * @throws DocumentException
	 * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#deleteDocument(java.lang.String)
	 */
	public String deleteDocument(String documentUri) throws DocumentException {
		return this.docManager.deleteDocument(documentUri);
	}

	/**
	 * @param documentId
	 * @return
	 * @throws DocumentException
	 * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#deleteDocumentById(java.lang.String)
	 */
	public String deleteDocumentById(String documentId)
			throws DocumentException {
		return this.docManager.deleteDocumentById(documentId);
	}

	/**
	 * @param documentUri
	 * @return
	 * @throws DocumentException
	 * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#getDocument(java.lang.String)
	 */
	public String getDocument(String documentUri) throws DocumentException {
		return this.docManager.getDocument(documentUri);
	}

	/**
	 * @param documentId
	 * @return
	 * @throws DocumentException
	 * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#getDocumentById(java.lang.String)
	 */
	public String getDocumentById(String documentId) throws DocumentException {
		return this.docManager.getDocumentById(documentId);
	}

	/**
	 * TODO: We should abstract the higher levels from the logging of Actions
	 * 
	 * @param agentUri
	 * @param actionUri
	 * @param object
	 * @param time
	 * @param method
	 * @throws LogException
	 * @see uk.ac.open.kmi.iserve.sal.manager.LogManager#log(java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.lang.String)
	 */
	public void log(String agentUri, String actionUri, String object,
			Date time, String method) throws LogException {
		this.logManager.log(agentUri, actionUri, object, time, method);
	}

	/**
	 * @return
	 * @throws DatatypeConfigurationException
	 * @see uk.ac.open.kmi.iserve.sal.manager.LogManager#getAllLogItems()
	 */
	public Map<String, LogItem> getAllLogItems()
			throws DatatypeConfigurationException {
		return this.logManager.getAllLogItems();
	}

	/**
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#listService()
	 */
	public List<String> listService() {
		return this.serviceManager.listService();
	}

	/**
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#getServicesRepositoryConnector()
	 */
	public RDFRepositoryConnector getServicesRepositoryConnector() {
		return this.serviceManager.getServicesRepositoryConnector();
	}

	/**
	 * @param fileName
	 * @param msmServiceDescriptionStream
	 * @param sourceUri
	 * @return
	 * @throws ServiceException
	 */
	public String addService(String fileName,
			InputStream msmServiceDescriptionStream, String sourceUri)
			throws ServiceException {
		//TODO: Fix the interface in Service Manager instead
		return addService(fileName, msmServiceDescriptionStream.toString(), sourceUri);
	}
	
	/**
	 * @param fileName
	 * @param serviceDescription
	 * @param sourceUri
	 * @return
	 * @throws ServiceException
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#addService(java.lang.String, java.lang.String, java.lang.String)
	 */
	public String addService(String fileName, String serviceDescription,
			String sourceUri) throws ServiceException {
		
		// detect type
		ServiceFormat format = ServiceFormat.UNSUPPORTED;
		try {
			format = formatDetector.detect(serviceDescription);
		} catch (IOException e1) {
			throw new ServiceException(e1);
		}

		// find corresponding importer
		if ( format.equals(ServiceFormat.UNSUPPORTED) ) {
			throw new ServiceException("The service is described in an unsupported format");
		}
		if ( fileName == null ) {
			// determine file name
			if ( format.equals(ServiceFormat.HTML) ) {
				fileName = "service.html";
			} else if ( format.equals(ServiceFormat.OWLS) ) {
				fileName = "service.owls";
			} else if ( format.equals(ServiceFormat.WSDL) ) {
				fileName = "service.wsdl";
			} else if ( format.equals(ServiceFormat.RDFXML) ) {
				fileName = "service.rdf.xml";
			}
		}
		// Transform the service prior to storing it
		ServiceImporter importer = importerMap.get(format);
		String serviceUri = null;
		try {
			InputStream msmServiceDescriptionStream = importer.transformStream(serviceDescription);
			serviceUri = this.serviceManager.addService(fileName, msmServiceDescriptionStream,
					sourceUri);
		} catch (ImporterException e) {
			throw new ServiceException(e);
		}
		
		return serviceUri;
	}

	/**
	 * @param serviceUri
	 * @return
	 * @throws ServiceException
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#deleteService(java.lang.String)
	 */
	public String deleteService(String serviceUri) throws ServiceException {
		return this.serviceManager.deleteService(serviceUri);
	}

	/**
	 * @param serviceId
	 * @return
	 * @throws ServiceException
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#deleteServiceById(java.lang.String)
	 */
	public String deleteServiceById(String serviceId) throws ServiceException {
		return this.serviceManager.deleteServiceById(serviceId);
	}

	/**
	 * @param serviceUri
	 * @param syntax
	 * @return
	 * @throws ServiceException
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#getService(java.lang.String, org.ontoware.rdf2go.model.Syntax)
	 */
	public String getService(String serviceUri, Syntax syntax)
			throws ServiceException {
		return this.serviceManager.getService(serviceUri, syntax);
	}

	/**
	 * @param serviceUri
	 * @return
	 * @throws ServiceException
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#getServiceAsModel(java.lang.String)
	 */
	public Model getServiceAsModel(String serviceUri)
			throws ServiceException {
		return this.serviceManager.getServiceAsModel(serviceUri);
	}

	/**
	 * @param serviceId
	 * @return
	 * @throws ServiceException
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#getServiceAsModelById(java.lang.String)
	 */
	public Model getServiceAsModelById(String serviceId)
			throws ServiceException {
		return this.serviceManager.getServiceAsModelById(serviceId);
	}

	/**
	 * @param serviceUri
	 * @return
	 * @throws ServiceException
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#getService(uk.ac.open.kmi.iserve.sal.model.common.URI)
	 */
	public Service getService(URI serviceUri) throws ServiceException {
		return this.serviceManager.getService(serviceUri);
	}

	/**
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.TaxonomyManager#listTaxonomy()
	 */
	public List<String> listTaxonomy() {
		return this.taxonomyManager.listTaxonomy();
	}

	/**
	 * @param taxonomyUri
	 * @return
	 * @throws TaxonomyException
	 * @see uk.ac.open.kmi.iserve.sal.manager.TaxonomyManager#addTaxonomy(java.lang.String)
	 */
	public String addTaxonomy(String taxonomyUri) throws TaxonomyException {
		return this.taxonomyManager.addTaxonomy(taxonomyUri);
	}

	/**
	 * @param taxonomyUri
	 * @return
	 * @throws TaxonomyException
	 * @see uk.ac.open.kmi.iserve.sal.manager.TaxonomyManager#loadTaxonomy(java.lang.String)
	 */
	public List<Category> loadTaxonomy(String taxonomyUri)
			throws TaxonomyException {
		return this.taxonomyManager.loadTaxonomy(taxonomyUri);
	}

	/**
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.UserManager#getUsersRepositoryConnector()
	 */
	public RDFRepositoryConnector getUsersRepositoryConnector() {
		return this.userManager.getUsersRepositoryConnector();
	}

	/**
	 * @param openId
	 * @return
	 * @throws UserException
	 * @see uk.ac.open.kmi.iserve.sal.manager.UserManager#getUser(uk.ac.open.kmi.iserve.sal.model.common.URI)
	 */
	public User getUser(URI openId) throws UserException {
		return this.userManager.getUser(openId);
	}

	/**
	 * @param userName
	 * @return
	 * @throws UserException
	 * @see uk.ac.open.kmi.iserve.sal.manager.UserManager#getUser(java.lang.String)
	 */
	public User getUser(String userName) throws UserException {
		return this.userManager.getUser(userName);
	}

	/**
	 * @param foafId
	 * @param openId
	 * @param userName
	 * @param password
	 * @return
	 * @throws UserException
	 * @see uk.ac.open.kmi.iserve.sal.manager.UserManager#addUser(uk.ac.open.kmi.iserve.sal.model.common.URI, uk.ac.open.kmi.iserve.sal.model.common.URI, java.lang.String, java.lang.String)
	 */
	public URI addUser(URI foafId, URI openId, String userName, String password)
			throws UserException {
		return this.userManager.addUser(foafId, openId, userName, password);
	}

	/**
	 * @param user
	 * @return
	 * @throws UserException
	 * @see uk.ac.open.kmi.iserve.sal.manager.UserManager#addUser(uk.ac.open.kmi.iserve.sal.model.user.User)
	 */
	public URI addUser(User user) throws UserException {
		return this.userManager.addUser(user);
	}

	/**
	 * @param foafId
	 * @throws UserException
	 * @see uk.ac.open.kmi.iserve.sal.manager.UserManager#removeUser(uk.ac.open.kmi.iserve.sal.model.common.URI)
	 */
	public void removeUser(URI foafId) throws UserException {
		this.userManager.removeUser(foafId);
	}

	/**
	 * @param userName
	 * @throws UserException
	 * @see uk.ac.open.kmi.iserve.sal.manager.UserManager#removeUser(java.lang.String)
	 */
	public void removeUser(String userName) throws UserException {
		this.userManager.removeUser(userName);
	}

	/**
	 * @param user
	 * @return
	 * @throws UserException
	 * @see uk.ac.open.kmi.iserve.sal.manager.UserManager#updateUser(uk.ac.open.kmi.iserve.sal.model.user.User)
	 */
	public URI updateUser(User user) throws UserException {
		return this.userManager.updateUser(user);
	}

	/**
	 * @param consumerUriString
	 * @param consumerKey
	 * @param consumerSecret
	 * @see uk.ac.open.kmi.iserve.sal.manager.KeyManager#saveConsumer(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void saveConsumer(String consumerUriString, String consumerKey,
			String consumerSecret) {
		this.keyManager.saveConsumer(consumerUriString, consumerKey,
				consumerSecret);
	}

	/**
	 * @param consumerKey
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.KeyManager#findConsumer(java.lang.String)
	 */
	public Consumer findConsumer(String consumerKey) {
		return this.keyManager.findConsumer(consumerKey);
	}

	/**
	 * @param requestTokenKey
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.KeyManager#findRequestToken(java.lang.String)
	 */
	public RequestToken findRequestToken(String requestTokenKey) {
		return this.keyManager.findRequestToken(requestTokenKey);
	}

	/**
	 * @param accessTokenKey
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.KeyManager#findAccessToken(java.lang.String)
	 */
	public AccessToken findAccessToken(String accessTokenKey) {
		return this.keyManager.findAccessToken(accessTokenKey);
	}

	/**
	 * @param requestTokenKey
	 * @param openid
	 * @param callback
	 * @see uk.ac.open.kmi.iserve.sal.manager.KeyManager#saveAuthorization(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void saveAuthorization(String requestTokenKey, String openid,
			String callback) {
		this.keyManager.saveAuthorization(requestTokenKey, openid, callback);
	}

	/**
	 * @param tokenKey
	 * @param tokenSecret
	 * @param consumerKey
	 * @see uk.ac.open.kmi.iserve.sal.manager.KeyManager#saveRequestToken(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void saveRequestToken(String tokenKey, String tokenSecret,
			String consumerKey) {
		this.keyManager.saveRequestToken(tokenKey, tokenSecret, consumerKey);
	}

	/**
	 * @param tokenKey
	 * @see uk.ac.open.kmi.iserve.sal.manager.KeyManager#deleteRequestToken(java.lang.String)
	 */
	public void deleteRequestToken(String tokenKey) {
		this.keyManager.deleteRequestToken(tokenKey);
	}

	/**
	 * @param tokenKey
	 * @param tokenSecret
	 * @param requestToken
	 * @param accessAs
	 * @see uk.ac.open.kmi.iserve.sal.manager.KeyManager#saveAccessToken(java.lang.String, java.lang.String, uk.ac.open.kmi.iserve.sal.model.oauth.RequestToken, java.lang.String)
	 */
	public void saveAccessToken(String tokenKey, String tokenSecret,
			RequestToken requestToken, String accessAs) {
		this.keyManager.saveAccessToken(tokenKey, tokenSecret, requestToken,
				accessAs);
	}

	/**
	 * @param tokenKey
	 * @see uk.ac.open.kmi.iserve.sal.manager.KeyManager#deleteAccessToken(java.lang.String)
	 */
	public void deleteAccessToken(String tokenKey) {
		this.keyManager.deleteAccessToken(tokenKey);
	}

	/**
	 * @param agentUri
	 * @param serviceUri
	 * @param rating
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#addRating(java.net.URI, java.net.URI, java.lang.String)
	 */
	public boolean addRating(java.net.URI agentUri, java.net.URI serviceUri, String rating) {
		return this._instance.addRating(agentUri, serviceUri, rating);
	}

	/**
	 * @param agentUri
	 * @param serviceUri
	 * @param comment
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#addComment(java.net.URI, java.net.URI, java.lang.String)
	 */
	public boolean addComment(java.net.URI agentUri, java.net.URI serviceUri, String comment) {
		return this._instance.addComment(agentUri, serviceUri, comment);
	}

	/**
	 * @param agentUri
	 * @param serviceUri
	 * @param tag
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#addTag(java.net.URI, java.net.URI, java.lang.String)
	 */
	public boolean addTag(java.net.URI agentUri, java.net.URI serviceUri, String tag) {
		return this._instance.addTag(agentUri, serviceUri, tag);
	}

	/**
	 * @param serviceUri
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#getRatings(java.net.URI)
	 */
	public List<String> getRatings(java.net.URI serviceUri) {
		return this._instance.getRatings(serviceUri);
	}

	/**
	 * @param serviceUri
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#getComments(java.net.URI)
	 */
	public List<String> getComments(java.net.URI serviceUri) {
		return this._instance.getComments(serviceUri);
	}

	/**
	 * @param serviceUri
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#getTags(java.net.URI)
	 */
	public List<String> getTags(java.net.URI serviceUri) {
		return this._instance.getTags(serviceUri);
	}

	/**
	 * @param agentUri
	 * @param serviceUri
	 * @param rating
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#updateRating(java.net.URI, java.net.URI, java.lang.String)
	 */
	public boolean updateRating(java.net.URI agentUri, java.net.URI serviceUri,
			String rating) {
		return this._instance.updateRating(agentUri, serviceUri, rating);
	}

	/**
	 * @param agentUri
	 * @param serviceUri
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#deleteRating(java.net.URI, java.net.URI)
	 */
	public boolean deleteRating(java.net.URI agentUri, java.net.URI serviceUri) {
		return this._instance.deleteRating(agentUri, serviceUri);
	}

	/**
	 * @param agentUri
	 * @param serviceUri
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#deleteComment(java.net.URI, java.net.URI)
	 */
	public boolean deleteComment(java.net.URI agentUri, java.net.URI serviceUri) {
		return this._instance.deleteComment(agentUri, serviceUri);
	}

	/**
	 * @param agentUri
	 * @param serviceUri
	 * @param tag
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#deleteTag(java.net.URI, java.net.URI, java.lang.String)
	 */
	public boolean deleteTag(java.net.URI agentUri, java.net.URI serviceUri,
			String tag) {
		return this._instance.deleteTag(agentUri, serviceUri, tag);
	}
	
}
