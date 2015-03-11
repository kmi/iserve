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

import com.google.common.collect.*;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.ConfigurationProperty;
import uk.ac.open.kmi.iserve.core.iServeProperty;
import uk.ac.open.kmi.iserve.discovery.api.freetextsearch.FreeTextSearchPlugin;
import uk.ac.open.kmi.iserve.discovery.api.ranking.*;
import uk.ac.open.kmi.iserve.discovery.api.ranking.impl.ReverseRanker;
import uk.ac.open.kmi.iserve.discovery.api.ranking.impl.StandardRanker;
import uk.ac.open.kmi.iserve.discovery.util.Pair;
import uk.ac.open.kmi.iserve.sal.events.Event;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.manager.IntegratedComponent;
import uk.ac.open.kmi.iserve.sal.util.caching.Cache;
import uk.ac.open.kmi.iserve.sal.util.caching.CacheException;
import uk.ac.open.kmi.iserve.sal.util.caching.CacheFactory;
import uk.ac.open.kmi.msm4j.vocabulary.MSM;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

/**
 * Created by Luca Panziera on 19/05/2014.
 */
public class DiscoveryEngineImpl extends IntegratedComponent implements DiscoveryEngine {

    private static Cache<String, Map<URI, Pair<Double, MatchResult>>> resultCache;
    private OperationDiscoverer operationDiscoverer;
    private ServiceDiscoverer serviceDiscoverer;
    private FreeTextSearchPlugin freeTextSearchPlugin;
    private Set<Filter> filters;
    private Set<Scorer> scorers;
    private ScoreComposer scoreComposer;
    private Logger logger = LoggerFactory.getLogger(DiscoveryEngineImpl.class);

    @Inject
    public DiscoveryEngineImpl(EventBus eventBus,
                               @iServeProperty(ConfigurationProperty.ISERVE_URL) String iServeUri,
                           ServiceDiscoverer serviceDiscoverer,
                           OperationDiscoverer operationDiscoverer,
                           @Nullable FreeTextSearchPlugin freeTextSearchPlugin,
                           @Nullable Set<Filter> filters,
                           @Nullable Set<AtomicFilter> atomicFilters,
                           @Nullable Set<Scorer> scorers,
                           @Nullable Set<AtomicScorer> atomicScorers,
                           @Nullable ScoreComposer scoreComposer,
                           CacheFactory cacheFactory

    ) throws SalException {
        super(eventBus, iServeUri);

        this.operationDiscoverer = operationDiscoverer;
        this.serviceDiscoverer = serviceDiscoverer;
        this.freeTextSearchPlugin = freeTextSearchPlugin;
        this.filters = filters;
        this.scorers = scorers;
        this.scoreComposer = scoreComposer;

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

        if (resultCache == null) {
            try {
                resultCache = cacheFactory.createPersistentCache("discovery-result");
            } catch (CacheException e) {
                resultCache = cacheFactory.createInMemoryCache("discovery-result");
            }

        }

    }

    public Map<URI, Pair<Double, MatchResult>> discover(String request) {
        if (resultCache.containsKey(request)) {
            return resultCache.get(request);
        } else {
            Map<URI, Pair<Double, MatchResult>> result = discover(new JsonParser().parse(request));
            resultCache.put(request, result);
            return result;
        }

    }

    public Map<URI, Pair<Double, MatchResult>> discover(JsonElement request) {
        return discover(parse(request));
    }

