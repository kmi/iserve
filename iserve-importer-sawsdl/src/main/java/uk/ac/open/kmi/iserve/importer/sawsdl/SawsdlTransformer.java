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

package uk.ac.open.kmi.iserve.importer.sawsdl;

import com.ebmwebsourcing.easybox.api.*;
import com.ebmwebsourcing.easysawsdl10.api.SawsdlHelper;
import com.ebmwebsourcing.easyschema10.api.SchemaXmlObject;
import com.ebmwebsourcing.easyschema10.api.element.Element;
import com.ebmwebsourcing.easyschema10.api.type.Type;
import com.ebmwebsourcing.easywsdl11.api.element.*;
import com.ebmwebsourcing.easywsdl11.api.type.TBindingOperationMessage;
import com.ebmwebsourcing.easywsdl11.api.type.TDocumented;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import uk.ac.open.kmi.iserve.commons.io.URIUtil;
import uk.ac.open.kmi.iserve.commons.model.AnnotableResource;
import uk.ac.open.kmi.iserve.commons.model.MessageContent;
import uk.ac.open.kmi.iserve.commons.model.MessagePart;
import uk.ac.open.kmi.iserve.commons.model.Resource;
import uk.ac.open.kmi.iserve.sal.exception.ImporterException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * SawsdlTransformer
 * New transformer based on EasyWSDL
 * <p/>
 * <p/>
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 03/07/2013
 * Time: 13:42
 */
public class SawsdlTransformer {

    private static final Logger log = LoggerFactory.getLogger(SawsdlTransformer.class);

    private final XmlContextFactory xmlContextFactory;
    private final XmlContext xmlContext;
    private final XmlObjectReader reader;


    public SawsdlTransformer() throws ImporterException {

        // create factory: can be static
        xmlContextFactory = new XmlContextFactory();

        // create context: can be static
        xmlContext = xmlContextFactory.newContext();

        // create generic reader: cannot be static!!! not thread safe!!!
        reader = xmlContext.createReader();

        // Set the parsing properties
//        System.setProperty("javax.xml.xpath.XPathFactory",
//                "net.sf.saxon.xpath.XPathFactoryImpl");
//
//        System.setProperty("javax.xml.transform.TransformerFactory",
//                "net.sf.saxon.TransformerFactoryImpl");
//
//        System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
//                "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
//
//        System.setProperty("javax.xml.parsers.SAXParserFactory",
//                "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");

        log.debug(this.getJaxpImplementationInfo("DocumentBuilderFactory", DocumentBuilderFactory.newInstance().getClass()));
        log.debug(this.getJaxpImplementationInfo("XPathFactory", XPathFactory.newInstance().getClass()));
        log.debug(this.getJaxpImplementationInfo("TransformerFactory", TransformerFactory.newInstance().getClass()));
        log.debug(this.getJaxpImplementationInfo("SAXParserFactory", SAXParserFactory.newInstance().getClass()));

    }

    private String getJaxpImplementationInfo(String componentName, Class componentClass) {
        CodeSource source = componentClass.getProtectionDomain().getCodeSource();
        return MessageFormat.format(
                "{0} implementation: {1} loaded from: {2}",
                componentName,
                componentClass.getCanonicalName(),
                source == null ? "Java Runtime" : source.getLocation());
    }

    public List<uk.ac.open.kmi.iserve.commons.model.Service> transform(InputStream originalDescription, String baseUri) throws ImporterException {

        List<uk.ac.open.kmi.iserve.commons.model.Service> msmServices = new ArrayList<uk.ac.open.kmi.iserve.commons.model.Service>();
        if (originalDescription == null)
            return msmServices;

        Definitions definitions;
        try {
            InputSource is = new InputSource(originalDescription);
            definitions = reader.readDocument(originalDescription, Definitions.class);
            if (definitions == null)
                return msmServices;

            uk.ac.open.kmi.iserve.commons.model.Service msmSvc;
            Service[] wsdlServices = definitions.getServices();
            for (Service wsdlSvc : wsdlServices) {
                msmSvc = transform(wsdlSvc);
                if (msmSvc != null)
                    msmServices.add(msmSvc);
            }

            return msmServices;
        } catch (XmlObjectReadException e) {
            log.error("Problems reading XML Object exception while parsing service", e);
            throw new ImporterException("Problems reading XML Object exception while parsing service", e);
        }

    }

