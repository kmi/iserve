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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.ExtensibleElement;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.node.Node;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rdf2go.RepositoryModel;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.iserve.commons.io.RDFRepositoryConnector;
import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.discovery.api.DiscoveryException;
import uk.ac.open.kmi.iserve.discovery.api.IServiceDiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.api.util.DiscoveryUtil;

public class RDFSInputOutputDiscoveryPlugin implements IServiceDiscoveryPlugin {

	private enum Degree {
		EXACT, PLUGIN, SUBSUME, PARTIAL_PLUGIN, PARTIAL_SUBSUME, FAIL
	};

	private RDFRepositoryConnector connector;

	private int count;

	private String feedSuffix;

	public RDFSInputOutputDiscoveryPlugin() throws RepositoryException, IOException {
		this.connector = Factory.getInstance().createRDFRepositoryConnector();
		this.count = 0;
	}

	public String getName() {
		return "io-rdfs";
	}

	/* 
	 * FIXME: This plugin should probably implement the Ranked Service interface
	 * (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.discovery.api.IServiceDiscoveryPlugin#discover(javax.ws.rs.core.MultivaluedMap)
	 */
	public Set<Entry> discover(MultivaluedMap<String, String> parameters) throws DiscoveryException {
		List<String> inputClasses = parameters.get("i");
		List<String> outputClasses = parameters.get("o");

		boolean matchingInputs = inputClasses != null && inputClasses.size() != 0;
		boolean matchingOutputs = outputClasses != null && outputClasses.size() != 0;
		if (!matchingInputs && !matchingOutputs) {
			throw new DiscoveryException(403, "IO discovery without parameters is not supported");
		}
		if ( matchingInputs == true ) {
			for (String clazz : inputClasses) {
				if (clazz == null) {
					throw new DiscoveryException(400, "empty class URI not allowed");
				}
			}
		}
		if ( matchingOutputs == true ) {
			for (String clazz : outputClasses) {
				if (clazz == null) {
					throw new DiscoveryException(400, "empty class URI not allowed");
				}
			}
		}
		String andOr = parameters.getFirst("f");
		boolean intersection = "and".equals(andOr);

		// sets of matching services
		Map<String, Degree> s_input = new HashMap<String, Degree>();
		Map<String, Degree> s_output = new HashMap<String, Degree>();
		Map<String, String> labels = new HashMap<String, String>();

		Set<String> matches = new HashSet<String>();

		if (matchingInputs) {
			matchInputs(inputClasses, s_input, labels);
			matches.addAll(s_input.keySet());
		}
		if (matchingOutputs) {
			matchOutputs(outputClasses, s_output, labels);
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

		Set<Entry> matchingResults = serializeServices(matches, s_input, s_output, labels);

		count = matchingResults.size();

		if (matchingInputs) {
			feedSuffix += "provided inputs: ";
			for (int i = 0; i < inputClasses.size(); i++) {
				feedSuffix += inputClasses.get(i)
						+ ((i < inputClasses.size() - 1) ? ", " : "");
			}
			if (matchingOutputs) {
				feedSuffix += " and ";
			}
		}
		if (matchingOutputs) {
			feedSuffix += "requested outputs: ";
			for (int i = 0; i < outputClasses.size(); i++) {
				feedSuffix += outputClasses.get(i)
						+ ((i < outputClasses.size() - 1) ? ", " : "");
			}
		}

		return matchingResults;
	}

	/**
	 * FIXME: Implement a proper comparator so that the services are ranked
	 * appropriately
	 * @param services
	 * @param inputDegrees
	 * @param outputDegrees
	 * @param labels
	 * @return
	 */
	private SortedSet<Entry> serializeServices(Set<String> services, Map<String, Degree> inputDegrees, Map<String, Degree> outputDegrees, Map<String, String> labels) {
		SortedSet<Entry> matchingResults = new TreeSet<Entry>();

		final Map<String, String> combinedServiceDegrees = new HashMap<String, String>();
        Map<String, String> degreeNames = new HashMap<String, String>();
        Map<String, String> degreeDescs = new HashMap<String, String>();
		
		for (String svc : services) {
		    Degree iDeg = inputDegrees.get(svc);
		    Degree oDeg = outputDegrees.get(svc);
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
            combinedServiceDegrees.put(svc, degreeNum);
            degreeNames.put(degreeNum, degreeName);
            degreeDescs.put(degreeNum, degreeDesc);
		}
		SortedSet<String> sortedServices = new TreeSet<String>(new Comparator<String>() {
            public int compare(String o1, String o2) {
                String deg1 = combinedServiceDegrees.get(o1);
                String deg2 = combinedServiceDegrees.get(o2);
                int retval = deg1.compareTo(deg2);
                if (retval == 0) {
                    retval = o1.compareTo(o2);
                }
                return retval;
            }
        });
		sortedServices.addAll(services);
		
		for (String svc : sortedServices) {
			String degreeNum = combinedServiceDegrees.get(svc);
			Entry result = DiscoveryUtil.getAbderaInstance().newEntry();
			result.addLink(svc);
			result.setTitle(labels.get(svc));
			ExtensibleElement e = result.addExtension(DiscoveryUtil.MATCH_DEGREE);
			e.setAttributeValue("num", degreeNum);
			e.setText(degreeNames.get(degreeNum));
			result.setContent(degreeDescs.get(degreeNum));
			matchingResults.add(result);
		}

		return matchingResults;
	}

	private void matchInputs(List<String> classes, Map<String, Degree> matches, Map<String, String> labels) throws DiscoveryException {
		RepositoryModel repoModel = connector.openRepositoryModel();

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
		String query = "prefix wl: <" + MSM.WL_NS_URI + ">\n"
				+ "prefix sawsdl: <" + MSM.SAWSDL_NS_URI + ">\n"
				+ "prefix msm: <" + MSM.NS_URI + ">\n" + "prefix rdfs: <"
				+ RDFS.NAMESPACE + ">\n"
				+ "select ?svc ?labels ?op ?labelop ?ic ";
		for (int i = 0; i < classes.size(); i++) {
			query += "?su" + i + " ?pl" + i + " ?ex" + i + " ";
		}
		query += "\nwhere {\n  ?svc a msm:Service ; msm:hasOperation ?op .\n"
				+ "  ?op msm:hasInput  ?imsg .\n"
				+ "  ?imsg msm:hasPart ?i .\n"
				+ "  OPTIONAL { ?imsg msm:hasPartTransitive ?i .\n }"
				+ "  ?i sawsdl:modelReference ?ic . \n"
				+ "  optional { ?svc rdfs:label ?labels }\n"
				+ "  optional { ?op rdfs:label ?labelop }\n";
		for (int i = 0; i < classes.size(); i++) {
			query += "  optional {\n" + "    ?ic rdfs:subClassOf <"
					+ classes.get(i).replace(">", "%3e") + "> ; ?su" + i + " <"
					+ classes.get(i).replace(">", "%3e") + "> .\n" + "  }\n";
			query += "  optional {\n" + "    <"
					+ classes.get(i).replace(">", "%3e")
					+ "> rdfs:subClassOf ?ic ; ?pl" + i + " ?ic .\n" + "  }\n";
			query += "  optional {\n" + "    ?i sawsdl:modelReference <"
					+ classes.get(i).replace(">", "%3e") + "> ; ?ex" + i + " <"
					+ classes.get(i).replace(">", "%3e") + "> .\n" + "  }\n";
		}
		query += "}";
		System.err.println("input matching query: \n" + query);

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
			String op = row.getValue("op").toString();
			op2svc.put(op, svc);
			// initially all ops are assumed to match until proven otherwise
			// (below)
			opExact.add(op);
			opPlugin.add(op);
			opSubsume.add(op);

			Node label = row.getValue("labels");
			if (label != null) {
				labels.put(svc, label.toString());
			}
			label = row.getValue("labelop");
			if (label != null) {
				labels.put(op, label.toString());
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

		opExact.removeAll(opNotExact);
		opPlugin.removeAll(opNotPlugin);
		opSubsume.removeAll(opNotSubsume);

		// the following order is significant - worse matches get rewritten by
		// better ones
		for (String op : opPartSubsume) {
			matches.put(op2svc.get(op), Degree.PARTIAL_SUBSUME);
		}
		for (String op : opPartPlugin) {
			matches.put(op2svc.get(op), Degree.PARTIAL_PLUGIN);
		}
		for (String op : opSubsume) {
			matches.put(op2svc.get(op), Degree.SUBSUME);
		}
		for (String op : opPlugin) {
			matches.put(op2svc.get(op), Degree.PLUGIN);
		}
		for (String op : opExact) {
			matches.put(op2svc.get(op), Degree.EXACT);
		}

		connector.closeRepositoryModel(repoModel);
	}

	private void matchOutputs(List<String> classes, Map<String, Degree> matches, Map<String, String> labels) throws DiscoveryException {
		RepositoryModel repoModel = connector.openRepositoryModel();

		// 1 exact match, 2 plugin goal inputs subclasses of service inputs, 3
		// subsume service inputs subclasses of goal inputs, 4 partial plugin, 5
		// partial subsume:
		// 1 find services that have all the goal classes
		// 2 find services that have a subclass of each of the goal classes
		// 3 find services that have a superclass of each of the goal classes
		// 4 find services that have a subclass for any of the goal classes
		// 5 find services that have a superclass for any of the goal classes
		// in the query, ?exact signifies exact match, ?cpX is annotation class
		// for plugin, ?csX is annotation class for subsume
		String query = "prefix wl: <" + MSM.WL_NS_URI + ">\n" +
				"prefix sawsdl: <" + MSM.SAWSDL_NS_URI + ">\n" +
				"prefix msm: <" + MSM.NS_URI + ">\n" +
				"prefix rdfs: <" + RDFS.NAMESPACE + ">\n" +
				"select ?svc ?labels ?op ?labelop ?exact ";
		for ( int i = 0; i < classes.size(); i++ ) {
			query += "?cp" + i + " ?cs" + i + " ";
		}
		query += "\nwhere {\n  ?svc a msm:Service ; msm:hasOperation ?op .\n"
				+ "  ?op msm:hasOutput ?omsg .\n"
				+ " ?omsg msm:hasPart ?o .\n"
				+ " OPTIONAL { ?omsg msm:hasPartTransitive ?o .\n } \n"
				+ "  optional { ?svc rdfs:label ?labels }\n"
				+ "  optional { ?op rdfs:label ?labelop }\n";
		for (int i = 0; i < classes.size(); i++) {
			query += "  optional {\n" + "    ?o sawsdl:modelReference ?cp" + i
					+ " .\n" + "    ?cp" + i + " rdfs:subClassOf <"
					+ classes.get(i).replace(">", "%3e") + "> .\n" + "  }\n";
			query += "  optional {\n" + "    ?o sawsdl:modelReference ?cs" + i
					+ " .\n" + "    <" + classes.get(i).replace(">", "%3e")
					+ "> rdfs:subClassOf ?cs" + i + " .\n" + "  }\n";
		}
		query += "  optional { ?o ?exact <" + classes.get(0).replace(">", "%3e")
				+ "> . \n";
		for (int i = 0; i < classes.size(); i++) {
			query += "    ?o sawsdl:modelReference <"
					+ classes.get(i).replace(">", "%3e") + "> .\n";
		}
		query += "  }\n";
		query += "}";
		System.err.println("output matching query: \n" + query);

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
				matches.put(svc, Degree.EXACT);
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
			Degree oldDegree = matches.get(svc);
			if (degree != Degree.FAIL
					&& (oldDegree == null || oldDegree.compareTo(degree) > 0)) {
				matches.put(svc, degree);
			}

		}
		connector.closeRepositoryModel(repoModel);
	}

	public String getDescription() {
		return "iServe RDFS input/output discovery API 2010/06/24";
	}

	public String getUri() {
		return "http://iserve.kmi.open.ac.uk/";
	}

	public String getFeedTitle() {
		String feedTitle = "rdfs i/o discovery results: " + count + " service(s) for " + feedSuffix;
		return feedTitle;
	}

}
