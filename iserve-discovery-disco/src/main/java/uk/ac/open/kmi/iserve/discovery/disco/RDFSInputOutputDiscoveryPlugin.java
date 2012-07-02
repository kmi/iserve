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

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.ExtensibleElement;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.node.Node;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rdf2go.RepositoryModel;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.iserve.commons.io.RDFRepositoryConnector;
import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.discovery.api.DiscoveryException;
import uk.ac.open.kmi.iserve.discovery.api.DiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.OperationDiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.api.ServiceDiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.util.DiscoveryUtil;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;
import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;
import uk.ac.open.kmi.iserve.sal.manager.impl.ServiceManagerRdf;

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
	
	public static String NEW_LINE = System.getProperty("line.separator");
	
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
	 * @param operationDiscovery
	 * @param parameters
	 * @return
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
		if ( matchingInputs == true ) {
			for (String clazz : inputClasses) {
				if (clazz == null) {
					throw new DiscoveryException(400, "Empty class URI not allowed");
				}
			}
		}
		if ( matchingOutputs == true ) {
			for (String clazz : outputClasses) {
				if (clazz == null) {
					throw new DiscoveryException(400, "Empty class URI not allowed");
				}
			}
		}
		String andOr = parameters.getFirst(FUNCTION_PARAM);
		boolean intersection = AND_FUNCTION.equals(andOr);

		// sets of matching services
		Map<String, Degree> s_input = new HashMap<String, Degree>();
		Map<String, Degree> s_output = new HashMap<String, Degree>();
		Map<String, String> labels = new HashMap<String, String>();

		Set<String> matches = new HashSet<String>();

		if (matchingInputs) {
			matchInputs(operationDiscovery, inputClasses, s_input, labels);
			matches.addAll(s_input.keySet());
		}
		if (matchingOutputs) {
			matchOutputs(operationDiscovery, outputClasses, s_output, labels);
			if (matchingInputs && intersection) {
				matches.retainAll(s_output.keySet());
			} else {
				matches.addAll(s_output.keySet());
			}
		}
		if (matchingInputs && matchingOutputs) {
			for (String svc : s_input.keySet()) {
				if (!s_output.containsKey(svc)) {
					s_output.put(svc, Degree.FAIL);
				}
			}
			for (String svc : s_output.keySet()) {
				if (!s_input.containsKey(svc)) {
					s_input.put(svc, Degree.FAIL);
				}
			}
		}

		SortedSet<Entry> matchingResults = serializeMatches(matches, s_input, s_output, labels);

		count = matchingResults.size();

		if (matchingInputs) {
			feedSuffix += inputClasses.size() + " provided inputs: ";
			for (int i = 0; i < inputClasses.size(); i++) {
				feedSuffix += inputClasses.get(i)
				+ ((i < inputClasses.size() - 1) ? ", " : "");
			}
			if (matchingOutputs) {
				feedSuffix += " and ";
			}
		}
		if (matchingOutputs) {
			feedSuffix += outputClasses.size() + " requested outputs: ";
			for (int i = 0; i < outputClasses.size(); i++) {
				feedSuffix += outputClasses.get(i)
				+ ((i < outputClasses.size() - 1) ? ", " : "");
			}
		}

