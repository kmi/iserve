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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.ExtensibleElement;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.node.Node;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rdf2go.RepositoryModel;

import uk.ac.open.kmi.iserve.commons.io.RDFRepositoryConnector;
import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.discovery.api.DiscoveryException;
import uk.ac.open.kmi.iserve.discovery.api.IServiceDiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.api.util.DiscoveryUtil;

public class RDFSClassificationDiscoveryPlugin implements IServiceDiscoveryPlugin {

	HashMap<String,Integer> matchTypesValuesMap;

	private RDFRepositoryConnector connector;

	private int count;

	private String feedSuffix;
	
	/**
	 * This plugin supports discovery over services and operations
	 * If this is true we will discovery operations rather than services
	 */
	private boolean operationDiscovery = false;

	public RDFSClassificationDiscoveryPlugin(RDFRepositoryConnector connector, boolean operationDiscovery) {
		this.connector = connector;
		matchTypesValuesMap = new HashMap<String, Integer>();
		matchTypesValuesMap.put(DiscoveryUtil.EXACT_DEGREE, Integer.valueOf(0));
		matchTypesValuesMap.put(DiscoveryUtil.SSSOG_DEGREE, Integer.valueOf(1));
		matchTypesValuesMap.put(DiscoveryUtil.GSSOS_DEGREE, Integer.valueOf(2));
		matchTypesValuesMap.put(DiscoveryUtil.INTER_DEGREE, Integer.valueOf(3));
		this.operationDiscovery = operationDiscovery;
	}

	public String getName() {
		return "func-rdfs";
	}

	//FIXME: Replace with the actual version of the plugin
	public String getDescription() {
		return "iServe RDFS functional discovery API 2010/06/23";
	}

	//FIXME: Replace with the actual URL of the discovery endpoint for the plugin?
	public String getUri() {
		return "http://iserve.kmi.open.ac.uk/";
	}

	public String getFeedTitle() {
		String feedTitle;
		if (operationDiscovery) {
			feedTitle = "rdfs i/o discovery results: " + count + " operation(s) for " + feedSuffix;
		}
		else {
			feedTitle = "rdfs i/o discovery results: " + count + " service(s) for " + feedSuffix;
		}
		return feedTitle;
	}

	/* 
	 * FIXME: This plugin should probably implement the Ranked plugin interface
	 * (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.discovery.api.IServiceDiscoveryPlugin#discover(javax.ws.rs.core.MultivaluedMap)
	 */
	public Set<Entry> discover(MultivaluedMap<String, String> parameters) throws DiscoveryException {
		List<String> classes = parameters.get("class");
		if ( classes == null || classes.size() == 0 ) {
			throw new DiscoveryException(403, "Functional discovery without parameters is not supported - add parameter 'class=uri'");
		}
		for (int i = 0; i < classes.size(); i++) {
			if ( classes.get(i) == null) {
				throw new DiscoveryException(400, "Empty class URI not allowed");
			}
		}

		// sets of services for the 4 matching degrees
		Set<String> s_exact = new HashSet<String>();
		Set<String> s_sssog = new HashSet<String>();
		Set<String> s_gssos = new HashSet<String>();
		Set<String> s_intersection = new HashSet<String>();
		
		Map<String, String> labels = new HashMap<String,String>();

		funcClassificationDisco(classes, s_exact, s_sssog, s_gssos, s_intersection, labels);

		Set<Entry> matchingResults = serializeResults(matchTypesValuesMap.get(DiscoveryUtil.EXACT_DEGREE), 
				DiscoveryUtil.EXACT_DEGREE, s_exact, labels);
		matchingResults.addAll(serializeResults(matchTypesValuesMap.get(DiscoveryUtil.SSSOG_DEGREE),
				DiscoveryUtil.SSSOG_DEGREE, s_sssog, labels));
		matchingResults.addAll(serializeResults(matchTypesValuesMap.get(DiscoveryUtil.GSSOS_DEGREE),
				DiscoveryUtil.GSSOS_DEGREE, s_gssos, labels));
		matchingResults.addAll(serializeResults(matchTypesValuesMap.get(DiscoveryUtil.INTER_DEGREE),
				DiscoveryUtil.INTER_DEGREE, s_intersection, labels));

		count = s_exact.size() + s_sssog.size() + s_gssos.size() + s_intersection.size();

		feedSuffix = "";
        for (int i = 0; i < classes.size(); i++) {
        	feedSuffix += xmlEncode(classes.get(i)) + ((i<classes.size() - 1) ? ", " : "");
        }

        return matchingResults;
	}

