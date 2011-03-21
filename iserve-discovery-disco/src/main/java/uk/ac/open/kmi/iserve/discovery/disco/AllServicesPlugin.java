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
package uk.ac.open.kmi.iserve2.discovery.disco;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

import uk.ac.open.kmi.iserve2.commons.io.RDFRepositoryConnector;
import uk.ac.open.kmi.iserve2.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve2.discovery.api.DiscoveryException;
import uk.ac.open.kmi.iserve2.discovery.api.IServiceDiscoveryPlugin;
import uk.ac.open.kmi.iserve2.discovery.api.util.DiscoveryUtil;

public class AllServicesPlugin implements IServiceDiscoveryPlugin {

	private RDFRepositoryConnector connector;

	private int count;

	public AllServicesPlugin(RDFRepositoryConnector connector) {
		this.connector = connector;
	}

	public Set<Entry> discover(MultivaluedMap<String, String> parameters) throws DiscoveryException {
		// set of services
		Set<String> services = new HashSet<String>();

		Map<String, String> labels = new HashMap<String,String>();

		RepositoryModel repoModel = connector.openRepositoryModel();

        String query = "prefix msm: <" + MSM.NS_URI + ">\n"
			+ "prefix rdfs: <" + RDFS.NAMESPACE + ">\n"
			+ "select ?svc ?label \n"
		    + "where {  ?svc a msm:Service .\n"
			+ "  optional { ?svc rdfs:label ?label }\n"
		    + "}";

		QueryResultTable qresult = repoModel.querySelect(query, "sparql");
		for (Iterator<QueryRow> it = qresult.iterator(); it.hasNext();) {
			QueryRow row = it.next();
			String svc = row.getValue("svc").toString();
			services.add(svc);

			Node label = row.getValue("label");
			if (label != null) {
				labels.put(svc, label.toString());
			}
		}

		connector.closeRepositoryModel(repoModel);

		count = services.size();

		Set<Entry> matchingResults = serializeServices(services, labels);
		return matchingResults;
	}

	private Set<Entry> serializeServices(Set<String> services, Map<String, String> labels) {
		Set<Entry> matchingResults = new HashSet<Entry>();
		for (Iterator<String> it = services.iterator(); it.hasNext();) {
			String svc = it.next();
			
			Entry result = DiscoveryUtil.getAbderaInstance().newEntry();
			result.addLink(svc);
			result.setTitle(labels.get(svc));
			matchingResults.add(result);

		}
		return matchingResults;
	}

	public String getName() {
		return "all";
	}

	public String getDescription() {
		return "iServe trivial discovery (all services) API 2010/04/27";
	}

	public String getUri() {
		return "http://iserve.kmi.open.ac.uk/";
	}

	public String getFeedTitle() {
		String feedTitle = "all " + count + " known service(s)";
		return feedTitle;
	}

}
