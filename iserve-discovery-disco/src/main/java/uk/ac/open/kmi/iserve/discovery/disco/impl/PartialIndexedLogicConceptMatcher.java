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


import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.api.MatchTypes;
import uk.ac.open.kmi.iserve.discovery.api.MultiMatcher;
import uk.ac.open.kmi.iserve.discovery.api.impl.EnumMatchTypes;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.manager.IntegratedComponent;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;

import javax.inject.Named;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class PartialIndexedLogicConceptMatcher extends IntegratedComponent implements MultiMatcher {


    private static final Logger log = LoggerFactory.getLogger(PartialIndexedLogicConceptMatcher.class);

    private static final MatchTypes<MatchType> matchTypes = EnumMatchTypes.of(LogicConceptMatchType.class);

    private Table<URI, URI, MatchResult> indexedMatches;
    private ServiceManager serviceManager;
    private MultiMatcher sparqlMatcher;

    @Inject
    public PartialIndexedLogicConceptMatcher(EventBus eventBus,
                                             @Named("iserve.url") String iServeUri,
                                             SparqlLogicConceptMatcher sparqlMatcher,
                                             ServiceManager serviceManager) throws SalException {

        super(eventBus, iServeUri);

        this.serviceManager = serviceManager;
        this.sparqlMatcher = sparqlMatcher;
        this.indexedMatches = populate();
    }


    private Table<URI, URI, MatchResult> populate() {
        Table<URI, URI, MatchResult> indexedMatches = HashBasedTable.create();
        Set<URI> serviceInputs = findAllInputs();
        Set<URI> serviceOutputs = findAllOutputs();
        int totalOutputs = serviceOutputs.size();
        int counter = 0;
        for (URI output : serviceOutputs) {
            counter++;
            for (URI input : serviceInputs) {
                MatchResult result = sparqlMatcher.match(output, input);
                indexedMatches.put(output, input, result);
            }
            log.info(output + " indexed (" + counter + "/" + totalOutputs + ")");
        }
        log.info("Index built");
        return indexedMatches;
    }

    private Set<URI> findAllInputs() {
        Set<URI> models = new HashSet<URI>();
        for (URI service : serviceManager.listServices()) {
            for (URI op : serviceManager.listOperations(service)) {
                for (URI input : serviceManager.listInputs(op)) {
                    models.addAll(getAllModels(input));
                }
            }
        }
        return models;
    }

    private Set<URI> findAllOutputs() {
        Set<URI> models = new HashSet<URI>();
        for (URI service : serviceManager.listServices()) {
            for (URI op : serviceManager.listOperations(service)) {
                for (URI output : serviceManager.listOutputs(op)) {
                    models.addAll(getAllModels(output));
                }
            }
        }
        return models;
    }

    private Set<URI> getAllModels(URI messageContent) {
        Set<URI> models = new HashSet<URI>();
        for (URI mandatoryPart : serviceManager.listMandatoryParts(messageContent)) {
            models.addAll(serviceManager.listModelReferences(mandatoryPart));
        }
        return models;
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
    public MatchResult match(URI origin, URI destination) {
        MatchResult result = this.indexedMatches.get(origin, destination);
        // If both concepts are not indexed, then call the delegated matcher

        if (result == null) {
            // Delegate
            result = this.sparqlMatcher.match(origin, destination);
        }

        return result;
    }

    // TODO; Delegate!

    @Override
    public Table<URI, URI, MatchResult> match(Set<URI> origins, Set<URI> destinations) {
        return this.sparqlMatcher.match(origins, destinations);
    }

}
