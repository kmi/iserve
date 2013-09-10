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

import uk.ac.open.kmi.iserve.discovery.api.MatchType;


/**
 * Enumeration of all the MatchTypes contemplated for logic concept matching
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @author <a href="mailto:pablo.rodriguez.mier@usc.es">Pablo Rodriguez Mier</a> (CITIUS - Universidad de Santiago de Compostela)
 * @author Jacek Kopecky (Knowledge Media Institute - The Open University)
 */
public enum LogicConceptMatchType implements MatchType<LogicConceptMatchType> {

    // Increasing order of match
    Fail("There is no subsumption relationship between the concept present and the one required"),
    Subsume("The concept present is a superclass of the one required"),
    Plugin("The concept present is a subclass of the one required"),
    Exact("The concept is an exact match with that required");

    private final String description;

    /**
     *
     */
    private LogicConceptMatchType(String description) {
        this.description = description;
    }

    /**
     * @return the longName
     */
    public String description() {
        return this.description;
    }
}