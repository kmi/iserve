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

import com.google.common.base.Stopwatch;
import com.google.common.collect.Table;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.api.MatchTypes;
import uk.ac.open.kmi.iserve.discovery.api.impl.AtomicMatchResult;
import uk.ac.open.kmi.iserve.discovery.api.impl.EnumMatchTypes;
import uk.ac.open.kmi.iserve.discovery.disco.DiscoMatchType;
import uk.ac.open.kmi.iserve.discovery.disco.Util;
import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * DiscoMatcher
 * TODO: Provide Description
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 30/07/2013
 */
public class LogicConceptMatcher implements ConceptMatcher {

    private static final Logger log = LoggerFactory.getLogger(LogicConceptMatcher.class);
    private static final String MATCH_VAR = "match";
    private static final String EXACT_VAR = "exact";
    private static final String SUB_VAR = "sub";
    private static final String SUPER_VAR = "super";
    private final URI sparqlEndpoint;
    MatchTypes<MatchType> matchTypes;

    public static String NL = System.getProperty("line.separator");

    public LogicConceptMatcher() {
        matchTypes = EnumMatchTypes.of(DiscoMatchType.class);
        sparqlEndpoint = ManagerSingleton.getInstance().getConfiguration().getDataSparqlUri();
        if (sparqlEndpoint == null) {
            log.error("A SPARQL endpoint is currently needed for matching.");
            // TODO throw exception
        }

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
     * Obtains the MatchTypes instance that contains the MatchTypes supported as well as their ordering information
     *
     * @return
     */
    @Override
    public MatchTypes<MatchType> getMatchTypesSupported() {
        return matchTypes;
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

        MatchType type = DiscoMatchType.Fail;
        if (ManagerSingleton.getInstance().listEquivalentClasses(origin).contains(destination)) {
            type = DiscoMatchType.Exact;
        } else {
            if (ManagerSingleton.getInstance().listSubClasses(origin, false).contains(destination)) {
                type = DiscoMatchType.Plugin;
            } else if (ManagerSingleton.getInstance().listSuperClasses(origin, false).contains(destination)) {
                type = DiscoMatchType.Subsume;
            }
        }

        MatchResult result = new AtomicMatchResult(origin, destination, type, this);
        return result;
    }

    /**
     * Perform a match between two Sets of URIs (from {@code origin} to {@code destination})
     * and returns the result.
     *
     * @param origin      Set of URIs of the elements to match
     * @param destination Set of URIs of the elements to match against
     * @return {@link uk.ac.open.kmi.iserve.discovery.api.MatchResult} with the result of the matching.
     */
    @Override
    public Table<URI, URI, MatchResult> match(Set<URI> origin, Set<URI> destination) {
        return null;  // TODO: implement
    }

    /**
     * Obtains all the matching resources that have a precise MatchType with the URI of {@code origin}.
     *
     * @param origin URI to match
     * @param type   the MatchType we want to obtain
     * @return a Map containing indexed by the URI of the matching resource and containing the particular {@code MatchResult}. If no
     *         result is found the Map should be empty not null.
     */
    @Override
    public Map<URI, MatchResult> listMatchesOfType(URI origin, MatchType type) {
        return null;  // TODO: implement
    }

    /**
     * Obtains all the matching resources that have a MatchType with the URI of {@code origin} of the type provided (inclusive) or more.
     *
     * @param origin  URI to match
     * @param minType the minimum MatchType we want to obtain
     * @return a Map containing indexed by the URI of the matching resource and containing the particular {@code MatchResult}. If no
     *         result is found the Map should be empty not null.
     */
    @Override
    public Map<URI, MatchResult> listMatchesAtLeastOfType(URI origin, MatchType minType) {
        return null;  // TODO: implement
    }

    /**
     * Obtain all the matching resources that have a MatchTyoe with the URI of {@code origin} of the type provided (inclusive) or less.
     *
     * @param origin  URI to match
     * @param maxType the maximum MatchType we want to obtain
     * @return a Map containing indexed by the URI of the matching resource and containing the particular {@code MatchResult}. If no
     *         result is found the Map should be empty not null.
     */
    @Override
    public Map<URI, MatchResult> listMatchesAtMostOfType(URI origin, MatchType maxType) {
        return null;  // TODO: implement
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
        return null;  // TODO: implement
    }

    public Map<URI, MatchResult> obtainAllMatchResults(Set<URI> origins) {
        Map<URI, MatchResult> result = new HashMap<URI, MatchResult>();
        if (origins == null || origins.isEmpty())
            return result;

        // Obtain an array to keep track of the order
        URI[] uris = origins.toArray(new URI[origins.size()]);
        String queryStr = generateAllMatchesQuery(uris);
        log.info("SPARQL Query generated: \n {}", queryStr); // TODO: Change to debug

        // Query the engine
        Query query = QueryFactory.create(queryStr);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(this.sparqlEndpoint.toASCIIString(), query);

        try {
            Stopwatch stopwatch = new Stopwatch().start();
            ResultSet qResults = qexec.execSelect();
            stopwatch.stop();
            log.info("Time taken for querying the registry: {}", stopwatch);

            Resource resource;
            URI matchUri;
            int index = 0;
            // Iterate over the results obtained starting with the matches for class0 onwards
            while (qResults.hasNext()) {
                QuerySolution soln = qResults.nextSolution();
                resource = soln.getResource(MATCH_VAR + index);

                // Get the current matching class, we assume the results come by batches over the same class ordered
                // If this is not the case we should loop every time to see which class matched
                while (resource == null && index < uris.length) {
                    index++;
                    resource = soln.getResource(MATCH_VAR + index);
                }

                if (resource.isURIResource()) {
                    matchUri = new URI(resource.getURI());
                    MatchType type = getMatchType(soln);
                    result.put(matchUri, new AtomicMatchResult(uris[index], matchUri, type, this));
                    log.info("Concept {} was matched to {} with type {}", uris[index], matchUri, type);   // TODO: Change to debug
                } else {
                    log.warn("Skipping result as the URL is null");
                    break;
                }
            }
        } catch (URISyntaxException e) {
            log.error("Error obtaining match result. Expected a correct URI", e);
        } finally {
            qexec.close();
        }
        return result;

    }

    private MatchType getMatchType(QuerySolution soln) {

        if (soln.contains(EXACT_VAR))
            return DiscoMatchType.Exact;

        if (soln.contains(SUB_VAR))
            return DiscoMatchType.Plugin;

        if (soln.contains(SUPER_VAR))
            return DiscoMatchType.Subsume;

        return DiscoMatchType.Fail;

    }

    private String generateAllMatchesQuery(URI[] origins) {
        StringBuffer query = new StringBuffer()
                .append("select * ").append(NL); // Obtain every variable for further details

        query.append("where {" + NL);

        int i = 0;
        List<String> patterns = new ArrayList<String>();
        for (URI origin : origins) {
            // Match the origin concept to strict subclasses
            patterns.add(Util.generateMatchStrictSubclassesPattern(MATCH_VAR + i, origin.toASCIIString(), SUB_VAR));
            // Match the origin concept to strict superclasses
            patterns.add(Util.generateMatchStrictSuperclassesPattern(MATCH_VAR + i, origin.toASCIIString(), SUPER_VAR));
            // Match the origin concept to exact matches
            patterns.add(Util.generateExactMatchPattern(MATCH_VAR + i, origin.toASCIIString(), EXACT_VAR));
            i++;
        }
        query.append(Util.generateUnionStatement(patterns));
        query.append("}");
        return query.toString();
    }
}
