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
		serviceManager = new ServiceManagerRdf(configuration);

		//		logManager = new LogManagerRdf(configuration);
		//		reviewManager = new ReviewManagerLuf(configuration);
		//		taxonomyManager = new TaxonomyManagerRdf(configuration);
		//		userManager = new UserManagerRdf(configuration);
		//		keyManager = new KeyManagerRdf(configuration);

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

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.iServeManager#listServices()
	 */
	@Override
	public List<java.net.URI> listServices() {
		return this.serviceManager.listServices();
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.iServeManager#importService(java.io.InputStream, uk.ac.open.kmi.iserve.sal.ServiceFormat)
	 */
	@Override
	public URI importService(InputStream serviceContent,
			ServiceFormat format) throws SalException {

		// Check first that we support the format
		if ( format.equals(ServiceFormat.UNSUPPORTED) || 
				!this.importerMap.containsKey(format)) {
			throw new ServiceException("Unable to import service. Format unsupported.");
		}
		// We have a suitable importer

		URI serviceUri = null;
		try {
			// 1st Store the document
			URI sourceDocUri = this.docManager.createDocument(serviceContent);

			// 2nd Transform the document
			ServiceImporter importer = importerMap.get(format);
			InputStream msmServiceDescriptionStream = importer.transformStream(serviceContent);

			// 3rd - Store the resulting MSM service
			serviceUri = this.serviceManager.addService(msmServiceDescriptionStream,
					sourceDocUri);

			// 4th Log it was all done correctly
			// TODO: log
		} finally {
			//TODO: Rollback
		}

		return serviceUri;
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


	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.iServeManager#registerService(java.net.URI, uk.ac.open.kmi.iserve.sal.ServiceFormat)
	 */
	@Override
	public URI registerService(URI sourceDocumentUri, ServiceFormat format) throws SalException {

		// Check first that we support the format
		if ( format.equals(ServiceFormat.UNSUPPORTED) || 
				!this.importerMap.containsKey(format)) {
			throw new ServiceException("Unable to import service. Format unsupported.");
		}
		// We have a suitable importer

		URI serviceUri = null;
		try {
			// 1st Obtain the document and transform it
			ServiceImporter importer = importerMap.get(format);
			InputStream is = sourceDocumentUri.toURL().openStream();
			InputStream msmServiceDescriptionStream = importer.transformStream(is);

			// 3rd - Store the resulting MSM service
			serviceUri = this.serviceManager.addService(msmServiceDescriptionStream,
					sourceDocumentUri);

			// 4th Log it was all done correctly
			// TODO: log
		} catch (MalformedURLException e) {
			log.error("Error obtaining the source document. Incorrect URL. " + sourceDocumentUri.toString());
			throw new ServiceException("Unable to register service. Incorrect source document URL.", e);
		} catch (IOException e) {
			log.error("Error obtaining the source document.");
			throw new ServiceException("Unable to register service. Unable to retrieve source document.", e);
		} finally {
			//TODO: Rollback
		}

		return serviceUri;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.iServeManager#unregisterService(java.net.URI)
	 */
	@Override
	public boolean unregisterService(URI serviceUri) throws SalException {

		// TODO: We should also delete the related documents from the server
		
		// Check the URI is correct and belongs to the server
		if (serviceUri == null || 
				!UriUtil.isResourceLocalToServer(serviceUri, configuration.getIserveUri()) ||
				!this.serviceManager.serviceExists(serviceUri)) {
			return false;
		}
		
		return this.serviceManager.deleteService(serviceUri);
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.iServeManager#getService(java.net.URI, uk.ac.open.kmi.iserve.sal.ServiceFormat)
	 */
	@Override
	public String getService(URI serviceUri, ServiceFormat format)
			throws ServiceException {
		//		return this.serviceManager.getServiceSerialisation(serviceUri, syntax);
		// TODO
		return null;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.iServeManager#addRelatedDocumentToService(java.net.URI, java.net.URI)
	 */
	@Override
	public boolean addRelatedDocumentToService(URI serviceUri,
			URI relatedDocument) throws SalException {
		// TODO Auto-generated method stub
		return false;
	}

	// Delegate Methods
	/*
	 *  Document Manager
	 */


	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.iServeManager#getDocument(java.net.URI)
	 */
	@Override
	public String getDocument(URI documentUri) throws DocumentException {
		return this.docManager.getDocument(documentUri);
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.iServeManager#createDocument(java.io.InputStream)
	 */
	@Override
	public URI createDocument(InputStream docContent) throws SalException {
		return this.docManager.createDocument(docContent);
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.iServeManager#listDocuments()
	 */
	@Override
	public List<URI> listDocuments() throws SalException {
		return this.docManager.listDocuments();
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.iServeManager#listDocumentsForService(java.net.URI)
	 */
	@Override
	public List<URI> listDocumentsForService(URI serviceURI) throws SalException {
		return this.docManager.listDocumentsForService(serviceURI);
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.iServeManager#deleteDocument(java.net.URI)
	 */
	@Override
	public boolean deleteDocument(URI documentUri) throws SalException {
		return this.docManager.deleteDocument(documentUri);
	}

}
