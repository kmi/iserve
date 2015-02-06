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

package uk.ac.open.kmi.iserve.discovery.index;

import com.google.common.collect.Table;
import com.google.inject.Inject;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.sal.manager.KnowledgeBaseManager;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Pablo Rodríguez Mier
 */
public class DefaultConceptMatcherIndexFactory implements IndexFactory<URI, Map<URI, String>> {

    private ConceptMatcher delegatedMatcher;
    private KnowledgeBaseManager kb;
    private MapFactory factory;

    public DefaultConceptMatcherIndexFactory() {
        //delegatedMatcher = MatchersFactory.createConceptMatcher();
    }

    @Inject
    public DefaultConceptMatcherIndexFactory(MapFactory factory) {

    }


    private void populate(Map<URI, Map<URI, String>> cache) {
        Set<URI> classes = new HashSet<URI>(kb.listConcepts(null));
        Table<URI, URI, MatchResult> table = null; //delegatedMatcher.listMatchesAtLeastOfType(classes, LogicConceptMatchType.Plugin);
        for (URI origin : table.rowKeySet()) {
            Map<URI, String> destMatch = new HashMap<URI, String>();
            Map<URI, MatchResult> dest = table.row(origin);
            for (URI destUri : dest.keySet()) {
                destMatch.put(destUri, dest.get(destUri).getMatchType().name());
            }
            cache.put(origin, destMatch);
        }

    }

    @Override
    public ConcurrentMap<URI, Map<URI, String>> createIndex() {
        ConcurrentMap<URI, Map<URI, String>> index = this.factory.createMap();
        populate(index);
        return index;
    }
}