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

package uk.ac.open.kmi.iserve.discovery.api;

import java.util.Map;

/**
 * MatchersRegistry
 * TODO: Provide Description
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 19/09/2013
 */
public interface MatchersRegistry {
    Map<String, ConceptMatcher> getConceptMatchers();

    Map<String, ServiceMatcher> getServiceMatchers();

    ConceptMatcher getDefaultConceptMatcher();

    ServiceMatcher getDefaultServiceMatcher();

    void setDefaultConceptMatcher(ConceptMatcher defaultConceptMatcher);

    void setDefaultServiceMatcher(ServiceMatcher defaultServiceMatcher);
}