    private uk.ac.open.kmi.iserve.commons.model.Service transform(Service wsdlSvc) {

        uk.ac.open.kmi.iserve.commons.model.Service msmSvc = null;
        if (wsdlSvc == null)
            return msmSvc;

        QName qname = wsdlSvc.inferQName();
        try {
            // Use WSDL 2.0 naming http://www.w3.org/TR/wsdl20/#wsdl-iri-references
            StringBuilder builder = new StringBuilder().
                    append(qname.getNamespaceURI()).append("#").
                    append("wsdl.service").append("(").append(qname.getLocalPart()).append(")");
            URI svcUri = new URI(builder.toString());
            msmSvc = new uk.ac.open.kmi.iserve.commons.model.Service(svcUri);
            msmSvc.setSource(svcUri);
            msmSvc.setWsdlGrounding(svcUri);
            msmSvc.setLabel(qname.getLocalPart());

            // Add documentation
            addComment(wsdlSvc, msmSvc);

            addModelReferences(wsdlSvc, msmSvc);

            // Process Operations
            URI baseUri;
            uk.ac.open.kmi.iserve.commons.model.Operation msmOp;
            Port[] ports = wsdlSvc.getPorts();
            for (Port port : ports) {
                if (port.hasBinding()) {
                    BindingOperation[] operations = port.findBinding().getOperations();
                    for (BindingOperation operation : operations) {
                        msmOp = transform(operation, URIUtil.getNameSpace(svcUri), port.getName());
                        msmSvc.addOperation(msmOp);
                    }

                }
            }


        } catch (URISyntaxException e) {
            log.error("Syntax exception while generating service URI", e);
        }

        return msmSvc;
    }

    private uk.ac.open.kmi.iserve.commons.model.Operation transform(BindingOperation wsdlOp, String namespace, String portName) {

        uk.ac.open.kmi.iserve.commons.model.Operation msmOp = null;
        if (wsdlOp == null)
            return msmOp;

        StringBuilder builder = new StringBuilder(namespace).append("#").
                append("wsdl.interfaceOperation").
                append("(").append(portName).append("/").append(wsdlOp.getName()).append(")");

        URI opUri = null;
        try {
            opUri = new URI(builder.toString());
            msmOp = new uk.ac.open.kmi.iserve.commons.model.Operation(opUri);
            msmOp.setSource(opUri);
            msmOp.setWsdlGrounding(opUri);
            msmOp.setLabel(wsdlOp.getName());

            // Add documentation
            addComment(wsdlOp, msmOp);

            // Add model references
            addModelReferences(wsdlOp, msmOp);

            // Process Inputs, Outputs and Faults
            BindingOperationInput input = wsdlOp.getInput();
            MessageContent mcIn = transform(input, namespace, portName, wsdlOp.getName());
            msmOp.addInput(mcIn);
            addModelReferences(input, mcIn);
//            addSchemaMappings(input, mcIn);

            BindingOperationOutput output = wsdlOp.getOutput();
            MessageContent mcOut = transform(output, namespace, portName, wsdlOp.getName());
            msmOp.addOutput(mcOut);
            addModelReferences(output, mcOut);
//            addSchemaMappings(output, mcOut);

            // TODO: Process faults

        } catch (URISyntaxException e) {
            log.error("Syntax exception while generating operation URI", e);
        }


        return msmOp;
    }

    private MessageContent transform(TBindingOperationMessage message, String namespace, String portName, String opName) {

        MessageContent mc = null;
        if (message == null)
            return mc;

        String direction = (message instanceof BindingOperationInput) ? "In" : "Out";

        StringBuilder builder = new StringBuilder(namespace).append("#").
                append("wsdl.interfaceMessageReference").
                append("(").append(portName).append("/").append(opName).append("/").append(direction).append(")");

        URI mcUri = null;
        try {
            mcUri = new URI(builder.toString());
            mc = new MessageContent(mcUri);
            mc.setSource(mcUri);
            mc.setWsdlGrounding(mcUri);

            mc.setLabel(message.getName());
            addComment(message, mc);

            // Process parts
//            List<Part> parts = message.getParts();
//            for (Part part : parts) {
//                mc.addMandatoryPart(transform(part));
//            }
        } catch (URISyntaxException e) {
            log.error("Syntax exception while generating message URI", e);
        }

        return mc;
    }

    private void addComment(TDocumented element, Resource resource) {
        Documentation doc = element.getDocumentation();
        if (doc != null && doc.getContent() != null && !doc.getContent().isEmpty())
            resource.setComment(doc.getContent());
    }

    private MessagePart transform(Part part) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private void addModelReferences(XmlObject object, AnnotableResource annotableResource) {

        URI[] modelRefs = SawsdlHelper.getModelReference(object);
        for (URI modelRef : modelRefs) {
            annotableResource.addModelReference(new Resource(modelRef));
        }

    }

    private void addSchemaMappings(SchemaXmlObject object, MessagePart message) {

        // Handle liftingSchemaMappings (only on elements or types)
        if (object == null)
            return;

        URI[] liftList = null;
        URI[] lowerList = null;

        if (object instanceof Type) {
            liftList = SawsdlHelper.getLiftingSchemaMapping((Type) object);
            lowerList = SawsdlHelper.getLoweringSchemaMapping((Type) object);
        } else if (object instanceof Element) {
            liftList = SawsdlHelper.getLiftingSchemaMapping((Element) object);
            lowerList = SawsdlHelper.getLoweringSchemaMapping((Element) object);
        }

        for (URI liftUri : liftList) {
            message.addLiftingSchemaMapping(liftUri);
        }

        for (URI lowerUri : lowerList) {
            message.addLiftingSchemaMapping(lowerUri);
        }

    }
}
