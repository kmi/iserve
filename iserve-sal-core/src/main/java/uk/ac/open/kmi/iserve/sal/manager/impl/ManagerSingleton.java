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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.iserve.commons.io.RDFRepositoryConnector;
import uk.ac.open.kmi.iserve.sal.ServiceFormat;
import uk.ac.open.kmi.iserve.sal.ServiceFormatDetector;
import uk.ac.open.kmi.iserve.sal.ServiceImporter;
import uk.ac.open.kmi.iserve.sal.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.exception.DocumentException;
import uk.ac.open.kmi.iserve.sal.exception.ImporterException;
import uk.ac.open.kmi.iserve.sal.exception.LogException;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
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
import uk.ac.open.kmi.iserve.sal.model.log.LogItem;
import uk.ac.open.kmi.iserve.sal.model.oauth.AccessToken;
import uk.ac.open.kmi.iserve.sal.model.oauth.Consumer;
import uk.ac.open.kmi.iserve.sal.model.oauth.RequestToken;
import uk.ac.open.kmi.iserve.sal.model.service.Service;
import uk.ac.open.kmi.iserve.sal.model.taxonomy.Category;
import uk.ac.open.kmi.iserve.sal.model.user.User;
import uk.ac.open.kmi.iserve.sal.util.UriUtil;

