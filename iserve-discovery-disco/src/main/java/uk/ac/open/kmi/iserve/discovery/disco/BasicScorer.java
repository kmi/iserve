/*
 * Copyright (c) 2013. Knowledge Media Institute - The Open University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.open.kmi.iserve.discovery.disco;

import com.google.common.base.Function;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;

/**
 * Class Description
 *
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public class BasicScorer implements Function<MatchResult, MatchResult> {

    /* (non-Javadoc)
     * @see com.google.common.base.Function#apply(java.lang.Object)
     */
    @Override
    public MatchResult apply(MatchResult input) {
        /*
        if (input == null) {
			return null;
		} else {

			if (input instanceof CompositeMatchResult) {
				float score = 0;
				for (MatchResult innerMatch : ((CompositeMatchResult) input).getInnerMatches()) {
					Float innerScore = innerMatch.getScore();
					if (innerScore == null) {
						this.apply(innerMatch);
						innerScore = innerMatch.getScore();
					}
					// TODO; Carry out a proper calculation here
					score += innerScore;
				}
				input.setScore(score);
			} else {
				MatchType matchType = input.getMatchType();
				input.setScore(Float.valueOf(matchType.ordinal()));
			} 
		}
		return input; */
        // TODO; Implementation required!
        return null;
    }

}
