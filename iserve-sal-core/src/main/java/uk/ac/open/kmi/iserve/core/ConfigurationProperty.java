/*
 * Copyright (c) 2014. Knowledge Media Institute - The Open University
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

import com.google.inject.TypeLiteral;

/**
 * iServe's Configuration Properties
 * Provides an enumeration of configuration values their, description, and default values.
 * These variables will be picked up by Guice at injection time based on annotation properties.
 * Code and approach taken from:
 * http://java.dzone.com/articles/flexible-configuration-guice
 *
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public enum ConfigurationProperty {

    // All iServe Configuration Properties
    PROXY_HOST(
            "iserve.proxyHost",
            "Hostname to be used as a proxy for HTTP communications.",
            new TypeLiteral<String>() {
            },
            ""
    ),

    PROXY_PORT(
            "iserve.proxyPort",
            "Port to be used as a proxy for HTTP communications",
            new TypeLiteral<String>() {
            },
            ""
    ),

    ISERVE_URL(
            "iserve.url",
            "URL where iServe will be exposed to clients. This URL will be used to determine the URLs of all services registered.",
            new TypeLiteral<String>() {
            },
            "http://localhost:9090/iserve"
    ),

    //TODO: Provide a proper solution to this
    ISERVE_VERSION(
            "iserve.version",
            "Version of iServe.",
            new TypeLiteral<String>() {
            },
            "2.0.0"
    ),

    DOCUMENTS_FOLDER(
            "iserve.documents.folder",
            "Documents folder to be used for storing service documents. Relative paths could be deleted when deploying new versions of the software on Web Application servers so absolute paths are recommended in these cases.",
            new TypeLiteral<String>() {
            },
            "/tmp/iserve-docs/"
    ),

    SERVICES_SPARQL_QUERY(
            "iserve.services.sparql.query",
            "URL of the SPARQL Query endpoint for services descriptions. This URL depends on the RDF Store used so check its documentation.",
            new TypeLiteral<String>() {
            },
            "http://localhost:8080/openrdf-sesame/repositories/iserve"
    ),

    SERVICES_SPARQL_UPDATE(
            "iserve.services.sparql.update",
            "URL of the SPARQL Update endpoint for services descriptions. This URL depends on the RDF Store used so check its documentation.",
            new TypeLiteral<String>() {
            },
            "http://localhost:8080/openrdf-sesame/repositories/iserve/statements"
    ),

    SERVICES_SPARQL_SERVICE(
            "iserve.services.sparql.service",
            "URL of the SPARQL Service endpoint for services descriptions. This URL depends on the RDF Store used so check its documentation.",
            new TypeLiteral<String>() {
            },
            "http://localhost:8080/openrdf-sesame/repositories/iserve/rdf-graphs/service"
    ),

    NFP_SPARQL_QUERY(
            "iserve.nfp.sparql.query",
            "URL of the SPARQL Query endpoint for non-functional properties. This URL depends on the RDF Store used so check its documentation.",
            new TypeLiteral<String>() {
            },
            "http://localhost:8080/openrdf-sesame/repositories/iserve"
    ),

    NFP_SPARQL_UPDATE(
            "iserve.nfp.sparql.update",
            "URL of the SPARQL Update endpoint for non-functional properties. This URL depends on the RDF Store used so check its documentation.",
            new TypeLiteral<String>() {
            },
            "http://localhost:8080/openrdf-sesame/repositories/iserve/statements"
    ),

    NFP_SPARQL_SERVICE(
            "iserve.nfp.sparql.service",
            "URL of the SPARQL Service endpoint for non-functional properties. This URL depends on the RDF Store used so check its documentation.",
            new TypeLiteral<String>() {
            },
            "http://localhost:8080/openrdf-sesame/repositories/iserve/rdf-graphs/service"
    ),

    CONCEPT_MATCHER(
            "iserve.discovery.conceptMatcher",
            "Fully qualified name of the concept matcher to be used for discovery.",
            new TypeLiteral<String>() {
            },
            "uk.ac.open.kmi.iserve.discovery.disco.impl.SparqlLogicConceptMatcher"
    ),

    FREE_TEXT_SEARCH(
            "iserve.discovery.freetextsearch",
            "Fully qualified name of the free text search plugin to use.",
            new TypeLiteral<String>() {
            },
            "uk.ac.open.kmi.iserve.discovery.freetextsearch.impl.OwlimSearchPlugin"
    );


    private String name;
    private String description;
    private TypeLiteral<?> valueType;
    private String defaultValue;

    private <V> ConfigurationProperty(String name, String description, TypeLiteral<V> valueType,
                                      String defaultValue) {
        this.name = name;
        this.description = description;
        this.valueType = valueType;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @SuppressWarnings("unchecked")
    public <V> TypeLiteral<V> getValueType() {
        return (TypeLiteral<V>) valueType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

}
