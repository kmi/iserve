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
package uk.ac.open.kmi.iserve.importer.sawsdl.util;

import org.apache.woden.wsdl20.xml.DocumentableElement;
import org.apache.woden.xml.XMLAttr;
import org.w3c.dom.NamedNodeMap;
import uk.ac.open.kmi.iserve.commons.model.AnnotableResource;
import uk.ac.open.kmi.iserve.commons.model.MessagePart;
import uk.ac.open.kmi.iserve.commons.model.Resource;
import uk.ac.open.kmi.iserve.commons.vocabulary.SAWSDL;

import javax.wsdl.WSDLElement;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class ModelReferenceExtractor {

    private static final QName modelRefQName = new QName(SAWSDL.NS, SAWSDL.modelReference.getLocalName());

    private static final QName loweringQName = new QName(SAWSDL.NS, SAWSDL.loweringSchemaMapping.getLocalName());

    private static final QName liftingQName = new QName(SAWSDL.NS, SAWSDL.liftingSchemaMapping.getLocalName());

    public static void extractAddress(WSDLElement element) {
        // TODO: hasAddress
        if (element == null)
            return;
        List<ExtensibilityElement> elements = element.getExtensibilityElements();
        if (null == elements || elements.size() <= 0)
            return;
        for (ExtensibilityElement extElement : elements) {
            if (extElement instanceof HTTPAddress) {
//				System.out.println(((HTTPAddress) extElement).getLocationURI());
            } else if (extElement instanceof SOAPAddress) {
//				System.out.println(((SOAPAddress) extElement).getLocationURI());
            } else if (extElement instanceof SOAP12Address) {
//				System.out.println(((SOAP12Address) extElement).getLocationURI());
            }
        }
    }

    public static void addModelReferences(WSDLElement element, AnnotableResource resource) throws URISyntaxException {

        if (element == null)
            return;

        Object obj = element.getExtensionAttribute(modelRefQName);
        if (obj != null) {
            String[] modelRefs = tokenizeModelReferenceString(obj.toString());
            for (String modelRef : modelRefs) {
                resource.addModelReference(new Resource(new URI(modelRef)));
            }
        }
        // process attrExtensions
        List<UnknownExtensibilityElement> extElements = element.getExtensibilityElements();
        if (extElements != null && extElements.size() > 0) {
            for (UnknownExtensibilityElement extElement : extElements) {
                NamedNodeMap nodeMap = extElement.getElement().getAttributes();
                if (null == nodeMap || 0 == nodeMap.getLength())
                    return;
                int len = nodeMap.getLength();
                for (int i = 0; i < len; i++) {
                    if (nodeMap.item(i) != null && nodeMap.item(i).getNodeName() != null && nodeMap.item(i).getNodeName().contains("modelReference")) {
                        String[] modelRefs = tokenizeModelReferenceString(nodeMap.item(i).getNodeValue());
                        for (String modelRef : modelRefs) {
                            resource.addModelReference(new Resource(new URI(modelRef)));
                        }
                    }
                }
            }
        }
    }

    public static void addLiLoSchemaMappings(WSDLElement element, MessagePart mp) throws URISyntaxException {
        if (element == null)
            return;

        // Lowering
        Object obj = element.getExtensionAttribute(loweringQName);
        if (obj != null) {
            String[] lilos = tokenizeLiLoString(obj.toString());
            for (String lilo : lilos) {
                mp.addLoweringSchemaMapping(new URI(lilo));
            }
        }

        // Lifting
        obj = element.getExtensionAttribute(liftingQName);
        if (obj != null) {
            String[] lilos = tokenizeLiLoString(obj.toString());
            for (String lilo : lilos) {
                mp.addLiftingSchemaMapping(new URI(lilo));
            }
        }

        // process attrExtensions
        List<UnknownExtensibilityElement> extElements = element.getExtensibilityElements();
        if (extElements != null && extElements.size() > 0) {
            for (UnknownExtensibilityElement extElement : extElements) {
                NamedNodeMap nodeMap = extElement.getElement().getAttributes();
                if (null == nodeMap || 0 == nodeMap.getLength())
                    return;
                int len = nodeMap.getLength();
                for (int i = 0; i < len; i++) {
                    if (nodeMap.item(i) != null && nodeMap.item(i).getNodeName() != null) {
                        if (nodeMap.item(i).getNodeName().contains("loweringSchemaMapping")) {
                            String[] lilos = tokenizeLiLoString(nodeMap.item(i).getNodeValue());
                            for (String lilo : lilos) {
                                mp.addLoweringSchemaMapping(new URI(lilo));
                            }
                        } else if (nodeMap.item(i).getNodeName().contains("liftingSchemaMapping")) {
                            String[] lilos = tokenizeLiLoString(nodeMap.item(i).getNodeValue());
                            for (String lilo : lilos) {
                                mp.addLiftingSchemaMapping(new URI(lilo));
                            }
                        }
                    }
                }
            }
        }
    }

    public static String[] tokenizeModelReferenceString(String modelRefString) {
        if (null == modelRefString || "" == modelRefString)
            return new String[0];
        modelRefString = modelRefString.trim();
        String[] modelRefs = modelRefString.split(" ");
        if (modelRefs != null && modelRefs.length > 0) {
            for (String modelRef : modelRefs) {
                if (modelRef != null && !modelRef.equalsIgnoreCase("")) {
                    modelRef = fixWsdl4jBug(modelRef);
                }
            }
        }
        return modelRefs;
    }

    public static String[] tokenizeLiLoString(String liloString) {
        if (null == liloString || "" == liloString)
            return new String[0];
        liloString = liloString.trim();
        String[] mappings = liloString.split(" ");
        if (mappings != null && mappings.length > 0) {
            for (String mapping : mappings) {
                mapping = mapping.trim();
            }
        }
        return mappings;
    }

    public static void addModelReferences(DocumentableElement element, AnnotableResource resource) throws URISyntaxException {
        XMLAttr[] attributes = element.getExtensionAttributes();
        if (null == attributes || attributes.length <= 0) {
            return;
        }
        for (XMLAttr attribute : attributes) {
            if (attribute.getAttributeType() != null && attribute.getAttributeType().getLocalPart() != null &&
                    attribute.getAttributeType().getLocalPart().contains("modelReference")) {
                String[] modelRefs = tokenizeModelReferenceString(attribute.getContent().toString());
                for (String modelRef : modelRefs) {
                    resource.addModelReference(new Resource(new URI(modelRef)));
                }
            }
        }
    }

    public static void addLiLoSchemaMappings(DocumentableElement element, MessagePart mp) throws URISyntaxException {

        XMLAttr[] attributes = element.getExtensionAttributes();
        if (null == attributes || attributes.length <= 0) {
            return;
        }

        for (XMLAttr attribute : attributes) {
            if (attribute.getAttributeType() != null && attribute.getAttributeType().getLocalPart() != null &&
                    attribute.getAttributeType().getLocalPart().contains("liftingSchemaMapping")) {
                String[] liftings = tokenizeLiLoString(attribute.getContent().toString());
                for (String lifting : liftings) {
                    mp.addLiftingSchemaMapping(new URI(lifting));
                }
            } else if (attribute.getAttributeType() != null && attribute.getAttributeType().getLocalPart() != null &&
                    attribute.getAttributeType().getLocalPart().contains("loweringSchemaMapping")) {
                String[] lowerings = tokenizeLiLoString(attribute.getContent().toString());
                for (String lowering : lowerings) {
                    mp.addLoweringSchemaMapping(new URI(lowering));
                }
            }
        }
    }

    private static String fixWsdl4jBug(String str) {
        if (str == null || str.equalsIgnoreCase("")) {
            return null;
        }
        String result = str;
        int ind = result.indexOf("}");
        if (ind > 0) {
            result = "http:" + result.substring(ind + 1);
        }
        return result;
    }

}
