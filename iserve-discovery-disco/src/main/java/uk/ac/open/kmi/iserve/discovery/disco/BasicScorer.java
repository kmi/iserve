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

import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchScorer;

/**
 * Class Description
 * 
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public class BasicScorer implements MatchScorer {

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.discovery.api.MatchScorer#computeScore(uk.ac.open.kmi.iserve.discovery.api.MatchResult)
	 */
	@Override
	public float computeScore(MatchResult match) {

		if (match == null) {
			return Float.MAX_VALUE;
		}

		if (match instanceof SimpleMatchResult) {
			MatchType matchType = ((SimpleMatchResult) match).getMatchType();

			switch (matchType) {
			// Scoring for FC
			case EXACT:
				return 0;

			case SSSOG:
				return 1;

			case GSSOS:
				return 2;

			default:
				return Float.MAX_VALUE;
			}
		}
		return Float.MAX_VALUE;

	}

}
