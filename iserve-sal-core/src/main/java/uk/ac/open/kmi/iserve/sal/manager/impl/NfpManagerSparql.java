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

import com.google.common.collect.*;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.ConfigurationProperty;
import uk.ac.open.kmi.iserve.core.iServeProperty;
import uk.ac.open.kmi.iserve.sal.events.Event;
import uk.ac.open.kmi.iserve.sal.events.OntologyCreatedEvent;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.manager.IntegratedComponent;
import uk.ac.open.kmi.iserve.sal.manager.NfpManager;
import uk.ac.open.kmi.iserve.sal.manager.SparqlGraphStoreManager;
import uk.ac.open.kmi.iserve.sal.util.caching.Cache;
import uk.ac.open.kmi.iserve.sal.util.caching.CacheFactory;
import uk.ac.open.kmi.msm4j.io.util.URIUtil;
import uk.ac.open.kmi.msm4j.vocabulary.SAWSDL;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Luca Panziera on 27/05/2014.
 */
@Singleton
public class NfpManagerSparql extends IntegratedComponent implements NfpManager {

    private static Cache<String, ConcurrentMap<String, Set<String>>> propertyValueCache;
    private Logger logger = LoggerFactory.getLogger(NfpManagerSparql.class);
    private String sparqlEndpoint;
    private SparqlGraphStoreManager graphStoreManager;

    private static final URI MSM_NFP_URI = URI.create("http://iserve.kmi.open.ac.uk/ns/msm-nfp");
    private static final URI SIOC_URI = URI.create("http://rdfs.org/sioc/ns");
    private static final URI FOAF_0_1_URI = URI.create("http://xmlns.com/foaf/0.1/");
    private static final URI CONTENT_VOCAB_URI = URI.create("http://www.w3.org/2011/content");
    private static final URI DCTERMS_URI = URI.create("http://purl.org/dc/terms/");

    @Inject
    public NfpManagerSparql(EventBus eventBus,
                            @iServeProperty(ConfigurationProperty.ISERVE_URL) String iServeUri,
                            @iServeProperty(ConfigurationProperty.NFP_SPARQL_QUERY) String sparqlQueryEndpoint,
                            @iServeProperty(ConfigurationProperty.NFP_SPARQL_UPDATE) String sparqlUpdateEndpoint,
                            @iServeProperty(ConfigurationProperty.NFP_SPARQL_SERVICE) String sparqlServiceEndpoint,
                            CacheFactory cacheFactory,
                            SparqlGraphStoreFactory sparqlGraphStoreFactory) throws SalException {
        super(eventBus, iServeUri);
        this.sparqlEndpoint = sparqlQueryEndpoint;
        propertyValueCache = cacheFactory.createInMemoryCache("nfp");

        Set<URI> defaultModelsToLoad = ImmutableSet.of(MSM_NFP_URI, SIOC_URI, FOAF_0_1_URI, CONTENT_VOCAB_URI,
                DCTERMS_URI);

        // Configuration for quick retrieval of ontologies by resolving them to local files.
        ImmutableMap.Builder<String, String> mappingsBuilder = ImmutableMap.builder();
        mappingsBuilder.put(FOAF_0_1_URI.toASCIIString(), this.getClass().getResource("/foaf-2010-08-09.ttl").toString());
        mappingsBuilder.put(CONTENT_VOCAB_URI.toASCIIString(), this.getClass().getResource("/content-2011-04-29.ttl").toString());
        mappingsBuilder.put(DCTERMS_URI.toASCIIString(), this.getClass().getResource("/dcterms-2012-06-14.ttl").toString());
        mappingsBuilder.put(MSM_NFP_URI.toASCIIString(), this.getClass().getResource("/msm-nfp-2015-10-01.ttl").toString());
        mappingsBuilder.put(SIOC_URI.toASCIIString(), this.getClass().getResource("/sioc-2010-03-25.ttl").toString());

        // Configuration for avoiding the import of certain files
        ImmutableSet<String> ignoredImports = ImmutableSet.of();

        this.graphStoreManager = sparqlGraphStoreFactory.create(sparqlQueryEndpoint, sparqlUpdateEndpoint,
                sparqlServiceEndpoint, defaultModelsToLoad, mappingsBuilder.build(), ignoredImports);
        logger.debug("Created NfpManagerSparql");
    }


    @Override
    public void shutdown() {

    }

