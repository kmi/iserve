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

import com.google.common.collect.ImmutableMap;

import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * Created by Luca Panziera on 20/05/2014.
 */
public class SemanticDiscoveryFunction extends DiscoveryFunction {

    protected String operator;
    protected String function;
    protected String type;
    protected String parameterType;
    protected Set<URI> parameters;
    protected OperationDiscoverer operationDiscoverer;
    protected ServiceDiscoverer serviceDiscoverer;

    public SemanticDiscoveryFunction(String operator, String function, String type, Set<URI> parameters, String parameterType, OperationDiscoverer operationDiscoverer, ServiceDiscoverer serviceDiscoverer) {
        this.operator = operator;
        this.function = function;
        this.parameters = parameters;
        this.operationDiscoverer = operationDiscoverer;
        this.serviceDiscoverer = serviceDiscoverer;
        this.type = type;
        this.parameterType = parameterType;
    }

    public SemanticDiscoveryFunction(String function, String type, Set<URI> parameters, String parameterType, OperationDiscoverer operationDiscoverer, ServiceDiscoverer serviceDiscoverer) {
        this.operator = "or";
        this.function = function;
        this.parameters = parameters;
        this.operationDiscoverer = operationDiscoverer;
        this.serviceDiscoverer = serviceDiscoverer;
        this.type = type;
        this.parameterType = parameterType;
    }

    public Map<URI, MatchResult> compute() {
        if (operator.equals("or")) {
            if (function.equals("func-rdfs") && type.equals("svc") && parameterType.equals("classes")) {
                return serviceDiscoverer.findServicesClassifiedBySome(parameters);
            }
            if (function.equals("func-rdfs") && type.equals("op") && parameterType.equals("classes")) {
                return operationDiscoverer.findOperationsClassifiedBySome(parameters);
            }
            if (function.equals("io-rdfs") && type.equals("op") && parameterType.equals("input")) {
                return operationDiscoverer.findOperationsConsumingSome(parameters);
            }
            if (function.equals("io-rdfs") && type.equals("op") && parameterType.equals("output")) {
                return operationDiscoverer.findOperationsProducingSome(parameters);
            }
            if (function.equals("io-rdfs") && type.equals("svc") && parameterType.equals("input")) {
                return serviceDiscoverer.findServicesConsumingSome(parameters);
            }
            if (function.equals("io-rdfs") && type.equals("svc") && parameterType.equals("output")) {
                return serviceDiscoverer.findServicesProducingSome(parameters);
            }
        }
        if (operator.equals("and")) {
            if (function.equals("func-rdfs") && type.equals("svc") && parameterType.equals("classes")) {
                return serviceDiscoverer.findServicesClassifiedByAll(parameters);
            }
            if (function.equals("func-rdfs") && type.equals("op") && parameterType.equals("classes")) {
                return operationDiscoverer.findOperationsClassifiedByAll(parameters);
            }
            if (function.equals("io-rdfs") && type.equals("op") && parameterType.equals("input")) {
                return operationDiscoverer.findOperationsConsumingAll(parameters);
            }
            if (function.equals("io-rdfs") && type.equals("op") && parameterType.equals("output")) {
                return operationDiscoverer.findOperationsProducingAll(parameters);
            }
            if (function.equals("io-rdfs") && type.equals("svc") && parameterType.equals("input")) {
                return serviceDiscoverer.findServicesConsumingAll(parameters);
            }
            if (function.equals("io-rdfs") && type.equals("svc") && parameterType.equals("output")) {
                return serviceDiscoverer.findServicesProducingAll(parameters);
            }
        }

        return ImmutableMap.of();
    }
}
