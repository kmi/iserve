/*
 * $Id: LevNStrategy.java 448 2007-12-10 17:01:19Z kiefer $
 *
 * Created by Christoph Kiefer, kiefer@ifi.uzh.ch
 *
 * See LICENSE for more information about licensing and warranties.
 */
package uk.ac.open.kmi.iserve.imatcher.strategy.impl;

import java.util.List;

import uk.ac.open.kmi.iserve.imatcher.strategy.api.AbstractStrategy;

public final class LevNStrategy extends AbstractStrategy {

	public final String getStrategy(List<String> parameters) {
		return prolog + NL + simpackNS + NL + "PREFIX msm: <http://cms-wg.sti2.org/ns/minimal-service-model#> " + NL
				+ "SELECT DISTINCT ?service ?label ?sim" + NL
				+ "WHERE { " + "?service rdf:type msm:Service . " + NL
				+ "?service rdfs:label ?label . "
				+ "IMPRECISE { " + NL
				+ "?sim simpack:levenshtein" + " (?label \"" + parameters.get(0) + "\") " + NL
				+ "} " + NL
				+ "} ORDER BY DESC (?sim)";
	}

	public final String getStrategy() {
		return prolog + NL + simpackNS + NL + "PREFIX msm: <http://cms-wg.sti2.org/ns/minimal-service-model#> " + NL
				+ "SELECT DISTINCT ?service ?label ?sim" + NL
				+ "WHERE { " + "?service rdf:type msm:Service . "
				+ NL + "?service rdfs:label ?label . "
				+ "IMPRECISE { " + NL
				+ "?sim simpack:levenshtein" + " (?label <<<queryURL>>>) " + NL
				+ "} " + NL
				+ "} ORDER BY DESC (?sim)";
	}

	public static void main(String[] args)  {
		LevNStrategy s = new LevNStrategy();
		System.out.println(s.getStrategy());
	}

}
