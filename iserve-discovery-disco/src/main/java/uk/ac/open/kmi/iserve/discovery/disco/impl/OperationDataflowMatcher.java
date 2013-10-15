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

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.DataflowMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.api.impl.AbstractMatcher;
import uk.ac.open.kmi.iserve.discovery.api.impl.EnumMatchTypes;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.discovery.util.MatchComparator;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import static uk.ac.open.kmi.iserve.discovery.disco.MatchResultsMerger.INTERSECTION;

/**
 * OperationDataflowMatcher implementation of DataflowMatching for Operations
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 27/09/2013
 */
public class OperationDataflowMatcher extends AbstractMatcher implements DataflowMatcher {


    private static final Logger log = LoggerFactory.getLogger(OperationDataflowMatcher.class);
    private final ServiceManager serviceManager;
    private final ConceptMatcher conceptMatcher;

    @Inject
    protected OperationDataflowMatcher(ServiceManager serviceManager, ConceptMatcher conceptMatcher) throws SalException {

        super(EnumMatchTypes.of(LogicConceptMatchType.class));

        this.serviceManager = serviceManager;
        this.conceptMatcher = conceptMatcher;
    }

    /**
     * Obtains a brief textual description of the particular Matcher to be presented to the user.
     *
     * @return the textual description
     */
    @Override
    public String getMatcherDescription() {
        return null;  // TODO: implement
    }

    /**
     * Obtains the particular version of the matcher being used.
     *
     * @return the version
     */
    @Override
    public String getMatcherVersion() {
        return null;  // TODO: implement
    }

    /**
     * Perform a match between two URIs (from {@code origin} to {@code destination})
     * and returns the result.
     *
     * @param origin      URI of the element to match
     * @param destination URI of the element to match against
     * @return {@link uk.ac.open.kmi.iserve.discovery.api.MatchResult} with the result of the matching.
     */
    @Override
    public MatchResult match(URI origin, URI destination) {
        Set<URI> originOutputs = this.serviceManager.listOutputs(origin);
        Set<URI> destinationInputs = this.serviceManager.listInputs(destination);

        // Obtain all combinations of matches
        Table<URI, URI, MatchResult> matches = this.conceptMatcher.match(originOutputs, destinationInputs);
        // Collect the best match results for each of the destination's inputs
        // We should try and maximise the fulfillment of destination's inputs rather than be guided by the origin outputs
        ImmutableSet.Builder<MatchResult> builder = ImmutableSet.builder();

        for (Map.Entry<URI, Map<URI, MatchResult>> entry : matches.columnMap().entrySet()) {
            Map<URI, MatchResult> matchesMap = entry.getValue();
            Ordering<URI> valueComparator = Ordering.from(MatchComparator.BY_TYPE)
                    .onResultOf(Functions.forMap(matchesMap))  // Order by value
                    .compound(Ordering.natural());  // Order by URI eventually

            URI bestMatchUri = valueComparator.max(matchesMap.keySet());
            log.info("The best match for {} is {}, with a Match Result of {}",
                    entry.getKey(), bestMatchUri, matchesMap.get(bestMatchUri).getMatchType());

            builder.add(matchesMap.get(bestMatchUri));
        }

        MatchResult result = INTERSECTION.apply(builder.build());
        log.info("Combined match result - {}", result);
        return result;
    }

    /**
     * Obtain all the matching resources with the URI of {@code origin} within the range of MatchTypes provided, both inclusive.
     *
     * @param origin  URI to match
     * @param minType the minimum MatchType we want to obtain
     * @param maxType the maximum MatchType we want to obtain
     * @return a Map containing indexed by the URI of the matching resource and containing the particular {@code MatchResult}. If no
     *         result is found the Map should be empty not null.
     */
    @Override
    public Map<URI, MatchResult> listMatchesWithinRange(URI origin, MatchType minType, MatchType maxType) {
        return ImmutableMap.of();  // TODO: implement
    }
}
