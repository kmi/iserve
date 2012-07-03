package uk.ac.open.kmi.iserve.discovery.api;
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

import java.net.URL;

/**
 * Interface that match results obtained from discovery plugins should implement
 * 
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public interface MatchResult {

	public URL getMatchUrl();
	
	public String getMatchLabel();
	
	public Float getScore();
	
	public void setScore(Float score);
	
	public URL getRequest();
	
	public void setRequest(URL request);
	
	public URL getEngineUrl();
	
	public void setEngineUrl(URL engineUrl);
	
	public String getExplanation();
	
}
