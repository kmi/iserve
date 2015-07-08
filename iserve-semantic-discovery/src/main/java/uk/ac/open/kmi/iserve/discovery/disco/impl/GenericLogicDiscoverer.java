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

package uk.ac.open.kmi.iserve.discovery.disco.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import uk.ac.open.kmi.iserve.discovery.api.*;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.discovery.disco.MatchResultsMerger;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;
import uk.ac.open.kmi.msm4j.vocabulary.MSM;
import uk.ac.open.kmi.msm4j.vocabulary.SAWSDL;

import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * GenericLogicDiscoverer provides discovery for both Operations and Services
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @author <a href="mailto:luca.panziera@open.ac.uk">Luca Panziera</a> (KMi - The Open University)
 * @since 27/09/2013
 */
public class GenericLogicDiscoverer implements OperationDiscoverer, ServiceDiscoverer {

    private final ServiceManager serviceManager;
    private final ConceptMatcher conceptMatcher;

    @Inject
    public GenericLogicDiscoverer(ServiceManager serviceManager, ConceptMatcher conceptMatcher) {
        this.serviceManager = serviceManager;
        this.conceptMatcher = conceptMatcher;
    }

    /**
     * Generic implementation for finding all the Services or Operations that have ALL the given types as inputs or outputs.
     *
     * @param entityType   the MSM URI of the type of entity we are looking for. Only supports Service and Operation.
     * @param relationship the MSM URI of the relationship we are looking for. Only supports hasInput and hasOutput.
     * @param types        the input/output types (modelReferences that is) we are looking for
     * @param matchType
     * @return a Map mapping operation/services URIs to MatchResults.
     */
    private Map<URI, MatchResult> findAll(URI entityType, URI relationship, Set<URI> types, MatchType matchType) {

        // Ensure that we have been given correct parameters
        if (types == null || types.isEmpty() ||
                (!entityType.toASCIIString().equals(MSM.Service.getURI()) && !entityType.toASCIIString().equals(MSM.Operation.getURI())) ||
                (!relationship.toASCIIString().equals(MSM.hasInput.getURI()) && !relationship.toASCIIString().equals(MSM.hasOutput.getURI()) && !relationship.toASCIIString().equals(SAWSDL.modelReference.getURI()))) {

            return ImmutableMap.of();
        }

        // Expand the input types to get all that match enough to be consumed
        // The structure is: <OriginalType, MatchingType, MatchResult>
        Table<URI, URI, MatchResult> expandedTypes;
        if (matchType.equals(LogicConceptMatchType.Subsume)) {
            expandedTypes = HashBasedTable.create();
            for (URI type : types) {
                expandedTypes.putAll(this.conceptMatcher.listMatchesAtMostOfType(ImmutableSet.of(type), LogicConceptMatchType.Subsume));
                expandedTypes.putAll(this.conceptMatcher.listMatchesOfType(ImmutableSet.of(type), LogicConceptMatchType.Exact));
            }
        } else if (matchType.equals(LogicConceptMatchType.Plugin)) {
            expandedTypes = this.conceptMatcher.listMatchesAtLeastOfType(types, LogicConceptMatchType.Plugin);
        } else {
            expandedTypes = HashBasedTable.create();
            for (URI type : types) {
                expandedTypes.putAll(this.conceptMatcher.listMatchesOfType(ImmutableSet.of(type), LogicConceptMatchType.Exact));
            }
        }

        // Track all the results in a multimap to push the details up the stack
        ListMultimap<URI, MatchResult> result = ArrayListMultimap.create();

        // Do the intersection of those operations that can consume each of the inputs separately
        boolean firstTime = true;
        Map<URI, MatchResult> intermediateMatches;
        Map<URI, Map<URI, MatchResult>> rowMap = expandedTypes.rowMap();
        // For each original type
        for (URI inputType : rowMap.keySet()) {
            // obtain those entities that match any of the expanded matching types
            intermediateMatches = findSome(entityType, relationship, rowMap.get(inputType).keySet(), matchType);
            if (firstTime) {
                // Add all entries
                firstTime = false;
                for (Map.Entry<URI, MatchResult> entry : intermediateMatches.entrySet()) {
                    result.put(entry.getKey(), entry.getValue());
                }
            } else {
                // Put all the values from the intersection
                Set<URI> intersection = Sets.intersection(result.keySet(), intermediateMatches.keySet());
                for (URI opUri : intersection) {
                    result.put(opUri, intermediateMatches.get(opUri));
                }

                // Drop all the values from the difference
                // Use an immutable copy since the views will be changed
                Set<URI> difference = Sets.difference(result.keySet(), intermediateMatches.keySet()).immutableCopy();
                for (URI opUri : difference) {
                    result.removeAll(opUri);
                }
            }
        }

        // Merge the results into a single map using Union
        return Maps.transformValues(result.asMap(), MatchResultsMerger.INTERSECTION);

    }

