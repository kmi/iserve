package uk.ac.open.kmi.iserve.discovery.disco;
import java.net.URL;

import uk.ac.open.kmi.iserve.discovery.api.MatchResult;

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

/**
 * Class Description
 * 
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public class MatchResultImpl implements MatchResult {

	private URL matchUrl;
	private String matchLabel;
	private MatchType matchType;
	private Float score;
	private URL request;
	private URL engineUrl;
	
	
	
	/**
	 * @param url
	 * @param label
	 * @param matchType
	 */
	public MatchResultImpl(URL url, String label) {
		super();
		this.matchUrl = url;
		this.matchLabel = label;
	}
	
	/**
	 * @param matchUrl
	 * @param matchLabel
	 * @param matchType
	 * @param score
	 * @param request
	 * @param engineUrl
	 */
	public MatchResultImpl(URL matchUrl, String matchLabel,
			MatchType matchType, float score, URL request, URL engineUrl) {
		super();
		this.matchUrl = matchUrl;
		this.matchLabel = matchLabel;
		this.matchType = matchType;
		this.score = score;
		this.request = request;
		this.engineUrl = engineUrl;
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
		return this.matchType.getLongName() + " match with a score of: " +
			this.score;
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

}
