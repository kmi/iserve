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
public class DiscoveryFunction {
    protected String function;
    protected Set<URI> parameters;
    protected OperationDiscoverer operationDiscoverer;
    protected ServiceDiscoverer serviceDiscoverer;

    DiscoveryFunction(String function, Set<URI> parameters, OperationDiscoverer operationDiscoverer, ServiceDiscoverer serviceDiscoverer) {
        this.function = function;
        this.parameters = parameters;
        this.operationDiscoverer = operationDiscoverer;
        this.serviceDiscoverer = serviceDiscoverer;
    }

    Map<URI, MatchResult> invoke() {
        if (function.equals("service-class")) {
            return serviceDiscoverer.findServicesClassifiedBySome(parameters);
        }
        if (function.equals("operation-class")) {
            return operationDiscoverer.findOperationsClassifiedBySome(parameters);
        }
        if (function.equals("operation-input")) {
            return operationDiscoverer.findOperationsConsumingSome(parameters);
        }
        if (function.equals("operation-output")) {
            return operationDiscoverer.findOperationsProducingSome(parameters);
        }
        if (function.equals("service-input")) {
            return serviceDiscoverer.findServicesConsumingSome(parameters);
        }
        if (function.equals("service-output")) {
            return serviceDiscoverer.findServicesProducingSome(parameters);
        }
        return ImmutableMap.of();
    }

}