    @Override
    public void createPropertyValue(URI resource, URI property, Object value) throws SalException {
        OntModel model = ModelFactory.createOntologyModel();
        RDFNode object;
        if (value instanceof URI) {
            object = model.createResource(((URI) value).toASCIIString());
            if (property.toASCIIString().equals(SAWSDL.modelReference.getURI())) {
                URI modelUri;
                try {
                    modelUri = URIUtil.getNameSpace((URI) value);
                    if (!graphStoreManager.containsGraph(modelUri)) {
                        if (graphStoreManager.fetchAndStore(modelUri)) {
                            getEventBus().post(new OntologyCreatedEvent(new Date(), modelUri));
                        }
                    }
                } catch (URISyntaxException e) {
                    logger.warn("Ignoring modelReference URI. URI syntax exception: {}", value );
                    e.printStackTrace();
                }
            }
        } else {
            object = model.createTypedLiteral(value);
        }

        Statement triple = model.createStatement(model.createResource(resource.toASCIIString()), model.createProperty(property.toASCIIString()), object);
        model.add(triple);

        URI graphUri = graphStoreManager.getGraphUriByResource(resource);
        if (graphUri == null) {
            throw new SalException("The requested resource does not exist");
        }
        graphStoreManager.addModelToGraph(graphUri, model);
        propertyValueCache.remove(resource.toASCIIString());
    }

    /**
     * Add a set of Subject, Property, Object entries to the given graph.
     * This provides a convenience method to create a single SPARQL Update request when many values
     * need to be added to a graph/service description.
     *
     * @param graphUri              URI of the graph to be updated. This typically corresponds to a Service Uri
     * @param subjectPropertyValues A table providing the set of Subject-Property-Object values to add.
     * @throws uk.ac.open.kmi.iserve.sal.exception.SalException Exception triggered if there where issues saving these results
     */
    @Override
    public void createPropertyValues(URI graphUri, Table<URI, URI, Object> subjectPropertyValues) throws SalException {

        // Validate the input
        if (graphUri == null || !graphStoreManager.containsGraph(graphUri)) {
            // The graph cannot be updated
            throw new SalException("The graph to update does not exist: " + graphUri.toASCIIString());
        }

        if (subjectPropertyValues == null || subjectPropertyValues.isEmpty()) {
            // Nothing to update
            return;
        }

        OntModel model = ModelFactory.createOntologyModel();

        URI subject = null;
        URI property;
        Object value;
        RDFNode object;
        Statement triple;

        // Loop over every subject-property-object entry
        for (Map.Entry<URI, Map<URI, Object>> spoEntry : subjectPropertyValues.rowMap().entrySet()) {
            subject = spoEntry.getKey();
            // Loop over every property-object pair
            for (Map.Entry<URI, Object> poEntry : spoEntry.getValue().entrySet()) {
                property = poEntry.getKey();
                value = poEntry.getValue();

                // Create the proper object for the update
                if (value instanceof URI) {
                    object = model.createResource(((URI) value).toASCIIString());
                    if (property.toASCIIString().equals(SAWSDL.modelReference.getURI())) {
                        URI modelUri;
                        try {
                            modelUri = URIUtil.getNameSpace((URI) value);
                            if (!graphStoreManager.containsGraph(modelUri)) {
                                if (graphStoreManager.fetchAndStore(modelUri)) {
                                    getEventBus().post(new OntologyCreatedEvent(new Date(), modelUri));
                                }
                            }
                        } catch (URISyntaxException e) {
                            logger.warn("Ignoring modelReference URI. URI syntax exception: {}", value );
                            e.printStackTrace();
                        }
                    }
                } else {
                    object = model.createTypedLiteral(value);
                }

                // Create the statement
                triple = model.createStatement(model.createResource(subject.toASCIIString()), model.createProperty(property.toASCIIString()), object);
                model.add(triple);
            }
        }

        // All spo have been added to the model now. Update the graph
        graphStoreManager.addModelToGraph(graphUri, model);
        if (subject != null) {
            propertyValueCache.remove(subject.toASCIIString());
        }
    }

    @Override
    public void createPropertyValuesOfResource(Map<URI, Object> propertyValueMap, URI resource) throws SalException {
        for (URI property : propertyValueMap.keySet()) {
            createPropertyValue(resource, property, propertyValueMap.get(property));
        }
    }

    @Override
    public void createPropertyValueOfResources(Map<URI, Object> resourceValueMap, URI property) throws SalException {
        for (URI resource : resourceValueMap.keySet()) {
            createPropertyValue(resource, property, resourceValueMap.get(resource));
        }
    }

