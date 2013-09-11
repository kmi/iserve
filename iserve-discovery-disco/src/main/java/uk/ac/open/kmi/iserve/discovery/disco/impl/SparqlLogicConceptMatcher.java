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
import com.google.common.collect.*;
import com.google.inject.Inject;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.api.MatchTypes;
import uk.ac.open.kmi.iserve.discovery.api.RangedMultiMatcher;
import uk.ac.open.kmi.iserve.discovery.api.impl.AtomicMatchResult;
import uk.ac.open.kmi.iserve.discovery.api.impl.EnumMatchTypes;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.discovery.disco.Util;
import uk.ac.open.kmi.iserve.discovery.util.MatchComparator;
import uk.ac.open.kmi.iserve.sal.SystemConfiguration;

import javax.inject.Named;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * SparqlLogicConceptMatcher implements a Matcher for concepts directly backed by a SPARQL endpoint.
 * The response time of this matcher will be fine for human interactions but too slow for high frequency queries by
 * software such as composition engines. Does not require to maintain its own index.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 30/07/2013
 */
public class SparqlLogicConceptMatcher implements RangedMultiMatcher {

    private static final Logger log = LoggerFactory.getLogger(SparqlLogicConceptMatcher.class);

    private static String NL = System.getProperty("line.separator");

    // Variable names used in queries
    private static final String MATCH_VAR = "match";
    private static final String EXACT_VAR = "exact";
    private static final String SUB_VAR = "sub";
    private static final String SUPER_VAR = "super";
    private static final String ORIGIN_VAR = "origin";
    private static final String DESTINATION_VAR = "destination";

    private static final MatchTypes<MatchType> matchTypes = EnumMatchTypes.of(LogicConceptMatchType.class);

    private URI sparqlEndpoint = null;

    // Function to getMatchResult from a Map
    protected final Function<Map.Entry<URI, MatchResult>, MatchResult> getMatchResult =
            new Function<Map.Entry<URI, MatchResult>, MatchResult>() {
                public MatchResult apply(Map.Entry<URI, MatchResult> entry) {
                    return entry.getValue();
                }
            };

    // Order the results by score and then by url
    protected final Ordering<Map.Entry<URI, MatchResult>> entryOrdering =
            Ordering.from(MatchComparator.BY_TYPE).onResultOf(getMatchResult).reverse().
                    compound(Ordering.from(MatchComparator.BY_URI).onResultOf(getMatchResult));

