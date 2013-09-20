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

package uk.ac.open.kmi.iserve.core;

/**
 * iServe's Configuration Properties
 *
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public class SystemConfiguration {

    // Configuration properties
    public static final String PROXY_HOST_NAME_PROP = "http.proxyHost";
    public static final String PROXY_PORT_PROP = "http.proxyPort";
    public static final String ISERVE_URL_PROP = "iserve.url";
    public static final String ISERVE_VERSION_PROP = "iserve.version";

    public static final String DOC_FOLDER_PATH_PROP = "iserve.documents.folder";

    // Services data
    public static final String SERVICES_REPOSITORY_SPARQL_PROP = "iserve.services.sparql.query";
    public static final String SERVICES_REPOSITORY_SPARQL_UPDATE_PROP = "iserve.services.sparql.update";
    public static final String SERVICES_REPOSITORY_SPARQL_SERVICE_PROP = "iserve.services.sparql.service";

    // Discovery config
    public static final String DEFAULT_CONCEPT_MATCHER_PROP = "discovery.default.conceptMatcher";
}
