/*
 * $Id: TFIDFDStrategy.java 448 2007-12-10 17:01:19Z kiefer $
 *
 * Created by Christoph Kiefer, kiefer@ifi.uzh.ch
 *
 * See LICENSE for more information about licensing and warranties.
 */
package uk.ac.open.kmi.iserve2.imatcher.strategy.impl;

import java.util.List;

import uk.ac.open.kmi.iserve2.imatcher.strategy.api.AbstractStrategy;

public class TFIDFDStrategy extends AbstractStrategy {

	public String getStrategy() {
		return prolog
				+ NL
				+ simpackNS + "PREFIX msm: <http://cms-wg.sti2.org/ns/minimal-service-model#> "
				+ NL
				+ "SELECT DISTINCT ?service ?sim "
				+ NL
				+ "WHERE { " + "?s rdf:type msm:Service . "
				+ NL + "?s dc:source ?service FILTER regex(str(?service), \"^file\", \"i\") .  "
				+ NL + "?s rdfs:comment ?serviceTextDescription . "
				+ NL + "?service2 owl:sameAs <<<queryURL>>> ."
				+ NL + "?service2 rdfs:comment ?queryTextDescription . "
//				+ "?service service:presents ?serviceProfile . "
				+ NL
//				+ "?serviceProfile profile:textDescription ?serviceTextDescription . "
				+ NL
//				+ "<<<queryURL>>>"
//				+ " service:presents ?queryProfile . "
				+ NL
//				+ "?queryProfile profile:textDescription ?queryTextDescription . " + NL
				
				+ "IMPRECISE { ?sim simpack:tfidf" + " (?serviceTextDescription ?queryTextDescription) } " + NL
				
				+ "} ORDER BY DESC (?sim)";
	}

	public String getStrategy(List<String> parameters) {
		return prolog + NL + simpackNS + "PREFIX msm: <http://cms-wg.sti2.org/ns/minimal-service-model#> " + NL
		+ "SELECT DISTINCT ?service ?label ?sim " + NL
		+ "WHERE { " + "?service rdf:type msm:Service . " + NL
		+ "OPTIONAL { ?service rdfs:label ?label . }" + NL
		+ "?service rdfs:comment ?serviceTextDescription . " + NL
		+ "IMPRECISE { ?sim simpack:tfidf" + " (?serviceTextDescription \"" + parameters.get(0) + "\") } " + NL
		+ "} ORDER BY DESC (?sim)";
	}

}
