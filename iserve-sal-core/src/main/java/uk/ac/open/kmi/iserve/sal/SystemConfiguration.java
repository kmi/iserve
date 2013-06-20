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

package uk.ac.open.kmi.iserve.sal;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Singleton with iServe's Configuration
 *
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public class SystemConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SystemConfiguration.class);

    // Default path for service and documents URIs.
    // Note that any change here should also affect the REST API
    // Keep the trailing slash
    private static final String SERVICES_URL_PATH = "services/";
    private static final String DOCUMENTS_URL_PATH = "documents/";

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
    private static final String PROXY_HOST_NAME_PROP = "http.proxyHost";
    private static final String PROXY_PORT_PROP = "http.proxyPort";
    private static final String ISERVE_URL_PROP = "iserve.url";

    private static final String DOC_FOLDER_PATH_PROP = "iserve.documents.folder";

    // Services data
    private static final String SERVICES_REPOSITORY_URL_PROP = "iserve.services.rdfserver";
    private static final String SERVICES_REPOSITORY_NAME_PROP = "iserve.services.repository";
    private static final String SERVICES_REPOSITORY_SPARQL_PROP = "iserve.services.sparql.query";
    private static final String SERVICES_REPOSITORY_SPARQL_UPDATE_PROP = "iserve.services.sparql.update";
    private static final String SERVICES_REPOSITORY_SPARQL_SERVICE_PROP = "iserve.services.sparql.service";
    // Logs data
    private static final String LOG_REPOSITORY_URL_PROP = "log.server";
    private static final String LOG_REPOSITORY_NAME_PROP = "log.repository";
    // Users data
    private static final String USERS_SERVER_URL_PROP = "users.server";
    private static final String USERS_REPOSITORY_NAME_PROP = "users.repository";
    // Tagging, rating, etc
    private static final String LUF_URL_PROP = "lufURL";


    private String dataRepositoryName;
    private String logRepositoryName;
    private String usersRepositoryName;
    private String proxyHostName;
    private String proxyPort;
    private String servicesPath;
    private String documentsPath;

    private URI iserveUri;
    private URI documentsFolderUri;
    private URI dataRepositoryUri;
    private URI dataSparqlUri;
    private URI dataSparqlUpdateUri;
    private URI logRepositoryUri;
    private URI usersRepositoryUri;
    private URI lufUri;
    private URI servicesUri;
    private URI documentsUri;
    private URI dataSparqlServiceUri;


    public SystemConfiguration(String configFileUrl) throws ConfigurationException {
        PropertiesConfiguration config;

        if (configFileUrl == null) {
            throw new ConfigurationException("Unable to setup iServe. Configuration file is null.");
        }

        log.info("Loading iServe configuration from " + configFileUrl);

        config = new PropertiesConfiguration(configFileUrl);
        try {
            this.logRepositoryName = config.getString(LOG_REPOSITORY_NAME_PROP, DEFAULT_LOGS_REPO);
            this.logRepositoryUri = new URI(config.getString(LOG_REPOSITORY_URL_PROP));
            this.lufUri = new URI(config.getString(LUF_URL_PROP, DEFAULT_LUF_URL));
            this.proxyHostName = config.getString(PROXY_HOST_NAME_PROP);
            this.proxyPort = config.getString(PROXY_PORT_PROP);
            this.dataRepositoryName = config.getString(SERVICES_REPOSITORY_NAME_PROP, DEFAULT_SERVICES_REPO);
            this.dataRepositoryUri = new URI(config.getString(SERVICES_REPOSITORY_URL_PROP));
            this.dataSparqlUri = new URI(config.getString(SERVICES_REPOSITORY_SPARQL_PROP));
            this.dataSparqlUpdateUri = new URI(config.getString(SERVICES_REPOSITORY_SPARQL_UPDATE_PROP));
            this.dataSparqlServiceUri = new URI(config.getString(SERVICES_REPOSITORY_SPARQL_SERVICE_PROP));

            this.iserveUri = new URI(config.getString(ISERVE_URL_PROP) +
                    (config.getString(ISERVE_URL_PROP).endsWith("/") ? "" : "/"));  // Ensure it has a final slash

            this.documentsFolderUri = new URI(config.getString(DOC_FOLDER_PATH_PROP) +
                    (config.getString(DOC_FOLDER_PATH_PROP).endsWith("/") ? "" : "/"));  // Ensure it has a final slash

            this.usersRepositoryName = config.getString(USERS_REPOSITORY_NAME_PROP, DEFAULT_USERS_REPO);
            this.usersRepositoryUri = new URI(config.getString(USERS_SERVER_URL_PROP));
            this.servicesPath = SERVICES_URL_PATH;
            this.documentsPath = DOCUMENTS_URL_PATH;

            // Create the services and documents base URIs
//            this.servicesUri = new URI (this.iserveUri.toASCIIString() + this.servicesPath);
//            this.documentsUri = new URI (this.iserveUri.toASCIIString() + this.documentsPath);

            this.servicesUri = this.iserveUri.resolve(this.servicesPath);
            this.documentsUri = this.iserveUri.resolve(this.documentsPath);

        } catch (URISyntaxException e) {
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
    public URI getIserveUri() {
        return this.iserveUri;
    }

    /**
     * @param uriPrefix the uriPrefix to set
     */
    public void setUriPrefix(URI uriPrefix) {
        this.iserveUri = uriPrefix;
    }

    /**
     * @return the documentsFolderUri
     */
    public URI getDocumentsFolderUri() {
        return this.documentsFolderUri;
    }

    /**
     * @param internalUri the internalUri of the documents folder to set
     */
    public void setDocumentsFolderUri(URI internalUri) {
        this.documentsFolderUri = internalUri;
    }

    /**
     * @return the repoServerUrl
     */
    public URI getServicesRepositoryUrl() {
        return this.dataRepositoryUri;
    }

    /**
     * @param repoServerUrl the repoServerUrl to set
     */
    public void setServicesRepositoryUrl(URI repoServerUrl) {
        this.dataRepositoryUri = repoServerUrl;
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
     * @return the logServerUri
     */
    public URI getLogServerUri() {
        return this.logRepositoryUri;
    }

    /**
     * @param logServerUri the logServerUrl to set
     */
    public void setLogServerUrl(URI logServerUri) {
        this.logRepositoryUri = logServerUri;
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
     * @return the userServerUri
     */
    public URI getUserServerUri() {
        return this.usersRepositoryUri;
    }

    /**
     * @param userServerUri the userServerUri to set
     */
    public void setUserServerUri(URI userServerUri) {
        this.usersRepositoryUri = userServerUri;
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
     * @return the lufUri
     */
    public URI getLufUri() {
        return this.lufUri;
    }

    /**
     * @param lufUri the lufUri to set
     */
    public void setLufUri(URI lufUri) {
        this.lufUri = lufUri;
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
     * @return the servicesPath
     */
    public String getServicesPath() {
        return this.servicesPath;
    }

    /**
     * @param servicesPath the servicesPath to set
     */
    public void setServicesPath(String servicesPath) {
        this.servicesPath = servicesPath;
    }

    /**
     * @return the documentsPath
     */
    public String getDocumentsPath() {
        return this.documentsPath;
    }

    /**
     * @param documentsPath the documentsPath to set
     */
    public void setDocumentsPath(String documentsPath) {
        this.documentsPath = documentsPath;
    }

    /**
     * @return the servicesUri
     */
    public URI getServicesUri() {
        return this.servicesUri;
    }

    /**
     * @param servicesUri the servicesUri to set
     */
    public void setServicesUri(URI servicesUri) {
        this.servicesUri = servicesUri;
    }

    /**
     * @return the documentsUri
     */
    public URI getDocumentsUri() {
        return this.documentsUri;
    }

    /**
     * @param documentsUri the documentsUri to set
     */
    public void setDocumentsUri(URI documentsUri) {
        this.documentsUri = documentsUri;
    }

    /**
     * @return the dataRepositoryName
     */
    public String getDataRepositoryName() {
        return this.dataRepositoryName;
    }

    /**
     * @param dataRepositoryName the dataRepositoryName to set
     */
    public void setDataRepositoryName(String dataRepositoryName) {
        this.dataRepositoryName = dataRepositoryName;
    }

    /**
     * @return the dataRepositoryUri
     */
    public URI getDataRepositoryUri() {
        return this.dataRepositoryUri;
    }

    /**
     * @param dataRepositoryUri the dataRepositoryUri to set
     */
    public void setDataRepositoryUri(URI dataRepositoryUri) {
        this.dataRepositoryUri = dataRepositoryUri;
    }

    /**
     * @return the dataSparqlUri
     */
    public URI getDataSparqlUri() {
        return this.dataSparqlUri;
    }

    /**
     * @param dataSparqlUri the dataSparqlUri to set
     */
    public void setDataSparqlUri(URI dataSparqlUri) {
        this.dataSparqlUri = dataSparqlUri;
    }

    public URI getDataSparqlUpdateUri() {
        return dataSparqlUpdateUri;
    }

    public void setDataSparqlUpdateUri(URI dataSparqlUpdateUri) {
        this.dataSparqlUpdateUri = dataSparqlUpdateUri;
    }

    public URI getDataSparqlServiceUri() {
        return dataSparqlServiceUri;
    }

    public void setDataSparqlServiceUri(URI dataSparqlServiceUri) {
        this.dataSparqlServiceUri = dataSparqlServiceUri;
    }

}
