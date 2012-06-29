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
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

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
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.OperationDiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.api.ServiceDiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.util.DiscoveryUtil;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;
import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;
import uk.ac.open.kmi.iserve.sal.manager.impl.ServiceManagerRdf;

public class AllServicesPlugin implements ServiceDiscoveryPlugin, OperationDiscoveryPlugin {

	private static final String PLUGIN_NAME = "all";

	private static final String PLUGIN_DESCRIPTION = "iServe trivial discovery. " +
	"Returns all the services or operations available.";

	private static final String PLUGIN_VERSION = "v1.1.2";

	// No Discovery Pluging Parameters	
	private static final Map<String, String> parameterDetails = new HashMap<String, String>();

	private static final Logger log = LoggerFactory.getLogger(AllServicesPlugin.class);

	public static String NEW_LINE = System.getProperty("line.separator");

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

	public Map<URL, MatchResult> discover(boolean operationDiscovery, MultivaluedMap<String, String> parameters) throws DiscoveryException {
		// If there is no service connector raise an error 
		if (serviceConnector == null) {
			throw new DiscoveryException("The '" + this.getName() + "' " + 
					this.getVersion() + " the RDF connector to the services repository is null.");
		}

		log.debug("Discover services: " + parameters);

		// Matching results
		Map<URL, MatchResult> results = new HashMap<URL, MatchResult>();
		
		// Query for every item
		StringBuffer query = new StringBuffer("prefix msm: <" + MSM.NS_URI + ">" + NEW_LINE);
		query.append("prefix rdfs: <" + RDFS.NAMESPACE + ">" + NEW_LINE);
		query.append("select ?svc ?labelSvc ");
		if (operationDiscovery) {
			query.append("?op ?labelOp");
		}
		query.append(NEW_LINE);
		query.append("where {  ?svc a msm:Service ." + NEW_LINE);
		
		if (operationDiscovery) {
			query.append("?svc msm:hasOperation ?op ." + NEW_LINE);
			query.append("?op a msm:Operation ." + NEW_LINE);
		} 
		query.append("  optional { ?svc rdfs:label ?labelSvc }" + NEW_LINE);
		if (operationDiscovery) {
			query.append("  optional { ?op rdfs:label ?labelOp }" + NEW_LINE);
		}
		query.append("}");

		log.info("Querying the backend: " + query);

		RepositoryModel repoModel = null;
		// Query and process the results
		try {
			repoModel = serviceConnector.openRepositoryModel();
			QueryResultTable qresult = repoModel.querySelect(query.toString(), "sparql");
			for (Iterator<QueryRow> it = qresult.iterator(); it.hasNext();) {
				QueryRow row = it.next();
				Node svcNode = row.getValue("svc");
	
				// Ignore and track services that are blank nodes
				if (svcNode instanceof BlankNode) {
					log.warn("Blank node found: " + svcNode.toString());
				} else {
					String svcUrl = svcNode.asURI().toString();
					String svcLabel = null;
					String opLabel = null;
					String opUrl = null;
					
					Node labelSvcNode = row.getValue("labelSvc");
					if (labelSvcNode != null) {
						svcLabel = labelSvcNode.toString();
					}
					
					if (operationDiscovery) {
						// Get operation details
						Node opNode = row.getValue("op");
						opUrl = opNode.asURI().toString();
						
						Node labelOpNode = row.getValue("labelOp");
						if (labelOpNode != null) {
							opLabel = labelOpNode.toString();
						} 
					}
					
					String matchLabel = DiscoveryUtil.createMatchLabel(operationDiscovery, svcUrl, svcLabel, opUrl, opLabel);
					URL matchUrl = (operationDiscovery) ? new URL(opUrl) : new URL(svcUrl);
					
					// TODO: Add plugin URL as well as that of the request
					// Default score for every match: 0
					MatchResult match = new SimpleMatchResult(matchUrl, matchLabel, 
							MatchType.EXACT, 0, null, null);
					
					results.put(matchUrl, match);
				}
			} 
		} catch (MalformedURLException e) {
			log.error("Error generating URL for resource", e);
		} catch (ClassCastException e) {
			log.error("Error casting resource", e);
		} finally {
			serviceConnector.closeRepositoryModel(repoModel);
		}
		
		log.debug("Matching results: " + results);
		return results;
	}
}
