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

package uk.ac.open.kmi.iserve.commons.io;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.model.*;
import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.commons.vocabulary.MSM_WSDL;
import uk.ac.open.kmi.iserve.commons.vocabulary.SAWSDL;
import uk.ac.open.kmi.iserve.commons.vocabulary.WSMO_LITE;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * ServiceReaderImpl
 * TODO: Provide Description
 * <p/>
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 01/06/2013
 * Time: 12:23
 */
public class ServiceReaderImpl implements ServiceReader {

    private static final Logger log = LoggerFactory.getLogger(ServiceReaderImpl.class);

    /**
     * Read a stream in RDF and return the corresponding set of services
     * as Java Objects
     *
     * @param in      The input stream of MSM services
     * @param baseUri Base URI to use for parsing
     * @param syntax  used within the stream
     * @return The collection of Services parsed from the stream
     */
    @Override
    public List<Service> parse(InputStream in, String baseUri, Syntax syntax) {

        OntModel model = null;
        List<Service> result = new ArrayList<Service>();
        try {
            // create an empty model
            model = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
            // Parse the stream into a model
            model.read(in, baseUri, syntax.getName());
            result = parseService(model);
        } finally {
            if (model != null)
                model.close();
        }

        return result;
    }

    public ArrayList<Service> parseService(OntModel model) {
        ArrayList<Service> result = new ArrayList<Service>();
        // Get the services
        Service service;
        Individual individual;
        ExtendedIterator<Individual> services = model.listIndividuals(MSM.Service);
        while (services.hasNext()) {
            individual = services.next();
            try {
                service = obtainService(individual);
                if (service != null) {
                    result.add(service);
                }
            } catch (URISyntaxException e) {
                log.error("Incorrect URI while parsing service.", e);
            }

        }

        return result;
    }

    private Service obtainService(Individual individual) throws URISyntaxException {

        if (individual == null)
            return null;

        Service service = new Service(new URI(individual.getURI()));
        setInvocableEntityProperties(individual, service);

        NodeIterator hasOpValues = null;
        try {
            hasOpValues = individual.listPropertyValues(MSM.hasOperation);
            List<Operation> operations = obtainOperations(hasOpValues);
            service.setOperations(operations);
        } finally {
            if (hasOpValues != null)
                hasOpValues.close();
        }

        return service;
    }

    private List<Operation> obtainOperations(NodeIterator hasOpValues) throws URISyntaxException {

        List<Operation> operations = new ArrayList<Operation>();
        if (hasOpValues == null)
            return operations;

        Operation operation;
        Individual individual;
        RDFNode value;

        while (hasOpValues.hasNext()) {
            value = hasOpValues.next();
            if (value.canAs(Individual.class)) {
                individual = value.as(Individual.class);
                operation = new Operation(new URI(individual.getURI()));
                setInvocableEntityProperties(individual, operation);

                // Process Message Contents
                MessageContent mc;
                RDFNode rdfNode;
                rdfNode = individual.getPropertyValue(MSM.hasInput);
                mc = obtainMessageContent(rdfNode);
                if (mc != null)
                    operation.addInput(mc);

                rdfNode = individual.getPropertyValue(MSM.hasOutput);
                mc = obtainMessageContent(rdfNode);
                if (mc != null)
                    operation.addOutput(mc);

                rdfNode = individual.getPropertyValue(MSM.hasInputFault);
                mc = obtainMessageContent(rdfNode);
                if (mc != null)
                    operation.addInputFault(mc);

                rdfNode = individual.getPropertyValue(MSM.hasOutputFault);
                mc = obtainMessageContent(rdfNode);
                if (mc != null)
                    operation.addOutputFault(mc);

                operations.add(operation);
            }
        }
        return operations;
    }

