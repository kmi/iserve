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

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.ServiceReader;
import uk.ac.open.kmi.iserve.commons.io.ServiceReaderImpl;
import uk.ac.open.kmi.iserve.commons.model.Service;
import uk.ac.open.kmi.iserve.sal.ServiceFormat;
import uk.ac.open.kmi.iserve.sal.ServiceFormatDetector;
import uk.ac.open.kmi.iserve.sal.ServiceImporter;
import uk.ac.open.kmi.iserve.sal.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.exception.DocumentException;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.manager.*;
import uk.ac.open.kmi.iserve.sal.util.UriUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Singleton Class providing a Facade to the entire Storage and Access Layer
 * Delegates the actual implementation to each of the specific managers configured
 *
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public class ManagerSingleton implements iServeManager {

    private static final Logger log = LoggerFactory.getLogger(ManagerSingleton.class);

    private static final String CONFIG_PROPERTIES_FILENAME = "config.properties";
    private static final int HALF_MB = 512 * 1024;

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

    private ManagerSingleton() throws ConfigurationException, SalException {

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
        if (proxyHost != null && proxyPort != null) {
            Properties prop = System.getProperties();
            prop.put("http.proxyHost", proxyHost);
            prop.put("http.proxyPort", proxyPort);
        }
    }

    public static ManagerSingleton getInstance() {
        if (_instance == null) {
            try {
                _instance = new ManagerSingleton();
            } catch (ConfigurationException e) {
                throw (new IllegalStateException("iServe's storage and access layer is not properly configured", e));
            } catch (SalException e) {
                throw (new IllegalStateException("iServe's storage and access layer is not properly configured", e));
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
     * uk.ac.open.kmi.iserve.commons.model.Service Management
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

        // Throw error if Format Unsupported
        if (format.equals(ServiceFormat.UNSUPPORTED) ||
                (!ServiceFormat.NATIVE_PARSERS_MAP.containsKey(format) && !this.importerMap.containsKey(format))) {
            throw new ServiceException("Unable to import service. Format unsupported.");
        }

        // Ensure we have a stream that we can read several times
        InputStream stream = serviceContent.markSupported() ? serviceContent : new BufferedInputStream(serviceContent);

        // We have a suitable importer
        ServiceImporter importer = importerMap.get(format);

        URI sourceDocUri = null;
        URI serviceUri = null;
        try {
            // 1st Store the document
            stream.mark(HALF_MB); // mark the init
            sourceDocUri = this.docManager.createDocument(stream, format);
            stream.reset(); // reset the stream
            if (sourceDocUri == null)
                throw new ServiceException("Unable to save service document. Operation aborted.");

            // 2nd Parse and Transform the document
            List<Service> services;
            if (ServiceFormat.NATIVE_PARSERS_MAP.containsKey(format)) {
                // Its a native format: parse it
                ServiceReader reader = new ServiceReaderImpl();
                services = reader.parse(stream, ServiceFormat.NATIVE_PARSERS_MAP.get(format));
            } else {
                // Its an external format: use the appropriate importer
                services = importer.transform(serviceContent);
            }

            // 3rd - Store the resulting MSM services. In principle it should be just one
            Service svc;
            if (services != null && !services.isEmpty()) {
                svc = services.get(0);
                svc.setSource(sourceDocUri); // The service is being imported -> update the source
                serviceUri = this.serviceManager.addService(svc);
            }

            // 4th Log it was all done correctly
            // TODO: log to the system and notify observers
            log.info("Service imported: " + serviceUri.toASCIIString());

        } catch (IOException e) {
            log.error("Problems accessing service content", e);
        } finally {
            // Rollback if something went wrong
            if (serviceUri == null && sourceDocUri != null) {
                this.docManager.deleteDocument(sourceDocUri);
                log.warn("There were problems importing the service. Changes undone.");
            }
        }

        return serviceUri;
    }

    /**
     * Clears the registry entirely: all documents and services are deleted
     * This operation cannot be undone. Use with care.
     *
     * @return true if the registry was cleared.
     * @throws uk.ac.open.kmi.iserve.sal.exception.SalException
     *
     */
    @Override
    public boolean clearRegistry() throws SalException {
        return this.clearServices() & this.clearDocuments();
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
        if (format.equals(ServiceFormat.UNSUPPORTED) ||
                !this.importerMap.containsKey(format)) {
            throw new ServiceException("Unable to import service. Format unsupported.");
        }
        // We have a suitable importer

        ServiceImporter importer;
        URI serviceUri = null;
        try {
            // 1st Obtain the document and transform it
            importer = importerMap.get(format);
            InputStream is = sourceDocumentUri.toURL().openStream();

            List<Service> services = importer.transform(is);

            if (services != null && !services.isEmpty())
                serviceUri = this.serviceManager.addService(services.get(0));

            // 4th Log it was all done correctly
            // TODO: log to the system and notify observers
            log.info("Service imported: " + serviceUri.toASCIIString());

        } catch (MalformedURLException e) {
            log.error("Error obtaining the source document. Incorrect URL. " + sourceDocumentUri.toString());
            throw new ServiceException("Unable to register service. Incorrect source document URL.", e);
        } catch (IOException e) {
            log.error("Error obtaining the source document.");
            throw new ServiceException("Unable to register service. Unable to retrieve source document.", e);
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

    /**
     * Delete all services from the server
     * This operation cannot be undone. Use with care.
     *
     * @return true if all the services could be deleted.
     * @throws uk.ac.open.kmi.iserve.sal.exception.SalException
     *
     */
    @Override
    public boolean clearServices() throws SalException {
        return this.serviceManager.clearServices();
    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.sal.manager.iServeManager#getService(java.net.URI, uk.ac.open.kmi.iserve.sal.ServiceFormat)
     */
    @Override
    public String exportService(URI serviceUri, ServiceFormat format)
            throws ServiceException {
        //		return this.serviceManager.getServiceSerialisation(serviceUri, syntax);
        // TODO
        return null;
    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.sal.manager.iServeManager#getService(java.net.URI)
	 */
    @Override
    public Service getService(URI serviceUri) throws SalException {
        return this.serviceManager.getService(serviceUri);
    }

    /* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.iServeManager#getServices(java.util.List)
	 */
    @Override
    public List<Service> getServices(List<URI> serviceUris) throws SalException {
        return this.serviceManager.getServices(serviceUris);
    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.sal.manager.iServeManager#addRelatedDocumentToService(java.net.URI, java.net.URI)
     */
    @Override
    public boolean addRelatedDocumentToService(URI serviceUri,
                                               URI relatedDocument) throws SalException {
        // TODO: Implement
        throw new SalException("Not yet implemented");
    }

    // Delegate Methods
	/*
	 *  Document Manager
	 */


    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.sal.manager.iServeManager#getDocument(java.net.URI)
     */
    @Override
    public InputStream getDocument(URI documentUri) throws DocumentException {
        return this.docManager.getDocument(documentUri);
    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.sal.manager.iServeManager#createDocument(java.io.InputStream, uk.ac.open.kmi.iserve.sal.ServiceFormat)
     */
    @Override
    public URI createDocument(InputStream docContent, ServiceFormat format) throws SalException {
        return this.docManager.createDocument(docContent, format);
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

    /**
     * Deletes all documents from the server
     * This operation cannot be undone. Use with care.
     *
     * @return true if all the documents could be deleted.
     * @throws uk.ac.open.kmi.iserve.sal.exception.SalException
     *
     */
    @Override
    public boolean clearDocuments() throws SalException {
        return this.docManager.clearDocuments();
    }

}