    @Inject
    public SparqlLogicConceptMatcher(@Named(SystemConfiguration.SERVICES_REPOSITORY_SPARQL_PROP) String sparqlEndpoint) throws URISyntaxException {

        if (sparqlEndpoint == null) {
            log.error("A SPARQL endpoint is currently needed for matching.");
        } else {
            this.sparqlEndpoint = new URI(sparqlEndpoint);
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
        return this.matchTypes;
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

        String queryStr = new StringBuffer()
                .append(generateQueryHeader())
                .append(generateMatchWhereClause(origin, destination, false))
                .append(generateQueryFooter())
                .toString();

        log.debug("SPARQL Query generated: \n {}", queryStr);
        return queryForMatchResult(origin, destination, queryStr);
    }

    private MatchResult queryForMatchResult(URI origin, URI destination, String queryStr) {

        MatchType type = LogicConceptMatchType.Fail;
        // Query the engine
        Query query = QueryFactory.create(queryStr);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(this.sparqlEndpoint.toASCIIString(), query);

        try {
            Stopwatch stopwatch = new Stopwatch().start();
            ResultSet qResults = qexec.execSelect();
            stopwatch.stop();
            log.info("Time taken for querying the registry: {}", stopwatch);

            // Obtain matches if any and figure out the type
            if (qResults.hasNext()) {
                QuerySolution soln = qResults.nextSolution();
                type = getMatchType(soln);
            }
            log.debug("Concept {} was matched to {} with type {}", origin, destination, type);
        } finally {
            qexec.close();
        }
        return new AtomicMatchResult(origin, destination, type, this);
    }

    /**
     * Perform a match between two Sets of URIs (from {@code origin} to {@code destination})
     * and returns the result.
     *
     * @param origins      Set of URIs of the elements to match
     * @param destinations Set of URIs of the elements to match against
     * @return a {@link com.google.common.collect.Table} with the result of the matching indexed by origin URI and then destination URI.
     */
    @Override
    public Table<URI, URI, MatchResult> match(Set<URI> origins, Set<URI> destinations) {

        StringBuffer queryBuffer = new StringBuffer()
                .append(generateQueryHeader());

        List<String> whereClauses = new ArrayList<String>();
        for (URI origin : origins) {
            for (URI destination : destinations) {
                whereClauses.add(generateMatchWhereClause(origin, destination, true));
            }
        }

        String queryStr = queryBuffer.append(Util.generateUnionStatement(whereClauses))
                .append(generateQueryFooter())
                .toString();

        log.debug("SPARQL Query generated: \n {}", queryStr);

        return queryForMatchResults(queryStr);
    }

    private Table<URI, URI, MatchResult> queryForMatchResults(String queryStr) {

        Table<URI, URI, MatchResult> result = HashBasedTable.create();

        // Query the engine
        Query query = QueryFactory.create(queryStr);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(this.sparqlEndpoint.toASCIIString(), query);

        try {
            Stopwatch stopwatch = new Stopwatch().start();
            ResultSet qResults = qexec.execSelect();
            stopwatch.stop();
            log.debug("Time taken for querying the registry: {}", stopwatch);

            // Obtain matches if any and figure out the type
            MatchType type;
            URI origin;
            URI destination;
            while (qResults.hasNext()) {
                QuerySolution soln = qResults.nextSolution();
                // Only process if we can get complete match information
                if (soln.contains(ORIGIN_VAR) && soln.contains(DESTINATION_VAR)) {
                    type = getMatchType(soln);
                    origin = new URI(soln.getResource(ORIGIN_VAR).getURI());
                    destination = new URI(soln.getResource(DESTINATION_VAR).getURI());
                    log.debug("Concept {} was matched to {} with type {}", origin, destination, type);
                    result.put(origin, destination, new AtomicMatchResult(origin, destination, type, this));
                }
            }

        } catch (URISyntaxException e) {
            log.error("Error creating URI for match results", e);
        } finally {
            qexec.close();
        }

        return result;
    }

    /**
     * Obtains all the matching resources that have a precise MatchType with the URI of {@code origin}.
     *
     * @param origin URI to match
     * @param type   the MatchType we want to obtain
     * @return an ImmutableMap sorted by value indexed by the URI of the matching resource and containing the particular {@code MatchResult}. If no
     *         result is found the Map should be empty not null.
     */
    @Override
    public Map<URI, MatchResult> listMatchesOfType(URI origin, MatchType type) {
        return listMatchesWithinRange(origin, type, type);
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


    /**
     * Obtains all the matching resources that have a MatchType with the URI of {@code origin} of the type provided (inclusive) or more.
     *
     * @param origin  URI to match
     * @param minType the minimum MatchType we want to obtain
     * @return an ImmutableMap sorted by value indexed by the URI of the matching resource and containing the particular {@code MatchResult}. If no
     *         result is found the Map should be empty not null.
     */
    @Override
    public Map<URI, MatchResult> listMatchesAtLeastOfType(URI origin, MatchType minType) {
        return listMatchesWithinRange(origin, minType, LogicConceptMatchType.Exact);
    }

    /**
     * Obtains all the matching resources that have a MatchType with the URIs of {@code origin} of the type provided (inclusive) or more.
     *
     * @param origins URIs to match
     * @param minType the minimum MatchType we want to obtain
     * @return a {@link com.google.common.collect.Table} with the result of the matching indexed by origin URI and then destination URI.
     */
    @Override
    public Table<URI, URI, MatchResult> listMatchesAtLeastOfType(Set<URI> origins, MatchType minType) {
        Table<URI, URI, MatchResult> matchTable = HashBasedTable.create();
        Stopwatch w = new Stopwatch();
        for (URI origin : origins) {
            w.start();
            Map<URI, MatchResult> result = listMatchesAtLeastOfType(origin, minType);
            for (Map.Entry<URI, MatchResult> dest : result.entrySet()) {
                matchTable.put(origin, dest.getKey(), dest.getValue());
            }
            log.debug("Computed matched types for {} in {}. {} total matches.", origin, w.stop().toString(), result.size());
            w.reset();
        }
        return matchTable;
    }


    /**
     * Obtain all the matching resources that have a MatchTyoe with the URI of {@code origin} of the type provided (inclusive) or less.
     *
     * @param origin  URI to match
     * @param maxType the maximum MatchType we want to obtain
     * @return an ImmutableMap sorted by value indexed by the URI of the matching resource and containing the particular {@code MatchResult}. If no
     *         result is found the Map should be empty not null.
     */
    @Override
    public Map<URI, MatchResult> listMatchesAtMostOfType(URI origin, MatchType maxType) {
        return listMatchesWithinRange(origin, LogicConceptMatchType.Fail, maxType);
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


    /**
     * Obtain all the matching resources with the URI of {@code origin} within the range of MatchTypes provided, both inclusive.
     *
     * @param origin  URI to match
     * @param minType the minimum MatchType we want to obtain
     * @param maxType the maximum MatchType we want to obtain
     * @return an ImmutableMap sorted by value containing indexed by the URI of the matching resource and containing the particular {@code MatchResult}. If no
     *         result is found the Map should be empty not null.
     */
    @Override
    public Map<URI, MatchResult> listMatchesWithinRange(URI origin, MatchType minType, MatchType maxType) {

        Map<URI, MatchResult> matches = obtainMatchResults(ImmutableSet.of(origin), minType, maxType);

        // Desired entries in desired order.  Put them in an ImmutableMap in this order filtered.
        ImmutableMap.Builder<URI, MatchResult> builder = ImmutableMap.builder();
        for (Map.Entry<URI, MatchResult> entry : entryOrdering.sortedCopy(matches.entrySet())) {
            builder.put(entry.getKey(), entry.getValue());
        }

        return builder.build();
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


    /**
     * Obtain the type of Match. The bindings indicate the relationship between the destination and the origin
     * That is, the subclasses of 'origin' will have a true value at binding 'sub'
     *
     * @param soln
     * @return
     */
    private MatchType getMatchType(QuerySolution soln) {

        if (soln.contains(EXACT_VAR))
            return LogicConceptMatchType.Exact;

        if (soln.contains(SUPER_VAR))
            return LogicConceptMatchType.Plugin;

        if (soln.contains(SUB_VAR))
            return LogicConceptMatchType.Subsume;

        return LogicConceptMatchType.Fail;
    }

    private Map<URI, MatchResult> obtainMatchResults(Set<URI> origins, MatchType minType, MatchType maxType) {
        Map<URI, MatchResult> result = new HashMap<URI, MatchResult>();
        // Exit fast if no data is provided or no matches can be found
        if (origins == null || origins.isEmpty() || minType.compareTo(maxType) > 0)
            return result;

        // Obtain an array to keep track of the order
        URI[] uris = origins.toArray(new URI[origins.size()]);

        // Create the query
        String queryStr = new StringBuffer()
                .append(generateQueryHeader())
                .append(generateRangeMatchWhereClause(uris, minType, maxType))
                .append(generateQueryFooter())
                .toString();

        log.debug("SPARQL Query generated: \n {}", queryStr);

        // Query the engine
        Query query = QueryFactory.create(queryStr);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(this.sparqlEndpoint.toASCIIString(), query);

        try {
            Stopwatch stopwatch = new Stopwatch().start();
            ResultSet qResults = qexec.execSelect();
            stopwatch.stop();
            log.debug("Time taken for querying the registry: {}", stopwatch);

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

                if (resource != null && resource.isURIResource()) {
                    matchUri = new URI(resource.getURI());
                    MatchType type = getMatchType(soln);
                    result.put(matchUri, new AtomicMatchResult(uris[index], matchUri, type, this));
                    log.debug("Concept {} was matched to {} with type {}", uris[index], matchUri, type);
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

    private String generateRangeMatchWhereClause(URI[] origins, MatchType minType, MatchType maxType) {

        StringBuffer queryBuffer = new StringBuffer();

        int i = 0;
        for (URI origin : origins) {
            queryBuffer.append(Util.generateUnionStatement(generatePatterns(origin, MATCH_VAR + i, minType, maxType)));
            i++;
        }

        return queryBuffer.toString();
    }

    private List<String> generatePatterns(URI origin, String destinationVar, MatchType minType, MatchType maxType) {
        List<String> patterns = new ArrayList<String>();
        if (minType.compareTo(LogicConceptMatchType.Subsume) <= 0 && maxType.compareTo(LogicConceptMatchType.Subsume) >= 0) {
            // Match the origin concept to strict subclasses
            patterns.add(Util.generateMatchStrictSubclassesPattern(origin, destinationVar, SUB_VAR, false));
        }

        if (minType.compareTo(LogicConceptMatchType.Plugin) <= 0 && maxType.compareTo(LogicConceptMatchType.Plugin) >= 0) {
            // Match the origin concept to strict superclasses
            patterns.add(Util.generateMatchStrictSuperclassesPattern(origin, destinationVar, SUPER_VAR, false));
        }

        if (minType.compareTo(LogicConceptMatchType.Exact) <= 0 && maxType.compareTo(LogicConceptMatchType.Exact) >= 0) {
            // Match the origin concept to exact matches
            patterns.add(Util.generateExactMatchPattern(origin, destinationVar, EXACT_VAR, false));
        }

        return patterns;
    }

    private String generateMatchWhereClause(URI origin, URI destination, boolean includeUrisBound) {

        StringBuffer queryBuffer = new StringBuffer();

        List<String> patterns = new ArrayList<String>();
        // Match the origin concept to strict subclasses
        patterns.add(Util.generateMatchStrictSubclassesPattern(origin, destination, SUB_VAR, includeUrisBound));
        // Match the origin concept to strict superclasses
        patterns.add(Util.generateMatchStrictSuperclassesPattern(origin, destination, SUPER_VAR, includeUrisBound));
        // Match the origin concept to exact matches
        patterns.add(Util.generateExactMatchPattern(origin, destination, EXACT_VAR, includeUrisBound));
        // Generate UNION
        queryBuffer.append(Util.generateUnionStatement(patterns));

        return queryBuffer.toString();
    }

    private String generateQueryHeader() {
        return new StringBuffer()
                .append("select * ").append(NL)
                .append("where {" + NL)
                .toString();
    }

    private String generateQueryFooter() {
        return new StringBuffer()
                .append("}")
                .toString();
    }
}
