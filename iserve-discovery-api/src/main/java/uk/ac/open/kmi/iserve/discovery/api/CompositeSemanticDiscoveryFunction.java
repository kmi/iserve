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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Luca Panziera on 21/05/2014.
 */
public class CompositeSemanticDiscoveryFunction extends SemanticDiscoveryFunction {
    private List<SemanticDiscoveryFunction> subFunctions = new LinkedList<SemanticDiscoveryFunction>();

    public CompositeSemanticDiscoveryFunction(String operator, String function, String type, String parameterType, OperationDiscoverer operationDiscoverer, ServiceDiscoverer serviceDiscoverer, JsonElement subFunctionElement) {
        super(operator, function, type, null, parameterType, operationDiscoverer, serviceDiscoverer);
        //parsing subfunctions
        try {
            if (subFunctionElement.isJsonPrimitive()) {
                subFunctions.add(new SemanticDiscoveryFunction(operator, function, type, ImmutableSet.of(new URI(subFunctionElement.getAsString())), parameterType, operationDiscoverer, serviceDiscoverer));
            }
            if (subFunctionElement.isJsonArray()) {
                JsonArray subFunctionArray = subFunctionElement.getAsJsonArray();
                Set<URI> parameters = Sets.newHashSet();
                for (JsonElement subFunctionArrayElement : subFunctionArray) {

                    if (subFunctionArrayElement.isJsonPrimitive()) {
                        parameters.add(new URI(subFunctionArrayElement.getAsString()));
                    }
                    if (subFunctionArrayElement.isJsonObject()) {
                        JsonObject subFunctionObject = subFunctionArrayElement.getAsJsonObject();
                        if (subFunctionObject.has("or")) {
                            subFunctions.add(new CompositeSemanticDiscoveryFunction("or", function, type, parameterType, operationDiscoverer, serviceDiscoverer, subFunctionObject.get("or")));
                        } else if (subFunctionObject.has("and")) {
                            subFunctions.add(new CompositeSemanticDiscoveryFunction("and", function, type, parameterType, operationDiscoverer, serviceDiscoverer, subFunctionObject.get("and")));
                        }
                    }
                }
                if (!parameters.isEmpty()) {
                    subFunctions.add(new SemanticDiscoveryFunction(operator, function, type, parameters, parameterType, operationDiscoverer, serviceDiscoverer));
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    public CompositeSemanticDiscoveryFunction(String operator, String function, String type, List<SemanticDiscoveryFunction> subFunctions, OperationDiscoverer operationDiscoverer, ServiceDiscoverer serviceDiscoverer) {
        super(operator, function, type, null, null, operationDiscoverer, serviceDiscoverer);
        this.operator = operator;
        this.subFunctions = subFunctions;
    }

    public Map<URI, MatchResult> invoke() {
        Map<URI, MatchResult> resultMap = Maps.newHashMap();
        for (SemanticDiscoveryFunction subFunction : subFunctions) {
            Map<URI, MatchResult> subResult = subFunction.invoke();
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
        }

        return resultMap;
    }

}
