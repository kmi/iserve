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

import org.apache.woden.WSDLException;
import org.apache.woden.WSDLFactory;
import org.apache.woden.WSDLReader;
import org.apache.woden.WSDLSource;
import org.apache.woden.schema.Schema;
import org.apache.woden.wsdl20.Description;
import org.apache.woden.wsdl20.enumeration.Direction;
import org.apache.woden.wsdl20.xml.*;
import org.apache.ws.commons.schema.XmlSchema;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import uk.ac.open.kmi.iserve.commons.model.MessageContent;
import uk.ac.open.kmi.iserve.commons.model.MessagePart;
import uk.ac.open.kmi.iserve.commons.model.Operation;
import uk.ac.open.kmi.iserve.commons.model.Service;
import uk.ac.open.kmi.iserve.importer.sawsdl.schema.SchemaMap;
import uk.ac.open.kmi.iserve.importer.sawsdl.schema.SchemaParser;
import uk.ac.open.kmi.iserve.importer.sawsdl.util.ModelReferenceExtractor;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Sawsdl20Transformer {

    private SchemaParser schemaParser;

    private List<SchemaMap> schemaMaps;

    private DocumentBuilderFactory factory;

    private DocumentBuilder builder;

    public Sawsdl20Transformer() throws ParserConfigurationException {
        factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        builder = factory.newDocumentBuilder();
    }

    private void initialise() {
        schemaParser = new SchemaParser();
        schemaMaps = new ArrayList<SchemaMap>();
    }

    public List<uk.ac.open.kmi.iserve.commons.model.Service> transform(InputStream originalDescription) throws WSDLException, SAXException, IOException {

        initialise();
        WSDLFactory wsdlFactory = WSDLFactory.newInstance();
        WSDLReader reader = wsdlFactory.newWSDLReader();
        reader.setFeature(WSDLReader.FEATURE_VALIDATION, false);
        List<uk.ac.open.kmi.iserve.commons.model.Service> result = new ArrayList<uk.ac.open.kmi.iserve.commons.model.Service>();

        Document dom = builder.parse(originalDescription);
        Element domElement = dom.getDocumentElement();

        WSDLSource wsdlSource = reader.createWSDLSource();
        wsdlSource.setSource(domElement);

        Description definition = reader.readWSDL(wsdlSource);
        DescriptionElement descElement = definition.toElement();

        uk.ac.open.kmi.iserve.commons.model.Service svc;
        try {
            processTypes(descElement.getTypesElement());
            ServiceElement[] parsedServices = descElement.getServiceElements();
            for (ServiceElement svcElmnt : parsedServices) {
                svc = processService(svcElmnt);
                if (svc != null)
                    result.add(svc);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return result;
    }

    private void processTypes(TypesElement typesElement) throws URISyntaxException {
        Schema[] schemas = typesElement.getInlinedSchemas();
        if (schemas != null) {
            for (Schema schema : schemas) {
                XmlSchema xmlSchema = schema.getSchemaDefinition();
                schemaMaps.add(schemaParser.parse(xmlSchema));
            }
        }
    }

    private uk.ac.open.kmi.iserve.commons.model.Service processService(ServiceElement serviceElement) throws URISyntaxException {
        if (serviceElement == null) {
            return null;
        }

        for (SchemaMap schemaMap : schemaMaps) {
//			schemaMap.toRdf(tempModel);
            // FIXME
        }

        uk.ac.open.kmi.iserve.commons.model.Service msmService = null;
        InterfaceElement intfElement = serviceElement.getInterfaceElement();
        if (intfElement != null && serviceElement != null && serviceElement.getName() != null) {
            URI serviceUri = new URI(serviceElement.getName().getNamespaceURI() + "wsdl.service(" + serviceElement.getName().getLocalPart() + ")");
            msmService = new Service(serviceUri);
            addOperations(intfElement.getInterfaceOperationElements(), msmService);
        }

        return msmService;

    }

    private void addOperations(InterfaceOperationElement[] operationElements, uk.ac.open.kmi.iserve.commons.model.Service msmService) throws URISyntaxException {
        if (operationElements == null || operationElements.length <= 0) {
            return;
        }

        uk.ac.open.kmi.iserve.commons.model.Operation msmOp;
        for (InterfaceOperationElement operationElement : operationElements) {
            URI operationUri = msmService.getUri().resolve(operationElement.getName().getLocalPart());
            msmOp = new uk.ac.open.kmi.iserve.commons.model.Operation(operationUri);
            msmService.addOperation(msmOp);
            ModelReferenceExtractor.addModelReferences(operationElement, msmOp);
            InterfaceMessageReferenceElement[] messages = operationElement.getInterfaceMessageReferenceElements();
            processMessages(messages, msmOp);
            InterfaceFaultReferenceElement[] faults = operationElement.getInterfaceFaultReferenceElements();
            processFaults(faults, msmOp);
        }
    }

    private void processFaults(InterfaceFaultReferenceElement[] faults, Operation msmOp) throws URISyntaxException {
        if (null == faults || faults.length <= 0) {
            return;
        }
        for (InterfaceFaultReferenceElement fault : faults) {
            URI faultUri = null;
            MessageContent faultMc = null;
            if (fault.getDirection().equals(Direction.IN)) {
                faultUri = msmOp.getUri().resolve("/infault/" + fault.getMessageLabel().toString());
                faultMc = new MessageContent(faultUri);
                msmOp.addInputFault(faultMc);
            } else if (fault.getDirection().equals(Direction.OUT)) {
                faultUri = msmOp.getUri().resolve("/outfault/" + fault.getMessageLabel().toString());
                faultMc = new MessageContent(faultUri);
                msmOp.addOutputFault(faultMc);
            }

            MessagePart mp;
            if (faultMc != null && fault.getRef() != null && fault.getRef().getLocalPart() != null) {
                URI faultPartUri = msmOp.getUri().resolve("types/" + fault.getRef().getLocalPart());
                mp = new MessagePart(faultPartUri);
                faultMc.addMandatoryPart(mp);
                // TODO: The types could also have annotations?
            }

            ModelReferenceExtractor.addModelReferences(fault, faultMc);
            ModelReferenceExtractor.addLiLoSchemaMappings(fault, faultMc);
        }
    }

    private void processMessages(InterfaceMessageReferenceElement[] messages, Operation msmOp) throws URISyntaxException {
        if (null == messages || messages.length <= 0) {
            return;
        }
        for (InterfaceMessageReferenceElement message : messages) {
            URI messageUri = null;
            MessageContent mc = null;
            if (message.getDirection().equals(Direction.IN)) {
                messageUri = msmOp.getUri().resolve("/input/" + message.getMessageLabel().toString());
                mc = new MessageContent(messageUri);
                msmOp.addInput(mc);
            } else if (message.getDirection().equals(Direction.OUT)) {
                messageUri = msmOp.getUri().resolve("/output/" + message.getMessageLabel().toString());
                mc = new MessageContent(messageUri);
                msmOp.addInput(mc);
            }

            // Deal with parts
            if (message.getElement() != null && message.getElement().getQName() != null &&
                    message.getElement().getQName().getLocalPart() != null) {
                URI messagePart = messageUri.resolve("types/" + message.getElement().getQName().getLocalPart());
                MessagePart mp = new MessagePart(messagePart);
                mc.addMandatoryPart(mp);
                // TODO: The types could also have annotations?
            }

            ModelReferenceExtractor.addModelReferences(message, mc);
            ModelReferenceExtractor.addLiLoSchemaMappings(message, mc);
        }
    }

}
