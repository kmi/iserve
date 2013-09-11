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


import com.google.common.collect.Table;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.api.MatchTypes;
import uk.ac.open.kmi.iserve.discovery.api.Matcher;
import uk.ac.open.kmi.iserve.discovery.api.impl.EnumMatchTypes;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.sal.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.events.OntologyCreatedEvent;
import uk.ac.open.kmi.iserve.sal.events.OntologyDeletedEvent;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.manager.IntegratedComponent;
import uk.ac.open.kmi.iserve.sal.manager.KnowledgeBaseManager;

import javax.inject.Named;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Rudimentary implementation of an in-memory index for logic matching.
 * We are assuming here that the ontologies are in a remote server exposed through a SPARQL endpoint. If we can sit in
 * the same server we should try and use the libraries from the store directly to avoid replicating the index.
 */
public class SparqlIndexedLogicConceptMatcher extends IntegratedComponent implements Matcher {

    private static final Logger log = LoggerFactory.getLogger(SparqlIndexedLogicConceptMatcher.class);

    private static final MatchTypes<MatchType> matchTypes = EnumMatchTypes.of(LogicConceptMatchType.class);

    private Table<URI, URI, MatchResult> indexedMatches;
    private KnowledgeBaseManager kbManager;
    private SparqlLogicConceptMatcher sparqlMatcher;

    @Inject
    public SparqlIndexedLogicConceptMatcher(EventBus eventBus,
                                            @Named(SystemConfiguration.ISERVE_URL_PROP) String iServeUri,
                                            KnowledgeBaseManager kbManager,
                                            SparqlLogicConceptMatcher sparqlMatcher) throws SalException {

        super(eventBus, iServeUri);

        this.sparqlMatcher = sparqlMatcher;
        this.kbManager = kbManager;
        this.indexedMatches = populate();
    }

    private Table<URI, URI, MatchResult> populate() {
        Set<URI> classes = new HashSet<URI>(this.kbManager.listConcepts(null));
        return sparqlMatcher.listMatchesAtLeastOfType(classes, LogicConceptMatchType.Plugin);
    }

    @Override
    public String getMatcherDescription() {
        return "Indexed Logic Concept Matcher";
    }

    @Override
    public String getMatcherVersion() {
        return "1.0";
    }

    /**
     * Obtains the MatchTypes instance that contains the MatchTypes supported as well as their ordering information
     *
     * @return
     */
    @Override
    public MatchTypes<MatchType> getMatchTypesSupported() {
        return this.matchTypes;
    }

    @Override
    public MatchResult match(final URI origin, final URI destination) {
        MatchResult result = this.indexedMatches.get(origin, destination);
        // If there are no entries for origin,dest assume fail.
        if (result == null) {
            result = new MatchResult() {
                @Override
                public URI getResourceToMatch() {
                    return origin;
                }

                @Override
                public URI getMatchedResource() {
                    return destination;
                }

                @Override
                public MatchType getMatchType() {
                    return LogicConceptMatchType.Fail;
                }

                @Override
                public Matcher getMatcher() {
                    return SparqlIndexedLogicConceptMatcher.this;
                }

                @Override
                public String getExplanation() {
                    return origin + "-[" + getMatchType().name() + "]->" + destination;
                }
            };
        }
        return result;
    }

    // Process events to update the indexes

    /**
     * A new ontology has been uploaded to the server and we need to update the indexes
     *
     * @param event the actual event that was triggered
     */
    @Subscribe
    public void handleOntologyCreated(OntologyCreatedEvent event) {

        // Obtain the concepts in the ontology uploaded
        List<URI> conceptUris = this.kbManager.listConcepts(event.getOntologyUri());

        // For each of them update their entries in the index (matched concepts will be updated later within the loop)
//        this.sparqlMatcher.listMatchesAtLeastOfType(conceptUris, LogicConceptMatchType.Plugin);

    }

    @Subscribe
    public void handleOntologyDeleted(OntologyDeletedEvent event) {


    }

}