    @Override
    public Object getPropertyValue(URI resource, URI property, Class valueClass) {
        if (!propertyValueCache.containsKey(resource.toASCIIString())) {
            // put values in cache
            buildCacheForResource(resource);
        }
        Map<String, Set<String>> resourceProperties = propertyValueCache.get(resource.toASCIIString());
        if (resourceProperties != null && resourceProperties.containsKey(property.toASCIIString())) {
            Set<String> values = resourceProperties.get(property.toASCIIString());
            logger.debug("{}, {}, {}", resource, property, values);

            if (values != null) {
                if (valueClass.equals(Double.class)) {
                    if (values.size() == 1) {
                        return new Double(values.iterator().next());
                    } else {
                        Set<Double> result = Sets.newHashSet();
                        for (String value : values) {
                            result.add(new Double(value));
                        }
                        return result;
                    }

                } else if (valueClass.equals(Integer.class)) {
                    if (values.size() == 1) {
                        return new Integer(values.iterator().next());
                    } else {
                        Set<Integer> result = Sets.newHashSet();
                        for (String value : values) {
                            result.add(new Integer(value));
                        }
                        return result;
                    }
                } else if (valueClass.equals(String.class)) {
                    if (values.size() == 1) {
                        return values.iterator().next();
                    } else {
                        return values;
                    }
                } else if (valueClass.equals(URI.class)) {
                    try {
                        if (values.size() == 1) {
                            return new URI(values.iterator().next());
                        } else {
                            Set<URI> result = Sets.newHashSet();
                            for (String value : values) {
                                result.add(new URI(value));
                            }
                            return result;
                        }
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
            if (!propertyValueCache.containsKey(resource.toASCIIString())) {
                propertyValueCache.put(resource.toASCIIString(), new ConcurrentHashMap<String, Set<String>>());
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
                        ConcurrentMap<String, Set<String>> predicateObjectMap;
                        if (!propertyValueCache.containsKey(subjectUri.toASCIIString())) {
                            predicateObjectMap = new ConcurrentHashMap<String, Set<String>>();
                        } else {
                            predicateObjectMap = propertyValueCache.get(subjectUri.toASCIIString());
                        }
                        if (predicateObjectMap.get(predicateUri.toASCIIString()) == null) {
                            predicateObjectMap.put(predicateUri.toASCIIString(), new HashSet<String>());
                        }
                        predicateObjectMap.get(predicateUri.toASCIIString()).add(object);
                        propertyValueCache.put(subjectUri.toASCIIString(), predicateObjectMap);
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
            if (resource != null && !propertyValueCache.containsKey(resource.toASCIIString())) {
                nonCachedResources.add(resource);
            }
        }
        buildCacheForResources(nonCachedResources);
        Map<URI, Object> result = Maps.newHashMap();
        for (URI resource : resources) {
            if (resource != null) {
                result.put(resource, getPropertyValue(resource, property, valueClass));
            }
        }
        return result;
    }

    @Override
    public Map<URI, Object> getPropertyValuesOfResource(URI resource, Map<URI, Class> propertyValueClassMap) {
        if (!propertyValueCache.containsKey(resource.toASCIIString())) {
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
        if (!propertyValueCache.containsKey(resource.toASCIIString())) {
            buildCacheForResource(resource);
        }
        Map<URI, Object> result = Maps.newHashMap();
        for (String property : propertyValueCache.get(resource.toASCIIString()).keySet()) {
            result.put(resource, propertyValueCache.get(resource.toASCIIString()).get(property));
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
    public void deletePropertyValue(URI resource, URI property, Object value) throws SalException {
        OntModel model = ModelFactory.createOntologyModel();
        RDFNode object;
        if (value instanceof URI) {
            object = model.createResource(((URI) value).toASCIIString());
// No need, we are deleting
//            if (property.toASCIIString().equals(SAWSDL.modelReference)) {
//                graphStoreManager.fetchAndStore((URI) value);
//            }
        } else {
            object = model.createTypedLiteral(value);
        }
        Statement triple = model.createStatement(model.createResource(resource.toASCIIString()), model.createProperty(property.toASCIIString()), object);
        model.add(triple);
        URI graphUri = graphStoreManager.getGraphUriByResource(resource);
        if (graphUri == null) {
            throw new SalException("The requested resource does not exist");
        }
        graphStoreManager.deleteStatementFromGraph(triple, graphUri);
        propertyValueCache.remove(resource.toASCIIString());

    }

    @Override
    public void deletePropertyValues(URI resource, URI property) {

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

    @Subscribe
    public void clearCache(Event e) {
        logger.debug("Clear NFP cache");
        propertyValueCache.clear();
    }

}