//		return matchingResults;
		return null;
	}

	/**
	 * TODO: Implement a proper comparator so that the services are ranked
	 * appropriately
	 * @param matches
	 * @param inputDegrees
	 * @param outputDegrees
	 * @param labels
	 * @return
	 */
	private SortedSet<Entry> serializeMatches(Set<String> matches, Map<String, Degree> inputDegrees, Map<String, Degree> outputDegrees, Map<String, String> labels) {
		SortedSet<Entry> matchingResults = new TreeSet<Entry>(new EntryComparatorClassificationMatching());

		final Map<String, String> combinedMatchDegrees = new HashMap<String, String>();
		Map<String, String> degreeNames = new HashMap<String, String>();
		Map<String, String> degreeDescs = new HashMap<String, String>();
		
		log.info("Serialising " + matches.size() + " discovery results.");

		for (String match : matches) {
			Degree iDeg = inputDegrees.get(match);
			Degree oDeg = outputDegrees.get(match);
			Degree degreeMajor = iDeg;
			Degree degreeMinor = oDeg;
			// switch them if output is a better match than input
			if (iDeg == null || oDeg != null && oDeg.compareTo(iDeg) < 0) {
				degreeMajor = oDeg;
				degreeMinor = iDeg;
			}
			String degreeNum = Integer.toString(degreeMajor.ordinal()); 
			String degreeName = degreeMajor.toString();
			if (degreeMinor != null) { 
				degreeNum += "." + degreeMinor.ordinal(); 
				if (degreeMinor != Degree.FAIL) {
					degreeName += "." + degreeMinor.toString();
				}
			}
			String degreeDesc = "";
			if (iDeg != null) {
				degreeDesc = "input " + iDeg.toString();
				if (oDeg != null) {
					degreeDesc += ", ";
				}
			}
			if (oDeg != null) {
				degreeDesc += "output " + oDeg.toString();
			}
			combinedMatchDegrees.put(match, degreeNum);
			degreeNames.put(degreeNum, degreeName);
			degreeDescs.put(degreeNum, degreeDesc);
		}
		
		for (String match : matches) {			
			String degreeNum = combinedMatchDegrees.get(match);
			Entry result = DiscoveryUtil.getAbderaInstance().newEntry();
			result.addLink(match);
			result.setTitle(labels.get(match));
			ExtensibleElement e = result.addExtension(DiscoveryUtil.MATCH_DEGREE);
			e.setAttributeValue("num", degreeNum);
			e.setText(degreeNames.get(degreeNum));
			result.setContent(degreeDescs.get(degreeNum));
			matchingResults.add(result);
		}

		return matchingResults;
	}

	private void matchInputs(boolean operationDiscovery, List<String> classes, Map<String, Degree> matches, Map<String, String> labels) throws DiscoveryException {

		RepositoryModel repoModel = serviceConnector.openRepositoryModel();
		
		String query = generateQuery(MSM.NS_URI + "hasInput", classes);
		log.info("Input matching query: " + NEW_LINE + query);

		QueryResultTable qresult = repoModel.querySelect(query, "sparql");

		Map<String, String> op2svc = new HashMap<String, String>();
		Set<String> opExact = new HashSet<String>();
		Set<String> opPlugin = new HashSet<String>();
		Set<String> opSubsume = new HashSet<String>();
		Set<String> opNotExact = new HashSet<String>();
		Set<String> opNotPlugin = new HashSet<String>();
		Set<String> opNotSubsume = new HashSet<String>();
		Set<String> opPartPlugin = new HashSet<String>();
		Set<String> opPartSubsume = new HashSet<String>();

		for (Iterator<QueryRow> it = qresult.iterator(); it.hasNext();) {
			QueryRow row = it.next();
			String svc = row.getValue("svc").toString();
			String labelSvc = null;
			Node label = row.getValue("labelSvc");
			if (label != null) {
				labelSvc = label.toString();
			}
			
			String op = row.getValue("op").toString();
			String labelOp = null;
			label = row.getValue("labelOp");
			if (label != null) {
				labelOp = label.toString();
			}
			
			op2svc.put(op, svc);
			// initially all ops are assumed to match until proven otherwise
			// (below)
			opExact.add(op);
			opPlugin.add(op);
			opSubsume.add(op);

//			String entryTitle = DiscoveryUtil.createMatchLabel(operationDiscovery, svc, labelSvc, op, labelOp);
			String entryTitle = null;
			if (operationDiscovery) {
				labels.put(op, entryTitle);
			} else {
				labels.put(svc, entryTitle);
			}

			boolean has_ex = false;
			boolean has_pl = false;
			boolean has_su = false;

			for (int i = 0; i < classes.size(); i++) {
				Node val = row.getValue("su" + i);
				if (val != null) {
					has_su = true;
				}
				val = row.getValue("pl" + i);
				if (val != null) {
					has_pl = true;
				}
				val = row.getValue("ex" + i);
				if (val != null) {
					has_ex = true;
				}
			}
			if (!has_su) {
				opNotSubsume.add(op);
			} else {
				opPartSubsume.add(op);
			}
			if (!has_pl) {
				opNotPlugin.add(op);
			} else {
				opPartPlugin.add(op);
			}
			if (!has_ex) {
				opNotExact.add(op);
			}
		}

		// FIXME: This is inefficient
		opExact.removeAll(opNotExact);
		opPlugin.removeAll(opNotPlugin);
		opSubsume.removeAll(opNotSubsume);

		// the following order is significant - worse matches get rewritten by
		// better ones
		for (String op : opPartSubsume) {
			if (operationDiscovery) {
				matches.put(op, Degree.PARTIAL_SUBSUME);
			} else {
				matches.put(op2svc.get(op), Degree.PARTIAL_SUBSUME);
			}
		}
		for (String op : opPartPlugin) {
			if (operationDiscovery) {
				matches.put(op, Degree.PARTIAL_PLUGIN);
			} else {
				matches.put(op2svc.get(op), Degree.PARTIAL_PLUGIN);
			}
		}
		for (String op : opSubsume) {
			if (operationDiscovery) {
				matches.put(op, Degree.SUBSUME);
			} else {
				matches.put(op2svc.get(op), Degree.SUBSUME);
			}
		}
		for (String op : opPlugin) {
			if (operationDiscovery) {
				matches.put(op, Degree.PLUGIN);
			} else {
				matches.put(op2svc.get(op), Degree.PLUGIN);
			}
		}
		for (String op : opExact) {
			if (operationDiscovery) {
				matches.put(op, Degree.EXACT);
			} else {
				matches.put(op2svc.get(op), Degree.EXACT);
			}
		}
		
		//TODO: handle connection properly
		serviceConnector.closeRepositoryModel(repoModel);
		
		log.info("Returning " + matches.size() + " discovery results.");
	}

	private void matchOutputs(boolean operationDiscovery, List<String> classes, Map<String, Degree> matches, Map<String, String> labels) throws DiscoveryException {

		RepositoryModel repoModel = serviceConnector.openRepositoryModel();
		
		String query = generateQuery(MSM.NS_URI + "hasOutput", classes);
		log.info("Output matching query: " + NEW_LINE + query);

		QueryResultTable qresult = repoModel.querySelect(query, "sparql");
		for (Iterator<QueryRow> it = qresult.iterator(); it.hasNext();) {
			QueryRow row = it.next();
			String svc = row.getValue("svc").toString();
			String op = row.getValue("op").toString();

			Node label = row.getValue("labels");
			if (label != null) {
				labels.put(svc, label.toString());
			}
			label = row.getValue("labelop");
			if (label != null) {
				labels.put(op, label.toString());
			}

			Node exact = row.getValue("exact");
			if (exact != null) {
				if (operationDiscovery) {
					matches.put(op, Degree.EXACT);
				} else {
					matches.put(svc, Degree.EXACT);
				}
				continue;
			}

			boolean has_cpx = false;
			boolean misses_cpx = false;
			boolean has_csx = false;
			boolean misses_csx = false;
			for (int i = 0; i < classes.size(); i++) {
				Node val = row.getValue("cp" + i);
				if (val != null) {
					has_cpx = true;
				} else {
					misses_cpx = true;
				}
				val = row.getValue("cs" + i);
				if (val != null) {
					has_csx = true;
				} else {
					misses_csx = true;
				}
			}
			Degree degree = Degree.FAIL;
			if (has_cpx) {
				if (misses_cpx) {
					degree = Degree.PARTIAL_PLUGIN;
				} else {
					degree = Degree.PLUGIN;
				}
			} else {
				if (has_csx) {
					if (misses_csx) {
						degree = Degree.PARTIAL_SUBSUME;
					} else {
						degree = Degree.SUBSUME;
					}
				}
			}
			Degree oldDegree;
			if (operationDiscovery) {
				oldDegree = matches.get(op);
			} else {
				oldDegree = matches.get(svc);
			}
			
			if (degree != Degree.FAIL
					&& (oldDegree == null || oldDegree.compareTo(degree) > 0)) {
				
				if (operationDiscovery) {
					matches.put(op, degree);
				} else {
					matches.put(svc, degree);
				}
				matches.put(svc, degree);
			}

		}
		//TODO: handle connection properly
		serviceConnector.closeRepositoryModel(repoModel);
		log.info("Returning " + matches.size() + " discovery results.");
	}
	
	/**
	 * Generates a discovery query taken into account the given relationship
	 * between the operation and the classes identified.
	 * 
	 * @param relOperationAndClasses The relationship between operations and 
	 * classes that we are looking for. 
	 * @param classes 
	 * 
	 * @return
	 */
	private String generateQuery(String relOperationAndClasses, List<String> classes) {
		
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
		query.append("prefix wl: <" + MSM.WL_NS_URI + ">" + NEW_LINE);
		query.append("prefix sawsdl: <" + MSM.SAWSDL_NS_URI + ">" + NEW_LINE);
		query.append("prefix msm: <" + MSM.NS_URI + ">" + NEW_LINE);  
		query.append("prefix rdfs: <" + RDFS.NAMESPACE + ">" + NEW_LINE);
		query.append("prefix rdf: <" + RDF.NAMESPACE + ">" + NEW_LINE);
		query.append("select ?svc ?labels ?op ?labelop ?ic ?icp ");
		
		for (int i = 0; i < classes.size(); i++) {
			query.append("?su" + i + " ?pl" + i + " ?ex" + i + " ");
		}
		
		query.append("\nwhere {" + NEW_LINE);
		query.append("  ?svc a msm:Service ; msm:hasOperation ?op ." + NEW_LINE);
		query.append("  ?op <" + relOperationAndClasses + "> ?imsg ." + NEW_LINE);
		query.append("  ?imsg msm:hasPartTransitive ?i ." + NEW_LINE);
		query.append("  ?i sawsdl:modelReference ?ic . " + NEW_LINE);     
		query.append("  ?i sawsdl:modelReference ?prop . " + NEW_LINE);   // Take into account annotations to properties of the type
		query.append("  ?prop rdfs:range ?icp . " + NEW_LINE);
		query.append("  OPTIONAL { ?svc rdfs:label ?labels }" + NEW_LINE);
		query.append("  OPTIONAL { ?op rdfs:label ?labelop }" + NEW_LINE);
		
		for (int i = 0; i < classes.size(); i++) {
			query.append("  OPTIONAL {" + NEW_LINE);
			query.append("    ?ic rdfs:subClassOf <"
					+ classes.get(i).replace(">", "%3e") + "> ; ?su" + i + " <"
					+ classes.get(i).replace(">", "%3e") + "> ." + NEW_LINE + "  }" + NEW_LINE);
			
			query.append("  OPTIONAL {" + NEW_LINE);
			query.append("    <" + classes.get(i).replace(">", "%3e")
					+ "> rdfs:subClassOf ?ic ; ?pl" + i + " ?ic ." + NEW_LINE + "  }" + NEW_LINE);
			
			query.append("  OPTIONAL {" + NEW_LINE);
			query.append("    ?i sawsdl:modelReference <"
					+ classes.get(i).replace(">", "%3e") + "> ; ?ex" + i + " <"
					+ classes.get(i).replace(">", "%3e") + "> ." + NEW_LINE + "  }" + NEW_LINE);
		}
		// Take into account annotations to properties of the type
		for (int i = 0; i < classes.size(); i++) {
			query.append("  OPTIONAL {" + NEW_LINE);
			query.append("    ?icp rdfs:subClassOf <"
					+ classes.get(i).replace(">", "%3e") + "> ; ?su" + i + " <"
					+ classes.get(i).replace(">", "%3e") + "> ." + NEW_LINE + "  }" + NEW_LINE);
			
			query.append("  OPTIONAL {" + NEW_LINE);
			query.append("    <" + classes.get(i).replace(">", "%3e")
					+ "> rdfs:subClassOf ?icp ; ?pl" + i + " ?icp ." + NEW_LINE + "  }" + NEW_LINE);
			
			query.append("  OPTIONAL {" + NEW_LINE);
			query.append("    ?icp rdfs:subClassOf <"
					+ classes.get(i).replace(">", "%3e") + "> ."
					+ "<" + classes.get(i).replace(">", "%3e") + "> rdfs:subClassOf ?icp ;"
					+ " ?ex" + i + " <"
					+ classes.get(i).replace(">", "%3e") + "> ." + NEW_LINE + "  }" + NEW_LINE);
		}
		query.append("}");
		return query.toString();
	}
}
