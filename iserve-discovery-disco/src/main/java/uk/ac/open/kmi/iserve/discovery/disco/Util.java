/*
   Copyright 2012  Knowledge Media Institute - The Open University

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

import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.node.BlankNode;
import org.ontoware.rdf2go.model.node.Node;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.iserve.commons.io.URIUtil;
import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.util.DiscoveryUtil;

/**
 * Class Description
 * 
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public class Util {
	
	private static final Logger log = LoggerFactory.getLogger(Util.class);
	
	// Common variables used for querying the RDF repository
	public static final String VAR_SVC = "svc";
	public static final String VAR_SVC_LABEL = "labelSvc";
	public static final String VAR_OP = "op";
	public static final String VAR_OP_LABEL = "labelOp";
	
	public static String NL = System.getProperty("line.separator");
	
	/**
	 * Given a query result using the default variable name for services 
	 * obtain its label
	 * 
	 * @param resultRow the result from a SPARQL query
	 * @return the label of the service of null if none was found
	 */
	public static String getServiceLabel(QueryRow resultRow) {
		return getStringValueFromRow(resultRow, VAR_SVC_LABEL);
	}
	
	/**
	 * Given a query result using the default variable name for operations 
	 * obtain its label
	 * 
	 * @param resultRow the result from a SPARQL query
	 * @return the label of the operation or null if none was found
	 */
	public static String getOperationLabel(QueryRow resultRow) {
		return getStringValueFromRow(resultRow, VAR_OP_LABEL);
	}

	/**
	 * Given a query result from a SPARQL query, obtain the given variable value
	 * as a String
	 * 
	 * @param resultRow the result from a SPARQL query
	 * @param variableName the name of the variable to obtain
	 * @return the value or null if it could not be found
	 */
	private static String getStringValueFromRow(QueryRow resultRow, String variableName) {
		// Check the input and exit immediately if null
		if (resultRow == null) {
			return null;
		}
		
		String result = null;
		
		Node node = resultRow.getValue(VAR_SVC_LABEL);
		if (node != null) {
			result = node.toString();
		}
		
		return result;
	}
	
	/**
	 * Given a query result using the default variable name for services 
	 * obtain its label
	 * 
	 * @param resultRow the result from a SPARQL query
	 * @return the URL of the service or null if none was found
	 */
	public static URL getServiceUrl(QueryRow resultRow) {
		return getUrlValueFromRow(resultRow, VAR_SVC);
	}
	
	/**
	 * Given a query result using the default variable name for operations 
	 * obtain its label
	 * 
	 * @param resultRow the result from a SPARQL query
	 * @return the URL of the operation or null if none was found
	 */
	public static URL getOperationUrl(QueryRow resultRow) {
		return getUrlValueFromRow(resultRow, VAR_OP);
	}
	
	/**
	 * Given a query result from a SPARQL query, obtain the given variable value
	 * as a URL
	 * 
	 * @param resultRow the result from a SPARQL query
	 * @param variableName the name of the variable to obtain
	 * @return the value or null if it could not be obtained
	 */
	private static URL getUrlValueFromRow(QueryRow resultRow, String variableName) {
		
		// Check the input and exit immediately if null
		if (resultRow == null) {
			return null;
		}
		URL result = null;
		Node node = resultRow.getValue(variableName);
		
		// Ignore and track services that are blank nodes
		if (node instanceof BlankNode) {
			log.warn("Blank node found and ignored " + node.toString());
		} else {
			try {
				result = new URL(node.asURI().toString());
			} catch (MalformedURLException e) {
				log.error("Malformed URL for node", e);
			} catch (ClassCastException e) {
				log.error("The node is not a URI", e);
			}
		}
		
		return result;
	}
	
	/**
	 * Given a query result from a SPARQL query, check if the given variable has
	 * a value or not
	 * 
	 * @param resultRow the result from a SPARQL query
	 * @param variableName the name of the variable to obtain
	 * @return true if the variable has a value, false otherwise
	 */
	public static boolean isVariableSet(QueryRow resultRow, String variableName) {
		
		if (resultRow != null) {
			Node val = resultRow.getValue(variableName);
			if (val != null) {
				return true;
			}
		} 
		
		return false;
	}
	
	public static String generateQueryPrefix() {
		return "prefix msm: <" + MSM.NS_URI + ">" + NL +
			"prefix rdfs: <" + RDFS.NAMESPACE + ">" + NL;
	}

	/**
	 * Given the match between concepts obtain the match type
	 * 
	 * @param isSubsume true if the concept match is subsumes
	 * @param isPlugin true if the concept match is plugin
	 * @return Exact, Plugin or Subsumes depending on the values 
	 */
	public static MatchType getMatchType(boolean isSubsume, boolean isPlugin) {
		if (isSubsume) {
			if (isPlugin) {
				// If plugin and subsume -> this is an exact match
				return MatchType.EXACT;
			}
			return MatchType.SUBSUME;
		} else {
			if (isPlugin) {
				return MatchType.PLUGIN;
			}
		}
		return MatchType.FAIL;
	}

	/**
	 * Given the best and worst match figure out the match type for the composite.
	 * The composite match type is determined by the worst case except when it is
	 * a FAIL. In this case, if the best is EXACT or PLUGIN the composite match
	 * will be PARTIAL_PLUGIN. If the best is SUBSUMES it is PARTIAL_SUBSUMES.
	 * 
	 * 
	 * @param bestMatch best match type within the composite match
	 * @param worstMatch worst match type within the composite match
	 * @return match type for the composite match
	 */
	public static MatchType calculateCompositeMatchType(MatchType bestMatch,
			MatchType worstMatch) {
		
		switch (worstMatch) {
		case EXACT:
			return MatchType.EXACT;
		
		case PLUGIN:
			return MatchType.PLUGIN;

		case SUBSUME:
			return MatchType.SUBSUME;
			
		case FAIL:
			switch (bestMatch) {
			case EXACT:
				return MatchType.PARTIAL_PLUGIN;
			
			case PLUGIN:
				return MatchType.PARTIAL_PLUGIN;

			case SUBSUME:
				return MatchType.PARTIAL_SUBSUME;
				
			default:
				log.warn("This match type is not supported: " + 
						bestMatch.getShortName());
				
				return MatchType.FAIL;
			}
			
		default:
			log.warn("This match type is not supported: " + 
					worstMatch.getShortName());
			
			return MatchType.FAIL;
		}
	}

	/**
	 * Obtain the match url given the query row and the kind of discovery 
	 * we are carrying out
	 *  
	 * @param row the result from a SPARQL query
	 * @param operationDiscovery true if we are doing operation discovery
	 * @return the URL for the match result
	 */
	public static URL getMatchUrl(QueryRow row, boolean operationDiscovery) {
		// Check the input and return immediately if null
		if (row == null) {
			return null;
		}
		
		URL matchUrl;
		
		if (operationDiscovery) {
			matchUrl = Util.getOperationUrl(row);
		} else {
			matchUrl = Util.getServiceUrl(row);
		}
		
		return matchUrl;
	}

	/**
	 * @param row the result from a SPARQL query
	 * @param operationDiscovery true if we are doing operation discovery
	 * @return
	 */
	public static String getMatchLabel(QueryRow row, boolean operationDiscovery) {
		// Check the input and return immediately if null
		if (row == null) {
			return null;
		}
		
		String matchLabel;
		
		if (operationDiscovery) {
			matchLabel = Util.getOperationLabel(row);
		} else {
			matchLabel = Util.getServiceLabel(row);
		}
		
		return matchLabel;
	}
	
	/**
	 * Obtain or generate a label for the match result given a row resulting 
	 * from a query.
	 * 
	 * @param row the result from a SPARQL query
	 * @param operationDiscovery true if we are doing operation discovery
	 * @return
	 */
	public static String getOrGenerateMatchLabel(QueryRow row, boolean operationDiscovery) {
		String label;
		if (operationDiscovery) {
			label = getOrGenerateOperationLabel(row);
		} else {
			label = getOrGenerateServiceLabel(row);
		} 
		return label;
	}

	/**
	 * Obtain or generate a label for a service.
	 * TODO: Deal better with WSDL naming convention
	 * 
	 * @param row the result from a SPARQL query
	 * @return
	 */
	private static String getOrGenerateServiceLabel(QueryRow row) {
		String label = getServiceLabel(row);
		if (label == null) {
			URL svcUrl = getServiceUrl(row);
			label = URIUtil.generateItemLabel(svcUrl);
		}
		
		return label;
	}

	/**
	 * Obtain or generate a label for an operation.
	 * 
	 * TODO: Deal better with WSDL naming convention
	 * 
	 * @param row the result from a SPARQL query
	 * @return
	 */
	private static String getOrGenerateOperationLabel(QueryRow row) {
		String result;
		String svcLabel = getOrGenerateServiceLabel(row);
		String opLabel = getOperationLabel(row);
		if (opLabel == null) {
			URL opUrl = getOperationUrl(row);
			result = URIUtil.generateItemLabel(svcLabel, opUrl);
		} else {
			result = svcLabel + "." + opLabel;
		}
		return result;
	}
	
}
