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
import java.util.HashSet;
import java.util.Set;

import uk.ac.open.kmi.iserve.discovery.api.CompositeMatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;

/**
 * Class Description
 * 
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public class CompositeMatchResultImpl extends MatchResultImpl implements CompositeMatchResult {

	private Set<MatchResult> innerMatches = new HashSet<MatchResult>();
	
	/**
	 * Basic constructor
	 * @param matchUrl
	 * @param matchLabel
	 */
	public CompositeMatchResultImpl(URL matchUrl, String matchLabel) {
		super(matchUrl, matchLabel);
	}
	
	/**
	 * Constructor
	 * 
	 * @param matchUrl
	 * @param matchLabel
	 * @param matchType
	 * @param score
	 * @param request
	 * @param engineUrl
	 * @param innerMatches
	 */
	public CompositeMatchResultImpl(URL matchUrl, String matchLabel,
			MatchType matchType, Float score, URL request, URL engineUrl, 
			Set<MatchResult> innerMatches) {
		super(matchUrl, matchLabel, matchType, score, request, engineUrl);
		this.innerMatches = innerMatches;
	}


	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.discovery.api.CompositeMatchResult#getInnerMatches()
	 */
	@Override
	public Set<MatchResult> getInnerMatches() {
		return this.innerMatches;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.discovery.api.CompositeMatchResult#setInnerMatches(java.util.Set)
	 */
	public void setInnerMatches(Set<MatchResult> innerMatches) {
		if (innerMatches != null) {
			this.innerMatches = innerMatches;
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.discovery.api.CompositeMatchResult#addInnerMatch(uk.ac.open.kmi.iserve.discovery.api.MatchResult)
	 */
	public void addInnerMatch(MatchResult innerMatch) {
		if (innerMatch != null) {
			this.innerMatches.add(innerMatch);
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.discovery.api.CompositeMatchResult#removeInnerMatch(uk.ac.open.kmi.iserve.discovery.api.MatchResult)
	 */
	public void removeInnerMatch(MatchResult innerMatch) {
		if (innerMatch != null) {
			this.innerMatches.remove(innerMatch);
		}
	}

	/* (non-Javadoc)
	 * @see MatchResult#getExplanation()
	 */
	@Override
	public String getExplanation() {
		StringBuilder result = new StringBuilder();
		result.append("Composite Match of type: " + this.getMatchType().getShortName() + ". " + Util.NL);
		result.append("Total Score: " + this.getScore() + ". " + Util.NL); 
		for (MatchResult match : innerMatches) {
			result.append("Inner match - " + match.getExplanation() + ". " + Util.NL);
		}
		return result.toString();
	}

}
