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

import com.google.common.collect.*;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.commons.vocabulary.SAWSDL;
import uk.ac.open.kmi.iserve.discovery.api.DiscoveryException;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.OperationDiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.api.ServiceDiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.disco.*;
import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;

import javax.ws.rs.core.MultivaluedMap;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

import static uk.ac.open.kmi.iserve.discovery.disco.MatchMapCombinator.INTERSECTION;
import static uk.ac.open.kmi.iserve.discovery.disco.MatchMapCombinator.UNION;

public class RDFSInputOutputDiscoveryPlugin implements ServiceDiscoveryPlugin, OperationDiscoveryPlugin {

    private static final Logger log = LoggerFactory.getLogger(RDFSInputOutputDiscoveryPlugin.class);

    private static final String PLUGIN_VERSION = "v0.3";

    private static final String PLUGIN_NAME = "io-rdfs";

    private static final String PLUGIN_DESCRIPTION = "iServe RDFS input/output discovery plugin. Discovers services and " +
            "operations based on their inputs and outputs using subsumption " +
            "reasoning as supported by the underlying RDF Store.";

    // Discovery Plugin Parameters
    private static final String FUNCTION_PARAM = "f";

    private static final String AND_FUNCTION = "and";

    private static final String FUNCTION_PARAM_DESCRIPTION = "This parameter should" +
            "indicate the function to be applied over the inputs and outputs discovery." +
            "It should be either 'and' (each condition needs to hold) or 'or' (some " +
            "condition must hold to be part of the results). The default value is 'or'.";

    private static final String OUTPUTS_PARAM = "o";

    private static final String OUTPUTS_PARAM_DESCRIPTION = "This parameter " +
            "indicates the URLs of the concepts the outputs should be matched" +
            "against.";

    private static final String INPUTS_PARAM = "i";

    private static final String INPUTS_PARAM_DESCRIPTION = "This parameter " +
            "indicates the URLs of the concepts the inputs should be matched" +
            "against.";

    private static final Map<String, String> parameterDetails;

    static {
        parameterDetails = new HashMap<String, String>();
        parameterDetails.put(FUNCTION_PARAM, FUNCTION_PARAM_DESCRIPTION);
        parameterDetails.put(OUTPUTS_PARAM, OUTPUTS_PARAM_DESCRIPTION);
        parameterDetails.put(INPUTS_PARAM, INPUTS_PARAM_DESCRIPTION);
    }

    public static String NL = System.getProperty("line.separator");

    private int count;

    private String feedSuffix;

    private URI sparqlEndpoint;

    // TODO: Make this configurable
    private static final int NUM_THREADS = 20;
    private ExecutorService pool;

    // TODO: Generalise this further in the future

    /**
     * Class Description
     *
     * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
     */
    public class InputMatchCallable implements Callable<Map<URL, MatchResult>> {

        boolean operationDiscovery;
        List<String> matchClasses;

        /**
         * @param operationDiscovery
         * @param matchClasses
         */
        public InputMatchCallable(boolean operationDiscovery,
                                  List<String> matchClasses) {
            super();
            this.operationDiscovery = operationDiscovery;
            this.matchClasses = matchClasses;
        }

        /* (non-Javadoc)
         * @see java.util.concurrent.Callable#call()
         */
        @Override
        public Map<URL, MatchResult> call() throws Exception {
            return matchInputs(operationDiscovery, matchClasses);
        }

    }

    /**
     * Class Description
     *
     * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
     */
    public class OutputMatchCallable implements Callable<Map<URL, MatchResult>> {

        /**
         * @param operationDiscovery
         * @param matchClasses
         */
        public OutputMatchCallable(boolean operationDiscovery,
                                   List<String> matchClasses) {
            super();
            this.operationDiscovery = operationDiscovery;
            this.matchClasses = matchClasses;
        }

        boolean operationDiscovery;
        List<String> matchClasses;

        /* (non-Javadoc)
         * @see java.util.concurrent.Callable#call()
         */
        @Override
        public Map<URL, MatchResult> call() throws Exception {
            return matchOutputs(operationDiscovery, matchClasses);
        }

    }

