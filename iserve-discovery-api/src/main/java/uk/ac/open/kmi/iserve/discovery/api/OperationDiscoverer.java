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

package uk.ac.open.kmi.iserve.discovery.api;

import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * OperationDiscoverer
 * TODO: Provide Description
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 27/09/2013
 */
public interface OperationDiscoverer {

    /**
     * Discover registered operations that consume all the types of input provided. That is, all those that have as input
     * the types provided. All the input types should be matched to different inputs.
     *
     * @param inputTypes the types of input to be consumed
     * @return a Set containing all the matching operations. If there are no solutions, the Set should be empty, not null.
     */
    Map<URI, MatchResult> findOperationsConsumingAll(Set<URI> inputTypes);

    /**
     * Discover registered operations that consume some (i.e., at least one) of the types of input provided. That is, all
     * those that have as input the types provided.
     *
     * @param inputTypes the types of input to be consumed
     * @return a Set containing all the matching operations. If there are no solutions, the Set should be empty, not null.
     */
    Map<URI, MatchResult> findOperationsConsumingSome(Set<URI> inputTypes);

    /**
     * Discover registered operations that produce all of the types of output provided. That is, all those that can be
     * matched to the output types provided. Note, each output should be matched to a different type.
     *
     * @param outputTypes the types of output to be produced
     * @return a Set containing all the matching operations. If there are no solutions, the Set should be empty, not null.
     */
    Map<URI, MatchResult> findOperationsProducingAll(Set<URI> outputTypes);

    /**
     * Discover registered operations that produce some (i.e., at least one) of the types of output provided. That is, all
     * those for which at least one output can be matched to the output types provided.
     *
     * @param outputTypes the types of output to be produced
     * @return a Set containing all the matching operations. If there are no solutions, the Set should be empty, not null.
     */
    Map<URI, MatchResult> findOperationsProducingSome(Set<URI> outputTypes);

    /**
     * Discover the operations that are classified by all the types given. That is, all those that have some level of
     * matching with respect to all operations provided in the model references.
     *
     * @param modelReferences the classifications to match against
     * @return a Set containing all the matching operations. If there are no solutions, the Set should be empty, not null.
     */
    Map<URI, MatchResult> findOperationsClassifiedByAll(Set<URI> modelReferences);

    /**
     * Discover the operations that are classified by some (i.e., at least one) of the types given. That is, all those
     * that have some level of matching with respect to at least one entity provided in the model references.
     *
     * @param modelReferences the classifications to match against
     * @return a Set containing all the matching operations. If there are no solutions, the Set should be empty, not null.
     */
    Map<URI, MatchResult> findOperationsClassifiedBySome(Set<URI> modelReferences);

    /**
     * Discover registered operations that can be invoked based on the types of input provided. That is, all those that
     * do not have any mandatory input that cannot be fulfilled with the provided ones.
     *
     * @param inputTypes the types of inputs provided
     * @return a Set containing all the invocable operations. If there are no solutions, the Set should be empty, not null.
     */
    Map<URI, MatchResult> findOperationsInvocableWith(Set<URI> inputTypes);

}
