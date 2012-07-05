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

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import uk.ac.open.kmi.iserve.discovery.api.CompositeMatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;

import com.google.common.base.Function;

/**
 * This Function supports merging two Match Results over the same item into one 
 * match. This is necessary when combining different separate matches into one.
 * The resulting match would be a composite one when all the inner matches are.
 * 
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public class MatchResultsMerger implements Function<Collection<MatchResult>, MatchResult> {

	/* (non-Javadoc)
	 * @see com.google.common.base.Function#apply(java.lang.Object)
	 */
	@Override
	public MatchResult apply(Collection<MatchResult> input) {
		if (input == null || input.isEmpty()) {
			return null;
		}
		
		Iterator<MatchResult> it = input.iterator();
		MatchResult match = it.next();
		
		if (input.size() == 1) {
			return match;
		}
		
		// There are at least 2 results
		// Use the first one as reference and ignore matches for different items
		URL matchUrl = match.getMatchUrl();
		String matchLabel = match.getMatchLabel();
		
		CompositeMatchResult result = new CompositeMatchResultImpl(matchUrl, matchLabel);
		result.addInnerMatch(match);
		
		while (it.hasNext()) {
			match = it.next();
			if (match.getMatchUrl().equals(matchUrl)) {
				// Just in case check the matches are for the same item
				result.addInnerMatch(match);
			}
		}
		
		return result;
	}
}