	/**
	 * FIXME: Make this return a sorted set a comparator so that results are ordered 
	 * @param degreeNum
	 * @param degree
	 * @param results
	 * @param labels
	 * @return
	 */
	private Set<Entry> serializeResults(int degreeNum, String degree, Set<String> results, Map<String, String> labels) {
		Set<Entry> matchingResults = new HashSet<Entry>();
		for (Iterator<String> it = results.iterator(); it.hasNext();) {
			String item = it.next();
			String content = "Matching degree: " + degree;
			Entry result = DiscoveryUtil.getAbderaInstance().newEntry();
			result.setId(item);
			result.addLink(item, "alternate");
			result.setTitle(labels.get(item));
			ExtensibleElement e = result.addExtension(DiscoveryUtil.MATCH_DEGREE);
			e.setAttributeValue("num", Integer.toString(degreeNum));
			e.setText(degree);
			result.setContent(content);
			matchingResults.add(result);
		}
		return matchingResults;
	}

	private void funcClassificationDisco(List<String> classes, Set<String> exact, Set<String> itemSubclassOfGoal,
            Set<String> goalSubclassOfItem, Set<String> intersection, Map<String, String> labels) {
        Set<String> goalNotSubclassOfItem = new HashSet<String>();

        RepositoryModel repoModel = connector.openRepositoryModel();
        
        // todo extension: don't care about WSMO-Lite wl:FCR
        
        // 1 exact match, 2 service subset of goal, 3 goal subset of service:
        // 1,2 find services that have a subcategory of each of the goal categories			
        // 1,3 find services for which the goal has a subcategory of every service category
        // in the query, the presence of sssog0 means that the service contains subcategories of all goal categories (service is a subset of goal)
        // the presence of gX means the goal contains a subcategory of a class category; if every row for a service contains at least one gX then the goal is a subset of service
        // todo what about kinda-gssos where all goal classes are subclasses of service classes? it's stronger gssos if also all service classes have goal subclasses.
        
        String query = "prefix wl: <" + MSM.WL_NS_URI + ">\n"
        		+ "prefix sawsdl: <" + MSM.SAWSDL_NS_URI + ">\n"
        		+ "prefix msm: <" + MSM.NS_URI + ">\n"
        		+ "prefix rdfs: <" + RDFS.NAMESPACE + ">\n"
        		+ "select ?item ?label ?sssog0 ?cat ";
        
        for (int i=0; i<classes.size(); i++) {
        	query += "?g" + i + " ";
        }
        
        String itemTypeStatement = "?item a msm:Service ";
        if (operationDiscovery) {
        	// Operations should inherit the service classifications
        	// FIXME: Return the actual opertaion not the SERVICE
        	itemTypeStatement += ". \n OPTIONAL {?item a msm:Operation} ";
        }
        
        query += "\nwhere {\n  " 
        		+ itemTypeStatement + " . \n"
        		+ "?item sawsdl:modelReference ?cat .\n"
        		+ "  ?cat rdfs:subClassOf [ a wl:FunctionalClassificationRoot ] .\n" 
        		+ "  optional { ?item rdfs:label ?label }";
        
        for (int i = 0; i < classes.size(); i++) {
        	query += "  optional {\n"
        			+ "    <" + classes.get(i).replace(">", "%3e") + "> rdfs:subClassOf ?cat ; ?g" + i + " ?cat .\n"
        			+ "  }\n";
        }
        query += "  optional {\n";
        for (int i = 0; i < classes.size(); i++) {
        	query += "    ?item sawsdl:modelReference ?sssog" + i + " . \n    ?sssog" + i + " rdfs:subClassOf <" + classes.get(i).replace(">", "%3e") + "> .\n";
        }
        query += "  }\n";
        query += "}";
        
        
        System.out.println("query: \n" + query);

        QueryResultTable qresult = repoModel.querySelect(query, "sparql");
        for (Iterator<QueryRow> it = qresult.iterator(); it.hasNext();) {
        	QueryRow row = it.next();
        	String item = row.getValue("item").toString();
        	Node sssog0 = row.getValue("sssog0");
        	if (sssog0 != null) {
        		itemSubclassOfGoal.add(item);
        	}
        	goalSubclassOfItem.add(item); // initially, all services are counted as being supersets of the goal, below the set s_notgssos counts the instances that aren't, which are removed from s_gssos after the loop
        	boolean lacks_gX = true;
        	for (int i=0; i<classes.size(); i++) {
        		Node gi = row.getValue("g"+i);
        		if (gi != null) {
        			lacks_gX = false;
        			break;
        		}
        	}
        	if (lacks_gX) {
        		goalNotSubclassOfItem.add(item);
        	}
        	
        	Node label = row.getValue("label");
        	if (label != null) {
        		labels.put(item, label.toString());
        	}
        }
        goalSubclassOfItem.removeAll(goalNotSubclassOfItem);
        
        exact.addAll(itemSubclassOfGoal);
        exact.retainAll(goalSubclassOfItem);
        itemSubclassOfGoal.removeAll(exact);
        goalSubclassOfItem.removeAll(exact);
        
        // intersection match:
        // TODO: find services for which there exists a category that is a subcategory of all the categories of both the goal and the svc; but remove any from above
        // TODO: also find potential intersections where some service and goal classes are related through subclass
        
        connector.closeRepositoryModel(repoModel);
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