    public RDFSInputOutputDiscoveryPlugin() {
        this.count = 0;
        this.feedSuffix = "";

        // Create the thread pool to carry out discovery tasks in parallel
        pool = Executors.newFixedThreadPool(NUM_THREADS);

        sparqlEndpoint = ManagerSingleton.getInstance().getConfiguration().getDataSparqlUri();
        if (sparqlEndpoint == null) {
            log.error("The '" + this.getName() + "' " + this.getVersion() +
                    " services discovery plugin currently requires a SPARQL endpoint.");
            // TODO: Disable plugin
        }

    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.discovery.api.DiscoveryPlugin#getName()
     */
    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.discovery.api.IServiceDiscoveryPlugin#getVersion()
     */
    @Override
    public String getVersion() {
        return PLUGIN_VERSION;
    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.discovery.api.DiscoveryPlugin#getDescription()
     */
    @Override
    public String getDescription() {
        return PLUGIN_DESCRIPTION;
    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.discovery.api.DiscoveryPlugin#getParametersDetails()
     */
    @Override
    public Map<String, String> getParametersDetails() {
        return parameterDetails;
    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.discovery.api.DiscoveryPlugin#getFeedTitle()
     */
    public String getFeedTitle() {
        String feedTitle = "RDFS I/O discovery results: " + count + " services(s) or operation(s) for " + feedSuffix;
        return feedTitle;
    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.discovery.api.ServiceDiscoveryPlugin#discoverServices(javax.ws.rs.core.MultivaluedMap)
     */
    @Override
    public Map<URL, MatchResult> discoverServices(MultivaluedMap<String, String> parameters) throws DiscoveryException {
        return discover(false, parameters);
    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.discovery.api.OperationDiscoveryPlugin#discoverOperations(javax.ws.rs.core.MultivaluedMap)
     */
    @Override
    public Map<URL, MatchResult> discoverOperations(MultivaluedMap<String,
            String> parameters) throws DiscoveryException {
        return discover(true, parameters);
    }

    /**
     * 5 match degrees for inputs: exact, plugin, subsume, partial-plugin, partial-subsume
     * exact           <= the goal has all the exact service input classes (but may have more)
     * plugin          <= for each service input class, goal has a subclass of it
     * subsume         <= for each service input class, goal has a superclass of it
     * partial-plugin  <= goal has subclass of some service input class
     * partial-subsume <= goal has superclass of some service input class
     * <p/>
     * 5 match degrees for outputs: exact, plugin, subsume, partial-plugin, partial-subsume
     * exact           <= the service has all the exact goal output classes (but may have more)
     * plugin          <= for each goal output class, service has a subclass of it
     * subsume         <= for each goal output class, service has a superclass of it
     * partial-plugin  <= service has subclass of some goal output class
     * partial-subsume <= service has superclass of some goal output class
     *
     * @param operationDiscovery true if carrying out operation discovery
     * @param parameters         request parameters
     * @return the set of discovery results
     * @throws DiscoveryException
     */
    public Map<URL, MatchResult> discover(boolean operationDiscovery,
                                          MultivaluedMap<String, String> parameters) throws DiscoveryException {

        // If there is no SPARQL endpoint raise an error
        if (sparqlEndpoint == null) {
            log.error("Unable to perform discovery, no SPARQL endpoint available.");
            throw new DiscoveryException(403, "Unable to perform discovery, no SPARQL endpoint available.");
        }

        List<String> inputClasses = parameters.get(INPUTS_PARAM);
        List<String> outputClasses = parameters.get(OUTPUTS_PARAM);

        boolean matchingInputs = inputClasses != null && inputClasses.size() != 0;
        boolean matchingOutputs = outputClasses != null && outputClasses.size() != 0;
        if (!matchingInputs && !matchingOutputs) {
            throw new DiscoveryException(403, "I/O discovery without parameters is not supported");
        }
        if (matchingInputs) {
            for (String clazz : inputClasses) {
                if (clazz == null) {
                    throw new DiscoveryException(400, "Empty class URI not allowed");
                }
            }
        }
        if (matchingOutputs) {
            for (String clazz : outputClasses) {
                if (clazz == null) {
                    throw new DiscoveryException(400, "Empty class URI not allowed");
                }
            }
        }

        String andOr = parameters.getFirst(FUNCTION_PARAM);
        boolean intersection = AND_FUNCTION.equals(andOr);

        Map<URL, MatchResult> results = null;
        Map<URL, MatchResult> inputMatches = null;
        Map<URL, MatchResult> outputMatches = null;

        if (matchingInputs && matchingOutputs) {
            Set<Map<URL, MatchResult>> toCombine = new HashSet<Map<URL, MatchResult>>();

            //			// Sequential execution
            //			toCombine.add(matchInputs(operationDiscovery, inputClasses));
            //			toCombine.add(matchOutputs(operationDiscovery, outputClasses));

            // Create the tasks and run them in parallel
            Set<Callable<Map<URL, MatchResult>>> callables = new HashSet<Callable<Map<URL, MatchResult>>>();
            callables.add(new InputMatchCallable(operationDiscovery, inputClasses));
            callables.add(new OutputMatchCallable(operationDiscovery, outputClasses));
            try {

                List<Future<Map<URL, MatchResult>>> futures = pool.invokeAll(callables);
                // Invoke each discovery task and get the results
                for (Future<Map<URL, MatchResult>> future : futures) {
                    toCombine.add(future.get());
                }

            } catch (InterruptedException e) {
                log.error("The discovery engine was interrupted while executing the discovery tasks.", e);
            } catch (ExecutionException e) {
                log.error("Error executing the discovery task.", e);
            }

            // Combine the results obtained into one Map
            // Based on Guava, returns views that will be computed every time so
            // if we have to loop several times we should actually materialise them
            Multimap<URL, MatchResult> combination;
            if (intersection) {
                combination = INTERSECTION.apply(toCombine);
                results = Maps.transformValues(combination.asMap(), MatchResultsMerger.INTERSECTION);
            } else {
                combination = UNION.apply(toCombine);
                results = Maps.transformValues(combination.asMap(), MatchResultsMerger.UNION);
            }

        } else {
            // Just one task, no need for parallel execution
            if (matchingInputs) {
                inputMatches = matchInputs(operationDiscovery, inputClasses);
                results = inputMatches;
                // TEST
                //				Map<URL, MatchResult> inputMatchesAlt = matchInputsAlternative(operationDiscovery, inputClasses);
            } else {
                outputMatches = matchOutputs(operationDiscovery, outputClasses);
                results = outputMatches;
            }
        }

        return results;
    }

    /**
     * Match Services and Operations inputs given the classes requested
     * <p/>
     * algorithm for inputs (how it was and should be, not necessarily how it is):
     * 1) with sparql, find all service operations and their inputs (represented by values of modelreference annotations)
     *    if ?suX is set, the input is a subclass of some goal input
     *    if ?plX is set, the goal has a subclass of the input
     *    if ?exX is set, the input is equal to a goal class X
     * 2) going through the results
     *    For each input that has a match > Fail
     * create an inner match
     * <p/>
     * If the service had some match
     * create a composite match based on the inner matches
     * <p/>
     * Figure out the aggregated match based on the best and worst matches
     *
     * @param operationDiscovery
     * @param classes
     * @throws DiscoveryException
     */
    private Map<URL, MatchResult> matchInputs(boolean operationDiscovery, List<String> classes) throws DiscoveryException {

        Map<URL, MatchResult> results = new HashMap<URL, MatchResult>();
        // Return immediately if there is nothing to discover
        if (classes == null || classes.isEmpty()) {
            return results;
        }

        String query = generateInOutQuery(classes, true);
        log.info("Input matching query: " + NL + query);
        results = processQueryResults(operationDiscovery, classes, query);
        log.info("Returning " + results.size() + " discovery results.");
        return results;
    }

    /**
     * @param operationDiscovery
     * @param classes
     * @param queryStr
     * @return
     */
    private Map<URL, MatchResult> processQueryResults(
            boolean operationDiscovery, List<String> classes, String queryStr) {

        Map<URL, MatchResult> results = new HashMap<URL, MatchResult>();
        // Return immediately if there is nothing to discover
        if (classes == null || classes.isEmpty()) {
            return results;
        }

        // Query the engine
        Query query = QueryFactory.create(queryStr, Syntax.syntaxSPARQL_11);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint.toASCIIString(), query);

        URL matchUrl = null;
        String matchLabel = null;
        Resource resource = null;
        Literal labelResource = null;
        String matchBinding = null;
        String labelBinding = null;

        if (operationDiscovery) {
            matchBinding = "op";
            labelBinding = "labelOp";
        } else {
            matchBinding = "svc";
            labelBinding = "labelSvc";
        }

        try {
            // TODO: Remove profiling
            long startTime = System.currentTimeMillis();

            ResultSet qResults = qexec.execSelect();

            // TODO: Remove profiling
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            log.info("Time taken for querying the registry: " + duration);

            // Map results to combine (one per class to match)
            // These are also multimaps as there can be several matches for the
            // same class for a given service and/or operation
            List<Multimap<URL, MatchResult>> toCombine = new ArrayList<Multimap<URL, MatchResult>>();
            for (int i = 0; i < classes.size(); i++) {
                toCombine.add(HashMultimap.<URL, MatchResult>create());
            }

            // Iterate over the results obtained
            while (qResults.hasNext()) {
                QuerySolution soln = qResults.nextSolution();

                // Get the match URL
                resource = soln.getResource(matchBinding);
                if (resource != null && resource.isURIResource()) {
                    matchUrl = new URL(resource.getURI());
                } else {
                    log.warn("Skipping result as the URL is null");
                    break;
                }

                // Get label if it exists
                labelResource = soln.getLiteral(labelBinding);
                if (labelResource != null) {
                    matchLabel = labelResource.toString();
                }

                boolean isExact = false;
                boolean isPlugin = false;
                boolean isSubsume = false;

                for (int i = 0; i < classes.size(); i++) {
                    isExact = soln.contains("ex" + i);
                    isSubsume = soln.contains("su" + i);
                    isPlugin = soln.contains("pl" + i);

                    // Create an inner match for each case if there is some
                    // match relationship. Fails are important within the
                    // Composite Match. Only relevant services (with some sort
                    // of match will be obtained.
                    DiscoMatchType matchType = null;
                    if (isExact) {
                        matchType = DiscoMatchType.EXACT;
                    } else if (isPlugin) {
                        matchType = DiscoMatchType.PLUGIN;
                    } else if (isSubsume) {
                        matchType = DiscoMatchType.SUBSUME;
                    } else {
                        // By default it's failed
                        matchType = DiscoMatchType.FAIL;
                    }

                    // TODO: Add more details for real debugging
                    MatchResultImpl match = new MatchResultImpl(matchUrl, matchLabel);
                    match.setMatchType(matchType);
                    toCombine.get(i).put(matchUrl, match);
                    log.debug("Adding match for " +
                            match.getMatchedResource() + " of type " +
                            match.getMatchType().getShortName());
                }
            }

            // Now merge the results obtained
            // First merge the matches for the same class & service/op (UNION)
            List<Map<URL, MatchResult>> classAndServiceMatches =
                    Lists.newArrayList(Iterables.<Multimap<URL, MatchResult>,
                            Map<URL, MatchResult>>transform(toCombine, MultimapToMapMerger.UNION));

            // Second merge the matches for diff classes, same service (INTER)
            Multimap<URL, MatchResult> svcMatches =
                    INTERSECTION.apply(classAndServiceMatches);

            results = ImmutableMap.copyOf(Maps.transformValues(svcMatches.asMap(),
                    MatchResultsMerger.INTERSECTION));

        } catch (MalformedURLException e) {
            log.error("Error obtaining match result. Expected a correct URL", e);
        } finally {
            qexec.close();
        }
        return results;
    }


    // REMOVE, ONLY FOR TESTS
    private Map<URL, MatchResult> matchInputsAlternative(boolean operationDiscovery, List<String> classes) throws DiscoveryException {

        Map<URL, MatchResult> results = new HashMap<URL, MatchResult>();
        // Return immediately if there is nothing to discover
        if (classes == null || classes.isEmpty()) {
            return results;
        }

        String queryStr = generateInputsQueryAlternative(classes);
        log.info("Input matching query: " + NL + queryStr);

        // Query the engine
        Query query = QueryFactory.create(queryStr);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint.toASCIIString(), query);

        URL matchUrl = null;
        String matchLabel = null;
        Resource resource = null;
        Literal labelResource = null;
        String matchBinding = null;
        String labelBinding = null;

        if (operationDiscovery) {
            matchBinding = "op";
            labelBinding = "labelOp";
        } else {
            matchBinding = "svc";
            labelBinding = "labelSvc";
        }

        try {
            // TODO: Remove profiling
            long startTime = System.currentTimeMillis();

            ResultSet qResults = qexec.execSelect();

            // TODO: Remove profiling
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            log.info("Time taken for querying the registry: " + duration);

            // Map results to combine (one per class to match)
            // These are also multimaps as there can be several matches for the
            // same class for a given service and/or operation
            List<Multimap<URL, MatchResult>> toCombine = new ArrayList<Multimap<URL, MatchResult>>();
            for (int i = 0; i < classes.size(); i++) {
                toCombine.add(HashMultimap.<URL, MatchResult>create());
            }

            // Iterate over the results obtained
            while (qResults.hasNext()) {
                QuerySolution soln = qResults.nextSolution();

                // Get the match URL
                resource = soln.getResource(matchBinding);
                if (resource != null && resource.isURIResource()) {
                    matchUrl = new URL(resource.getURI());
                } else {
                    log.warn("Skipping result as the URL is null");
                    break;
                }

                // Get label if it exists
                labelResource = soln.getLiteral(labelBinding);
                if (labelResource != null) {
                    matchLabel = labelResource.toString();
                }

                boolean isExact = false;
                boolean isPlugin = false;
                boolean isSubsume = false;

                for (int i = 0; i < classes.size(); i++) {
                    isExact = soln.contains("ex" + i);
                    isSubsume = soln.contains("su" + i);
                    isPlugin = soln.contains("pl" + i);

                    // Create an inner match for each case if there is some
                    // match relationship. Fails are important within the
                    // Composite Match. Only relevant services (with some sort
                    // of match will be obtained.
                    DiscoMatchType matchType = null;
                    if (isExact) {
                        matchType = DiscoMatchType.EXACT;
                    } else if (isPlugin) {
                        matchType = DiscoMatchType.PLUGIN;
                    } else if (isSubsume) {
                        matchType = DiscoMatchType.SUBSUME;
                    } else {
                        // By default it's failed
                        matchType = DiscoMatchType.FAIL;
                    }

                    // TODO: Add more details for real debugging
                    MatchResultImpl match = new MatchResultImpl(matchUrl, matchLabel);
                    match.setMatchType(matchType);
                    toCombine.get(i).put(matchUrl, match);
                    log.debug("Adding match for " +
                            match.getMatchedResource() + " of type " +
                            match.getMatchType().getShortName());
                }
            }

            // Now merge the results obtained
            // First merge the matches for the same class & service/op (UNION)
            List<Map<URL, MatchResult>> classAndServiceMatches =
                    Lists.newArrayList(Iterables.<Multimap<URL, MatchResult>,
                            Map<URL, MatchResult>>transform(toCombine, MultimapToMapMerger.UNION));

            // Second merge the matches for diff classes, same service (INTER)
            Multimap<URL, MatchResult> svcMatches =
                    INTERSECTION.apply(classAndServiceMatches);

            results = ImmutableMap.copyOf(Maps.transformValues(svcMatches.asMap(),
                    MatchResultsMerger.INTERSECTION));

        } catch (MalformedURLException e) {
            log.error("Error obtaining match result. Expected a correct URL", e);
        } finally {
            qexec.close();
        }
        return results;
    }


    /**
     * @param operationDiscovery
     * @param classes
     * @return
     * @throws DiscoveryException
     */
    private Map<URL, MatchResult> matchOutputs(boolean operationDiscovery, List<String> classes) throws DiscoveryException {

        Map<URL, MatchResult> results = new HashMap<URL, MatchResult>();
        // Return immediately if there is nothing to discover
        if (classes == null || classes.isEmpty()) {
            return results;
        }

        String query = generateInOutQuery(classes, false);
        log.info("Output matching query: " + NL + query);
        results = processQueryResults(operationDiscovery, classes, query);
        log.info("Returning " + results.size() + " discovery results.");
        return results;
    }

    /**
     * Generates a discovery query that retrieves all the matches between service
     * operations inputs/outputs (and parts thereof) and the classes passed as a parameter.
     * Each of the rows will provide 3 different matches:
     * - Exact (?exX has a value) if the class was matched perfectly
     * - Plugin (?plX has a value) if a subclass was found
     * - Subsume (?suX has a value) if a superclass was found
     * <p/>
     * The query is based on UNION statements rather than OPTIONAL ones for
     * performance reasons. The patterns are more restrictive which help us
     * reduce the querying time. Additionally only the matching results are
     * returned which reduces post-processing time.
     * <p/>
     * The resulting query will bind the results to the following variables:
     * - ?svc -> service that matches
     * - ?labelSvc -> service label. In principle there should only be one label per
     * service and operation. In the strange event that there are several they
     * will appear separated by a comma
     * - ?op -> operation that matches
     * - ?labelOp -> operation label. In principle there should only be one label per
     * service and operation. In the strange event that there are several they
     * will appear separated by a comma
     * - ?msg the message content
     * - ?part the message part
     * - ?prop the property
     * - ?mr the model reference
     * - ?suX -> the number of matches where the input subsumes on the class #X
     * - ?plX -> the number of matches where the input is plugin on the class #X
     * - ?exX -> the number of matches where the input is exact on the class #X
     *
     * @param classes the classes we want to match
     * @return the SPARQL query
     */
    private String generateInOutQuery(List<String> classes, boolean inputMatch) {

        StringBuffer query = new StringBuffer().
                append("select * ").append(NL); // Obtain every variable for further details

        query.append("where {" + NL).
                append("  ?svc a <" + MSM.Service.getURI() + "> ; <" + MSM.hasOperation.getURI() + "> ?op ." + NL).
                append("  ?op a <" + MSM.Operation.getURI() + "> ." + NL);

        if (inputMatch)
            query.append("  ?op <" + MSM.hasInput.getURI() + "> ?msg ." + NL);
        else
            query.append("  ?op <" + MSM.hasOutput.getURI() + "> ?msg ." + NL);

        query.append("  ?msg a <" + MSM.MessageContent.getURI() + "> ." + NL);

        // Bind ?mr to modelReferences at one of the possible levels
        // 1) Input, 2) Part of the Input, 3) Part of the Input through a property
        // Each option is bound with UNION statements for performance and to
        // ensure that at least one of the options is bound

        // Reference at the message level
        query.append("  { ?msg <" + SAWSDL.modelReference.getURI() + "> ?mr . } " + NL);

        // Reference at the level of Parts
        // The following statement requires MSM v2 at least
        // Can be replaced with variable length paths in SPARQL 1.1.
        query.append("  UNION { ?msg <" + MSM.hasPartTransitive.getURI() + "> ?part ." + NL);
        query.append("  ?part <" + SAWSDL.modelReference.getURI() + "> ?mr . } " + NL);

        // Reference at the level of Parts through a property
        query.append("  UNION { ?msg <" + MSM.hasPartTransitive.getURI() + "> ?part ." + NL);
        query.append("  ?part <" + SAWSDL.modelReference.getURI() + "> ?prop . " + NL);
        query.append("  ?prop <" + RDFS.range.getURI() + "> ?mr . } " + NL);

        List<String> patterns = new ArrayList<String>();
        for (int i = 0; i < classes.size(); i++) {
            String currClass = classes.get(i).replace(">", "%3e");

            if (inputMatch) {
                // Match the modelRef of the input to strict subclasses of the class
                patterns.add(Util.generateMatchStrictSubclassesPattern("mr", currClass, "su" + i));
                // Match the modelRef of the input to strict superclasses of the class
                patterns.add(Util.generateMatchStrictSuperclassesPattern("mr", currClass, "pl" + i));
            } else {
                // Invert the matching for outputs
                // Match the modelRef of the input to strict subclasses of the class
                patterns.add(Util.generateMatchStrictSubclassesPattern("mr", currClass, "pl" + i));
                // Match the modelRef of the input to strict superclasses of the class
                patterns.add(Util.generateMatchStrictSuperclassesPattern("mr", currClass, "su" + i));
            }
            // Match the modelRef of the input to exact matches for the class

        }

        query.append(Util.generateUnionStatement(patterns));

        // Obtain labels is they exist
        query.append("  OPTIONAL {" + Util.generateLabelPattern("svc", "labelSvc") + "}" + NL);
        query.append("  OPTIONAL {" + Util.generateLabelPattern("op", "labelOp") + "}" + NL);

        query.append("}");
        return query.toString();
    }

    private String generateInputsQueryAlternative(List<String> classes) {

        StringBuffer query = new StringBuffer().
                append("select ?svc ?op ");
        // Obtain the labels. In principle there should only be one label per
        // service and operation. In the strange event that there are several
        // they will appear separated by a comma
        query.append("(GROUP_CONCAT(DISTINCT ?sLabel; separator = \",\") as ?labelSvc) ").
                append("(GROUP_CONCAT(DISTINCT ?oLabelOp; separator = \",\") as ?labelOp) ");
        for (int i = 0; i < classes.size(); i++) {
            query.append(" (COUNT (?interSu" + i + ") as ?su" + i + ") ").
                    append(" (COUNT (?interPl" + i + ") as ?pl" + i + ") ").
                    append(" (COUNT (?interEx" + i + ") as ?ex" + i + ") ");
        }
        query.append(NL);

        query.append("where {" + NL).
                append("  ?svc a <" + MSM.Service.getURI() + "> ; <" + MSM.hasOperation.getURI() + "> ?op ." + NL).
                append("  ?op <" + MSM.hasInput.getURI() + "> ?imsg ." + NL);

        // Bind ?ic to modelReferences at one of the possible levels
        // 1) Input, 2) Part of the Input, 3) Part of the Input through a property
        // Each option is bound with UNION statements for performance and to
        // ensure that at least one of the options is bound

        // Reference at the message level
        query.append("  { ?imsg <" + SAWSDL.modelReference.getURI() + "> ?ic } " + NL);

        // Reference at the level of Parts
        // The following statement requires MSM v2 at least
        // Can be replaced with variable length paths in SPARQL 1.1.
        query.append("  UNION { ?imsg <" + MSM.hasPartTransitive.getURI() + "> ?i ." + NL);
        query.append("  ?i <" + SAWSDL.modelReference.getURI() + "> ?ic } " + NL);

        // Reference at the level of Parts through a property
        query.append("  UNION { ?imsg <" + MSM.hasPartTransitive.getURI() + "> ?i ." + NL);
        query.append("  ?i <" + SAWSDL.modelReference.getURI() + "> ?prop . " + NL);
        query.append("  ?prop <" + RDFS.range.getURI() + "> ?ic . } " + NL);

        List<String> patterns = new ArrayList<String>();
        for (int i = 0; i < classes.size(); i++) {
            String currClass = classes.get(i).replace(">", "%3e");

            // Match the modelRef of the input to strict subclasses of the class
            patterns.add(Util.generateMatchStrictSubclassesPattern("ic", currClass, "interSu" + i));
            // Match the modelRef of the input to strict superclasses of the class
            patterns.add(Util.generateMatchStrictSuperclassesPattern("ic", currClass, "interPl" + i));
            // Match the modelRef of the input to exact matches for the class
            patterns.add(Util.generateExactMatchPattern("ic", currClass, "interEx" + i));
        }

        query.append(Util.generateUnionStatement(patterns));

        // Obtain labels is they exist
        query.append("  OPTIONAL {" + Util.generateLabelPattern("svc", "slabel") + "}" + NL);
        query.append("  OPTIONAL {" + Util.generateLabelPattern("op", "oLabel") + "}" + NL);

        query.append("} " + NL);
        // Group the results by SVC and OP
        query.append("GROUP BY ?svc ?op");
        return query.toString();
    }

}
