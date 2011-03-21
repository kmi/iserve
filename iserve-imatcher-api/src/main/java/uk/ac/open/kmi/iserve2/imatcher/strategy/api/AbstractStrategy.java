/*
 * $Id: AbstractStrategy.java 448 2007-12-10 17:01:19Z kiefer $
 *
 * Created by Christoph Kiefer, kiefer@ifi.uzh.ch
 *
 * See LICENSE for more information about licensing and warranties.
 */
package uk.ac.open.kmi.iserve2.imatcher.strategy.api;

public abstract class AbstractStrategy implements IStrategy {

	public final static String NL = System.getProperty("line.separator");

	public final static String prolog = "PREFIX process: <http://www.daml.org/services/owl-s/1.1/Process.owl#>  "
			+ NL
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>   "
			+ NL
			+ "PREFIX grounding: <http://www.daml.org/services/owl-s/1.1/Grounding.owl#>  "
			+ NL
			+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>  "
			+ NL
			+ "PREFIX service: <http://www.daml.org/services/owl-s/1.1/Service.owl#>  "
			+ NL
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
			+ NL
			+ "PREFIX profile: <http://www.daml.org/services/owl-s/1.1/Profile.owl#> "
			+ NL
			+ "PREFIX dc: <http://purl.org/dc/elements/1.1/> "
			+ NL
			+ "PREFIX ph: <http://www.ifi.uzh.ch/ddis/ph/2006/08/ProcessHandbook.owl#> ";

	public final static String simpackNS = "PREFIX simpack: <java:uk.ac.open.kmi.iserve2.imatcher.isparql.apf.simpack.>"; 

	public final static String olaNS = "PREFIX ola: <java:ch.uzh.ifi.isparql.apf.ola.>";

	public final static String owlsmxNS = "PREFIX owlsmx: <java:ch.uzh.ifi.isparql.apf.owlsmx.>";

	public final static String PROFILE_NS = "http://www.daml.org/services/owl-s/1.1/Profile.owl#";

	public String getName() {
		return this.getClass().getName();
	}

	public String getShortName() {
		return this.getClass().getSimpleName();
	}
}
