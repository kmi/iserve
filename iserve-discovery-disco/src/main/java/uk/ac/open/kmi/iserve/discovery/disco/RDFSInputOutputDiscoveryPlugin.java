/*
   Copyright ${year}  Knowledge Media Institute - The Open University

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package uk.ac.open.kmi.iserve.discovery.disco;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rdf2go.RepositoryModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.iserve.commons.io.RDFRepositoryConnector;
import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.discovery.api.DiscoveryException;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.OperationDiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.api.ServiceDiscoveryPlugin;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;
import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;
import uk.ac.open.kmi.iserve.sal.manager.impl.ServiceManagerRdf;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class RDFSInputOutputDiscoveryPlugin implements ServiceDiscoveryPlugin, OperationDiscoveryPlugin {
	
	private static final Logger log = LoggerFactory.getLogger(RDFSInputOutputDiscoveryPlugin.class);

	private static final String PLUGIN_VERSION = "v1.1.2";

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
	
	// TODO: Add additional details to the enum? (Merge with others?)
	private enum Degree {
		EXACT, PLUGIN, SUBSUME, PARTIAL_PLUGIN, PARTIAL_SUBSUME, FAIL
	};

	private int count;

	private String feedSuffix;

	private RDFRepositoryConnector serviceConnector;

	public RDFSInputOutputDiscoveryPlugin() {
		this.count = 0;
		this.feedSuffix = "";
		
		ServiceManager svcManager = ManagerSingleton.getInstance().getServiceManager();
		if (svcManager instanceof ServiceManagerRdf) {
			serviceConnector = ((ServiceManagerRdf)svcManager).getRepoConnector();
		} else {
			throw new WebApplicationException(
					new IllegalStateException("The '" + this.getName() + "' " + 
							this.getVersion() + " services discovery plugin currently requires a Service Manager based backed by an RDF Repository."), 
					Response.Status.INTERNAL_SERVER_ERROR);
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
	 * TODO: Fix the implementation to adapt to the new interface
	 * 
	 * 5 match degrees for inputs: exact, plugin, subsume, partial-plugin, partial-subsume
	 * exact           <= the goal has all the exact service input classes (but may have more)
	 * plugin          <= for each service input class, goal has a subclass of it
	 * subsume         <= for each service input class, goal has a superclass of it
	 * partial-plugin  <= goal has subclass of some service input class
	 * partial-subsume <= goal has superclass of some service input class
	 * 
	 * 5 match degrees for outputs: exact, plugin, subsume, partial-plugin, partial-subsume
	 * exact           <= the service has all the exact goal output classes (but may have more)
	 * plugin          <= for each goal output class, service has a subclass of it
	 * subsume         <= for each goal output class, service has a superclass of it
	 * partial-plugin  <= service has subclass of some goal output class
	 * partial-subsume <= service has superclass of some goal output class
	 * 
	 * 
	 * 
	 * @param operationDiscovery true if carrying out operation discovery
	 * @param parameters request parameters
	 * @return the set of discovery results
	 * @throws DiscoveryException
	 */
	public Map<URL, MatchResult> discover(boolean operationDiscovery, 
			MultivaluedMap<String, String> parameters) throws DiscoveryException {
		
		// If there is no service connector raise an error 
		if (serviceConnector == null) {
			throw new DiscoveryException("The '" + this.getName() + "' " + 
							this.getVersion() + " the RDF connector to the services repository is null.");
		}
		
		List<String> inputClasses = parameters.get(INPUTS_PARAM);
		List<String> outputClasses = parameters.get(OUTPUTS_PARAM);

		boolean matchingInputs = inputClasses != null && inputClasses.size() != 0;
		boolean matchingOutputs = outputClasses != null && outputClasses.size() != 0;
		if (!matchingInputs && !matchingOutputs) {
			throw new DiscoveryException(403, "I/O discovery without parameters is not supported");
		}
		if ( matchingInputs ) {
			for (String clazz : inputClasses) {
				if (clazz == null) {
					throw new DiscoveryException(400, "Empty class URI not allowed");
				}
			}
		}
		if ( matchingOutputs ) {
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

		if (matchingInputs) {
			inputMatches = matchInputs(operationDiscovery, inputClasses);
		}
		
		if (matchingOutputs) {
			outputMatches = matchOutputs(operationDiscovery, outputClasses);
		}
		
		// Combine the matches into one Map
		// Based on Guava, returns views that will be computed every time so 
		// if we have to loop several times we should actually materialise them
		if (matchingInputs) {
			if (matchingOutputs) {
				Set<Map<URL, MatchResult>> toCombine = new HashSet<Map<URL,MatchResult>>();
				toCombine.add(inputMatches);
				toCombine.add(outputMatches);
				Multimap<URL, MatchResult> combination;
				if (intersection) {
					combination = MatchMapCombinator.INTERSECTION.apply(toCombine);
					
				} else {
					combination = MatchMapCombinator.UNION.apply(toCombine);
				}
				results = Maps.transformValues(combination.asMap(), new MatchResultsMerger());
			} else {
				results = inputMatches;
			}
		} else {
			// Should be matching outputs
			results = outputMatches;
		}

		return results;
	}

	/**
	 * Match Services and Operations inputs given the classes requested
	 * 
	 * TODO: The current query generated takes way too long
	 * 
	 * 
	 * algorithm for inputs (how it was and should be, not necessarily how it is):
	 * 1) with sparql, find all service operations and their inputs (represented by values of modelreference annotations)
	 *    if ?suX is set, the input is a subclass of some goal input
	 *    if ?plX is set, the goal has a subclass of the input
	 *    if ?exX is set, the input is equal to a goal class X
	 * 2) going through the results
	 *    For each input that has a match > Fail 
	 * 		create an inner match
	 * 
	 * 	  If the service had some match
	 * 		create a composite match based on the inner matches
	 * 
	 * 	  Figure out the aggregated match based on the best and worst matches
	 * 
	 * @param operationDiscovery
	 * @param classes
	 * @param matches
	 * @param labels
	 * @throws DiscoveryException
	 */
	private Map<URL, MatchResult> matchInputs(boolean operationDiscovery, List<String> classes) throws DiscoveryException {

		Map<URL, MatchResult> results = new HashMap<URL, MatchResult>();
		// Return immediately if there is nothing to discover
		if (classes == null || classes.isEmpty()) {
			return results;
		}
		
		RepositoryModel repoModel = null;
		ClosableIterator<QueryRow> it = null;
		
		String query = generateInputsQuery(classes);
		log.info("Input matching query: " + NL + query);

		try {
			repoModel = serviceConnector.openRepositoryModel();
			// TODO: Remove profiling
			long startTime = System.currentTimeMillis();
			
			QueryResultTable qresult = repoModel.querySelect(query, "sparql");
			
			// TODO: Remove profiling
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			log.info("Time taken for querying the registry: " + duration);
			
			it = qresult.iterator();
			// Iterate over the results obtained
			while (it.hasNext()) {
				QueryRow row = it.next();
				
				URL matchUrl = Util.getMatchUrl(row, operationDiscovery);
				// Only continue processing if the match exists
				if (matchUrl == null) {
					log.warn("Skipping result as the URL is null");
					break;
				}
				
				String matchLabel = Util.getOrGenerateMatchLabel(row, operationDiscovery);
	
				boolean isExact = false;
				boolean isPlugin = false;
				boolean isSubsume = false;
				// Keep track of the best and worst match for the aggregated 
				// match type
				MatchType worstMatch = MatchType.EXACT;
				MatchType bestMatch = MatchType.FAIL;
				
				// Create an inner match per class matched
				// The overall match will be determined by the best and worst match
				Set<MatchResult> innerMatches = new HashSet<MatchResult>();
	
				for (int i = 0; i < classes.size(); i++) {
					isExact = Util.isVariableSet(row, "ex" + i); // TODO: We probably can skip this
					isSubsume = Util.isVariableSet(row, "su" + i);
					isPlugin = Util.isVariableSet(row, "pl" + i);
					
					// Create an inner match  if there is some match relationship
					// We don't keep track of the fail matches within the composite
					MatchType matchType = Util.getMatchType(isSubsume, isPlugin);
					if (matchType == null) {
						log.warn("Skipping result as the Match Type is null");
						break;
					}
					
					if (matchType != MatchType.FAIL) { 
						MatchResultImpl innerMatch = new MatchResultImpl(matchUrl, matchLabel);
						innerMatch.setMatchType(matchType);
						innerMatches.add(innerMatch);
						log.debug("Adding inner match for " + 
								innerMatch.getMatchUrl() + " of type " + 
								innerMatch.getMatchType().getShortName());
					}
					
					if (matchType.compareTo(bestMatch) > 0) {
						bestMatch = matchType;
					}
					
					if (matchType.compareTo(worstMatch) < 0) {
						worstMatch = matchType;
					}
				}
				
				if (!innerMatches.isEmpty()) {
					// The service has some match. Add it
					MatchType aggregatedType = Util.calculateCompositeMatchType(bestMatch, worstMatch);
					CompositeMatchResultImpl compositeMatch = new CompositeMatchResultImpl(matchUrl, matchLabel);
					compositeMatch.setInnerMatches(innerMatches);
					compositeMatch.setMatchType(aggregatedType);
					
					// TODO: Ensure we take care of several rows of results 
					// for the same match (i.e., several ops for the service)
					results.put(compositeMatch.getMatchUrl(), compositeMatch);
					
					log.debug("Adding Match for " + 
							compositeMatch.getMatchUrl() + " of type " + 
							compositeMatch.getMatchType().getShortName());
				}
			}
				
				
		} finally {
			it.close();
			serviceConnector.closeRepositoryModel(repoModel);
		}

		log.info("Returning " + results.size() + " discovery results.");
		return results;
	}

	/**
	 * algorithm for outputs (how it should be, not necessarily how it is):
	 * 1) with sparql, find all service operations
	 * 	if ?cpX is set, the operation has an output message part that is a subclass of goal class X
	 *  if ?csX is set, the operation has an output message part that is a superclass of goal class X
	 *  if ?exact is set, for each goal class the operation has an output message part with equal class
	 *    
	 * 2) going through the results
	 *	if any ?cpX is set, the operation is partial-plugin match
	 *  if all ?cpX are set, the operation is plugin match
	 *  if any ?csX is set, the operation is partial-subsume match
	 *  if all ?csX are set, the operation is subsume match
	 *  if ?exact is set, the operation is an exact match
	 *        
	 * 3) each matching service has the best match degree from among its operations
	 * 
	 * @param operationDiscovery
	 * @param classes
	 * @param matches
	 * @param labels
	 * @throws DiscoveryException
	 */
	private Map<URL, MatchResult> matchOutputs(boolean operationDiscovery, List<String> classes) throws DiscoveryException {

		Map<URL, MatchResult> results = new HashMap<URL, MatchResult>();
		// Return immediately if there is nothing to discover
		if (classes == null || classes.isEmpty()) {
			return results;
		}
		
		RepositoryModel repoModel = null;
		ClosableIterator<QueryRow> it = null;	
		String query = generateOutputsQuery(classes);
		log.info("Output matching query: " + NL + query);

		try {
			repoModel = serviceConnector.openRepositoryModel();
			
			// TODO: Remove profiling
			long startTime = System.currentTimeMillis();
			
			QueryResultTable qresult = repoModel.querySelect(query, "sparql");
			
			// TODO: Remove profiling
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			log.info("Time taken for querying the registry: " + duration);
			
			it = qresult.iterator();
			while (it.hasNext()) {
				QueryRow row = it.next();
				URL matchUrl = Util.getMatchUrl(row, operationDiscovery);
				// Only continue processing if the match exists
				if (matchUrl == null) {
					log.warn("Skipping result as the URL is null");
					break;
				}
				
				String matchLabel = Util.getOrGenerateMatchLabel(row, operationDiscovery);
				
				
				// TODO: Check the variables
				// XXX: Will this ever be true?
//				Node exact = row.getValue("exact");
//				if (exact != null) {
//					if (operationDiscovery) {
//						matches.put(op, Degree.EXACT);
//					} else {
//						matches.put(svc, Degree.EXACT);
//					}
//					continue;
//				}
	
				boolean has_cpx = false;
				boolean has_csx = false;
				
				// Keep track of the best and worst match for the aggregated 
				// match type
				MatchType worstMatch = MatchType.EXACT;
				MatchType bestMatch = MatchType.FAIL;
				
				// Create an inner match per class matched
				// The overall match will be determined by the best and worst match
				Set<MatchResult> innerMatches = new HashSet<MatchResult>();
	
				for (int i = 0; i < classes.size(); i++) {
					has_cpx = Util.isVariableSet(row, "cp" + i); 
					has_csx = Util.isVariableSet(row, "cs" + i);
					
					MatchType matchType = Util.getMatchType(has_csx, has_cpx);
					if (matchType == null) {
						log.warn("Skipping result as the Match Type is null");
						break;
					}
					
					if (matchType != MatchType.FAIL) { 
						MatchResultImpl innerMatch = new MatchResultImpl(matchUrl, matchLabel);
						innerMatch.setMatchType(matchType);
						innerMatches.add(innerMatch);
					}
					
					if (matchType.compareTo(bestMatch) > 0) {
						bestMatch = matchType;
					}
					
					if (matchType.compareTo(worstMatch) < 0) {
						worstMatch = matchType;
					}
				}
				
				if (!innerMatches.isEmpty()) {
					// The service has some match. Add it
					MatchType aggregatedType = Util.calculateCompositeMatchType(bestMatch, worstMatch);
					CompositeMatchResultImpl compositeMatch = new CompositeMatchResultImpl(matchUrl, matchLabel);
					compositeMatch.setInnerMatches(innerMatches);
					compositeMatch.setMatchType(aggregatedType);
					
					// TODO: Ensure we take care of several rows of results 
					// for the same match (i.e., several ops for the service)
					results.put(compositeMatch.getMatchUrl(), compositeMatch);
				}
			}
		} finally {
			it.close();
			serviceConnector.closeRepositoryModel(repoModel);
		}
		
		log.info("Returning " + results.size() + " discovery results.");
		return results;
	}
	
	/**
	 * Generates a discovery query taken into account the given relationship
	 * between the operation and the classes identified.
	 * 
	 * The resulting query will bind the results to the following variables:
	 * ?svc -> service 
	 * ?labelSvc -> service label
	 * ?op -> operation
	 * ?labelOp -> operation label
	 * ?ic -> modelReference at the message level
	 * ?icp -> modelReference at the property level
	 * ?suX -> the match is subsumes on the class #X
	 * ?plX -> the match is plugin on the class #X
	 * ?exX -> the match is exact on the class #X
	 * 
	 * @param classes the classes we want to match
	 * @return the SPARQL query
	 */
	private String generateInputsQuery(List<String> classes) {
		// 1 exact match, 2 plugin goal inputs subclasses of service inputs, 3
		// subsume service inputs subclasses of goal inputs, 4 partial plugin, 5
		// partial subsume:
		// 1 find services that have all the goal classes
		// 2 find services that have a subclass of each of the goal classes
		// 3 find services that have a superclass of each of the goal classes
		// 4 find services that have a subclass for any of the goal classes
		// 5 find services that have a superclass for any of the goal classes
		// in the query, ?exX is exact annotation, ?cpX is annotation class for
		// plugin, ?csX is annotation class for subsume
		
		StringBuffer query = new StringBuffer();
		query.append("prefix wl: <" + MSM.WL_NS_URI + ">" + NL);
		query.append("prefix sawsdl: <" + MSM.SAWSDL_NS_URI + ">" + NL);
		query.append("prefix msm: <" + MSM.NS_URI + ">" + NL);  
		query.append("prefix rdfs: <" + RDFS.NAMESPACE + ">" + NL);
		query.append("prefix rdf: <" + RDF.NAMESPACE + ">" + NL);
		query.append("select ?svc ?labelSvc ?op ?labelOp ?ic ?icp ");
		
		for (int i = 0; i < classes.size(); i++) {
			query.append("?su" + i + " ?pl" + i + " ?ex" + i + " ");
		}
		query.append(NL);
		
		query.append("where {" + NL);
		query.append("  ?svc a msm:Service ; msm:hasOperation ?op ." + NL);
		query.append("  ?op msm:hasInput ?imsg ." + NL);
		query.append("  ?imsg msm:hasPartTransitive ?i ." + NL); 		// TODO: Ensure that hasPartTransitive is properly defined in the latest MSM version
		query.append("  OPTIONAL { ?imsg sawsdl:modelReference ?ic } " + NL); // Reference at the message level
		query.append("  OPTIONAL { ?i sawsdl:modelReference ?ic } " + NL);  // Reference at the message part level   
		query.append("  OPTIONAL { ?i sawsdl:modelReference ?prop . " + NL); // Reference to properties of the type    
		query.append("    ?prop rdfs:range ?icp . } " + NL);
		query.append("  OPTIONAL { ?svc rdfs:label ?labelSvc }" + NL);  
		query.append("  OPTIONAL { ?op rdfs:label ?labelOp }" + NL);  
		
		for (int i = 0; i < classes.size(); i++) {
			query.append("  OPTIONAL {" + NL);
			query.append("    ?ic rdfs:subClassOf <"
					+ classes.get(i).replace(">", "%3e") + "> ; ?su" + i + " <"
					+ classes.get(i).replace(">", "%3e") + "> ." + NL + "  }" + NL);
			
			query.append("  OPTIONAL {" + NL);
			query.append("    <" + classes.get(i).replace(">", "%3e")
					+ "> rdfs:subClassOf ?ic ; ?pl" + i + " ?ic ." + NL + "  }" + NL);
			
			query.append("  OPTIONAL {" + NL);
			query.append("    ?i sawsdl:modelReference <"
					+ classes.get(i).replace(">", "%3e") + "> ; ?ex" + i + " <"
					+ classes.get(i).replace(">", "%3e") + "> ." + NL + "  }" + NL);
		}
		// Take into account annotations to properties of the type
		for (int i = 0; i < classes.size(); i++) {
			query.append("  OPTIONAL {" + NL);
			query.append("    ?icp rdfs:subClassOf <"
					+ classes.get(i).replace(">", "%3e") + "> ; ?su" + i + " <"
					+ classes.get(i).replace(">", "%3e") + "> ." + NL + "  }" + NL);
			
			query.append("  OPTIONAL {" + NL);
			query.append("    <" + classes.get(i).replace(">", "%3e")
					+ "> rdfs:subClassOf ?icp ; ?pl" + i + " ?icp ." + NL + "  }" + NL);
			
			query.append("  OPTIONAL {" + NL);
			query.append("    ?icp rdfs:subClassOf <"
					+ classes.get(i).replace(">", "%3e") + "> ."
					+ "<" + classes.get(i).replace(">", "%3e") + "> rdfs:subClassOf ?icp ;"
					+ " ?ex" + i + " <"
					+ classes.get(i).replace(">", "%3e") + "> ." + NL + "  }" + NL);
		}
		query.append("}");
		return query.toString();
	}
	
	/**
	 * Generate the SPARQL Query for obtaining the matches at the outputs level.
	 * The resulting query will bind the results to the following variables:
	 * ?svc -> service 
	 * ?labelSvc -> service label
	 * ?op -> operation
	 * ?labelOp -> operation label
	 * ?oc -> modelReference at the message level
	 * ?ocp -> modelReference at the property level
	 * ?suX -> the match is subsumes on the class #X
	 * ?plX -> the match is plugin on the class #X
	 * ?exX -> the match is exact on the class #X
	 * 
	 * @param classes the classes we want to match
	 * @return the SPARQL query
	 */
	private String generateOutputsQuery(List<String> classes) {
		
		/*
		 * EXACT < PLUGIN < SUBSUME < PARTIAL PLUGIN < PARTIAL SUBSUME < FAIL
		 * 
		 * Algorithm
		 * EXACT: find services that have all the goal classes
		 * PLUGIN: find services that have a subclass of each of the goal classes
		 * SUBSUME: find services that have a superclass of each of the goal classes
		 * PARTIAL PLUGIN: find services that have a subclass for any of the 
		 * goal classes
		 * PARTIAL SUBSUME: find services that have a superclass for any of the 
		 * goal classes 
		 * 
		 * TODO: Check this
		 * In the query, ?exact signifies exact match, ?cpX is annotation class
		 * for plugin, ?csX is annotation class for subsume 
		 * 
		 * TODO: Ensure that when services have different matching operations
		 * the best match is the one that is kept
		 * 
		 * Variables:
		 * ?svc -> service 
		 * ?labelSvc -> service label
		 * ?op -> operation
		 * ?labelOp -> operation label
		 * ?oc -> modelReference at the message level
		 * ?ocp -> modelReference at the property level
		 * ?suX -> the match is subsumes on the class #X
		 * ?plX -> the match is plugin on the class #X
		 * ?exX -> the match is exact on the class #X
		 * 
		 */
		
		StringBuffer query = new StringBuffer();
		query.append("prefix wl: <" + MSM.WL_NS_URI + ">" + NL);
		query.append("prefix sawsdl: <" + MSM.SAWSDL_NS_URI + ">" + NL);
		query.append("prefix msm: <" + MSM.NS_URI + ">" + NL);  
		query.append("prefix rdfs: <" + RDFS.NAMESPACE + ">" + NL);
		query.append("prefix rdf: <" + RDF.NAMESPACE + ">" + NL);
		query.append("select ?svc ?labelSvc ?op ?labelOp ?oc ?ocp ");
		
		for (int i = 0; i < classes.size(); i++) {
			query.append("?su" + i + " ?pl" + i + " ?ex" + i + " ");
		}
		query.append(NL);
		
		query.append("where {" + NL);
		query.append("  ?svc a msm:Service ; msm:hasOperation ?op ." + NL);
		query.append("  ?op msm:hasOutput ?omsg ." + NL);
		query.append("  ?omsg msm:hasPartTransitive ?out ." + NL); 		// TODO: Ensure that hasPartTransitive is properly defined in the latest MSM version
		query.append("  OPTIONAL { ?omsg sawsdl:modelReference ?oc } " + NL); // Reference at the message level
		query.append("  OPTIONAL { ?out sawsdl:modelReference ?oc } " + NL);  // Reference at the message part level   
		query.append("  OPTIONAL { ?out sawsdl:modelReference ?prop . " + NL); // Reference to properties of the type
		query.append("    ?prop rdfs:range ?ocp . }" + NL);
		query.append("  OPTIONAL { ?svc rdfs:label ?labelSvc }" + NL);  
		query.append("  OPTIONAL { ?op rdfs:label ?labelOp }" + NL);  
		
		for (int i = 0; i < classes.size(); i++) {
			query.append("  OPTIONAL {" + NL);
			query.append("    ?oc rdfs:subClassOf <"
					+ classes.get(i).replace(">", "%3e") + "> ; ?su" + i + " <"
					+ classes.get(i).replace(">", "%3e") + "> ." + NL + "  }" + NL);
			
			query.append("  OPTIONAL {" + NL);
			query.append("    <" + classes.get(i).replace(">", "%3e")
					+ "> rdfs:subClassOf ?oc ; ?pl" + i + " ?oc ." + NL + "  }" + NL);
			
			query.append("  OPTIONAL {" + NL);
			query.append("    ?out sawsdl:modelReference <"
					+ classes.get(i).replace(">", "%3e") + "> ; ?ex" + i + " <"
					+ classes.get(i).replace(">", "%3e") + "> ." + NL + "  }" + NL);
		}
		// Take into account annotations to properties of the type
		for (int i = 0; i < classes.size(); i++) {
			query.append("  OPTIONAL {" + NL);
			query.append("    ?ocp rdfs:subClassOf <"
					+ classes.get(i).replace(">", "%3e") + "> ; ?su" + i + " <"
					+ classes.get(i).replace(">", "%3e") + "> ." + NL + "  }" + NL);
			
			query.append("  OPTIONAL {" + NL);
			query.append("    <" + classes.get(i).replace(">", "%3e")
					+ "> rdfs:subClassOf ?ocp ; ?pl" + i + " ?ocp ." + NL + "  }" + NL);
			
			query.append("  OPTIONAL {" + NL);
			query.append("    ?ocp rdfs:subClassOf <"
					+ classes.get(i).replace(">", "%3e") + "> ."
					+ "<" + classes.get(i).replace(">", "%3e") + "> rdfs:subClassOf ?ocp ;"
					+ " ?ex" + i + " <"
					+ classes.get(i).replace(">", "%3e") + "> ." + NL + "  }" + NL);
		}
		query.append("}");
		return query.toString();
	}
}
