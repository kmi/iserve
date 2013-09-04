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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PartialIndexedLogicConceptMatcher extends AbstractLogicConceptMatcher {
    private static final Logger log = LoggerFactory.getLogger(PartialIndexedLogicConceptMatcher.class);
    private Table<URI, URI, MatchResult> indexedMatches;
    private ServiceManager manager;
    private AbstractLogicConceptMatcher matcher;

    public PartialIndexedLogicConceptMatcher(ServiceManager manager, AbstractLogicConceptMatcher matcher) {
        this.manager = manager;
        this.matcher = matcher;
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
                MatchResult result = matcher.match(output, input);
                indexedMatches.put(output, input, result);
            }
            log.info(output + " indexed (" + counter + "/" + totalOutputs + ")");
        }
        log.info("Index built");
        return indexedMatches;
    }

    private Set<URI> findAllInputs() {
        Set<URI> models = new HashSet<URI>();
        for (URI service : manager.listServices()) {
            for (URI op : manager.listOperations(service)) {
                for (URI input : manager.listInputs(op)) {
                    models.addAll(getAllModels(input));
                }
            }
        }
        return models;
    }

    private Set<URI> findAllOutputs() {
        Set<URI> models = new HashSet<URI>();
        for (URI service : manager.listServices()) {
            for (URI op : manager.listOperations(service)) {
                for (URI output : manager.listOutputs(op)) {
                    models.addAll(getAllModels(output));
                }
            }
        }
        return models;
    }

    private Set<URI> getAllModels(URI messageContent) {
        Set<URI> models = new HashSet<URI>();
        for (URI mandatoryPart : manager.listMandatoryParts(messageContent)) {
            models.addAll(manager.listModelReferences(mandatoryPart));
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

    @Override
    public MatchResult match(URI origin, URI destination) {
        MatchResult result = this.indexedMatches.get(origin, destination);
        // If both concepts are not indexed, then call the delegated matcher

        if (result == null) {
            // Delegate
            result = this.matcher.match(origin, destination);
        }

        return result;
    }

    @Override
    public Table<URI, URI, MatchResult> match(Set<URI> origin, Set<URI> destination) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<URI, MatchResult> listMatchesOfType(URI origin, MatchType type) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Obtains all the matching resources that have a precise MatchType with the URIs of {@code origin}.
     *
     * @param origins URIs to match
     * @param type    the MatchType we want to obtain
     * @return a {@link com.google.common.collect.Table} with the result of the matching indexed by origin URI and then destination URI.
     */
    @Override
    public Table<URI, URI, MatchResult> listMatchesOfType(Set<URI> origins, MatchType type) {
        return null;  // TODO: implement
    }

    @Override
    public Table<URI, URI, MatchResult> listMatchesOfType(Set<URI> origins, MatchType type) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<URI, MatchResult> listMatchesAtLeastOfType(URI origin, MatchType minType) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<URI, MatchResult> listMatchesAtMostOfType(URI origin, MatchType maxType) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Obtain all the matching resources that have a MatchTyoe with the URIs of {@code origin} of the type provided (inclusive) or less.
     *
     * @param origins URIs to match
     * @param maxType the maximum MatchType we want to obtain
     * @return a {@link com.google.common.collect.Table} with the result of the matching indexed by origin URI and then destination URI.
     */
    @Override
    public Table<URI, URI, MatchResult> listMatchesAtMostOfType(Set<URI> origins, MatchType maxType) {
        return null;  // TODO: implement
    }

    @Override
    public Table<URI, URI, MatchResult> listMatchesAtMostOfType(Set<URI> origins, MatchType maxType) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<URI, MatchResult> listMatchesWithinRange(URI origin, MatchType minType, MatchType maxType) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Obtain all the matching resources with the URIs of {@code origin} within the range of MatchTypes provided, both inclusive.
     *
     * @param origins URIs to match
     * @param minType the minimum MatchType we want to obtain
     * @param maxType the maximum MatchType we want to obtain
     * @return a {@link com.google.common.collect.Table} with the result of the matching indexed by origin URI and then destination URI.
     */
    @Override
    public Table<URI, URI, MatchResult> listMatchesWithinRange(Set<URI> origins, MatchType minType, MatchType maxType) {
        return null;  // TODO: implement
    }

    @Override
    public Table<URI, URI, MatchResult> listMatchesWithinRange(Set<URI> origins, MatchType minType, MatchType maxType) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Table<URI, URI, MatchResult> listMatchesAtLeastOfType(Set<URI> origins, MatchType minType) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
