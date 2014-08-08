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
import com.google.common.collect.Maps;

import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * Created by Luca Panziera on 21/05/2014.
 */
public class CompositeDiscoveryFunction extends DiscoveryFunction {
    private String operator;
    private DiscoveryFunction prev;
    private DiscoveryFunction next;

    public CompositeDiscoveryFunction(DiscoveryFunction prev, DiscoveryFunction next, String operator) {
        super(null, null, prev.operationDiscoverer, prev.serviceDiscoverer);
        this.prev = prev;
        this.next = next;
        this.operator = operator;
    }

    public CompositeDiscoveryFunction(String function, Set<URI> parameters, OperationDiscoverer operationDiscoverer, ServiceDiscoverer serviceDiscoverer) {
        super(function, parameters, operationDiscoverer, serviceDiscoverer);
    }

    Map<URI, MatchResult> invoke() {
        if (prev.getClass().equals(this.getClass())) {
            if (((CompositeDiscoveryFunction) prev).operator.equals(this.operator)) {
                prev.parameters.addAll(next.parameters);
                return prev.invoke();
            } else {

                if (operator.equals("union")) {
                    Map<URI, MatchResult> prevResult = prev.invoke();
                    Map<URI, MatchResult> nextResult = next.invoke();

                    Map<URI, MatchResult> mergedResult = Maps.newHashMap(prevResult);
                    mergedResult.putAll(nextResult);
                    return mergedResult;
                } else if (operator.equals("intersection")) {
                    Map<URI, MatchResult> prevResult = prev.invoke();
                    Map<URI, MatchResult> nextResult = next.invoke();

                    Map<URI, MatchResult> intersectedResult = Maps.newHashMap(prevResult);

                    intersectedResult.keySet().retainAll(nextResult.keySet());

                    return intersectedResult;
                }

            }
        } else {
            prev.parameters.addAll(next.parameters);
            if (operator.equals("union")) {
                prev.parameters.addAll(next.parameters);
                return prev.invoke();
            } else if (operator.equals("intersection")) {
                if (prev.function.equals("service-class")) {
                    return serviceDiscoverer.findServicesClassifiedByAll(prev.parameters);
                } else if (prev.function.equals("operation-class")) {
                    return operationDiscoverer.findOperationsClassifiedByAll(prev.parameters);
                } else if (prev.function.equals("operation-input")) {
                    return operationDiscoverer.findOperationsConsumingAll(prev.parameters);
                } else if (prev.function.equals("operation-output")) {
                    return operationDiscoverer.findOperationsProducingAll(prev.parameters);
                } else if (prev.function.equals("service-input")) {
                    return serviceDiscoverer.findServicesConsumingAll(prev.parameters);
                } else if (prev.function.equals("service-output")) {
                    return serviceDiscoverer.findServicesProducingAll(prev.parameters);
                }
            }
        }

        return ImmutableMap.of();
    }

}
