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

import org.xml.sax.InputSource;
import uk.ac.open.kmi.iserve.commons.model.MessageContent;
import uk.ac.open.kmi.iserve.commons.model.MessagePart;
import uk.ac.open.kmi.iserve.importer.sawsdl.schema.Element;
import uk.ac.open.kmi.iserve.importer.sawsdl.schema.SchemaMap;
import uk.ac.open.kmi.iserve.importer.sawsdl.schema.SchemaParser;
import uk.ac.open.kmi.iserve.importer.sawsdl.util.ModelReferenceExtractor;

import javax.wsdl.*;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class Sawsdl11Transformer {

    private WSDLReader reader;

    private SchemaParser schemaParser;

    private List<SchemaMap> schemaMaps;

    public Sawsdl11Transformer() throws WSDLException {
        reader = WSDLFactory.newInstance().newWSDLReader();
        schemaParser = new SchemaParser();
        schemaMaps = new ArrayList<SchemaMap>();
    }

    public List<uk.ac.open.kmi.iserve.commons.model.Service> transform(InputStream originalDescription) throws WSDLException {
        InputSource is = new InputSource(originalDescription);
        Definition definition = reader.readWSDL(null, is);
        List<uk.ac.open.kmi.iserve.commons.model.Service> result = new ArrayList<uk.ac.open.kmi.iserve.commons.model.Service>();

        Types types = definition.getTypes();
        processTypes(types);

        uk.ac.open.kmi.iserve.commons.model.Service svc;
        Map<QName, Service> parsedServices = definition.getServices();
        for (Map.Entry<QName, Service> entry : parsedServices.entrySet()) {
            svc = processService(entry.getKey(), entry.getValue());
            if (svc != null)
                result.add(svc);
        }

        return result;
    }

    private void processTypes(Types types) {
        if (types == null) {
            return;
        }
        List<Schema> schemas = types.getExtensibilityElements();
        if (schemas != null) {
            for (Schema schema : schemas) {
                Map<String, Vector> importsMap = schema.getImports();
                // process imported schemas
                try {
                    SchemaMap importedSchema = processImports(importsMap);
                    if (importedSchema != null) {
                        schemaMaps.add(importedSchema);
                    }
                    SchemaMap schemaMap = null;

                    schemaMap = schemaParser.parse(schema);
                    if (schemaMap != null) {
                        schemaMaps.add(schemaMap);
                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

            }
        }
    }

    private SchemaMap processImports(Map<String, Vector> importsMap) throws URISyntaxException {
        SchemaMap result = null;
        Set<String> keys = importsMap.keySet();
        if (keys != null) {
            result = new SchemaMap();
            Iterator<String> iter = keys.iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                Vector<SchemaImport> vector = importsMap.get(key);
                if (vector != null && !vector.isEmpty()) {
                    Iterator<SchemaImport> schemaImportsIter = vector.iterator();
                    while (schemaImportsIter != null && schemaImportsIter.hasNext()) {
                        SchemaImport schemaImport = schemaImportsIter.next();
                        if (schemaImport != null && schemaImport.getReferencedSchema() != null) {
                            SchemaMap schemaMap = schemaParser.parse(schemaImport.getReferencedSchema());
                            if (schemaMap != null) {
                                result.addSchemaMap(schemaMap);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private uk.ac.open.kmi.iserve.commons.model.Service processService(QName qname, Service service) {

        uk.ac.open.kmi.iserve.commons.model.Service msmService = null;
        try {
            URI serviceUri = new URI(qname.getNamespaceURI() + "wsdl.service(" + qname.getLocalPart() + ")");
            msmService = new uk.ac.open.kmi.iserve.commons.model.Service(serviceUri);
            msmService.setSource(new URI(qname.getNamespaceURI() + qname.getLocalPart()));
            msmService.setLabel(qname.getLocalPart());
            ModelReferenceExtractor.addModelReferences(service, msmService);
            processPortType(service, msmService);
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return msmService;
    }

    private void processPortType(Service service, uk.ac.open.kmi.iserve.commons.model.Service msmService) throws URISyntaxException {
        Map<String, Port> ports = service.getPorts();
        if (null == ports)
            return;
        Set<String> keys = ports.keySet();
        if (null == keys)
            return;
        Iterator<String> iter = keys.iterator();
        if (null == iter)
            return;
        while (iter.hasNext()) {
            String key = iter.next();
            Port port = ports.get(key);
            // TODO: hasAddress
            PortType portType = port.getBinding().getPortType();
            ModelReferenceExtractor.addModelReferences(portType, msmService);
            addOperations(portType.getOperations(), msmService);
        }
    }

    private void addOperations(List<Operation> operations, uk.ac.open.kmi.iserve.commons.model.Service msmService) throws URISyntaxException {
        if (null == operations || operations.size() <= 0)
            return;

        uk.ac.open.kmi.iserve.commons.model.Operation msmOp;
        for (Operation operation : operations) {
            URI operationUri = msmService.getUri().resolve(operation.getName());
            msmOp = new uk.ac.open.kmi.iserve.commons.model.Operation(operationUri);
            msmService.addOperation(msmOp);

            ModelReferenceExtractor.addModelReferences(operation, msmOp);
            Input input = operation.getInput();
            if (input != null) {
                MessageContent msmContent = processMessage(msmOp.getUri(), input.getMessage());
                msmOp.addInput(msmContent);
            }
            Output output = operation.getOutput();
            if (output != null) {
                MessageContent msmContent = processMessage(msmOp.getUri(), output.getMessage());
                msmOp.addInput(msmContent);
            }
        }
    }

    private MessageContent processMessage(URI parentUri, Message message) throws URISyntaxException {
        URI messageUri = parentUri.resolve(message.getQName().getLocalPart());
        MessageContent mc = new MessageContent(messageUri);
        ModelReferenceExtractor.addModelReferences(message, mc);
        ModelReferenceExtractor.addLiLoSchemaMappings(message, mc);
        List<MessagePart> mps = processMessageParts(message, messageUri);
        mc.setMandatoryParts(mps);
        return mc;
    }

    private List<MessagePart> processMessageParts(Message message, URI messageUri) throws URISyntaxException {
        List<MessagePart> result = new ArrayList<MessagePart>();
        Map<String, Part> parts = message.getParts();
        for (Map.Entry<String, Part> entry : parts.entrySet()) {
            URI partUri = messageUri.resolve(entry.getValue().getName());
            MessagePart mp = new MessagePart(partUri);
            result.add(mp);
            // TODO: Check and fix this
            ModelReferenceExtractor.addModelReferences(entry.getValue(), mp);
            ModelReferenceExtractor.addLiLoSchemaMappings(entry.getValue(), mp);
            QName elementName = entry.getValue().getElementName();
            if (elementName != null && elementName.getLocalPart() != null && elementName.getLocalPart() != "") {
//				System.out.println(part.getElementName().getLocalPart());
                for (SchemaMap schemaMap : schemaMaps) {
                    Element element = schemaMap.findElementType(elementName.getLocalPart());
                    if (element != null && element.getEntityUri() != null) {
                        MessagePart innerPart = new MessagePart(element.getEntityUri());
                        mp.addMandatoryPart(innerPart);
                    }
                }
            } else {
                QName typeName = entry.getValue().getTypeName();
                if (typeName != null && typeName.getLocalPart() != null && typeName.getLocalPart() != "") {
                    for (SchemaMap schemaMap : schemaMaps) {
//						schemaMap.typeToRdf(typeName.getLocalPart(), partUri);
                        //FIXME
                    }
                }
            }
        }
        return result;
    }
}
