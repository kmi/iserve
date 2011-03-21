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
package uk.ac.open.kmi.iserve2.discovery.imatcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.ExtensibleElement;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

import uk.ac.open.kmi.iserve2.commons.io.RDFRepositoryConnector;
import uk.ac.open.kmi.iserve2.discovery.api.DiscoveryException;
import uk.ac.open.kmi.iserve2.discovery.api.IServiceDiscoveryPlugin;
import uk.ac.open.kmi.iserve2.discovery.api.util.DiscoveryUtil;
import uk.ac.open.kmi.iserve2.imatcher.IServeIMatcher;
import uk.ac.open.kmi.iserve2.imatcher.strategy.api.IStrategy;
import uk.ac.open.kmi.iserve2.imatcher.strategy.impl.LevNStrategy;
import uk.ac.open.kmi.iserve2.imatcher.strategy.impl.TFIDFDStrategy;

public class IMatcherDiscoveryPlugin implements IServiceDiscoveryPlugin {

	private int count;

	private String feedSuffix;

	private String strategyName;

	private IServeIMatcher iServeIMatcher;

	public IMatcherDiscoveryPlugin(RDFRepositoryConnector connector) {
		// initialize iServe-iMatcher
		iServeIMatcher = IServeIMatcher.getInstance();
//		iServeIMatcher.init(connector);
	}

	/* FIXME: THis should implement the Ranked Service Discovery plugin
	 * and return a SortedSet instead
	 * 
	 * (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve2.discovery.api.IServiceDiscoveryPlugin#discover(javax.ws.rs.core.MultivaluedMap)
	 */
	public Set<Entry> discover(MultivaluedMap<String, String> parameters) throws DiscoveryException {
		IStrategy strategy = null;
		strategyName = parameters.getFirst("strategy");
		if ( strategyName == null ) {
			throw new DiscoveryException(400, "empty strategy not allowed");
		}
		List<String> queryParam = new ArrayList<String>();
		if ( strategyName.equalsIgnoreCase("levenshtein") ) {
			String label = parameters.getFirst("label");
			if ( label == null ) {
				throw new DiscoveryException(400, "empty service label not allowed");
			}
			queryParam.add(label);
			feedSuffix = "label being '" + label + "'";
			strategy = new LevNStrategy();
		} else if ( strategyName.equalsIgnoreCase("tfidfd") ) {
			String comment = parameters.getFirst("comment");
			if ( comment == null ) {
				throw new DiscoveryException(400, "empty comment on service not allowed");
			}
			queryParam.add(comment);
			feedSuffix = "comment being '" + comment + "'";
			strategy = new TFIDFDStrategy();
		}

		ResultSet rs = iServeIMatcher.query(strategy, queryParam);
		Set<Entry> matchingResults = serializeServices(strategy, rs);
		return matchingResults;
	}

	public String getName() {
		return "imatch";
	}

	public String getUri() {
		return "http://iserve.kmi.open.ac.uk/";
	}

	public String getDescription() {
		return "iServe iMatcher discovery API 2010/11/29";
	}

	public String getFeedTitle() {
		String feedTitle = "iMatcher using " + strategyName + " discovery results: " + count + " service(s) for " + feedSuffix;
		return feedTitle;
	}

	/**
	 * FIXME: This method should return the results ordered
	 * 
	 * @param strategy
	 * @param rs
	 * @return
	 */
	private Set<Entry> serializeServices(IStrategy strategy, ResultSet rs) {
		Set<Entry> matchingResults = new HashSet<Entry>();
		if ( null == rs || !rs.hasNext())
			return matchingResults;
		count = 0;
		for (; rs.hasNext();) {
			QuerySolution rb = rs.nextSolution();
			RDFNode service = rb.get("service");
			RDFNode label = rb.get("label");
			RDFNode sim = rb.get("sim");
			if (service.isURIResource()) {
				
				com.hp.hpl.jena.rdf.model.Resource res = (com.hp.hpl.jena.rdf.model.Resource) service;
				
				Entry result = DiscoveryUtil.getAbderaInstance().newEntry();
				result.setId(res.getURI());
				result.addLink(res.getURI(), "alternate");
				result.setTitle(label.toString());
				ExtensibleElement e = result.addExtension(DiscoveryUtil.MATCH_DEGREE);

				e.setText(strategyName);
				
				if ( sim.isLiteral() == true ) {
					Literal l = (Literal) sim;
					e.setAttributeValue("num", String.valueOf(1 - l.getDouble()));
					String content = "Matching degree: " + String.valueOf(l.getDouble());
					result.setContent(content);
					
					if ( l.getDouble() > 0.0 ) {
						matchingResults.add(result);
						count++;
					}
				} else {
					e.setAttributeValue("num", String.valueOf(sim.toString()));
					String content = "Matching degree: " + sim.toString();
					result.setContent(content);
					matchingResults.add(result);
					count++;
				}
				
			}
		}
		return matchingResults;
	}

}