    private DiscoveryRequest parse(JsonElement jsonRequest) {
        boolean filter = false;
        boolean rank = false;
        Map<Class, String> parametersMap = Maps.newHashMap();
        Set<Class> requestedModules = Sets.newHashSet();
        String rankingType = null;

        JsonObject requestObject = jsonRequest.getAsJsonObject();

        // Discovery parsing
        JsonObject discovery = requestObject.getAsJsonObject("discovery");
        DiscoveryFunction discoveryFunction = null;
        // Free text search parsing
        if (discovery.has("query")) {
            URI type = null;
            if (discovery.get("type").getAsString().equals("svc")) {
                type = URI.create(MSM.Service.getURI());
            } else if (discovery.get("type").getAsString().equals("op")) {
                type = URI.create(MSM.Operation.getURI());
            }
            discoveryFunction = new FreeTextDiscoveryFunction(freeTextSearchPlugin, discovery.get("query").getAsString(), type);
        }
        //Semantic Discovery parsing

        if (discovery.has("func-rdfs")) {
            JsonObject functionObject = discovery.getAsJsonObject("func-rdfs");
            String type = functionObject.get("type").getAsString();
            discoveryFunction = buildSemanticDiscoveryFunction("func-rdfs", type, "classes", functionObject);
        }
        if (discovery.has("io-rdfs")) {
            String type = discovery.getAsJsonObject("io-rdfs").get("type").getAsString();
            JsonObject expressionObject = discovery.getAsJsonObject("io-rdfs").getAsJsonObject("expression");
            if (expressionObject.has("input")) {
                discoveryFunction = buildSemanticDiscoveryFunction("io-rdfs", type, "input", expressionObject);
            } else if (expressionObject.has("output")) {
                discoveryFunction = buildSemanticDiscoveryFunction("io-rdfs", type, "output", expressionObject);
            } else if (expressionObject.has("or")) {
                List<SemanticDiscoveryFunction> subFunctions = Lists.newArrayList();
                JsonObject or = expressionObject.getAsJsonObject("or");
                subFunctions.add(buildSemanticDiscoveryFunction("io-rdfs", type, "input", or));
                subFunctions.add(buildSemanticDiscoveryFunction("io-rdfs", type, "output", or));
                discoveryFunction = new CompositeSemanticDiscoveryFunction("or", "io-rdfs", type, subFunctions, operationDiscoverer, serviceDiscoverer);
            } else if (expressionObject.has("and")) {
                List<SemanticDiscoveryFunction> subFunctions = Lists.newArrayList();
                JsonObject and = expressionObject.getAsJsonObject("and");
                subFunctions.add(buildSemanticDiscoveryFunction("io-rdfs", type, "input", and));
                subFunctions.add(buildSemanticDiscoveryFunction("io-rdfs", type, "output", and));
                discoveryFunction = new CompositeSemanticDiscoveryFunction("and", "io-rdfs", type, subFunctions, operationDiscoverer, serviceDiscoverer);
            }
        }

        try {
            // filtering parsing
            if (requestObject.has("filtering")) {
                filter = true;
                JsonArray filters = requestObject.getAsJsonArray("filtering");
                for (JsonElement filterElement : filters) {
                    JsonObject filterObject = filterElement.getAsJsonObject();
                    Class filterClass = Class.forName(filterObject.get("filterClass").getAsString());
                    requestedModules.add(filterClass);
                    if (filterObject.has("parameters")) {
                        parametersMap.put(filterClass, filterObject.get("parameters").toString());
                    }
                }

            }
            //scoring parsing
            if (requestObject.has("scoring")) {
                rank = true;
                JsonArray scorers = requestObject.getAsJsonArray("scoring");
                for (JsonElement scorerElement : scorers) {
                    JsonObject scorerObject = scorerElement.getAsJsonObject();
                    Class scorerClass = Class.forName(scorerObject.get("scorerClass").getAsString());
                    requestedModules.add(scorerClass);
                    if (scorerObject.has("parameters")) {
                        parametersMap.put(scorerClass, scorerObject.get("parameters").toString());
                    }
                }
                rankingType = "standard";

            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        // ranking parsing
        if (requestObject.has("ranking")) {
            rank = true;
            if (!requestObject.get("ranking").getAsString().equalsIgnoreCase("inverse")) {
                rankingType = "standard";
            } else {
                rankingType = requestObject.get("ranking").getAsString().toLowerCase();
            }
        }

        DiscoveryRequest discoveryRequest = new DiscoveryRequest(discoveryFunction, requestedModules, filter, rank, parametersMap, rankingType);
        return discoveryRequest;
    }

    private SemanticDiscoveryFunction buildSemanticDiscoveryFunction(String function, String type, String parameterType, JsonObject functionObject) {
        try {
            if (functionObject.get(parameterType).isJsonPrimitive()) {
                Set<URI> parameters = ImmutableSet.of(new URI(functionObject.get(parameterType).getAsString()));
                return new SemanticDiscoveryFunction(function, type, parameters, parameterType, operationDiscoverer, serviceDiscoverer);
            } else if (functionObject.get(parameterType).isJsonObject()) {
                JsonObject classes = functionObject.getAsJsonObject(parameterType);
                return buildCompositeSemanticDiscoveryFuction(function, type, parameterType, classes);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    private CompositeSemanticDiscoveryFunction buildCompositeSemanticDiscoveryFuction(String function, String type, String parameterType, JsonObject expression) {
        if (expression.has("or")) {
            return new CompositeSemanticDiscoveryFunction("or", function, type, parameterType, operationDiscoverer, serviceDiscoverer, expression.get("or"));
        }
        if (expression.has("and")) {
            return new CompositeSemanticDiscoveryFunction("and", function, type, parameterType, operationDiscoverer, serviceDiscoverer, expression.get("and"));
        }
        return null;
    }


    public Map<URI, Pair<Double, MatchResult>> discover(DiscoveryRequest discoveryRequest) {

        logger.info("Functional discovery");
        DiscoveryFunction discoveryFunction = discoveryRequest.getDiscoveryFunction();
        ForkJoinPool pool = new ForkJoinPool();
        Map<URI, MatchResult> funcResults = pool.invoke(discoveryFunction);

        Set<URI> filteredResources = funcResults.keySet();
        if (discoveryRequest.filter()) {
            for (Filter filter : filters) {
                Class filterClass;
                if (filter instanceof MolecularFilter) {
                    filterClass = ((MolecularFilter) filter).getAtomicFilter().getClass();
                } else {
                    filterClass = filter.getClass();
                }
                if (discoveryRequest.getRequestedModules().isEmpty() || discoveryRequest.getRequestedModules().contains(filterClass)) {
                    logger.info("Filtering: {}", filter);
                    if (discoveryRequest.getModuleParametersMap().containsKey(filterClass)) {
                        filteredResources = filter.apply(filteredResources, discoveryRequest.getModuleParametersMap().get(filterClass));
                    } else {
                        filteredResources = filter.apply(filteredResources);
                    }
                }
            }
        }

        if (discoveryRequest.rank()) {
            if (scorers == null) {
                logger.warn("Injection of scores without score composer: Ranking omitted!");
            } else {
                logger.info("Scoring");
                Map<Scorer, Map<URI, Double>> localScoresMap = Maps.newHashMap();

                for (Scorer scorer : scorers) {
                    Class scorerClass;
                    if (scorer instanceof MolecularScorer) {
                        scorerClass = ((MolecularScorer) scorer).getAtomicScorer().getClass();
                    } else {
                        scorerClass = scorer.getClass();
                    }
                    if (discoveryRequest.getRequestedModules().isEmpty() || discoveryRequest.getRequestedModules().contains(scorerClass)) {
                        Map<URI, Double> localScores;
                        if (discoveryRequest.getModuleParametersMap().containsKey(scorerClass)) {
                            localScores = scorer.apply(filteredResources, discoveryRequest.getModuleParametersMap().get(scorerClass));
                        } else {
                            localScores = scorer.apply(filteredResources);
                        }
                        localScoresMap.put(scorer, localScores);
                    }
                }

                Map<URI, Double> globalScores = scoreComposer.compose(localScoresMap);


                logger.info("Ranking");
                Map<URI, Double> rankedURIs;
                if (discoveryRequest.getRankingType().equals("standard")) {
                    rankedURIs = new StandardRanker().rank(globalScores);
                } else {
                    rankedURIs = new ReverseRanker().rank(globalScores);
                }

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

    @Subscribe
    public void clearCache(Event e) {
        resultCache.clear();
    }
}
