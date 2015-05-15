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

package uk.ac.open.kmi.iserve.discovery.disco.impl;


import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.ConfigurationProperty;
import uk.ac.open.kmi.iserve.core.iServeProperty;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.api.impl.AbstractMatcher;
import uk.ac.open.kmi.iserve.discovery.api.impl.AtomicMatchResult;
import uk.ac.open.kmi.iserve.discovery.api.impl.EnumMatchTypes;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.discovery.util.MatchResultPredicates;
import uk.ac.open.kmi.iserve.sal.events.OntologyCreatedEvent;
import uk.ac.open.kmi.iserve.sal.events.OntologyDeletedEvent;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.manager.RegistryManager;
import uk.ac.open.kmi.iserve.sal.util.caching.Cache;
import uk.ac.open.kmi.iserve.sal.util.caching.CacheException;
import uk.ac.open.kmi.iserve.sal.util.caching.CacheFactory;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Rudimentary implementation of an in-memory index for logic matching.
 * We are assuming here that the ontologies are in a remote server exposed through a SPARQL endpoint. If we can sit in
 * the same server we should try and use the libraries from the store directly to avoid replicating the index.
 */
public class SparqlIndexedLogicConceptMatcher extends AbstractMatcher implements ConceptMatcher {

    private static final Logger log = LoggerFactory.getLogger(SparqlIndexedLogicConceptMatcher.class);
    private final RegistryManager manager;
    private final SparqlLogicConceptMatcher sparqlMatcher;
    private transient Cache<URI, ConcurrentMap<URI, MatchResult>> indexedMatches;

    @Inject
    protected SparqlIndexedLogicConceptMatcher(RegistryManager registryManager, @iServeProperty(ConfigurationProperty.SERVICES_SPARQL_QUERY) String sparqlEndpoint, CacheFactory cacheFactory) throws SalException, URISyntaxException {

        super(EnumMatchTypes.of(LogicConceptMatchType.class));

        this.sparqlMatcher = new SparqlLogicConceptMatcher(sparqlEndpoint);
        this.manager = registryManager;
        this.manager.registerAsObserver(this);
        if (indexedMatches == null) {
            try {
                this.indexedMatches = cacheFactory.createPersistentCache("concept-matcher-index");
            } catch (CacheException e) {
                this.indexedMatches = cacheFactory.createInMemoryCache("concept-matcher-index");
            }
        }
        if (indexedMatches.isEmpty()) {
            log.info("Populating Matcher Index...");// if index is empty
            Stopwatch w = new Stopwatch().start();
            populate();
            log.info("Population done in {}. Number of entries {}", w.stop().toString(), indexedMatches.size());
        }

    }

    private synchronized void populate() {
        Set<URI> classes = new HashSet<URI>(this.manager.getKnowledgeBaseManager().listConcepts(null));
        Map<URI, Map<URI, MatchResult>> matchesTable = sparqlMatcher.listMatchesAtLeastOfType(classes, LogicConceptMatchType.Subsume).rowMap();
        for (URI c : classes) {
            if (matchesTable.get(c) != null) {
                indexedMatches.put(c, new ConcurrentHashMap(matchesTable.get(c)));
            }
        }
    }

    @Override
    public String getMatcherDescription() {
        return "Indexed Logic Concept Matcher";
    }

    @Override
    public String getMatcherVersion() {
        return "1.0";
    }

    @Override
    public synchronized MatchResult match(final URI origin, final URI destination) {
        MatchResult result = this.indexedMatches.get(origin).get(destination);
        // If there are no entries for origin,dest assume fail.
        if (result == null) {
            result = new AtomicMatchResult(origin, destination, LogicConceptMatchType.Fail, this);
        }
        return result;
    }


//    /**
//     * Perform a match between two Sets of URIs (from {@code origin} to {@code destination})
//     * and returns the result.
//     *
//     * @param origins      Set of URIs of the elements to match
//     * @param destinations Set of URIs of the elements to match against
//     * @return a {@link com.google.common.collect.Table} with the result of the matching indexed by origin URI and then destination URI.
//     */
//    @Override
//    public Table<URI, URI, MatchResult> match(Set<URI> origins, Set<URI> destinations) {
//        ImmutableTable.Builder<URI, URI, MatchResult> builder = ImmutableTable.builder();
//        for (URI origin : origins) {
//            for (URI destination : destinations) {
//                MatchResult mr = this.indexedMatches.get(origin, destination);
//                if (mr == null) {
//                    mr = new AtomicMatchResult(origin, destination, LogicConceptMatchType.Fail, this);
//                }
//
//                builder.put(origin, destination, mr);
//            }
//        }
//        return builder.build();
//    }

