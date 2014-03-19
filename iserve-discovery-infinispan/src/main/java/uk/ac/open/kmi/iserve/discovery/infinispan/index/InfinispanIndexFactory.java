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
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.discovery.index.IndexFactory;
import uk.ac.open.kmi.iserve.sal.manager.KnowledgeBaseManager;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Pablo Rodríguez Mier
 */
public class InfinispanIndexFactory implements IndexFactory<URI, Map<URI, MatchResult>> {
    private ConceptMatcher delegatedMatcher;
    private KnowledgeBaseManager kb;

    public InfinispanIndexFactory(ConceptMatcher delegatedMatcher, KnowledgeBaseManager kb) {
        // Get the default concept matcher to populate the index
        this.delegatedMatcher = delegatedMatcher;
        this.kb = kb;
    }

    private void populate(Cache<URI, Map<URI, MatchResult>> cache) {
        Set<URI> classes = new HashSet<URI>(kb.listConcepts(null));
        Table<URI, URI, MatchResult> table = delegatedMatcher.listMatchesAtLeastOfType(classes, LogicConceptMatchType.Subsume);
        for (URI origin : table.rowKeySet()) {
            Map<URI, MatchResult> dest = table.row(origin);
            cache.put(origin, dest);
        }
    }

    @Override
    public ConcurrentMap<URI, Map<URI, MatchResult>> createIndex() {
        Cache<URI, Map<URI, MatchResult>> cache = new DefaultCacheManager().getCache();
        populate(cache);
        return cache;
    }
}
