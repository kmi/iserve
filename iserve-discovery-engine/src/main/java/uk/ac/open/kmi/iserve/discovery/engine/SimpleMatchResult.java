package uk.ac.open.kmi.iserve.discovery.engine;
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
public class SimpleMatchResult implements MatchResult {

	private URL matchUrl;
	private float score;
	private URL request;
	private URL engineUrl;
	
	/**
	 * @param url1
	 * @param score
	 * @param request
	 * @param engineUrl
	 */
	public SimpleMatchResult(URL url1, float score, URL request, URL engineUrl) {
		super();
		this.matchUrl = url1;
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
	 * @see MatchResult#getScore()
	 */
	public float getScore() {
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
		// TODO Auto-generated method stub
		return null;
	}

}
