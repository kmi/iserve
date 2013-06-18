/*
 * Copyright (c) 2013. Knowledge Media Institute - The Open University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.open.kmi.iserve.discovery.disco.impl;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.discovery.api.DiscoveryException;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.OperationDiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.api.ServiceDiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.disco.DiscoMatchType;
import uk.ac.open.kmi.iserve.discovery.disco.MatchResultImpl;
import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;

import javax.ws.rs.core.MultivaluedMap;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AllServicesPlugin implements ServiceDiscoveryPlugin, OperationDiscoveryPlugin {

    private static final String PLUGIN_NAME = "all-jena";

    private static final String PLUGIN_DESCRIPTION = "iServe trivial discovery. " +
            "Returns all the services or operations available.";

    private static final String PLUGIN_VERSION = "v0.2";

    // No Discovery Pluging Parameters
    private static final Map<String, String> parameterDetails = new HashMap<String, String>();

    private static final Logger log = LoggerFactory.getLogger(AllServicesPlugin.class);

    public static String NL = System.getProperty("line.separator");

    private int count;

    private URI sparqlEndpoint;

    public AllServicesPlugin() {
        sparqlEndpoint = ManagerSingleton.getInstance().getConfiguration().getDataSparqlUri();
        if (sparqlEndpoint == null) {
            log.error("The '" + this.getName() + "' " + this.getVersion() +
                    " services discovery plugin currently requires a SPARQL endpoint.");
            // TODO: Disable plugin
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

        if (sparqlEndpoint == null) {
            log.error("Unable to perform discovery, no SPARQL endpoint available.");
            throw new DiscoveryException(403, "Unable to perform discovery, no SPARQL endpoint available.");
        }

        log.debug("Discover services: " + parameters);

        // Matching results
        Map<URL, MatchResult> results = new HashMap<URL, MatchResult>();

        // Query for every item
        StringBuilder queryBuffer = new StringBuilder().
                append("select ?svc ?labelSvc ");
        if (operationDiscovery) {
            queryBuffer.append("?op ?labelOp");
        }
        queryBuffer.append(NL);
        queryBuffer.append("where {  ?svc a <" + MSM.Service.getURI() + "> ." + NL);

        if (operationDiscovery) {
            queryBuffer.append("?svc <" + MSM.hasOperation.getURI() + "> ?op ." + NL).
                    append("?op a <" + MSM.Operation.getURI() + "> ." + NL);
        }
        queryBuffer.append("  optional { ?svc <" + RDFS.label.getURI() + "> ?labelSvc }" + NL);
        if (operationDiscovery) {
            queryBuffer.append("  optional { ?op <" + RDFS.label.getURI() + "> ?labelOp }" + NL);
        }
        queryBuffer.append("}");

        log.debug("Querying the backend: " + sparqlEndpoint + NL + queryBuffer);

        // Query the engine
        Query query = QueryFactory.create(queryBuffer.toString());
        QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint.toASCIIString(), query);

        URL matchUrl = null;
        String matchLabel = null;
        Resource resource;
        Literal labelResource;
        String matchBinding;
        String labelBinding;

        if (operationDiscovery) {
            matchBinding = "op";
            labelBinding = "labelOp";
        } else {
            matchBinding = "svc";
            labelBinding = "labelSvc";
        }

        try {
            ResultSet qResults = qexec.execSelect();
            while (qResults.hasNext()) {
                QuerySolution soln = qResults.nextSolution();

                // Get the match URL
                resource = soln.getResource(matchBinding);
                if (resource != null && resource.isURIResource()) {
                    matchUrl = new URL(resource.getURI());
                }

                // Get label if it exists
                labelResource = soln.getLiteral(labelBinding);
                if (labelResource != null) {
                    matchLabel = labelResource.toString();
                }

                if (matchUrl != null) {
                    // Ensure we got a result before proceeding further
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
            }
        } catch (MalformedURLException e) {
            log.error("Error obtaining match result. Expected a correct URL", e);
        } finally {
            qexec.close();
        }

        log.debug("Matching results: " + results);
        return results;
    }
}
