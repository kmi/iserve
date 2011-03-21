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
package uk.ac.open.kmi.iserve.sal.gwt.server;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.iserve.commons.io.IOUtil;
import uk.ac.open.kmi.iserve.sal.config.SalConfig;
import uk.ac.open.kmi.iserve.sal.gwt.server.util.LufConnector;
import uk.ac.open.kmi.iserve.sal.manager.DocumentManager;
import uk.ac.open.kmi.iserve.sal.manager.LogManager;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;
import uk.ac.open.kmi.iserve.sal.manager.TaxonomyManager;
import uk.ac.open.kmi.iserve.sal.manager.UserManager;
import uk.ac.open.kmi.iserve.sal.model.impl.URIImpl;

public class Factory {

	private static Factory factory = null;

	private SalConfig config; 

	private Factory() throws IOException {
		String baseDir = ServiceBrowseServiceImpl.class.getResource("/").getPath();
		baseDir = baseDir.replaceAll("%20", " ");

		Properties prop = IOUtil.readProperties(new File(baseDir + "../config.properties"));
		prop.setProperty(SalConfig.XSLT_PATH, baseDir + "../hrests.xslt");
		config = new SalConfig(prop);
	}

	public static Factory getInstance() throws IOException {
		if ( factory == null ) {
			factory = new Factory();
		}
		return factory;
	}

	public ServiceManager createServiceManager() throws RepositoryException, TransformerConfigurationException, WSDLException, ParserConfigurationException {
		return new ServiceManager(config);
	}

	public DocumentManager createDocumentManager() {
		return new DocumentManager(config);
	}

	public LogManager createLogManager() throws RepositoryException {
		return new LogManager(config);
	}

	public TaxonomyManager createTaxonomyManager() throws RepositoryException {
		return new TaxonomyManager(config);
	}

	public UserManager createUserManager() throws RepositoryException {
		return new UserManager(config);
	}

	public LufConnector createLufConnector() {
		// initialize the connector to LUF
		LufConnector lufConnector = new LufConnector(new URIImpl(config.getLufUrl()));
		if ( config.getProxyHostName() != null && config.getProxyHostName() != "" ||
				config.getProxyPort() != null && config.getProxyPort() != "" ) {
			lufConnector.setProxy(config.getProxyHostName(), Integer.valueOf(config.getProxyPort()));
		}
		return lufConnector;
	}

	public SalConfig getSalConfig() {
		return config;
	}

}
