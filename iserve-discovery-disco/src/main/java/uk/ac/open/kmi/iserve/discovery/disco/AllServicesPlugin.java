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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.abdera.model.Entry;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.node.BlankNode;
import org.ontoware.rdf2go.model.node.Node;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rdf2go.RepositoryModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.iserve.commons.io.RDFRepositoryConnector;
import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.discovery.api.DiscoveryException;
import uk.ac.open.kmi.iserve.discovery.api.IServiceDiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.util.DiscoveryUtil;
import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;

public class AllServicesPlugin implements IServiceDiscoveryPlugin {

	private static final Logger log = LoggerFactory.getLogger(AllServicesPlugin.class);

	private int count;

	public AllServicesPlugin() {	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.discovery.api.IServiceDiscoveryPlugin#discover(javax.ws.rs.core.MultivaluedMap)
	 */
	public Set<Entry> discover(MultivaluedMap<String, String> parameters) throws DiscoveryException {
		log.debug("Discover services: " + parameters);
		
		// set of services
		Set<String> services = new HashSet<String>();

		Map<String, String> labels = new HashMap<String,String>();

		RDFRepositoryConnector connector = ManagerSingleton.getInstance().getServicesRepositoryConnector();
		RepositoryModel repoModel = connector.openRepositoryModel();

        String query = "prefix msm: <" + MSM.NS_URI + ">\n"
			+ "prefix rdfs: <" + RDFS.NAMESPACE + ">\n"
			+ "select ?svc ?label \n"
		    + "where {  ?svc a msm:Service .\n"
			+ "  optional { ?svc rdfs:label ?label }\n"
		    + "}";

        log.info("Querying for services: " + query);
        
		QueryResultTable qresult = repoModel.querySelect(query, "sparql");
		for (Iterator<QueryRow> it = qresult.iterator(); it.hasNext();) {
			QueryRow row = it.next();
			Node svcNode = row.getValue("svc");
			
			// Filter blank nodes
			if (svcNode instanceof BlankNode) {
				log.warn("Service blank node found: " + svcNode.toString());
			} else {
				String svc = svcNode.asURI().toString();
				services.add(svc);
				
				log.debug("Adding result: " + svc);

				Node label = row.getValue("label");
				if (label != null) {
					labels.put(svc, label.toString());
				} 
			}

		}

		connector.closeRepositoryModel(repoModel);

		count = services.size();

		Set<Entry> matchingResults = serializeServices(services, labels);
		return matchingResults;
	}

	private Set<Entry> serializeServices(Set<String> services, Map<String, String> labels) {
		
		log.debug("Serialising " + services.size() + " results");
		
		Set<Entry> matchingResults = new HashSet<Entry>();
		for (Iterator<String> it = services.iterator(); it.hasNext();) {
			String svc = it.next();
			Entry result = DiscoveryUtil.getAbderaInstance().newEntry();
			result.setId(svc);
			result.addLink(svc, "alternate");
			result.setTitle(labels.get(svc));
			matchingResults.add(result);

		}
		return matchingResults;
	}

	public String getName() {
		return "all";
	}

	public String getDescription() {
		return "iServe trivial discovery (all services) API 2012/06/01";
	}

	public String getUri() {
		return "http://iserve.kmi.open.ac.uk/";
	}

	public String getFeedTitle() {
		String feedTitle = "all " + count + " known service(s)";
		return feedTitle;
	}

}