/**
 * Singleton Class providing a Facade to the entire Storage and Access Layer
 * Delegates the actual implementation to each of the specific managers configured
 * 
 * TODO: Can we homogenise the use of URI to java.net? At least at this level this 
 * looks like it is more appropriate.
 * 
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public class ManagerSingleton implements iServeManager {

	private static final Logger log = LoggerFactory.getLogger(iServeManager.class);

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

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.iServeManager#registerImporter(uk.ac.open.kmi.iserve.sal.ServiceFormat, uk.ac.open.kmi.iserve.sal.ServiceImporter)
	 */
	@Override
	public ServiceImporter registerImporter(ServiceFormat format,
			ServiceImporter importer) {
		return this.importerMap.put(format, importer);
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.iServeManager#unregisterImporter(uk.ac.open.kmi.iserve.sal.ServiceFormat)
	 */
	@Override
	public ServiceImporter unregisterImporter(ServiceFormat format) {
		return this.importerMap.remove(format);
	}
	
	/*
	 * Service Management
	 */

	/**
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#listServices()
	 */
	public List<java.net.URI> listServices() {
		return this.serviceManager.listServices();
	}

	/**
	 * @param serviceContent
	 * @param format
	 * @return
	 * @throws SalException
	 */
	public URI importService(InputStream serviceContent,
			ServiceFormat format) throws SalException {

		// Check first that we support the format
		if ( format.equals(ServiceFormat.UNSUPPORTED) || 
				!this.importerMap.containsKey(format)) {
			throw new ServiceException("Unable to import service. Format unsupported.");
		}

		// 1st generate a unique URI and validate it does not already exist
		URI serviceUri = serviceManager.generateServiceUri();
		if (serviceManager.serviceExists(serviceUri)) {
			log.error("Error while generating unique service Id");
			throw new ServiceException("Unable to store service. Problems generating unique Id.");
		}

		// 2nd - Store the definition file within the server
		String fileName = "service." + format.getFileExtension();
		URI documentUri = this.docManager.addDocumentToService(fileName, serviceContent, serviceUri);

		// 3rd - Transform the service
		URI result = null;
		ServiceImporter importer = importerMap.get(format);
		try {
			InputStream msmServiceDescriptionStream = importer.transformStream(serviceContent);

			// 3rd - Store the resulting MSM service
			result = this.serviceManager.addService(serviceUri, msmServiceDescriptionStream,
					documentUri);	

		} catch (ImporterException e) {
			throw new ServiceException(e);
		}

		// 4th - log that the service has been added
		// TODO: log 

		return result;
	}

	//	/**
	//	 * @param serviceContent
	//	 * @return
	//	 * @throws ServiceException
	//	 */
	//	private ServiceFormat guessContentType(InputStream serviceContent)
	//			throws ServiceException {
	//		// detect type
	//		ServiceFormat format = ServiceFormat.UNSUPPORTED;
	//		try {
	//			format = formatDetector.detect(serviceContent);
	//		} catch (IOException e1) {
	//			throw new ServiceException(e1);
	//		}
	//
	//		// find corresponding importer
	//		if ( format.equals(ServiceFormat.UNSUPPORTED) ) {
	//			throw new ServiceException("The service is described in an unsupported format");
	//		}
	//		if ( fileName == null ) {
	//			// determine file name
	//			if ( format.equals(ServiceFormat.HTML) ) {
	//				fileName = "service.html";
	//			} else if ( format.equals(ServiceFormat.OWLS) ) {
	//				fileName = "service.owls";
	//			} else if ( format.equals(ServiceFormat.WSDL) ) {
	//				fileName = "service.wsdl";
	//			} else if ( format.equals(ServiceFormat.RDFXML) ) {
	//				fileName = "service.rdf.xml";
	//			}
	//		}
	//		return format;
	//	}


	/**
	 * @param sourceDocumentUri
	 * @param format
	 * @return
	 * @throws SalException
	 */
	public URI registerService(URI sourceDocumentUri, ServiceFormat format) throws SalException {

		// Check first that we support the format
		if ( format.equals(ServiceFormat.UNSUPPORTED) || 
				!this.importerMap.containsKey(format)) {
			throw new ServiceException("Unable to import service. Format unsupported.");
		}

		// 1st generate a unique Id
		URI serviceUri = serviceManager.generateServiceUri();
		if (serviceManager.serviceExists(serviceUri)) {
			log.error("Error while generating unique service Id");
			throw new ServiceException("Unable to store service. Problems generating unique Id.");
		}

		// We don't need to store the service description as it is available somewhere else
		// 2rd - Transform the service
		ServiceImporter importer = importerMap.get(format);
		InputStream is;
		try {
			is = sourceDocumentUri.toURL().openStream();
			InputStream msmServiceDescriptionStream = importer.transformStream(is);

			// 3rd - Store the resulting MSM service with a link to the external file
			serviceUri = this.serviceManager.addService(serviceUri, msmServiceDescriptionStream,
					sourceDocumentUri);
		} catch (MalformedURLException e) {
			log.error("Error obtaining the source document. Incorrect URL. " + sourceDocumentUri.toString());
			throw new ServiceException("Unable to register service. Incorrect source document URL.", e);
		} catch (IOException e) {
			log.error("Error obtaining the source document.");
			throw new ServiceException("Unable to register service. Unable to retrieve source document.", e);
		}
	
		// 4th - log that the service has been added

		return serviceUri;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.iServeManager#unregisterService(java.net.URI)
	 */
	@Override
	public boolean unregisterService(URI serviceUri) throws SalException {
		
		// Check the URI is correct and belongs to the server
		try {
			if (serviceUri == null || 
					!UriUtil.isResourceLocalToServer(serviceUri, configuration.getIserveUrl().toURI()) ||
					!this.serviceManager.serviceExists(serviceUri)) {
				return false;
			}
		} catch (URISyntaxException e) {
			log.error("Could not unregister service. The URI given is incorrect.", e);
			throw new SalException("Incorrect URI when unregistering service.", e);
		}
		
		return this.serviceManager.deleteService(serviceUri);
	}

	/**
	 * @param serviceUri
	 * @param syntax
	 * @return
	 * @throws ServiceException
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#getServiceSerialisation(java.net.URI, org.ontoware.rdf2go.model.Syntax)
	 */
	public String getServiceSerialisation(URI serviceUri, Syntax syntax)
			throws ServiceException {
		return this.serviceManager.getServiceSerialisation(serviceUri, syntax);
	}

	/**
	 * @param serviceUri
	 * @return
	 * @throws ServiceException
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#getServiceAsModel(java.net.URI)
	 */
	public Model getServiceAsModel(URI serviceUri) throws ServiceException {
		return this.serviceManager.getServiceAsModel(serviceUri);
	}

	/**
	 * @param serviceUri
	 * @return
	 * @throws ServiceException
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#getService(java.net.URI)
	 */
	public Service getService(URI serviceUri) throws ServiceException {
		return this.serviceManager.getService(serviceUri);
	}
	
	
	// Delegate Methods
	/*
	 *  Document Manager
	 */
	/**
	 * @param documentUri
	 * @return
	 * @throws DocumentException
	 * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#getDocument(java.net.URI)
	 */
	public String getDocument(URI documentUri) throws DocumentException {
		return this.docManager.getDocument(documentUri);
	}

	/**
	 * @param fileName
	 * @param docContent
	 * @param serviceUri
	 * @return
	 * @throws SalException
	 * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#addDocumentToService(java.lang.String, java.io.InputStream, java.net.URI)
	 */
	public URI addDocumentToService(String fileName, InputStream docContent,
			URI serviceUri) throws SalException {
		return this.docManager.addDocumentToService(fileName, docContent, serviceUri);
	}

	/**
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#listDocuments()
	 */
	public List<URI> listDocuments() throws SalException {
		return this.docManager.listDocuments();
	}

	/**
	 * @param serviceURI
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#listDocumentsForService(java.net.URI)
	 */
	public List<URI> listDocumentsForService(URI serviceURI) throws SalException {
		return this.docManager.listDocumentsForService(serviceURI);
	}

	/**
	 * @param documentUri
	 * @return
	 * @throws DocumentException
	 * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#deleteDocument(java.net.URI)
	 */
	public boolean deleteDocument(URI documentUri) throws SalException {
		return this.docManager.deleteDocument(documentUri);
	}


	/*
	 *  Log Manager
	 */

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
	 * @param userUri
	 * @return
	 * @throws DatatypeConfigurationException
	 * @see uk.ac.open.kmi.iserve.sal.manager.LogManager#getAllLogItemsForUser(java.net.URI)
	 */
	public Map<String, LogItem> getAllLogItemsForUser(URI userUri)
			throws DatatypeConfigurationException {
		return this.logManager.getAllLogItemsForUser(userUri);
	}

	/**
	 * @param resourceUri
	 * @return
	 * @throws DatatypeConfigurationException
	 * @see uk.ac.open.kmi.iserve.sal.manager.LogManager#getAllLogItemsAboutResource(java.net.URI)
	 */
	public Map<String, LogItem> getAllLogItemsAboutResource(URI resourceUri)
			throws DatatypeConfigurationException {
		return this.logManager.getAllLogItemsAboutResource(resourceUri);
	}


	/*
	 * Taxonomy Manager
	 */

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

	/*
	 * Users Manager
	 */

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
	public boolean removeUser(URI foafId) throws UserException {
		return this.userManager.removeUser(foafId);
	}

	/**
	 * @param userName
	 * @throws UserException
	 * @see uk.ac.open.kmi.iserve.sal.manager.UserManager#removeUser(java.lang.String)
	 */
	public boolean removeUser(String userName) throws UserException {
		return this.userManager.removeUser(userName);
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

	/*
	 * Key Manager
	 */

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


	/*
	 * Review Manager
	 */
	
	/**
	 * @param agentUri
	 * @param serviceUri
	 * @param rating
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#addRating(java.net.URI, java.net.URI, java.lang.String)
	 */
	public boolean addRating(java.net.URI agentUri, java.net.URI serviceUri, String rating) {
		return this.reviewManager.addRating(agentUri, serviceUri, rating);
	}

	/**
	 * @param agentUri
	 * @param serviceUri
	 * @param comment
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#addComment(java.net.URI, java.net.URI, java.lang.String)
	 */
	public boolean addComment(java.net.URI agentUri, java.net.URI serviceUri, String comment) {
		return this.reviewManager.addComment(agentUri, serviceUri, comment);
	}

	/**
	 * @param agentUri
	 * @param serviceUri
	 * @param tag
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#addTag(java.net.URI, java.net.URI, java.lang.String)
	 */
	public boolean addTag(java.net.URI agentUri, java.net.URI serviceUri, String tag) {
		return this.reviewManager.addTag(agentUri, serviceUri, tag);
	}

	/**
	 * @param agentUri
	 * @param resourceUri
	 * @param tags
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#addTags(java.net.URI, java.net.URI, java.util.List)
	 */
	public boolean addTags(URI agentUri, URI resourceUri, List<String> tags) {
		return this.reviewManager.addTags(agentUri, resourceUri, tags);
	}

	/**
	 * @param serviceUri
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#getRatings(java.net.URI)
	 */
	public List<String> getRatings(java.net.URI serviceUri) {
		return this.reviewManager.getRatings(serviceUri);
	}

	/**
	 * @param serviceUri
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#getComments(java.net.URI)
	 */
	public List<String> getComments(java.net.URI serviceUri) {
		return this.reviewManager.getComments(serviceUri);
	}

	/**
	 * @param serviceUri
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#getTags(java.net.URI)
	 */
	public List<String> getTags(java.net.URI serviceUri) {
		return this.reviewManager.getTags(serviceUri);
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
		return this.reviewManager.updateRating(agentUri, serviceUri, rating);
	}

	/**
	 * @param agentUri
	 * @param serviceUri
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#deleteRating(java.net.URI, java.net.URI)
	 */
	public boolean deleteRating(java.net.URI agentUri, java.net.URI serviceUri) {
		return ManagerSingleton._instance.deleteRating(agentUri, serviceUri);
	}

	/**
	 * @param agentUri
	 * @param serviceUri
	 * @return
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#deleteComment(java.net.URI, java.net.URI)
	 */
	public boolean deleteComment(java.net.URI agentUri, java.net.URI serviceUri) {
		return this.reviewManager.deleteComment(agentUri, serviceUri);
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
		return this.reviewManager.deleteTag(agentUri, serviceUri, tag);
	}

}