    /**
     * Generic implementation for finding all the Services or Operations that have SOME of the given types as inputs or outputs.
     *
     * @param entityType   the MSM URI of the type of entity we are looking for. Only supports Service and Operation.
     * @param relationship the MSM URI of the relationship we are looking for. Only supports hasInput and hasOutput.
     * @param types        the input/output types (modelReferences that is) we are looking for
     * @param matchType
     * @return a Map mapping operation/services URIs to MatchResults.
     */
    private Map<URI, MatchResult> findSome(URI entityType, URI relationship, Set<URI> types, MatchType matchType) {

        // Ensure that we have been given correct parameters
        if (types == null || types.isEmpty() ||
                (!entityType.toASCIIString().equals(MSM.Service.getURI()) && !entityType.toASCIIString().equals(MSM.Operation.getURI())) ||
                (!relationship.toASCIIString().equals(MSM.hasInput.getURI()) && !relationship.toASCIIString().equals(MSM.hasOutput.getURI()) && !relationship.toASCIIString().equals(SAWSDL.modelReference.getURI()))) {

            return ImmutableMap.of();
        }

        // Expand the input types to get all that match enough to be consumed
        // The structure is: <OriginalType, MatchingType, MatchResult>
        Table<URI, URI, MatchResult> expandedTypes;
        if (matchType.equals(LogicConceptMatchType.Subsume)) {
            expandedTypes = HashBasedTable.create();
            for (URI type : types) {
                expandedTypes.putAll(this.conceptMatcher.listMatchesAtMostOfType(ImmutableSet.of(type), LogicConceptMatchType.Subsume));
                expandedTypes.putAll(this.conceptMatcher.listMatchesOfType(ImmutableSet.of(type), LogicConceptMatchType.Exact));
            }
        } else if (matchType.equals(LogicConceptMatchType.Plugin)) {
            expandedTypes = this.conceptMatcher.listMatchesAtLeastOfType(types, LogicConceptMatchType.Plugin);
        } else {
            expandedTypes = HashBasedTable.create();
            for (URI type : types) {
                expandedTypes.putAll(this.conceptMatcher.listMatchesOfType(ImmutableSet.of(type), LogicConceptMatchType.Exact));
            }
        }

        // Track all the results in a multimap to push the details up the stack
        Multimap<URI, MatchResult> result = ArrayListMultimap.create();

        // Find all the entities with modelReferences to the expanded types
        // The column view is the one with all the possible matches since a class will always match itself
        Map<URI, Map<URI, MatchResult>> columnMap = expandedTypes.columnMap();
        for (URI type : columnMap.keySet()) {
            Set<URI> entities = ImmutableSet.of();
            if (relationship.toASCIIString().equals(SAWSDL.modelReference.getURI())) {
                entities = listEntitesWithModelReference(entityType, type);
            } else if (relationship.toASCIIString().equals(MSM.hasInput.getURI()) || relationship.toASCIIString().equals(MSM.hasOutput.getURI())) {
                entities = listEntitiesWithType(entityType, relationship, type);
            }

            for (URI entity : entities) {
                result.putAll(entity, columnMap.get(type).values());
            }
        }

        // Merge the results into a single map using Union
        return Maps.transformValues(result.asMap(), MatchResultsMerger.UNION);

    }

    /**
     * Generic implementation for finding all the Services or Operations that have one specific model reference.
     *
     * @param entityType     the MSM URI of the type of entity we are looking for. Only supports Service and Operation.
     * @param modelReference the model reference URI we are looking for.
     * @returns a Set of URIs of matching services/operations. Note that this method makes no use of reasoning and therefore
     * the match will always be exact in this case.
     */

