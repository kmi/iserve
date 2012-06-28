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
package uk.ac.open.kmi.iserve.discovery.engine;

import java.util.Comparator;

import com.google.common.primitives.Floats;

import uk.ac.open.kmi.iserve.discovery.api.MatchResult;

/**
 * Reference implementation of basic Comparators methods for Match Results
 * 
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public enum MatchesRankers implements Comparator<MatchResult> {

	/**
	 * Simple comparator for Match Results based on the score of the match results
	 * 
	 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
	 */
	BY_SCORE{

		@Override
		public int compare(MatchResult arg0, MatchResult arg1) {
			return Floats.compare(arg0.getScore(), arg1.getScore());
		}
    },
    
    /**
     * Simple comparator for Match Results based on the URLs of the match results
     * 
     * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
     */
    BY_URL{

		@Override
		public int compare(MatchResult arg0, MatchResult arg1) {
			return arg0.getMatchUrl().toString().compareTo(arg1.getMatchUrl().toString());
		}
    }
	
}
