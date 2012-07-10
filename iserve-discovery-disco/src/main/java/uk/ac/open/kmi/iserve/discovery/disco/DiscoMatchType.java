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

import uk.ac.open.kmi.iserve.discovery.api.MatchType;


/**
 * Enumeration of all the MatchTypes currently contemplated by the discovery
 * implementation
 * 
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 * @author Jacek Kopecky (Knowledge Media Institute - The Open University)
 */
public enum DiscoMatchType implements MatchType {

	// Increasing order of match
	FAIL("Fail", "Fail", "There is no subsumption relationship between the concept present and the one required"),
	PARTIAL_SUBSUME("Partial Subsume", "Partial Subsume", "The goal has a superclass of some service input class"),
	PARTIAL_PLUGIN("Partial Plugin", "Partial Plugin", "The goal has a subclass of some service input class"),
	SUBSUME("Subsume", "Subsume", "The concept present is a superclass of the one required"),
	PLUGIN("Plugin", "Plugin", "The concept present is a subclass of the one required"),
	EXACT("Exact", "Exact", "The concept is an exact match with that required"),
	
	// Functional Classification Match Types
	SSSOG("SSSOG", "Service Subset of Goal", "For each goal class the service has a category that is a subclass of it"),
	GSSOS("GSSOS", "Goal Subset of Service", "For each service category the goal asks for a subclass of it"),
	INTER_DEGREE("Intersection", "Intersection", "There is a class that is a subclass of all goal classes and service categories"); // Not implemented	
	
	private final String shortName;
	private final String longName;
	private final String description;
	
	/**
	 * 
	 */
	private DiscoMatchType(String shortName, String longName, String description) {
		this.description = description;
		this.shortName = shortName;
		this.longName = longName;
	}
	
	/**
	 * @return the longName
	 */
	public String getDescription() {
		return this.description;
	}
	
	/**
	 * @return the shortName
	 */
	public String getShortName() {
		return this.shortName;
	}
	
	/**
	 * @return the longName
	 */
	public String getLongName() {
		return this.longName;
	}
}