    private Set<URI> listEntitesWithModelReference(URI entityType, URI modelReference) {
        Set<URI> entities = ImmutableSet.of();
        // Deal with services
        if (entityType.toASCIIString().equals(MSM.Service.getURI())) {
            entities = this.serviceManager.listServicesWithModelReference(modelReference);
        } else if (entityType.toASCIIString().equals(MSM.Operation.getURI())) {
            entities = this.serviceManager.listOperationsWithModelReference(modelReference);
        }
        return entities;
    }

    /**
     * Generic implementation for finding all the Services or Operations that have one specific type as input or output.
     *
     * @param entityType   the MSM URI of the type of entity we are looking for. Only supports Service and Operation.
     * @param relationship the MSM URI of the relationship we are looking for. Only supports hasInput and hasOutput.
     * @param type         the input/output type (modelReference that is) we are looking for.
     * @returns a Set of URIs of matching services/operations. Note that this method makes no use of reasoning and therefore
     * the match will always be exact in this case.
     */
    private Set<URI> listEntitiesWithType(URI entityType, URI relationship, URI type) {
        Set<URI> entities = ImmutableSet.of();
        // Deal with services
        if (entityType.toASCIIString().equals(MSM.Service.getURI())) {
            // Get the adequate type
            if (relationship.toASCIIString().equals(MSM.hasInput.getURI())) {
                entities = this.serviceManager.listServicesWithInputType(type);

            } else if (relationship.toASCIIString().equals(MSM.hasOutput.getURI())) {
                entities = this.serviceManager.listServicesWithOutputType(type);
            }
            // Deal with operations
        } else if (entityType.toASCIIString().equals(MSM.Operation.getURI())) {

            // Get the adequate type
            if (relationship.toASCIIString().equals(MSM.hasInput.getURI())) {
                entities = this.serviceManager.listOperationsWithInputType(type);

            } else if (relationship.toASCIIString().equals(MSM.hasOutput.getURI())) {
                entities = this.serviceManager.listOperationsWithOutputType(type);
            }
        }
        return entities;
    }


    /**
     * Discover registered operations that consume all the types of input provided. That is, all those that have as input
     * the types provided. All the input types should be matched to different inputs.
     *
     * @param inputTypes the types of input to be consumed
     * @return a Set containing all the matching operations. If there are no solutions, the Set should be empty, not null.
     */
    @Override
    public Map<URI, MatchResult> findOperationsConsumingAll(Set<URI> inputTypes) {
        return findOperationsConsumingAll(inputTypes, LogicConceptMatchType.Plugin);
    }

    @Override
    public Map<URI, MatchResult> findOperationsConsumingAll(Set<URI> inputTypes, MatchType matchType) {
        return findAll(URI.create(MSM.Operation.getURI()), URI.create(MSM.hasInput.getURI()), inputTypes, matchType);
    }

    /**
     * Discover registered operations that consume some (i.e., at least one) of the types of input provided. That is, all
     * those that have as input the types provided.
     *
     * @param inputTypes the types of input to be consumed
     * @return a Set containing all the matching operations. If there are no solutions, the Set should be empty, not null.
     */
    @Override
    public Map<URI, MatchResult> findOperationsConsumingSome(Set<URI> inputTypes) {
        return findServicesClassifiedBySome(inputTypes, LogicConceptMatchType.Plugin);
    }

    @Override
    public Map<URI, MatchResult> findOperationsConsumingSome(Set<URI> inputTypes, MatchType matchType) {
        return findSome(URI.create(MSM.Operation.getURI()), URI.create(MSM.hasInput.getURI()), inputTypes, matchType);
    }

    /**
     * Discover registered operations that produce all of the types of output provided. That is, all those that can be
     * matched to the output types provided. Note, each output should be matched to a different type.
     *
     * @param outputTypes the types of output to be produced
     * @return a Set containing all the matching operations. If there are no solutions, the Set should be empty, not null.
     */
    @Override
    public Map<URI, MatchResult> findOperationsProducingAll(Set<URI> outputTypes) {
        return findOperationsProducingAll(outputTypes, LogicConceptMatchType.Plugin);
    }

    @Override
    public Map<URI, MatchResult> findOperationsProducingAll(Set<URI> outputTypes, MatchType matchType) {
        return findAll(URI.create(MSM.Operation.getURI()), URI.create(MSM.hasOutput.getURI()), outputTypes, matchType);
    }

