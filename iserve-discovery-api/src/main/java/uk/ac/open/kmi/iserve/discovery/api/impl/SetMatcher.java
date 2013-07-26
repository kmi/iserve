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

package uk.ac.open.kmi.iserve.discovery.api.impl;


import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.Matcher;


public class SetMatcher<E> implements Matcher<E> {

    private Matcher<E> matcher;

    public SetMatcher(Matcher<E> matcher) {
        this.matcher = matcher;
    }

    /*
    public ComplexMatch match(Set<E> source, Set<E> destination) {
        ComplexMatch<E> result = new ComplexMatch<E>();
        for(E e1 : source){
            for (E e2 : destination){
                MatchResult matchResult = match(e1,e2);
                //result.add()
            }
        }
    }*/

    @Override
    public MatchResult match(E origin, E destination) {
        return this.matcher.match(origin, destination);
    }

    @Override
    public String getMatcherDescription() {
        return matcher.getMatcherDescription() + " (Wrapped)";
    }

    @Override
    public String getMatcherVersion() {
        return matcher.getMatcherVersion();
    }
}
