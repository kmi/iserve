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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.model.*;
import uk.ac.open.kmi.iserve.commons.model.util.Vocabularies;
import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.commons.vocabulary.MSM_WSDL;
import uk.ac.open.kmi.iserve.commons.vocabulary.SAWSDL;
import uk.ac.open.kmi.iserve.commons.vocabulary.WSMO_LITE;

import java.io.OutputStream;
import java.net.URI;

/**
 * Service Writer Implementation
 * <p/>
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 22/05/2013
 * Time: 02:13
 */
public class ServiceWriterImpl implements ServiceWriter {

    private static final Logger log = LoggerFactory.getLogger(ServiceWriterImpl.class);

    public ServiceWriterImpl() {
    }

    @Override
    public void serialise(Service service, OutputStream out, Syntax syntax) {
        Model model = generateModel(service);
        model.setNsPrefixes(Vocabularies.prefixes);
        try {
            model.write(out, syntax.getName());
        } finally {
            model.close();
        }
    }

    public Model generateModel(Service service) {

        Model model = ModelFactory.createDefaultModel();

        // Exit early if null
        if (service == null)
            return model;

        // Create the service resource
        com.hp.hpl.jena.rdf.model.Resource current = model.createResource(service.getUri().toASCIIString());
        current.addProperty(RDF.type, MSM.Service);

        // Add the base annotations
        addResourceMetadata(model, service);
        // Add the modelReferences
        addModelReferences(model, service);
        // Add the grounding
        addGrounding(model, service);

        // Process Operations
        for (Operation op : service.getOperations()) {
            current.addProperty(MSM.hasOperation,
                    model.createResource(op.getUri().toASCIIString()));
            addOperationToModel(model, op);
        }

        return model;
    }


    private void addOperationToModel(Model model, Operation op) {

        // Exit early if null
        if (op == null)
            return;

        com.hp.hpl.jena.rdf.model.Resource current = model.createResource(op.getUri().toASCIIString());
        current.addProperty(RDF.type, MSM.Operation);
        // Add the base annotations
        addResourceMetadata(model, op);
        // Add the modelReferences
        addModelReferences(model, op);
        // Add the grounding
        addGrounding(model, op);

        // Process inputs
        for (MessageContent input : op.getInputs()) {
            current.addProperty(MSM.hasInput,
                    model.createResource(input.getUri().toASCIIString()));
            addMessageContent(model, input);
        }

        // Process outputs
        for (MessageContent output : op.getOutputs()) {
            current.addProperty(MSM.hasOutput,
                    model.createResource(output.getUri().toASCIIString()));
            addMessageContent(model, output);
        }

        // Process Input Faults
        for (MessageContent fault : op.getInputFaults()) {
            current.addProperty(MSM.hasInputFault,
                    model.createResource(fault.getUri().toASCIIString()));
            addMessageContent(model, fault);
        }

        // Process Output Faults
        for (MessageContent fault : op.getOutputFaults()) {
            current.addProperty(MSM.hasOutputFault,
                    model.createResource(fault.getUri().toASCIIString()));
            addMessageContent(model, fault);
        }

    }

    private void addMessageContent(Model model, MessageContent messageContent) {

        // Exit early if null
        if (messageContent == null)
            return;

        com.hp.hpl.jena.rdf.model.Resource current = model.createResource(messageContent.getUri().toASCIIString());
        current.addProperty(RDF.type, MSM.MessageContent);
        addMessagePart(model, messageContent);

        // Add the grounding
        addGrounding(model, messageContent);

        // Process liftings
        for (URI mapping : messageContent.getLiftingSchemaMappings()) {
            current.addProperty(SAWSDL.liftingSchemaMapping, mapping.toASCIIString());
        }

        // Process lowerings
        for (URI mapping : messageContent.getLoweringSchemaMappings()) {
            current.addProperty(SAWSDL.loweringSchemaMapping, mapping.toASCIIString());
        }
    }

    private void addMessagePart(Model model, MessagePart messagePart) {

        // Exit early if null
        if (messagePart == null)
            return;

        com.hp.hpl.jena.rdf.model.Resource current = model.createResource(messagePart.getUri().toASCIIString());
        current.addProperty(RDF.type, MSM.MessagePart);
        // Add the base annotations
        addResourceMetadata(model, messagePart);
        // Add the modelReferences
        addModelReferences(model, messagePart);

        // Process mandatory parts
        for (MessagePart part : messagePart.getMandatoryParts()) {
            current.addProperty(MSM.hasMandatoryPart,
                    model.createResource(part.getUri().toASCIIString()));
            addMessagePart(model, part);
        }
        // Process optional parts
        for (MessagePart part : messagePart.getOptionalParts()) {
            current.addProperty(MSM.hasOptionalPart,
                    model.createResource(part.getUri().toASCIIString()));
            addMessagePart(model, part);
        }

    }

