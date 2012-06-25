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
package uk.ac.open.kmi.iserve.sal;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton with iServe's Configuration
 * 
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public class SystemConfiguration {

	private static final Logger log = LoggerFactory.getLogger(SystemConfiguration.class);
	
	/**
	 * 
	 */
	private static final String DEFAULT_XSLT_PATH = "../hrests.xslt";

	/**
	 * Default services repository name
	 */
	private static final String DEFAULT_SERVICES_REPO = "iserve-services";

	/**
	 * Default logs repository name
	 */
	private static final String DEFAULT_LOGS_REPO = "iserve-logs";

	/**
	 * Default users repository
	 */
	private static final String DEFAULT_USERS_REPO = DEFAULT_LOGS_REPO;

	/**
	 * Default Linked User Feedback URL
	 */
	private static final String DEFAULT_LUF_URL = "http://soa4all.isoco.net/luf";

	// Configuration properties
	private static final String ISERVE_URL_PROP = "iserve.url";
	private static final String DOC_FOLDER_PATH_PROP = "iserve.documents";
	private static final String PROXY_HOST_NAME_PROP = "iserve.proxyHost";
	private static final String PROXY_PORT_PROP = "iserve.proxyPort";
	// Services data
	private static final String SERVICES_REPOSITORY_URL_PROP = "services.rdfserver";
	private static final String SERVICES_REPOSITORY_NAME_PROP = "services.repository";
	// Logs data
	private static final String LOG_REPOSITORY_URL_PROP = "log.server";
	private static final String LOG_REPOSITORY_NAME_PROP = "log.repository";
	// Users data
	private static final String USERS_SERVER_URL_PROP = "users.server";
	private static final String USERS_REPOSITORY_NAME_PROP = "users.repository";
	// Tagging, rating, etc
	private static final String LUF_URL_PROP = "lufURL";

	
	// TODO: Remove this one?
	private static final String CORPUS_FILE_PROP = "corpusFile";
	
	private static final String XSLT_PATH_PROP = "xsltPath";

	// This should contain the URL to the base of iServe's server (thus including hostname and module path)
	private URL iserveUrl;

	private URL dataRepositoryUrl;

	private String dataRepositoryName;

	private URL logRepositoryUrl;

	private String logRepositoryName;

	private URL usersRepositoryUrl;

	private String usersRepositoryName;

	private URL lufUrl;

	private String proxyHostName;

	private String proxyPort;

	private String corpusFile;

	private String docFolderPath;

	private String microWsmoXsltPath;

	public SystemConfiguration(String configFileUrl) throws ConfigurationException {
		PropertiesConfiguration config;
		
		if (configFileUrl == null) {
			throw new ConfigurationException("Unable to setup iServe. Configuration file is null.");
		}
		
		log.info("Loading iServe configuration from " + configFileUrl);
		
		config = new PropertiesConfiguration(configFileUrl);
		try {
			this.corpusFile = config.getString(CORPUS_FILE_PROP);
			this.docFolderPath = config.getString(DOC_FOLDER_PATH_PROP);
			this.logRepositoryName = config.getString(LOG_REPOSITORY_NAME_PROP, DEFAULT_LOGS_REPO);
			this.logRepositoryUrl = new URL(config.getString(LOG_REPOSITORY_URL_PROP));
			this.lufUrl = new URL(config.getString(LUF_URL_PROP, DEFAULT_LUF_URL));		
			this.proxyHostName = config.getString(PROXY_HOST_NAME_PROP);
			this.proxyPort = config.getString(PROXY_PORT_PROP);
			this.dataRepositoryName = config.getString(SERVICES_REPOSITORY_NAME_PROP, DEFAULT_SERVICES_REPO);
			this.dataRepositoryUrl = new URL(config.getString(SERVICES_REPOSITORY_URL_PROP));
			this.iserveUrl = new URL(config.getString(ISERVE_URL_PROP));
			this.usersRepositoryName = config.getString(USERS_REPOSITORY_NAME_PROP, DEFAULT_USERS_REPO);
			this.usersRepositoryUrl = new URL(config.getString(USERS_SERVER_URL_PROP));
			this.microWsmoXsltPath = config.getString(XSLT_PATH_PROP, DEFAULT_XSLT_PATH);
		} catch (MalformedURLException e) {
			throw new ConfigurationException("Unable to setup iServe.", e);
		}
	}

	/**
	 * Returns the URL where iServe is. This shall include both the hostname as well
	 * as the path to the iServe module within the application if necessary.
	 * All the REST services will be deployed in relative URLs to this.
	 * 
	 * @return the uriPrefix
	 */
	public URL getIserveUrl() {
		return this.iserveUrl;
	}

	/**
	 * @param uriPrefix the uriPrefix to set
	 */
	public void setUriPrefix(URL uriPrefix) {
		this.iserveUrl = uriPrefix;
	}

	/**
	 * @return the docFolderPath
	 */
	public String getDocumentsFolder() {
		return this.docFolderPath;
	}

	/**
	 * @param docFolderPath the docFolderPath to set
	 */
	public void setDocumentsFolder(String docFolderPath) {
		this.docFolderPath = docFolderPath;
	}

	/**
	 * @return the repoServerUrl
	 */
	public URL getServicesRepositoryUrl() {
		return this.dataRepositoryUrl;
	}

	/**
	 * @param repoServerUrl the repoServerUrl to set
	 */
	public void setServicesRepositoryUrl(URL repoServerUrl) {
		this.dataRepositoryUrl = repoServerUrl;
	}

	/**
	 * @return the repoName
	 */
	public String getServicesRepositoryName() {
		return this.dataRepositoryName;
	}

	/**
	 * @param repoName the repoName to set
	 */
	public void setServicesRepositoryName(String repoName) {
		this.dataRepositoryName = repoName;
	}

	/**
	 * @return the logServerUrl
	 */
	public URL getLogServerUrl() {
		return this.logRepositoryUrl;
	}

	/**
	 * @param logServerUrl the logServerUrl to set
	 */
	public void setLogServerUrl(URL logServerUrl) {
		this.logRepositoryUrl = logServerUrl;
	}

	/**
	 * @return the logRepoName
	 */
	public String getLogsRepositoryName() {
		return this.logRepositoryName;
	}

	/**
	 * @param logRepoName the logRepoName to set
	 */
	public void setLogsRepositoryName(String logRepoName) {
		this.logRepositoryName = logRepoName;
	}

	/**
	 * @return the userServerUrl
	 */
	public URL getUserServerUrl() {
		return this.usersRepositoryUrl;
	}

	/**
	 * @param userServerUrl the userServerUrl to set
	 */
	public void setUserServerUrl(URL userServerUrl) {
		this.usersRepositoryUrl = userServerUrl;
	}

	/**
	 * @return the userRepoName
	 */
	public String getUsersRepositoryName() {
		return this.usersRepositoryName;
	}

	/**
	 * @param userRepoName the userRepoName to set
	 */
	public void setUsersRepositoryName(String userRepoName) {
		this.usersRepositoryName = userRepoName;
	}

	/**
	 * @return the lufUrl
	 */
	public URL getLufUrl() {
		return this.lufUrl;
	}

	/**
	 * @param lufUrl the lufUrl to set
	 */
	public void setLufUrl(URL lufUrl) {
		this.lufUrl = lufUrl;
	}

	/**
	 * @return the proxyHostName
	 */
	public String getProxyHostName() {
		return this.proxyHostName;
	}

	/**
	 * @param proxyHostName the proxyHostName to set
	 */
	public void setProxyHostName(String proxyHostName) {
		this.proxyHostName = proxyHostName;
	}

	/**
	 * @return the proxyPort
	 */
	public String getProxyPort() {
		return this.proxyPort;
	}

	/**
	 * @param proxyPort the proxyPort to set
	 */
	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}

	/**
	 * @return the corpusFile
	 */
	public String getCorpusFile() {
		return this.corpusFile;
	}

	/**
	 * @param corpusFile the corpusFile to set
	 */
	public void setCorpusFile(String corpusFile) {
		this.corpusFile = corpusFile;
	}

	/**
	 * @return the xsltPath
	 */
	public String getXsltPath() {
		return this.microWsmoXsltPath;
	}

	/**
	 * @param xsltPath the xsltPath to set
	 */
	public void setXsltPath(String xsltPath) {
		this.microWsmoXsltPath = xsltPath;
	}
}
