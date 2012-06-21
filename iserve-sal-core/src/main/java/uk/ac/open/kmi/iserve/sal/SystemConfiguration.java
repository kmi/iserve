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
	 * Default services reporitory name
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
	private static final String URI_PREFIX = "serverName";
	private static final String DOC_FOLDER_PATH = "docFolder";
	private static final String REPO_SERVER_URL = "sesameServerURL";
	private static final String REPO_NAME = "repoName";
	private static final String LOG_SERVER_URL = "logServerURL";
	private static final String LOG_REPO_NAME = "logRepoName";
	private static final String USER_SERVER_URL = "userServerURL";
	private static final String USER_REPO_NAME = "userRepoName";
	private static final String LUF_URL = "lufURL";
	private static final String PROXY_HOST_NAME = "proxyHostName";
	private static final String PROXY_PORT = "proxyPort";
	private static final String CORPUS_FILE = "corpusFile";
	private static final String XSLT_PATH = "xsltPath";

	private String uriPrefix;

	private String dataRepositoryUrl;

	private String dataRepositoryName;

	private String logRepositoryUrl;

	private String logRepositoryName;

	private String userRepositoryUrl;

	private String userRepositoryName;

	private String lufUrl;

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
		this.corpusFile = config.getString(CORPUS_FILE);
		this.docFolderPath = config.getString(DOC_FOLDER_PATH);
		this.logRepositoryName = config.getString(LOG_REPO_NAME, DEFAULT_LOGS_REPO);
		this.logRepositoryUrl = config.getString(LOG_SERVER_URL);
		this.lufUrl = config.getString(LUF_URL, DEFAULT_LUF_URL);
		this.proxyHostName = config.getString(PROXY_HOST_NAME);
		this.proxyPort = config.getString(PROXY_PORT);
		this.dataRepositoryName = config.getString(REPO_NAME, DEFAULT_SERVICES_REPO);
		this.dataRepositoryUrl = config.getString(REPO_SERVER_URL);
		this.uriPrefix = config.getString(URI_PREFIX);
		this.userRepositoryName = config.getString(USER_REPO_NAME, DEFAULT_USERS_REPO);
		this.userRepositoryUrl = config.getString(USER_SERVER_URL);
		this.microWsmoXsltPath = config.getString(XSLT_PATH, DEFAULT_XSLT_PATH);
	}

	/**
	 * TODO: Fix name for this operation
	 * @return the uriPrefix
	 */
	public String getUriPrefix() {
		return this.uriPrefix;
	}

	/**
	 * @param uriPrefix the uriPrefix to set
	 */
	public void setUriPrefix(String uriPrefix) {
		this.uriPrefix = uriPrefix;
	}

	/**
	 * @return the docFolderPath
	 */
	public String getDocFolderPath() {
		return this.docFolderPath;
	}

	/**
	 * @param docFolderPath the docFolderPath to set
	 */
	public void setDocFolderPath(String docFolderPath) {
		this.docFolderPath = docFolderPath;
	}

	/**
	 * @return the repoServerUrl
	 */
	public String getRepoServerUrl() {
		return this.dataRepositoryUrl;
	}

	/**
	 * @param repoServerUrl the repoServerUrl to set
	 */
	public void setRepoServerUrl(String repoServerUrl) {
		this.dataRepositoryUrl = repoServerUrl;
	}

	/**
	 * @return the repoName
	 */
	public String getRepoName() {
		return this.dataRepositoryName;
	}

	/**
	 * @param repoName the repoName to set
	 */
	public void setRepoName(String repoName) {
		this.dataRepositoryName = repoName;
	}

	/**
	 * @return the logServerUrl
	 */
	public String getLogServerUrl() {
		return this.logRepositoryUrl;
	}

	/**
	 * @param logServerUrl the logServerUrl to set
	 */
	public void setLogServerUrl(String logServerUrl) {
		this.logRepositoryUrl = logServerUrl;
	}

	/**
	 * @return the logRepoName
	 */
	public String getLogRepoName() {
		return this.logRepositoryName;
	}

	/**
	 * @param logRepoName the logRepoName to set
	 */
	public void setLogRepoName(String logRepoName) {
		this.logRepositoryName = logRepoName;
	}

	/**
	 * @return the userServerUrl
	 */
	public String getUserServerUrl() {
		return this.userRepositoryUrl;
	}

	/**
	 * @param userServerUrl the userServerUrl to set
	 */
	public void setUserServerUrl(String userServerUrl) {
		this.userRepositoryUrl = userServerUrl;
	}

	/**
	 * @return the userRepoName
	 */
	public String getUserRepoName() {
		return this.userRepositoryName;
	}

	/**
	 * @param userRepoName the userRepoName to set
	 */
	public void setUserRepoName(String userRepoName) {
		this.userRepositoryName = userRepoName;
	}

	/**
	 * @return the lufUrl
	 */
	public String getLufUrl() {
		return this.lufUrl;
	}

	/**
	 * @param lufUrl the lufUrl to set
	 */
	public void setLufUrl(String lufUrl) {
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
