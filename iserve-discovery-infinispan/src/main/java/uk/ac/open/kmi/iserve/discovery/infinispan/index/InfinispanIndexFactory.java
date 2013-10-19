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

package uk.ac.open.kmi.iserve.discovery.infinispan.index;

import com.google.common.collect.Table;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.impl.MatchersFactory;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.discovery.disco.index.IndexFactory;
import uk.ac.open.kmi.iserve.sal.manager.iServeManager;
import uk.ac.open.kmi.iserve.sal.manager.impl.iServeFacade;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Pablo Rodríguez Mier
 */
public class InfinispanIndexFactory implements IndexFactory<URI, Map<URI, String>> {
    private iServeManager facade;
    private ConceptMatcher delegatedMatcher;

    public InfinispanIndexFactory() {
        // Get the default iServe facade
        facade = iServeFacade.getInstance();
        // Get the default concept matcher to populate the index
        delegatedMatcher = MatchersFactory.createConceptMatcher();
    }

    public InfinispanIndexFactory(ConceptMatcher matcher) {
        // Get the default iServe facade
        facade = iServeFacade.getInstance();
        // Get the default concept matcher to populate the index
        delegatedMatcher = matcher;
    }

    private void populate(Cache<URI, Map<URI, String>> cache) {
        Set<URI> classes = new HashSet<URI>(facade.getKnowledgeBaseManager().listConcepts(null));
        Table<URI, URI, MatchResult> table = delegatedMatcher.listMatchesAtLeastOfType(classes, LogicConceptMatchType.Plugin);
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
        Cache<URI, Map<URI, String>> cache = new DefaultCacheManager().getCache();
        populate(cache);
        return cache;
    }
}
