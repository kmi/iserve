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

package uk.ac.open.kmi.iserve.sal.manager.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.manager.IntegratedComponent;
import uk.ac.open.kmi.iserve.sal.manager.NfpManager;

import javax.inject.Named;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

/**
 * Created by Luca Panziera on 27/05/2014.
 */

// TODO reuse graph store manager functionality
@Singleton
public class NfpManagerSparql extends IntegratedComponent implements NfpManager {

    private Logger logger = LoggerFactory.getLogger(NfpManagerSparql.class);

    private String sparqlEndpoint;
    private Map<URI, Map<URI, String>> propertyValueCache;

    @Inject
    public NfpManagerSparql(EventBus eventBus, @Named(SystemConfiguration.ISERVE_URL_PROP) String iServeUri, @Named(SystemConfiguration.SERVICES_REPOSITORY_SPARQL_PROP) String sparqlEndpoint) throws SalException {
        super(eventBus, iServeUri);
        this.sparqlEndpoint = sparqlEndpoint;
        propertyValueCache = Maps.newHashMap();
    }


    @Override
    public void shutdown() {

    }

    @Override
    public void createPropertyValue(URI resource, URI property, Object value) {

    }

    @Override
    public void createPropertyValuesOfResource(Map<URI, Object> propertyValueMap, URI resource) {

    }

    @Override
    public void createPropertyValueOfResources(Map<URI, Object> resourceValueMap, URI property) {

    }

    @Override
    public Object getPropertyValue(URI resource, URI property, Class valueClass) {
        if (!propertyValueCache.keySet().contains(resource)) {
            // put values in cache
            buildCacheForResource(resource);
        }
        Map<URI, String> resourceProperties = propertyValueCache.get(resource);
        if (resourceProperties != null && resourceProperties.containsKey(property)) {
            String value = resourceProperties.get(property);
            logger.debug("{}, {}, {}", resource, property, value);

            if (value != null) {
                if (valueClass.equals(Double.class)) {
                    return new Double(value);
                } else if (valueClass.equals(Integer.class)) {
                    return new Integer(value);
                } else if (valueClass.equals(String.class)) {
                    return value;
                } else if (valueClass.equals(URI.class)) {
                    try {
                        return new URI(value);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }

        }

        return null;
    }

    private void buildCacheForResource(URI resource) {
        buildCacheForResources(ImmutableSet.of(resource));
    }

    private void buildCacheForResources(Set<URI> resources) {

        Model describeModel = getDescribeModel(resources);
        for (URI resource : resources) {
            if (!propertyValueCache.containsKey(resource)) {
                propertyValueCache.put(resource, Maps.<URI, String>newHashMap());
            }

        }

        if (describeModel != null) {
            StmtIterator statements = describeModel.listStatements();
            for (Statement statement : statements.toList()) {

                String subject = statement.getSubject().getURI();
                String predicate = statement.getPredicate().getURI();
                if (subject != null && predicate != null) {
                    URI subjectUri = URI.create(subject);
                    URI predicateUri = URI.create(predicate);
                    String object = null;
                    if (statement.getObject().isLiteral()) {
                        object = statement.getObject().asLiteral().getString();
                    }
                    if (statement.getObject().isResource()) {
                        object = statement.getObject().asResource().getURI();
                    }
                    if (object != null) {
                        if (!propertyValueCache.containsKey(subjectUri)) {
                            propertyValueCache.put(subjectUri, Maps.<URI, String>newHashMap());
                        }
                        propertyValueCache.get(subjectUri).put(predicateUri, object);
                    }
                }
            }
        }

    }

    private Model getDescribeModel(Set<URI> resources) {

        Model describeModel = null;

        if (!resources.isEmpty()) {
            int i = 0;
            StringBuilder queryBuilder = new StringBuilder();
            for (URI resource : resources) {
                if (resource != null) {
                    if (i == 0) {
                        queryBuilder = new StringBuilder();
                        queryBuilder.append("DESCRIBE ");
                    }
                    queryBuilder.append("<").append(resource).append("> ");
                    i++;
                    if (i == 1000) {
                        i = 0;
                        Query query = QueryFactory.create(queryBuilder.toString());
                        logger.debug("Executing SPARQL query: {}", query);
                        QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint.toString(), query);

                        if (describeModel == null) {
                            describeModel = qexec.execDescribe();
                        } else {
                            describeModel.add(qexec.execDescribe());
                        }
                        qexec.close();
                    }
                }
            }
            if (i > 0) {
                Query query = QueryFactory.create(queryBuilder.toString());
                logger.debug("Executing SPARQL query: {}", query);
                QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint.toString(), query);

                if (describeModel == null) {
                    describeModel = qexec.execDescribe();
                } else {
                    describeModel.add(qexec.execDescribe());
                }
                qexec.close();
            }

        }
        return describeModel;
    }

    @Override
    public Map<URI, Object> getPropertyValueOfResources(Set<URI> resources, URI property, Class valueClass) {
        Set<URI> nonCachedResources = Sets.newHashSet();
        for (URI resource : resources) {
            if (!propertyValueCache.containsKey(resource)) {
                nonCachedResources.add(resource);
            }
        }
        buildCacheForResources(nonCachedResources);
        Map<URI, Object> result = Maps.newHashMap();
        for (URI resource : resources) {
            result.put(resource, getPropertyValue(resource, property, valueClass));
        }
        return result;
    }

    @Override
    public Map<URI, Object> getPropertyValuesOfResource(URI resource, Map<URI, Class> propertyValueClassMap) {
        if (!propertyValueCache.containsKey(resource)) {
            buildCacheForResource(resource);
        }
        Map<URI, Object> result = Maps.newHashMap();
        for (URI property : propertyValueClassMap.keySet()) {
            result.put(resource, getPropertyValue(resource, property, propertyValueClassMap.get(property)));
        }
        return result;
    }

    @Override
    public Map<URI, Object> getAllPropertyValuesOfResource(URI resource) {
        if (!propertyValueCache.containsKey(resource)) {
            buildCacheForResource(resource);
        }
        Map<URI, Object> result = Maps.newHashMap();
        for (URI property : propertyValueCache.get(resource).keySet()) {
            result.put(resource, propertyValueCache.get(resource).get(property));
        }
        return result;

    }

    @Override
    public Map<URI, Map<URI, Object>> getPropertyValuesOfResources(Set<URI> resources, Map<URI, Class> propertyValueClassMap) {
        buildCacheForResources(resources);
        Map<URI, Map<URI, Object>> result = Maps.newHashMap();
        for (URI resource : resources) {
            result.put(resource, Maps.<URI, Object>newHashMap());
            for (URI property : propertyValueClassMap.keySet()) {
                result.get(resource).put(property, getPropertyValue(resource, property, propertyValueClassMap.get(property)));
            }
        }
        return result;
    }

    @Override
    public void updatePropertyValue(URI resource, URI property, Object value) {

    }

    @Override
    public void updatePropertyValuesOfResource(Map<URI, Object> propertyValueMap, URI resource) {

    }

    @Override
    public void updatePropertyValueOfResources(Map<URI, Object> resourceValueMap, URI property) {

    }

    @Override
    public void deletePropertyValue(URI resource, URI property) {

    }

    @Override
    public void deletePropertyValueOfResources(Set<URI> resource, URI property) {

    }

    @Override
    public void deletePropertyValuesOfResource(URI resource, Set<URI> properties) {

    }

    @Override
    public void deleteAllPropertyValuesOfResource(URI resource) {

    }

    @Override
    public void deleteAllValuesOfProperty(URI property) {

    }

    @Override
    public void clearPropertyValues() {

    }
}
