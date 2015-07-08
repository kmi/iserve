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

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import uk.ac.open.kmi.iserve.discovery.api.freetextsearch.FreeTextSearchPlugin;
import uk.ac.open.kmi.iserve.discovery.util.StringToMatchTypeConverter;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveTask;

/**
 * Created by Luca Panziera on 20/05/2014.
 */
public class DiscoveryFunction extends RecursiveTask<Map<URI, MatchResult>> {

    protected static ServiceDiscoverer serviceDiscoverer;
    protected static OperationDiscoverer operationDiscoverer;
    protected static FreeTextSearchPlugin freeTextSearchPlugin;
    protected static StringToMatchTypeConverter converter;
    protected List<DiscoveryFunction> subFunctions = new LinkedList<DiscoveryFunction>();
    protected String operator;

    public DiscoveryFunction(JsonObject discovery, ServiceDiscoverer serviceDiscoverer, OperationDiscoverer operationDiscoverer, FreeTextSearchPlugin freeTextSearchPlugin, StringToMatchTypeConverter converter) {
        DiscoveryFunction.converter = converter;
        parse(discovery);
        DiscoveryFunction.serviceDiscoverer = serviceDiscoverer;
        DiscoveryFunction.operationDiscoverer = operationDiscoverer;
        DiscoveryFunction.freeTextSearchPlugin = freeTextSearchPlugin;
    }

    public DiscoveryFunction(JsonObject discovery) {
        parse(discovery);
    }

    private void parse(JsonObject discovery) {
        if (discovery != null) {
            if (discovery.has("and")) {
                operator = "and";
                JsonElement subDiscovery = discovery.get("and");
                if (subDiscovery.isJsonArray()) {
                    for (JsonElement sub : subDiscovery.getAsJsonArray()) {
                        subFunctions.add(new DiscoveryFunction(sub.getAsJsonObject()));
                    }
                }
            } else if (discovery.has("or")) {
                operator = "or";
                JsonElement subDiscovery = discovery.get("or");
                if (subDiscovery.isJsonArray()) {
                    for (JsonElement sub : subDiscovery.getAsJsonArray()) {
                        subFunctions.add(new DiscoveryFunction(sub.getAsJsonObject()));
                    }
                }
            } else if (discovery.has("diff")) {
                operator = "diff";
                JsonElement subDiscovery = discovery.get("diff");
                if (subDiscovery.isJsonArray()) {
                    for (JsonElement sub : subDiscovery.getAsJsonArray()) {
                        subFunctions.add(new DiscoveryFunction(sub.getAsJsonObject()));
                    }
                }
            } else if (discovery.has("query")) {
                subFunctions.add(new FreeTextDiscoveryFunction(discovery));
            } else if (discovery.has("func-rdfs")) {
                subFunctions.add(new ClassificationDiscoveryFunction(discovery));
            } else if (discovery.has("io-rdfs")) {
                subFunctions.add(new IODiscoveryFunction(discovery));
            }
        }
    }

    @Override
    protected Map<URI, MatchResult> compute() {
        Map<URI, MatchResult> resultMap = Maps.newHashMap();
        if (operator == null) {
            return subFunctions.get(0).fork().join();
        } else {
            for (DiscoveryFunction subFunction : subFunctions) {
                subFunction.fork();
            }
            for (DiscoveryFunction subFunction : subFunctions) {
                Map<URI, MatchResult> subResult = subFunction.join();
                if (operator.equals("or")) {
                    resultMap.putAll(subResult);
                }
                if (operator.equals("and")) {
                    if (resultMap.isEmpty()) {
                        resultMap.putAll(subResult);
                    } else {
                        resultMap.keySet().retainAll(subResult.keySet());
                    }
                }
                if (operator.equals("diff")) {
                    if (resultMap.isEmpty()) {
                        resultMap.putAll(subResult);
                    } else {
                        resultMap.keySet().removeAll(subResult.keySet());
                    }
                }
            }
        }
        return resultMap;
    }
}
