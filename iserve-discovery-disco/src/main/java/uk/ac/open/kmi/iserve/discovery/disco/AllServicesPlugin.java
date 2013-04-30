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

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.openrdf.rdf2go.RepositoryModel;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.iserve.commons.io.RDFRepositoryConnector;
import uk.ac.open.kmi.iserve.discovery.api.DiscoveryException;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.OperationDiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.api.ServiceDiscoveryPlugin;
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
		//TODO: Change to use a sparql endpoint instead
		String repoName = ManagerSingleton.getInstance().getConfiguration().getDataRepositoryName();
		URI repoUri = ManagerSingleton.getInstance().getConfiguration().getDataRepositoryUri();
		
		if (repoName != null && repoUri != null) {
			try {
				serviceConnector = new RDFRepositoryConnector(repoUri.toString(), repoName);
			} catch (RepositoryException e) {
				throw new WebApplicationException(
						new IllegalStateException("The '" + this.getName() + "' " + 
								this.getVersion() + " services discovery plugin could not connect to the RDF Repository."), 
								Response.Status.INTERNAL_SERVER_ERROR);
			}
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
		StringBuffer query = new StringBuffer(Util.generateQueryPrefix());
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
		ClosableIterator<QueryRow> it = null;
		// Query and process the results
		try {
			repoModel = serviceConnector.openRepositoryModel();
			QueryResultTable qresult = repoModel.querySelect(query.toString(), "sparql");
			it = qresult.iterator();
			while (it.hasNext()) {
				QueryRow row = it.next();
				URL matchUrl;
				String matchLabel;
				
				if (operationDiscovery) {
					matchUrl = Util.getOperationUrl(row);
					matchLabel = Util.getOperationLabel(row);
				} else {
					matchUrl = Util.getServiceUrl(row);
					matchLabel = Util.getServiceLabel(row);
				}
				// Only continue processing if the match exists
				if (matchUrl == null) {
					break;
				}
				// Create a match result 
				MatchResultImpl match = new MatchResultImpl(matchUrl, matchLabel);
				match.setMatchType(DiscoMatchType.EXACT);
				match.setScore(Float.valueOf(0));
				// TODO: Add these
				// match.setEngineUrl(engineUrl);
				// match.setRequest(request);
				// Add the result
				results.put(match.getMatchUrl(), match);
			} 
		} catch (ClassCastException e) {
			log.error("Error casting resource", e);
		} finally {
			it.close();
			serviceConnector.closeRepositoryModel(repoModel);
		}
		
		log.debug("Matching results: " + results);
		return results;
	}
}