    private void addModelReferences(Model model, AnnotableResource annotableResource) {
        // Exit early if null
        if (annotableResource == null)
            return;

        com.hp.hpl.jena.rdf.model.Resource current = model.createResource(annotableResource.getUri().toASCIIString());

        // Process general model references
        for (Resource mr : annotableResource.getModelReferences()) {
            com.hp.hpl.jena.rdf.model.Resource refResource = model.createResource(mr.getUri().toASCIIString());
            current.addProperty(SAWSDL.modelReference, refResource);
        }

        // Process NFPs
        for (Resource nfp : annotableResource.getNfps()) {
            com.hp.hpl.jena.rdf.model.Resource refResource = model.createResource(nfp.getUri().toASCIIString());
            current.addProperty(SAWSDL.modelReference, refResource);
            refResource.addProperty(RDF.type, WSMO_LITE.NonfunctionalParameter);
        }

        if (annotableResource instanceof InvocableEntity) {
            InvocableEntity invEnt = (InvocableEntity) annotableResource;
            // Process Conditions
            for (Condition cond : invEnt.getConditions()) {
                com.hp.hpl.jena.rdf.model.Resource refResource = createRdfResource(model, cond);
                current.addProperty(SAWSDL.modelReference, refResource);
                refResource.addProperty(RDF.type, WSMO_LITE.Condition);
                refResource.addLiteral(RDF.value, cond.getTypedValue());
            }

            // Process Effects
            for (Effect effect : invEnt.getEffects()) {
                com.hp.hpl.jena.rdf.model.Resource refResource = createRdfResource(model, effect);
                current.addProperty(SAWSDL.modelReference, refResource);
                refResource.addProperty(RDF.type, WSMO_LITE.Effect);
                refResource.addLiteral(RDF.value, effect.getTypedValue());
            }
        }


    }

    /**
     * Creates an RDF Resource in a given model. Takes care of dealing with nonUri resources
     * and creates a blank node in these cases.
     *
     * @param model    the model
     * @param resource the MSM resource to be mapped to Jena
     * @return a URI resource or a blank node otherwise
     */
    private com.hp.hpl.jena.rdf.model.Resource createRdfResource(Model model, Resource resource) {

        com.hp.hpl.jena.rdf.model.Resource result;
        if (resource.getUri() != null) {
            result = model.createResource(resource.getUri().toASCIIString());
        } else {
            // create blank node
            result = model.createResource();
        }

        return result;
    }

    private void addGrounding(Model model, Resource resource) {
        // Exit early if null
        if (resource == null)
            return;

        // Add WSDL grounding if present
        URI grounding = resource.getWsdlGrounding();
        if (grounding != null) {
            // Create the resource
            com.hp.hpl.jena.rdf.model.Resource current = model.createResource(resource.getUri().toASCIIString());
            com.hp.hpl.jena.rdf.model.Resource gdgRes = model.createResource(grounding.toASCIIString());
            current.addProperty(MSM_WSDL.isGroundedIn, gdgRes);
        }

    }

    private void addResourceMetadata(Model model, Resource basicResource) {
        // Exit early if null
        if (basicResource == null)
            return;

        com.hp.hpl.jena.rdf.model.Resource current = model.createResource(basicResource.getUri().toASCIIString());

        String label = basicResource.getLabel();
        if (label != null) {
            current.addProperty(RDFS.label, label);
        }

        String comment = basicResource.getComment();
        if (comment != null) {
            current.addProperty(RDFS.comment, comment);
        }

        URI creator = basicResource.getCreator();
        if (creator != null) {
            current.addProperty(DC.creator,
                    model.createResource(creator.toASCIIString()));
        }

        URI seeAlso = basicResource.getSeeAlso();
        if (seeAlso != null) {
            current.addProperty(RDFS.seeAlso,
                    model.createResource(seeAlso.toASCIIString()));
        }

        URI source = basicResource.getSource();
        if (source != null) {
            current.addProperty(DC.source,
                    model.createResource(source.toASCIIString()));
        }

    }
}