    /**
     * Discover registered operations that produce some (i.e., at least one) of the types of output provided. That is, all
     * those for which at least one output can be matched to the output types provided.
     *
     * @param outputTypes the types of output to be produced
     * @return a Set containing all the matching operations. If there are no solutions, the Set should be empty, not null.
     */
    @Override
    public Map<URI, MatchResult> findOperationsProducingSome(Set<URI> outputTypes) {
        return findServicesProducingSome(outputTypes, LogicConceptMatchType.Plugin);
    }

    @Override
    public Map<URI, MatchResult> findOperationsProducingSome(Set<URI> outputTypes, MatchType matchType) {
        return findSome(URI.create(MSM.Operation.getURI()), URI.create(MSM.hasOutput.getURI()), outputTypes, matchType);
    }

    /**
     * Discover the operations that are classified by all the types given. That is, all those that have some level of
     * matching with respect to all operations provided in the model references.
     *
     * @param modelReferences the classifications to match against
     * @return a Set containing all the matching operations. If there are no solutions, the Set should be empty, not null.
     */
    @Override
    public Map<URI, MatchResult> findOperationsClassifiedByAll(Set<URI> modelReferences) {
        return findOperationsClassifiedByAll(modelReferences, LogicConceptMatchType.Subsume);
    }

    @Override
    public Map<URI, MatchResult> findOperationsClassifiedByAll(Set<URI> modelReferences, MatchType matchType) {
        return findAll(URI.create(MSM.Operation.getURI()), URI.create(SAWSDL.modelReference.getURI()), modelReferences, matchType);
    }

    /**
     * Discover the operations that are classified by some (i.e., at least one) of the types given. That is, all those
     * that have some level of matching with respect to at least one entity provided in the model references.
     *
     * @param modelReferences the classifications to match against
     * @return a Set containing all the matching operations. If there are no solutions, the Set should be empty, not null.
     */
    @Override
    public Map<URI, MatchResult> findOperationsClassifiedBySome(Set<URI> modelReferences) {
        return findServicesClassifiedBySome(modelReferences, LogicConceptMatchType.Subsume);
    }

    @Override
    public Map<URI, MatchResult> findOperationsClassifiedBySome(Set<URI> modelReferences, MatchType matchType) {
        return findSome(URI.create(MSM.Operation.getURI()), URI.create(SAWSDL.modelReference.getURI()), modelReferences, matchType);
    }

    /**
     * Discover registered operations that can be invoked based on the types of input provided. That is, all those that
     * do not have any mandatory input that cannot be fulfilled with the provided ones.
     *
     * @param inputTypes the types of inputs provided
     * @return a Set containing all the invocable operations. If there are no solutions, the Set should be empty, not null.
     */
    @Override
    public Map<URI, MatchResult> findOperationsInvocableWith(Set<URI> inputTypes) {
        return findOperationsInvocableWith(inputTypes, LogicConceptMatchType.Plugin);
    }

    @Override
    public Map<URI, MatchResult> findOperationsInvocableWith(Set<URI> inputTypes, MatchType matchType) {
        return ImmutableMap.of();  // TODO: implement
    }

    /**
     * Discover registered services that consume all the types of input provided. That is, all those that have as input
     * the types provided. All the input types should be matched to different inputs.
     *
     * @param inputTypes the types of input to be consumed
     * @return a Set containing all the matching services. If there are no solutions, the Set should be empty, not null.
     */
    @Override
    public Map<URI, MatchResult> findServicesConsumingAll(Set<URI> inputTypes) {
        return findServicesConsumingAll(inputTypes, LogicConceptMatchType.Plugin);
    }

    @Override
    public Map<URI, MatchResult> findServicesConsumingAll(Set<URI> inputTypes, MatchType matchType) {
        return findAll(URI.create(MSM.Service.getURI()), URI.create(MSM.hasInput.getURI()), inputTypes, matchType);
    }

    /**
     * Discover registered services that consume some (i.e., at least one) of the types of input provided. That is, all
     * those that have as input the types provided.
     *
     * @param inputTypes the types of input to be consumed
     * @return a Set containing all the matching services. If there are no solutions, the Set should be empty, not null.
     */
    @Override
    public Map<URI, MatchResult> findServicesConsumingSome(Set<URI> inputTypes) {
        return findServicesConsumingSome(inputTypes, LogicConceptMatchType.Plugin);
    }

