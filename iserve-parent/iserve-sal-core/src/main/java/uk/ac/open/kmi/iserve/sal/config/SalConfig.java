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
package uk.ac.open.kmi.iserve.sal.config;

import java.util.Properties;

import uk.ac.open.kmi.iserve.importer.ImporterConfig;

public class SalConfig {

	public static final String URI_PREFIX = "serverName";

	public static final String DOC_FOLDER_PATH = "docFolder";

	public static final String REPO_SERVER_URL = "sesameServerURL";

	public static final String REPO_NAME = "repoName";

	public static final String LOG_SERVER_URL = "logServerURL";

	public static final String LOG_REPO_NAME = "logRepoName";

	public static final String USER_SERVER_URL = "userServerURL";

	public static final String USER_REPO_NAME = "userRepoName";

	public static final String LUF_URL = "lufURL";

	public static final String PROXY_HOST_NAME = "proxyHostName";

	public static final String PROXY_PORT = "proxyPort";

	public static final String CORPUS_FILE = "corpusFile";

	public static final String XSLT_PATH = "xsltPath";

	private String uriPrefix;

	private String docFolderPath;

	private String repoServerUrl;

	private String repoName;

	private String logServerUrl;

	private String logRepoName;

	private String userServerUrl;

	private String userRepoName;

	private String lufUrl;

	private String proxyHostName;

	private String proxyPort;

	private String corpusFile;

	private ImporterConfig importerConfig;

	private String xsltPath;

	public SalConfig(Properties prop) {
		uriPrefix = prop.getProperty(URI_PREFIX);
		docFolderPath = prop.getProperty(DOC_FOLDER_PATH);
		repoServerUrl = prop.getProperty(REPO_SERVER_URL);
		repoName = prop.getProperty(REPO_NAME);
		setLogServerUrl(prop.getProperty(LOG_SERVER_URL));
		setLogRepoName(prop.getProperty(LOG_REPO_NAME));
		setLufUrl(prop.getProperty(LUF_URL));
		setProxyHostName(prop.getProperty(PROXY_HOST_NAME));
		setProxyPort(prop.getProperty(PROXY_PORT));
		setCorpusFile(prop.getProperty(CORPUS_FILE));
		xsltPath = prop.getProperty(XSLT_PATH);
		userServerUrl = prop.getProperty(USER_SERVER_URL);
		userRepoName = prop.getProperty(USER_REPO_NAME);
		importerConfig = new ImporterConfig(uriPrefix, docFolderPath, repoServerUrl, repoName);
	}

	public ImporterConfig getImporterConfig() {
		return importerConfig;
	}

	public String getXsltPath() {
		return xsltPath;
	}

	public void setLogServerUrl(String logServerUrl) {
		this.logServerUrl = logServerUrl;
	}

	public String getLogServerUrl() {
		return logServerUrl;
	}

	public void setLogRepoName(String logRepoName) {
		this.logRepoName = logRepoName;
	}

	public String getLogRepoName() {
		return logRepoName;
	}

	public void setProxyHostName(String proxyHostName) {
		this.proxyHostName = proxyHostName;
	}

	public String getProxyHostName() {
		return proxyHostName;
	}

	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getProxyPort() {
		return proxyPort;
	}

	public void setCorpusFile(String corpusFile) {
		this.corpusFile = corpusFile;
	}

	public String getCorpusFile() {
		return corpusFile;
	}

	public void setLufUrl(String lufUrl) {
		this.lufUrl = lufUrl;
	}

	public String getLufUrl() {
		return lufUrl;
	}

	public String getDocFolderPath() {
		return docFolderPath;
	}

	public String getUriPrefix() {
		return uriPrefix;
	}

	public void setUserServerUrl(String userServerUrl) {
		this.userServerUrl = userServerUrl;
	}

	public String getUserServerUrl() {
		return userServerUrl;
	}

	public void setUserRepoName(String userRepoName) {
		this.userRepoName = userRepoName;
	}

	public String getUserRepoName() {
		return userRepoName;
	}

}
