/*
 * Copyright (c) 2014. Knowledge Media Institute - The Open University
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

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.discovery.api.ranking.*;
import uk.ac.open.kmi.iserve.discovery.util.Pair;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Luca Panziera on 19/05/2014.
 */
public class DiscoveryEngine {

    private OperationDiscoverer operationDiscoverer;
    private ServiceDiscoverer serviceDiscoverer;
    private Set<Filter> filters;
    private Set<Scorer> scorers;
    private ScoreComposer scoreComposer;
    private Ranker ranker;

    private boolean filtering = false;
    private boolean ranking = false;

    private Logger logger = LoggerFactory.getLogger(DiscoveryEngine.class);

    @Inject
    public DiscoveryEngine(ServiceDiscoverer serviceDiscoverer, OperationDiscoverer operationDiscoverer, Set<Filter> filters, Set<Scorer> scorers, Set<AtomicFilter> atomicFilters, Set<AtomicScorer> atomicScorers, @Nullable ScoreComposer scoreComposer, @Nullable Ranker ranker) {
        this.operationDiscoverer = operationDiscoverer;
        this.serviceDiscoverer = serviceDiscoverer;
        this.filters = filters;
        this.scorers = scorers;
        this.scoreComposer = scoreComposer;
        this.ranker = ranker;

        if (atomicFilters != null) {
            if (this.filters == null) {
                this.filters = new HashSet<Filter>();
            } else {
                this.filters = new HashSet<Filter>(this.filters);
            }
            for (AtomicFilter atomicFilter : atomicFilters) {
                this.filters.add(new MolecularFilter(atomicFilter));
            }
        }

        if (atomicScorers != null) {
            if (this.scorers == null) {
                this.scorers = new HashSet<Scorer>();
            } else {
                this.scorers = new HashSet<Scorer>(this.scorers);
            }
            for (AtomicScorer atomicScorer : atomicScorers) {
                this.scorers.add(new MolecularScorer(atomicScorer));
            }
        }
    }

    public Map<URI, Pair<Double, MatchResult>> discover(String request) {
        return discover(parse(request));
    }

    // TODO Implement a proper parser ()
    private DiscoveryFunction parse(String request) {
        List<String> tokens = Lists.newLinkedList(Splitter.on(CharMatcher.WHITESPACE).omitEmptyStrings().split(request));
        if (tokens.contains("filter")) {
            tokens.remove("filter");
            filtering = true;
        }

        if (tokens.contains("rank")) {
            tokens.remove("rank");
            ranking = true;
        }

        // Building of functional discovery logic

        DiscoveryFunction discoveryFunction = null;

        for (int i = 0; i < tokens.size(); i++) {
            if (discoveryFunction == null) {
                try {
                    discoveryFunction = new DiscoveryFunction(tokens.get(i), Sets.newHashSet(new URI(tokens.get(i + 1))), operationDiscoverer, serviceDiscoverer);
                    i += 2;
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
            if (i < tokens.size()) {
                try {
                    discoveryFunction = new CompositeDiscoveryFunction(discoveryFunction, new DiscoveryFunction(tokens.get(i + 1), Sets.newHashSet(new URI(tokens.get(i + 2))), operationDiscoverer, serviceDiscoverer), tokens.get(i));
                    i += 2;
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }

        return discoveryFunction;

    }


    public Map<URI, Pair<Double, MatchResult>> discover(DiscoveryFunction discoveryFunction) {


        logger.info("Functional discovery");
        Map<URI, MatchResult> funcResults = discoveryFunction.invoke();

        Set<URI> filteredResources = funcResults.keySet();
        if (filtering) {
            for (Filter filter : filters) {
                logger.info("Filtering: {}", filter);
                filteredResources = filter.apply(filteredResources);
            }
        }

        if (ranking) {
            if (scorers == null) {
                logger.warn("Injection of scores without score composer: Ranking omitted!");
            } else {
                logger.info("Scoring");
                Map<Scorer, Map<URI, Double>> localScoresMap = Maps.newHashMap();

                for (Scorer scorer : scorers) {
                    Map<URI, Double> localScores = scorer.apply(filteredResources);
                    localScoresMap.put(scorer, localScores);
                }

                Map<URI, Double> globalScores = scoreComposer.compose(localScoresMap);

                logger.info("Ranking");
                Map<URI, Double> rankedURIs = ranker.rank(globalScores);

                ImmutableMap.Builder<URI, Pair<Double, MatchResult>> builder = ImmutableMap.builder();
                for (URI resource : rankedURIs.keySet()) {
                    builder.put(resource, new Pair<Double, MatchResult>(rankedURIs.get(resource), funcResults.get(resource)));
                }


                return builder.build();
            }
        } else {
            ImmutableMap.Builder<URI, Pair<Double, MatchResult>> builder = ImmutableMap.builder();
            for (URI resource : filteredResources) {
                builder.put(resource, new Pair<Double, MatchResult>(new Double(0), funcResults.get(resource)));
            }
            return builder.build();
        }

        return ImmutableMap.of();
    }

    // It returns discovery engine configuration
    public String toString() {
        StringBuilder descriptionBuilder = new StringBuilder();
        descriptionBuilder.append("Operation Discoverer: ").append(operationDiscoverer.getClass().getName()).append("\n")
                .append("Service Discoverer: ").append(serviceDiscoverer.getClass().getName()).append("\n");
        if (!filters.isEmpty()) {
            descriptionBuilder.append("Filters: { ");
            for (Filter filter : filters) {
                descriptionBuilder.append(filter.getClass().getName()).append(" ");
            }
            descriptionBuilder.append("}\n");
        }
        if (!scorers.isEmpty()) {
            descriptionBuilder.append("Scorers: { ");
            for (Scorer scorer : scorers) {
                descriptionBuilder.append(scorer.getClass().getName()).append(" ");
            }
            descriptionBuilder.append("}\n");
        }

        return descriptionBuilder.toString();
    }

}