    private MessageContent obtainMessageContent(RDFNode inputNode) throws URISyntaxException {
        MessageContent result = null;
        if (inputNode == null || !inputNode.canAs(Individual.class)) {
            return result;
        }

        Individual individual = inputNode.as(Individual.class);
        result = new MessageContent(new URI(individual.getURI()));
        setAnnotableResourceProperties(individual, result);

        // Process mandatory parts
        List<MessagePart> mps;
        mps = obtainParts(individual, MSM.hasMandatoryPart);
        result.setMandatoryParts(mps);

        // Process optional parts
        mps = obtainParts(individual, MSM.hasOptionalPart);
        result.setOptionalParts(mps);

        // Process Lifting and Lowerings
        List<URI> mappings;
        mappings = obtainMappings(individual, SAWSDL.liftingSchemaMapping);
        result.setLiftingSchemaMappings(mappings);

        mappings = obtainMappings(individual, SAWSDL.loweringSchemaMapping);
        result.setLoweringSchemaMappings(mappings);

        return result;
    }

    private List<URI> obtainMappings(Individual individual, Property mappingProperty) throws URISyntaxException {

        List<URI> result = new ArrayList<URI>();
        if (individual == null || mappingProperty == null)
            return result;

        NodeIterator nodeIterator = null;
        Resource schemaMap;
        RDFNode node;
        try {
            nodeIterator = individual.listPropertyValues(mappingProperty);
            while (nodeIterator.hasNext()) {
                node = nodeIterator.next();
                if (node.isResource()) {
                    schemaMap = node.asResource();
                    result.add(new URI(schemaMap.getURI()));
                }
            }
        } finally {
            if (nodeIterator != null)
                nodeIterator.close();
        }
        return result;
    }

    private List<MessagePart> obtainParts(Individual individual, Property partsProperty) throws URISyntaxException {

        List<MessagePart> result = new ArrayList<MessagePart>();
        if (individual == null)
            return result;

        RDFNode node;
        MessagePart messagePart;
        NodeIterator nodeIterator = null;
        try {
            nodeIterator = individual.listPropertyValues(partsProperty);
            while (nodeIterator.hasNext()) {
                node = nodeIterator.next();
                messagePart = obtainMessagePart(node);
                if (messagePart != null)
                    result.add(messagePart);
            }
        } finally {
            if (nodeIterator != null)
                nodeIterator.close();
        }
        return result;
    }

    private MessagePart obtainMessagePart(RDFNode inputNode) throws URISyntaxException {

        MessagePart result = null;
        if (inputNode == null && !inputNode.canAs(Individual.class)) {
            return result;
        }

        Individual individual = inputNode.as(Individual.class);
        result = new MessagePart(new URI(individual.getURI()));
        setAnnotableResourceProperties(individual, result);

        // Process mandatory parts
        List<MessagePart> mps;
        mps = obtainParts(individual, MSM.hasMandatoryPart);
        result.setMandatoryParts(mps);

        // Process optional parts
        mps = obtainParts(individual, MSM.hasOptionalPart);
        result.setOptionalParts(mps);

        return result;
    }

    private void setInvocableEntityProperties(Individual individual, InvocableEntity invocEntity) throws URISyntaxException {

        if (individual == null || invocEntity == null)
            return;

        setAnnotableResourceProperties(individual, invocEntity);

        List<Condition> conditions = (List<Condition>) obtainAxioms(individual, Condition.class);
        invocEntity.setConditions(conditions);

        List<Effect> effects = (List<Effect>) obtainAxioms(individual, Effect.class);
        invocEntity.setEffects(effects);
    }