    @Override
    public Map<URI, MatchResult> findServicesConsumingSome(Set<URI> inputTypes, MatchType matchType) {
        return findSome(URI.create(MSM.Service.getURI()), URI.create(MSM.hasInput.getURI()), inputTypes, matchType);
    }

    /**
     * Discover registered services that produce all of the types of output provided. That is, all those that can be
     * matched to the output types provided. Note, each output should be matched to a different type.
     *
     * @param outputTypes the types of output to be produced
     * @return a Set containing all the matching services. If there are no solutions, the Set should be empty, not null.
     */
    @Override
    public Map<URI, MatchResult> findServicesProducingAll(Set<URI> outputTypes) {
        return findServicesProducingAll(outputTypes, LogicConceptMatchType.Plugin);
    }

    @Override
    public Map<URI, MatchResult> findServicesProducingAll(Set<URI> outputTypes, MatchType matchType) {
        return findAll(URI.create(MSM.Service.getURI()), URI.create(MSM.hasOutput.getURI()), outputTypes, matchType);
    }

    /**
     * Discover registered services that produce some (i.e., at least one) of the types of output provided. That is, all
     * those for which at least one output can be matched to the output types provided.
     *
     * @param outputTypes the types of output to be produced
     * @return a Set containing all the matching services. If there are no solutions, the Set should be empty, not null.
     */
    @Override
    public Map<URI, MatchResult> findServicesProducingSome(Set<URI> outputTypes) {
        return findServicesProducingSome(outputTypes, LogicConceptMatchType.Plugin);
    }

    @Override
    public Map<URI, MatchResult> findServicesProducingSome(Set<URI> outputTypes, MatchType matchType) {
        return findSome(URI.create(MSM.Service.getURI()), URI.create(MSM.hasOutput.getURI()), outputTypes, matchType);
    }

    /**
     * Discover the services that are classified by all the types given. That is, all those that have some level of
     * matching with respect to all services provided in the model references.
     *
     * @param modelReferences the classifications to match against
     * @return a Set containing all the matching services. If there are no solutions, the Set should be empty, not null.
     */
    @Override
    public Map<URI, MatchResult> findServicesClassifiedByAll(Set<URI> modelReferences) {
        return findServicesClassifiedByAll(modelReferences, LogicConceptMatchType.Subsume);
    }

    @Override
    public Map<URI, MatchResult> findServicesClassifiedByAll(Set<URI> modelReferences, MatchType matchType) {
        return findAll(URI.create(MSM.Service.getURI()), URI.create(SAWSDL.modelReference.getURI()), modelReferences, matchType);
    }

    /**
     * Discover the services that are classified by some (i.e., at least one) of the types given. That is, all those
     * that have some level of matching with respect to at least one entity provided in the model references.
     *
     * @param modelReferences the classifications to match against
     * @return a Set containing all the matching services. If there are no solutions, the Set should be empty, not null.
     */
    @Override
    public Map<URI, MatchResult> findServicesClassifiedBySome(Set<URI> modelReferences) {
        return findServicesClassifiedBySome(modelReferences, LogicConceptMatchType.Plugin);
    }

    @Override
    public Map<URI, MatchResult> findServicesClassifiedBySome(Set<URI> modelReferences, MatchType matchType) {
        return findSome(URI.create(MSM.Service.getURI()), URI.create(SAWSDL.modelReference.getURI()), modelReferences, matchType);
    }

    /**
     * Discover registered services that can be invoked based on the types of input provided. That is, all those that
     * do not have any mandatory input that cannot be fulfilled with the provided ones.
     *
     * @param inputTypes the types of inputs provided
     * @return a Set containing all the invocable services. If there are no solutions, the Set should be empty, not null.
     */
    @Override
    public Map<URI, MatchResult> findServicesInvocableWith(Set<URI> inputTypes) {
        return findServicesInvocableWith(inputTypes, LogicConceptMatchType.Plugin);
    }

    @Override
    public Map<URI, MatchResult> findServicesInvocableWith(Set<URI> inputTypes, MatchType matchType) {
        return ImmutableMap.of();  // TODO: implement
    }
}
