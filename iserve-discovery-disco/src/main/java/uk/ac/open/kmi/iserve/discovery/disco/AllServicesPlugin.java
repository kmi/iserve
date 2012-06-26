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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

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
import uk.ac.open.kmi.iserve.discovery.api.ServiceDiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.util.DiscoveryUtil;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;
import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;
import uk.ac.open.kmi.iserve.sal.manager.impl.ServiceManagerRdf;

public class AllServicesPlugin implements ServiceDiscoveryPlugin {

	private static final String PLUGIN_NAME = "all";

	private static final String PLUGIN_DESCRIPTION = "iServe trivial discovery. " +
			"Returns all the services available.";

	private static final String PLUGIN_VERSION = "v1.1.2";

	// No Discovery Pluging Parameters	
	private static final Map<String, String> parameterDetails = new HashMap<String, String>();
		
	private static final Logger log = LoggerFactory.getLogger(AllServicesPlugin.class);

	private int count;

	RDFRepositoryConnector serviceConnector;

	public AllServicesPlugin() {	
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
	public String getName() {
		return PLUGIN_NAME;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.discovery.api.DiscoveryPlugin#getDescription()
	 */
	public String getDescription() {
		return PLUGIN_DESCRIPTION;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.discovery.api.DiscoveryPlugin#getFeedTitle()
	 */
	public String getFeedTitle() {
		String feedTitle = "all " + count + " known service(s)";
		return feedTitle;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.discovery.api.IServiceDiscoveryPlugin#getVersion()
	 */
	@Override
	public String getVersion() {
		return PLUGIN_VERSION;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.discovery.api.DiscoveryPlugin#getParametersDetails()
	 */
	@Override
	public Map<String, String> getParametersDetails() {
		return parameterDetails;
	}
	
	
	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.discovery.api.ServiceDiscoveryPlugin#discoverServices(javax.ws.rs.core.MultivaluedMap)
	 */
	public SortedSet<Entry> discoverServices(MultivaluedMap<String, String> parameters) throws DiscoveryException {
		
		// If there is no service connector raise an error 
		if (serviceConnector == null) {
			throw new DiscoveryException("The '" + this.getName() + "' " + 
							this.getVersion() + " the RDF connector to the services repository is null.");
		}
		
		log.debug("Discover services: " + parameters);

		// set of services
		SortedSet<String> services = new TreeSet<String>();
		Map<String, String> labels = new HashMap<String,String>();

		RepositoryModel repoModel = serviceConnector.openRepositoryModel();

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

		serviceConnector.closeRepositoryModel(repoModel);

		count = services.size();

		SortedSet<Entry> matchingResults = serializeServices(services, labels);
		return matchingResults;
	}

	/**
	 * @param services
	 * @param labels
	 * @return
	 */
	private SortedSet<Entry> serializeServices(Set<String> services, Map<String, String> labels) {

		log.debug("Serialising " + services.size() + " results");

		SortedSet<Entry> matchingResults = new TreeSet<Entry>();
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

}