    private List<? extends LogicalAxiom> obtainAxioms(Individual individual, Class<? extends LogicalAxiom> axiomClass) throws URISyntaxException {

        List<? extends LogicalAxiom> result;
        Resource resourceType;

        if (axiomClass.equals(Condition.class)) {
            result = new ArrayList<Condition>();
            resourceType = WSMO_LITE.Condition;

        } else {
            result = new ArrayList<Effect>();
            resourceType = WSMO_LITE.Effect;
        }

        RDFNode node;
        NodeIterator nodeIterator = null;
        FilterByRdfType myFilter = new FilterByRdfType();

        // Process the appropriate model references
        try {
            nodeIterator = individual.listPropertyValues(SAWSDL.modelReference);
            myFilter.setRdfType(resourceType);
            nodeIterator.filterKeep(myFilter);
            while (nodeIterator.hasNext()) {
                node = nodeIterator.next();
                Individual axiomIndiv = node.as(Individual.class); // Should be possible given the filter
                if (axiomClass.equals(Condition.class)) {
                    Condition cond = new Condition(new URI(axiomIndiv.getURI()));
                    setResourceProperties(axiomIndiv, cond);
                    cond.setTypedValue(getAxiomBody(axiomIndiv));
                    ((List<Condition>) result).add(cond);

                } else {
                    Effect effect = new Effect(new URI(axiomIndiv.getURI()));
                    setResourceProperties(axiomIndiv, effect);
                    effect.setTypedValue(getAxiomBody(axiomIndiv));
                    ((List<Effect>) result).add(effect);
                }
            }
        } finally {
            if (nodeIterator != null)
                nodeIterator.close();
        }

        return result;
    }

    private String getAxiomBody(Individual axiomIndiv) {

        RDFNode value = axiomIndiv.getPropertyValue(RDF.value);
        if (value != null && value.isLiteral()) {
            return value.asLiteral().getString();
        }

        return null;
    }

    private void setAnnotableResourceProperties(Individual individual, AnnotableResource annotRes) throws URISyntaxException {

        if (individual == null || annotRes == null)
            return;

        setResourceProperties(individual, annotRes);

        FilterByRdfType filter;
        NodeIterator nodeIter = null;
        ExtendedIterator<RDFNode> filteredIter;
        try {
            nodeIter = individual.listPropertyValues(SAWSDL.modelReference);
            filter = new FilterByRdfType();
            // Process ModelReferences that are have no known type
            // Filter the modelRefs first then
            filter.setRdfType(WSMO_LITE.Condition);
            filteredIter = nodeIter.filterDrop(filter);
            filter.setRdfType(WSMO_LITE.Effect);
            filteredIter = filteredIter.filterDrop(filter);
            filter.setRdfType(WSMO_LITE.NonfunctionalParameter);
            filteredIter = filteredIter.filterDrop(filter);
            while (filteredIter.hasNext()) {
                RDFNode node = filteredIter.next();
                if (node.isResource()) {
                    annotRes.addModelReference(
                            new uk.ac.open.kmi.iserve.commons.model.Resource(new URI(node.asResource().getURI())));
                }
            }
        } finally {
            if (nodeIter != null)
                nodeIter.close();
        }

        // Process NFPs
        try {
            nodeIter = individual.listPropertyValues(SAWSDL.modelReference);
            filter.setRdfType(WSMO_LITE.NonfunctionalParameter);
            filteredIter = nodeIter.filterKeep(filter);
            while (filteredIter.hasNext()) {
                RDFNode node = filteredIter.next();
                if (node.isResource()) {
                    annotRes.addNonFunctionalProperty(
                            new NonFunctionalProperty(new URI(node.asResource().getURI())));
                }
            }
        } finally {
            if (nodeIter != null)
                nodeIter.close();
        }

    }

    private void setResourceProperties(Individual individual, uk.ac.open.kmi.iserve.commons.model.Resource result) throws URISyntaxException {
        Resource res;
        result.setComment(individual.getComment(null));
        result.setLabel(individual.getLabel(null));

        res = individual.getSeeAlso();
        if (res != null) {
            result.setSeeAlso(new URI(res.getURI()));
        }

        res = individual.getPropertyResourceValue(DC.source);
        if (res != null) {
            result.setSource(new URI(res.getURI()));
        }

        res = individual.getPropertyResourceValue(DC.creator);
        if (res != null) {
            result.setCreator(new URI(res.getURI()));
        }

        res = individual.getPropertyResourceValue(MSM_WSDL.isGroundedIn);
        if (res != null) {
            result.setWsdlGrounding(new URI(res.getURI()));
        }
    }
}
