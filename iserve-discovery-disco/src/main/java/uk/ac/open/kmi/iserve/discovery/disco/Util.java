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
	public static final String VAR_SVC_LABEL = "svcLabel";
	public static final String VAR_OP = "op";
	public static final String VAR_OP_LABEL = "opLabel";
	
	public static String NEW_LINE = System.getProperty("line.separator");
	
	public static SimpleMatchResult createMatchResult(QueryRow resultRow, 
			boolean operationDiscovery, MatchType matchType) {
		
		// Check the input and exit immediately if null
		if (resultRow == null) {
			return null;
		}
		
		SimpleMatchResult result = null;
		String svcUrl = null;
		String svcLabel = null;
		String opLabel = null;
		String opUrl = null;
		
		Node svcNode = resultRow.getValue(VAR_SVC);
		
		// Ignore and track services that are blank nodes
		if (svcNode instanceof BlankNode) {
			log.warn("Blank node found: " + svcNode.toString());
		} else {
			svcUrl = svcNode.asURI().toString();
			
			Node labelSvcNode = resultRow.getValue(VAR_SVC_LABEL);
			if (labelSvcNode != null) {
				svcLabel = labelSvcNode.toString();
			}
			
			if (operationDiscovery) {
				// Get operation details
				Node opNode = resultRow.getValue(VAR_OP);
				opUrl = opNode.asURI().toString();
				
				Node labelOpNode = resultRow.getValue(VAR_OP_LABEL);
				if (labelOpNode != null) {
					opLabel = labelOpNode.toString();
				} 
			}
			
			// Generate the match instance
			String matchLabel = createMatchLabel(operationDiscovery, svcUrl, 
					svcLabel, opUrl, opLabel);
			URL matchUrl;
			try {
				matchUrl = (operationDiscovery) ? new URL(opUrl) : new URL(svcUrl);
				result = new SimpleMatchResult(matchUrl, matchLabel, matchType);
			} catch (MalformedURLException e) {
				log.error("Incorrect URL for match result", e);
			}
		}
		
		return result;
	}
	
	public static String generateQueryPrefix() {
		return "prefix msm: <" + MSM.NS_URI + ">" + NEW_LINE +
			"prefix rdfs: <" + RDFS.NAMESPACE + ">" + NEW_LINE;
	}
	
	
	/**
	 * @param svcUri
	 * @param svcLabel
	 * @param opUri
	 * @param opLabel
	 * @return
	 */
	public static String createMatchLabel(boolean operationDiscovery, String svcUri, String svcLabel, String opUri, String opLabel) {
		String result;
		if (operationDiscovery) {
			if (opLabel != null) {
				result = opLabel;
			} else {
				result = generateOperationLabel(svcUri, svcLabel, opUri);
			}
		} else {
			if (svcLabel != null) {
				result = svcLabel;
			} else {
				result = generateServiceLabel(svcUri);
			}	
		}
		return result;
	}
	
	private static String generateServiceLabel(String serviceUri) {
		return URIUtil.getLocalName(serviceUri);
	}
	
	private static String generateOperationLabel(String serviceUri, String serviceLabel, String opUri) {
		String result = null;
		if (serviceLabel != null) {
			result = serviceLabel;
		} else {
			result = generateServiceLabel(serviceUri);
		}
		result += "." + URIUtil.getLocalName(opUri);
		return result;
	}
	
}
