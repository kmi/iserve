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
package uk.ac.open.kmi.iserve.discovery.api;

import java.util.Set;

/**
 * Composite Match Results capture detailed match results within inner matches.
 * This allows capturing both complex match results as well as the traces of
 * these results for ulterior processing (scoring, ranking, explanation, etc)
 * 
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public interface CompositeMatchResult extends MatchResult {

	/**
	 * Adds an inner match to this composite object
	 * 
	 * @param innerMatch the match to add
	 */
	public void addInnerMatch(MatchResult innerMatch);
	
	/**
	 * Get inner matches for this composite object
	 * 
	 * @return the inner matches
	 */
	public Set<MatchResult> getInnerMatches();
	
	/**
	 * Set the inner matches for this composite object
	 * 
	 * @param innerMatches
	 */
	public void setInnerMatches(Set<MatchResult> innerMatches);
	
	/**
	 * Removes an inner match from this composite object
	 * 
	 * @param innerMatch the match to remove
	 */
	public void removeInnerMatch(MatchResult innerMatch);
	
}
