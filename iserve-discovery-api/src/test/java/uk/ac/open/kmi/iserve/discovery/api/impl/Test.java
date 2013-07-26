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

import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.api.MatchTypes;

/**
 * Test
 * TODO: Provide Description
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @author <a href="mailto:pablo.rodriguez.mier@usc.es">Pablo Rodriguez Mier</a> (CITIUS - Universidad de Santiago de Compostela)
 * @since 26/07/2013
 */
public class Test {

    public static void main(String[] args) {

        MatchTypes<MatchType> myTypes = EnumMatchTypes.of(DiscoMatchType.class);
        System.out.println("Lowest type: " + myTypes.getLowest());
        System.out.println("Highest type: " + myTypes.getHighest());
        System.out.println("Next of " + DiscoMatchType.Fail.name() + " is " + myTypes.getNext(DiscoMatchType.Fail));
        System.out.println("Next of (should be null) " + DiscoMatchType.Exact.name() + " is " + myTypes.getNext(DiscoMatchType.Exact));
        System.out.println("Previous of " + DiscoMatchType.Exact.name() + " is " + myTypes.getPrevious(DiscoMatchType.Exact));
        System.out.println("Previous of (should be null): " + DiscoMatchType.Fail.name() + " is " + myTypes.getPrevious(DiscoMatchType.Fail));

        System.out.println("Compare " + DiscoMatchType.Fail.name() + " with " + myTypes.getPrevious(DiscoMatchType.Subsume) + " = " + DiscoMatchType.Fail.compareTo(DiscoMatchType.Subsume));

    }


}
