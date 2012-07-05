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

/**
 * Class Description
 * 
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public class CompositeMatchResultImpl implements CompositeMatchResult {

	private URL matchUrl;
	private String matchLabel;
	private MatchType matchType;
	private Float score;
	private URL request;
	private URL engineUrl;
	private Set<MatchResult> innerMatches = new HashSet<MatchResult>();
	
	
	/**
	 * Constructor 
	 * 
	 * @param url
	 * @param label
	 * @param matchType
	 */
	public CompositeMatchResultImpl(URL url, String label) {
		super();
		this.matchUrl = url;
		this.matchLabel = label;
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
		super();
		this.matchUrl = matchUrl;
		this.matchLabel = matchLabel;
		this.matchType = matchType;
		this.score = score;
		this.request = request;
		this.engineUrl = engineUrl;
		this.innerMatches = innerMatches;
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
	 * @see uk.ac.open.kmi.iserve.discovery.api.CompositeMatchResult#setInnerMatches(java.util.Set)
	 */
	public void setInnerMatches(Set<MatchResult> innerMatches) {
		if (innerMatches != null) {
			this.innerMatches = innerMatches;
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
	 * @see MatchResult#getMatchUrl()
	 */
	public URL getMatchUrl() {
		return matchUrl;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.discovery.api.MatchResult#getMatchLabel()
	 */
	public String getMatchLabel() {
		return matchLabel;
	}
	
	/**
	 * @return the matchType
	 */
	public MatchType getMatchType() {
		return this.matchType;
	}

	/* (non-Javadoc)
	 * @see MatchResult#getScore()
	 */
	public Float getScore() {
		return score;
	}

	/* (non-Javadoc)
	 * @see MatchResult#getRequest()
	 */
	public URL getRequest() {
		return request;
	}

	/* (non-Javadoc)
	 * @see MatchResult#getEngineUrl()
	 */
	public URL getEngineUrl() {
		return engineUrl;
	}

	/* (non-Javadoc)
	 * @see MatchResult#getExplanation()
	 */
	public String getExplanation() {
		StringBuffer result = new StringBuffer();
		result.append("Composite Match of type: " + this.matchType.getShortName() + Util.NL);
		result.append("Total Score: " + this.getScore() + Util.NL); 
		for (MatchResult match : innerMatches) {
			result.append("Inner match - " + match.getExplanation() + Util.NL);
		}
		return result.toString();
	}

	/* (non-Javadoc)
	 * @see MatchResult#setScore()
	 */
	public void setScore(Float score) {
		this.score = score;
	}
	
	/**
	 * @param matchType the matchType to set
	 */
	public void setMatchType(MatchType matchType) {
		this.matchType = matchType;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.discovery.api.MatchResult#setRequest(java.net.URL)
	 */
	@Override
	public void setRequest(URL request) {
		this.request = request;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.discovery.api.MatchResult#setEngineUrl(java.net.URL)
	 */
	@Override
	public void setEngineUrl(URL engineUrl) {
		this.engineUrl = engineUrl;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.discovery.api.CompositeMatchResult#getInnerMatches()
	 */
	@Override
	public Set<MatchResult> getInnerMatches() {
		return this.innerMatches;
	}

}
