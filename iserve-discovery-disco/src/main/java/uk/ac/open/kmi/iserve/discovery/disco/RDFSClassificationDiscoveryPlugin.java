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

import java.net.MalformedURLException;
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
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rdf2go.RepositoryModel;
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

/**
 * Plugin for discovering both services and operations given Functional Classifications
 * using RDFS reasoning. This plugin requires that the underlying Service Manager
 * is based on an RDF Store.
 * 
 * @author Jacek Kopecky (Knowledge Media Institute - The Open University)
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public class RDFSClassificationDiscoveryPlugin implements ServiceDiscoveryPlugin, OperationDiscoveryPlugin {

	// Discovery Pluging Parameters
	
	private static final String CLASS_PARAMETER = "class";
	private static final String CLASS_PARAM_DESCRIPTION = "This parameter should" +
			"contain a list of 1 or more URLs of concepts identifying the " +
			"Functional Classifications we wish to use for discovery.";
	
	private static final Map<String, String> parameterDetails;

	static {
		parameterDetails = new HashMap<String, String>();
		parameterDetails.put(CLASS_PARAMETER, CLASS_PARAM_DESCRIPTION);
	}

	private static final Logger log = LoggerFactory.getLogger(RDFSClassificationDiscoveryPlugin.class);
	
	public static String NEW_LINE = System.getProperty("line.separator");

	HashMap<String,Integer> matchTypesValuesMap;

	private int count;

	private String feedSuffix;

	private RDFRepositoryConnector serviceConnector;

	public RDFSClassificationDiscoveryPlugin() {
		
		//TODO: Replace this with an Enum
		matchTypesValuesMap = new HashMap<String, Integer>();
		matchTypesValuesMap.put(DiscoveryUtil.EXACT_DEGREE, Integer.valueOf(0));
		matchTypesValuesMap.put(DiscoveryUtil.SSSOG_DEGREE, Integer.valueOf(1));
		matchTypesValuesMap.put(DiscoveryUtil.GSSOS_DEGREE, Integer.valueOf(2));
		matchTypesValuesMap.put(DiscoveryUtil.INTER_DEGREE, Integer.valueOf(3));
		
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
		return "func-rdfs";
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.discovery.api.DiscoveryPlugin#getDescription()
	 */
	@Override
	public String getDescription() {
		return "iServe RDFS functional discovery plugin. Discovers services and " +
				"operations based on their functional classification using " +
				"subsumption reasoning as supported by the underlying RDF Store.";
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.discovery.api.IServiceDiscoveryPlugin#getVersion()
	 */
	@Override
	public String getVersion() {
		return "v1.1.2";
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.discovery.api.DiscoveryPlugin#getFeedTitle()
	 */
	@Override
	public String getFeedTitle() {
		String feedTitle = "RDFS Functional Classification discovery results: " + 
			count + " service(s) or operation(s) for " + feedSuffix;
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
	public Map<URL, MatchResult> discoverOperations(MultivaluedMap<String, String> parameters) throws DiscoveryException {
		return discover(true, parameters);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.discovery.api.DiscoveryPlugin#getParametersDetails()
	 */
	@Override
	public Map<String, String> getParametersDetails() {
		return parameterDetails;
	}
	
	/**
	 * @param operationDiscovery
	 * @param parameters
	 * @return
	 * @throws DiscoveryException
	 */
	public Map<URL, MatchResult> discover(boolean operationDiscovery, MultivaluedMap<String, String> parameters) throws DiscoveryException {
	
		// If there is no service connector raise an error 
		if (serviceConnector == null) {
			throw new DiscoveryException("The '" + this.getName() + "' " + 
							this.getVersion() + 
							" the RDF connector to the services repository is null.");
		}
		
		List<String> classes = parameters.get(CLASS_PARAMETER);
		if ( classes == null || classes.size() == 0 ) {
			throw new DiscoveryException(403, "Functional discovery without parameters is not supported - add parameter 'class=uri'");
		}
		for (int i = 0; i < classes.size(); i++) {
			if ( classes.get(i) == null) {
				throw new DiscoveryException(400, "Empty class URI not allowed");
			}
		}

		Map<URL, MatchResult> results = funcClassificationDisco(operationDiscovery, classes);

		return results;
	}

	private Map<URL, MatchResult> funcClassificationDisco(boolean operationDiscovery, List<String> classes) {
		
		Map<URL, MatchResult> results = new HashMap<URL, MatchResult>();
		// Return immediately if there is nothing to discover
		if (classes == null || classes.isEmpty()) {
			return results;
		}
		

		String query = generateQuery(operationDiscovery, classes);
		log.info("Querying for services: \n" + query);
		
		RepositoryModel repoModel = null;
		try{
			repoModel = serviceConnector.openRepositoryModel();
			QueryResultTable qresult = repoModel.querySelect(query.toString(), "sparql");
			for (Iterator<QueryRow> it = qresult.iterator(); it.hasNext();) {
				QueryRow row = it.next();
	
				String svcUri = row.getValue("svc").toString();
				String svcLabel = null;
				String opUri = null;
				String opLabel = null;
				String item;
	
				Node label = row.getValue("labelSvc");
				if (label != null) {
					svcLabel = label.toString();
				}
	
				if (operationDiscovery) {
					opUri = row.getValue("op").toString();
					label = row.getValue("labelOp");
					if (label != null) {
						opLabel = label.toString();
					}
					item = opUri;
				} else {
					item = svcUri;
				}
				
				// Prepare the Match result instance
				URL itemURL = new URL(item);
				String itemLabel = null;
				if (svcLabel != null) {
					itemLabel = svcLabel;
				}
				if (opLabel != null) {
					itemLabel += "." + opLabel;
				}
	
				Node sssog0 = row.getValue("sssog0");
				if (sssog0 != null) {
					results.put(itemURL, new SimpleMatchResult(itemURL, itemLabel, MatchType.SSSOG, null, null) );
//					itemSubclassOfGoal.add(item);
				}
				// initially, all services are counted as being supersets of the goal, 
				// below the set s_notgssos counts the instances that aren't, which 
				// are removed from s_gssos after the loop
//				goalSubclassOfItem.add(item);
				
				// Only add to GSSOS if no gX is missing
				boolean lacks_gX = false;
				for (int i = 0; i < classes.size(); i++) {
					Node gi = row.getValue("g" + i);
					if (gi == null) {
						lacks_gX = true;
						break;
					}
				}
				
				if (!lacks_gX) {
					// The service is either GSSOS or Exact if it is SSOG too
					if (results.containsKey(itemURL)) {
						// If it is there, it's a SSOG too
						results.put(itemURL, new SimpleMatchResult(itemURL, itemLabel, MatchType.EXACT, null, null) );
					} else {
						results.put(itemURL, new SimpleMatchResult(itemURL, itemLabel, MatchType.GSSOS, null, null) );
					}
						
//					goalNotSubclassOfItem.add(item);
				}
	
			}
//			goalSubclassOfItem.removeAll(goalNotSubclassOfItem);
			
//			exact.addAll(itemSubclassOfGoal);
//			exact.retainAll(goalSubclassOfItem);	
			
//			itemSubclassOfGoal.removeAll(exact);
//			goalSubclassOfItem.removeAll(exact);
			
		} catch (MalformedURLException e) {
			log.error("Unable to create URL for item.", e);
		} finally {
			serviceConnector.closeRepositoryModel(repoModel);
		}
		
		return results;
		
	}

	/**
	 * Discovery query for functional classification of items
	 * 
	 * TODO extension: don't care about WSMO-Lite wl:FCR
	 * TODO: what about kinda-gssos where all goal classes are subclasses of 
	 * service classes? it's stronger gssos if also all service classes have 
	 * goal subclasses.
	 * TODO: Intersection match
	 *  - find services for which there exists a category that is a subcategory 
	 *  of all the categories of both the goal and the svc; but remove any from 
	 *  above
	 * 	- also find potential intersections where some service and goal classes 
	 * are related through subclass
	 * 
	 * 1 exact match, 2 service subset of goal, 3 goal subset of service:
	 * 1,2 find services that have a subcategory of each of the goal categories			
	 * 1,3 find services for which the goal has a subcategory of every service category
	 * 
	 * In the query, the presence of sssog0 means that the service contains 
	 * subcategories of all goal categories (service is a subset of goal)
	 * 
	 * The presence of gX means the goal contains a subcategory of a class 
	 * category; if every row for a service contains at least one gX then the 
	 * goal is a subset of service
	 * 
	 * 
	 * Evaluating this query should lead to the following variables:
	 * ?svc - indicates the service
	 * ?labelSvc - contains the label of the service
	 * ?catSvc - contains the categories of the service
	 * ?sssog0 - contains all the sssog
	 * ?op - indicates the op (if necessary)
	 * ?labelOp - contains the label of the operation (if necessary)
	 * ?catOp - contains the categories of the operation (if necessary)
	 * ?gX as in ?g0, ?g1 - contain the goal classes
	 * 
	 * @param operationDiscovery
	 * @param classes
	 * @return
	 */
	private String generateQuery(boolean operationDiscovery,
			List<String> classes) {
		
		log.debug("Generating query for classes: " + classes.toString());
		
		StringBuffer query = new StringBuffer("prefix wl: <" + MSM.WL_NS_URI + ">" + NEW_LINE);
		query.append("prefix sawsdl: <" + MSM.SAWSDL_NS_URI + ">" + NEW_LINE);
		query.append("prefix msm: <" + MSM.NS_URI + ">" + NEW_LINE);
		query.append("prefix rdfs: <" + RDFS.NAMESPACE + ">" + NEW_LINE);
		
		query.append("select ?svc ?labelSvc ?catSvc ?sssog0");
		if (operationDiscovery) {
			query.append(" ?op  ?labelOp  ?catOp ");
		}

		for (int i=0; i<classes.size(); i++) {
			query.append("?g" + i + " ");
		}

		query.append(NEW_LINE + "where {" + NEW_LINE);
		query.append("?svc a msm:Service . " + NEW_LINE);
		query.append("optional { ?svc rdfs:label ?labelSvc } " + NEW_LINE);

		if (operationDiscovery) {
			query.append("?svc msm:hasOperation ?op . " + NEW_LINE);
			query.append("?op a msm:Operation . " + NEW_LINE);
			query.append("optional { ?op rdfs:label ?labelOp } " + NEW_LINE);
		}

		// Generate the optional query for SVC and subclasses of the FC
		query.append("optional {");
		query.append("?svc sawsdl:modelReference ?catSvc ." + NEW_LINE);
		query.append("  ?catSvc rdfs:subClassOf [ a wl:FunctionalClassificationRoot ] . " + NEW_LINE) ;

		for (int i = 0; i < classes.size(); i++) {
			query.append(" <" + classes.get(i).replace(">", "%3e") + "> rdfs:subClassOf ?catSvc ; ?g" + i + " ?catSvc ." + NEW_LINE) ;
		}

		query.append("  }" + NEW_LINE);
		// End

		// Generate the optional query for OP and subclasses of the FC
		if (operationDiscovery) {
			query.append("optional {");
			query.append("?op sawsdl:modelReference ?catOp . " + NEW_LINE);
			query.append("?catOp rdfs:subClassOf [ a wl:FunctionalClassificationRoot ] . " + NEW_LINE);

			for (int i = 0; i < classes.size(); i++) {
				query.append("    <" + classes.get(i).replace(">", "%3e") + "> rdfs:subClassOf ?catOp ; ?g" + i + " ?catOp ." + NEW_LINE);
			}
			query.append("  }" + NEW_LINE);
		}		        

		query.append("  optional {" + NEW_LINE);
		for (int i = 0; i < classes.size(); i++) {
			query.append("    ?svc sawsdl:modelReference ?sssog" + i + " . " + NEW_LINE);
			query.append("    ?sssog" + i + " rdfs:subClassOf <" + classes.get(i).replace(">", "%3e") + "> ." + NEW_LINE);
		}
		query.append("  }" + NEW_LINE);

		if (operationDiscovery) {
			query.append("  optional {" + NEW_LINE);
			for (int i = 0; i < classes.size(); i++) {
				query.append("    ?op sawsdl:modelReference ?sssog" + i + " . " + NEW_LINE);
				query.append("    ?sssog" + i + " rdfs:subClassOf <" + classes.get(i).replace(">", "%3e") + "> ." + NEW_LINE);
			}
			query.append("  }" + NEW_LINE);
		}

		query.append("}");
		
		return query.toString();
	}

	/**
	 * encode string for XML element value
	 * @param src string
	 * @return encoded string
	 */
	public static String xmlEncode(String src) {
		if (src == null) {
			return "";
		}
		return src.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	/**
	 * encode string for an XML attribute value
	 * @param src string
	 * @return encoded string
	 */
	public static String xmlAttEncode(String src) {
		return xmlEncode(src).replace("'", "&apos;").replace("\"", "&quot;");
	}
}