    /**
     * Obtain all the matching resources with the URI of {@code origin} within the range of MatchTypes provided, both inclusive.
     *
     * @param origin  URI to match
     * @param minType the minimum MatchType we want to obtain
     * @param maxType the maximum MatchType we want to obtain
     * @return a Map containing indexed by the URI of the matching resource and containing the particular {@code MatchResult}. If no
     * result is found the Map should be empty not null.
     */
    @Override
    public synchronized Map<URI, MatchResult> listMatchesWithinRange(URI origin, MatchType minType, MatchType maxType) {

        if (origin == null || minType == null | maxType == null) {
            return ImmutableMap.of();
        }

        Function<MatchResult, MatchType> getTypeFunction = new Function<MatchResult, MatchType>() {
            @Override
            public MatchType apply(@Nullable MatchResult matchResult) {
                if (matchResult != null) {
                    return matchResult.getMatchType();
                }
                return null;
            }
        };

        // Get all the matches
        Map<URI, MatchResult> matches = this.indexedMatches.get(origin);
        // Return an immutable map out of the filtered view. Drop copyOf to obtain a live view
        if (matches != null) {
            return ImmutableMap.copyOf(Maps.filterValues(matches, MatchResultPredicates.withinRange(minType, BoundType.CLOSED, maxType, BoundType.CLOSED)));
        } else {
            return ImmutableMap.of();
        }

    }

    // Process events to update the indexes

    /**
     * A new ontology has been uploaded to the server and we need to update the indexes
     *
     * @param event the actual event that was triggered
     */
    @Subscribe
    @AllowConcurrentEvents
    public synchronized void handleOntologyCreated(OntologyCreatedEvent event) {

        log.info("Processing Ontology Created Event - {}", event.getOntologyUri());

        // Obtain the concepts in the ontology uploaded
        Set<URI> conceptUris = this.manager.getKnowledgeBaseManager().listConcepts(event.getOntologyUri());
        log.info("Fetching matches for all the {} concepts present in the ontology - {}", conceptUris.size(), event.getOntologyUri());

        // For each of them update their entries in the index (matched concepts will be updated later within the loop)
        for (URI c : conceptUris) {
            indexedMatches.put(c, new ConcurrentHashMap<URI, MatchResult>(sparqlMatcher.listMatchesAtLeastOfType(c, LogicConceptMatchType.Subsume)));
        }
    }

    /**
     * An ontology has been deleted from the KB and we should thus update the indexes. By the time we want to process
     * this event we won't know any longer which concepts need to be removed unless we keep this somewhere.
     *
     * @param event the ontology deletion event
     */
    @Subscribe
    @AllowConcurrentEvents
    public synchronized void handleOntologyDeleted(OntologyDeletedEvent event) {

        log.info("Processing Ontology Deleted Event - {}", event.getOntologyUri());

        // Obtain all concepts in the KB
        Set<URI> conceptUris = this.manager.getKnowledgeBaseManager().listConcepts(null);

        // Cross reference them with those for which we have entries. The ones unmatched were defined in the previous
        // ontology (note that tse of namespaces could be abused and therefore may not be a guarantee).
        Set<URI> difference = Sets.difference(conceptUris, this.indexedMatches.keySet());

        for (URI conceptUri : difference) {
            this.indexedMatches.remove(conceptUri);
        }

        for (URI indexedConcept : indexedMatches.keySet()) {
            Map<URI, MatchResult> matchResultMap = indexedMatches.get(indexedConcept);
            for (URI conceptUri : difference) {
                matchResultMap.remove(conceptUri);
            }
            indexedMatches.put(indexedConcept, new ConcurrentHashMap<URI, MatchResult>(matchResultMap));
        }

    }

}
