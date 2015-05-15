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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Luca Panziera on 20/05/2014.
 */
public class ClassificationDiscoveryFunction extends DiscoveryFunction {

    protected String operator;
    protected String type;
    protected Set<URI> parameters = new LinkedHashSet<URI>();

    public ClassificationDiscoveryFunction(JsonElement expression) {
        super(null);
        parse(expression);
    }

    public ClassificationDiscoveryFunction(JsonElement expression, String type) {
        super(null);
        this.type = type;
        parse(expression);
    }

    private void parse(JsonElement discovery) {
        if (discovery.isJsonObject()) {
            if (discovery.getAsJsonObject().has("func-rdfs")) {
                JsonObject functionObject = discovery.getAsJsonObject().getAsJsonObject("func-rdfs");
                type = functionObject.get("type").getAsString();
                JsonElement classes = discovery.getAsJsonObject().getAsJsonObject("func-rdfs").get("classes");
                subFunctions.add(new ClassificationDiscoveryFunction(classes, type));
            } else {
                JsonElement operatorEl = null;
                if (discovery.getAsJsonObject().has("and")) {
                    operator = "and";
                    operatorEl = discovery.getAsJsonObject().get("and");
                }
                if (discovery.getAsJsonObject().has("or")) {
                    operator = "or";
                    operatorEl = discovery.getAsJsonObject().get("or");
                }
                if (discovery.getAsJsonObject().has("diff")) {
                    operator = "diff";
                    operatorEl = discovery.getAsJsonObject().get("diff");
                }
                if (operatorEl.isJsonPrimitive()) {
                    try {
                        parameters.add(new URI(operatorEl.getAsString()));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                } else if (operatorEl.isJsonArray()) {
                    for (JsonElement clazz : operatorEl.getAsJsonArray()) {
                        if (clazz.isJsonPrimitive()) {
                            if (operator.equals("diff")) {
                                subFunctions.add(new ClassificationDiscoveryFunction(clazz, type));
                            } else {
                                try {
                                    parameters.add(new URI(clazz.getAsString()));
                                } catch (URISyntaxException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (clazz.isJsonObject()) {
                            subFunctions.add(new ClassificationDiscoveryFunction(clazz.getAsJsonObject(), type));
                        }
                    }
                }
            }
        } else if (discovery.isJsonPrimitive()) {
            try {
                parameters.add(new URI(discovery.getAsString()));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<URI, MatchResult> compute() {
        Map<URI, MatchResult> resultMap = Maps.newHashMap();
        if (!subFunctions.isEmpty()) {
            for (DiscoveryFunction subFunction : subFunctions) {
                subFunction.fork();
            }
        }
        if (!parameters.isEmpty()) {
            if (operator == null) {
                operator = "or";
            }
            if (operator.equals("or")) {
                if (type.equals("svc")) {
                    resultMap.putAll(serviceDiscoverer.findServicesClassifiedBySome(parameters));
                }
                if (type.equals("op")) {
                    resultMap.putAll(operationDiscoverer.findOperationsClassifiedBySome(parameters));
                }
            }
            if (operator.equals("and")) {
                if (type.equals("svc")) {
                    resultMap.putAll(serviceDiscoverer.findServicesClassifiedByAll(parameters));
                }
                if (type.equals("op")) {
                    resultMap.putAll(operationDiscoverer.findOperationsClassifiedByAll(parameters));
                }
            }
        }

        if (!subFunctions.isEmpty()) {
            for (DiscoveryFunction subFunction : subFunctions) {
                Map<URI, MatchResult> subResult = subFunction.join();
                if (operator == null || operator.equals("or")) {
                    resultMap.putAll(subResult);
                } else if (operator.equals("and")) {
                    if (resultMap.isEmpty()) {
                        resultMap.putAll(subResult);
                    } else {
                        resultMap.keySet().retainAll(subResult.keySet());
                    }
                } else if (operator.equals("diff")) {
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
