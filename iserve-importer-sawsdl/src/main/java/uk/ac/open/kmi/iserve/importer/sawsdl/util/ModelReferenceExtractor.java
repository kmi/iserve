/*
   Copyright ${year}  Knowledge Media Institute - The Open University

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package uk.ac.open.kmi.iserve.importer.sawsdl.util;

import java.util.List;

import javax.wsdl.WSDLElement;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.xml.namespace.QName;

import org.apache.woden.wsdl20.xml.DocumentableElement;
import org.apache.woden.xml.XMLAttr;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.node.URI;
import org.w3c.dom.NamedNodeMap;

import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.commons.vocabulary.SAWSDL;

public class ModelReferenceExtractor {

	private static final QName modelRefQName = new QName("http://www.w3.org/ns/sawsdl", "modelReference");

	private static final QName loweringQName = new QName("http://www.w3.org/ns/sawsdl", "loweringSchemaMapping");

	private static final QName liftingQName = new QName("http://www.w3.org/ns/sawsdl", "liftingSchemaMapping");

	public static void extractAddress(WSDLElement element, Model model, URI elementUri) {
		// TODO: hasAddress
		if ( null == model || false == model.isOpen() )
			return;
		List<ExtensibilityElement> elements = element.getExtensibilityElements();
		if ( null == elements || elements.size() <= 0 )
			return;
		for ( ExtensibilityElement extElement : elements ) {
			if ( extElement instanceof HTTPAddress ) {
//				System.out.println(((HTTPAddress) extElement).getLocationURI());
			} else if ( extElement instanceof SOAPAddress ) {
//				System.out.println(((SOAPAddress) extElement).getLocationURI());
			} else if ( extElement instanceof SOAP12Address ) {
//				System.out.println(((SOAP12Address) extElement).getLocationURI());
			}
		}
	}

	public static void extractModelReferences(WSDLElement element, Model model, URI elementUri) {
		if ( null == model || false == model.isOpen() )
			return;
		Object obj = element.getExtensionAttribute(modelRefQName);
		if ( obj != null ) {
			processModelRefString(obj.toString(), model, elementUri);
		}
		// process attrExtensions
		List<UnknownExtensibilityElement> extElements = element.getExtensibilityElements();
		if ( extElements != null && extElements.size() > 0 ) {
			for ( UnknownExtensibilityElement extElement : extElements ) {
				NamedNodeMap nodeMap = extElement.getElement().getAttributes();
				if ( null == nodeMap || 0 == nodeMap.getLength() )
					return;
				int len = nodeMap.getLength();
				for ( int i = 0; i < len; i++ ) {
					if ( nodeMap.item(i) != null && nodeMap.item(i).getNodeName() != null && nodeMap.item(i).getNodeName().contains("modelReference") ) {
						processModelRefString(nodeMap.item(i).getNodeValue(), model, elementUri);
					}					
				}
			}
		}
	}

	public static void extractLiLoSchema(WSDLElement element, Model model, URI elementUri) {
		if ( null == model || false == model.isOpen() )
			return;
		Object obj = element.getExtensionAttribute(loweringQName);
		if ( obj != null ) {
			processLiLoString(obj.toString(), model, elementUri, model.createURI(SAWSDL.LOWERING_SCHEMA_MAPPING));
		}
		obj = element.getExtensionAttribute(liftingQName);
		if ( obj != null ) {
			processLiLoString(obj.toString(), model, elementUri, model.createURI(SAWSDL.LIFTING_SCHEMA_MAPPING));
		}
		// process attrExtensions
		List<UnknownExtensibilityElement> extElements = element.getExtensibilityElements();
		if ( extElements != null && extElements.size() > 0 ) {
			for ( UnknownExtensibilityElement extElement : extElements ) {
				NamedNodeMap nodeMap = extElement.getElement().getAttributes();
				if ( null == nodeMap || 0 == nodeMap.getLength() )
					return;
				int len = nodeMap.getLength();
				for ( int i = 0; i < len; i++ ) {
					if ( nodeMap.item(i) != null && nodeMap.item(i).getNodeName() != null ) {
						if ( nodeMap.item(i).getNodeName().contains("loweringSchemaMapping") ) {
							processLiLoString(nodeMap.item(i).getNodeValue(), model, elementUri, model.createURI(SAWSDL.LOWERING_SCHEMA_MAPPING));
						} else if ( nodeMap.item(i).getNodeName().contains("liftingSchemaMapping") ) {
							processLiLoString(nodeMap.item(i).getNodeValue(), model, elementUri, model.createURI(SAWSDL.LIFTING_SCHEMA_MAPPING));
						}
					} 
				}
			}
		}
	}

	public static void processModelRefString(String modelRefString, Model model, URI elementUri) {
		if ( null == modelRefString || "" == modelRefString )
			return;
		modelRefString = modelRefString.trim();
		String[] modelRefs = modelRefString.split(" ");
		if ( modelRefs != null && modelRefs.length > 0 ) {
			for ( String modelRef : modelRefs ) {
				if ( modelRef != null && !modelRef.equalsIgnoreCase("") ) {
//					System.out.println("modelRef: " + modelRef);
					modelRef = fixWsdl4jBug(modelRef);
					model.addStatement(elementUri, model.createURI(SAWSDL.MODEL_REFERENCE), model.createURI(modelRef));
				}
			}
		}
	}

	public static void processLiLoString(String liloString, Model model, URI elementUri, URI lilo) {
		if ( null == liloString || "" == liloString )
			return;
		liloString = liloString.trim();
		String[] mappings = liloString.split(" ");
		if ( mappings != null && mappings.length > 0 ) {
			for ( String mapping : mappings ) {
				if ( mapping != null && mapping != "" ) {
					model.addStatement(elementUri, lilo, model.createURI(mapping));
				}
			}
		}
	}

	public static void extractModelReferences(DocumentableElement element, Model model, URI elementUri) {
		XMLAttr[] attributes = element.getExtensionAttributes();
		if ( null == attributes || attributes.length <= 0 ) {
			return;
		}
		for ( XMLAttr attribute : attributes ) {
			if ( attribute.getAttributeType() != null && attribute.getAttributeType().getLocalPart() != null &&
					attribute.getAttributeType().getLocalPart().contains("modelReference") ) {
				processModelRefString(attribute.getContent().toString(), model, elementUri);
			}
		}
	}

	public static void extractLiLoSchema(DocumentableElement element, Model model, URI elementUri) {
		XMLAttr[] attributes = element.getExtensionAttributes();
		if ( null == attributes || attributes.length <= 0 ) {
			return;
		}
		for ( XMLAttr attribute : attributes ) {
			if ( attribute.getAttributeType() != null && attribute.getAttributeType().getLocalPart() != null &&
					attribute.getAttributeType().getLocalPart().contains("liftingSchemaMapping") ) {
				processLiLoString(attribute.getContent().toString(), model, elementUri, model.createURI(SAWSDL.LIFTING_SCHEMA_MAPPING));
			} else if ( attribute.getAttributeType() != null && attribute.getAttributeType().getLocalPart() != null &&
					attribute.getAttributeType().getLocalPart().contains("loweringSchemaMapping") ) {
				processLiLoString(attribute.getContent().toString(), model, elementUri, model.createURI(SAWSDL.LOWERING_SCHEMA_MAPPING));
			}
		}
	}

	private static String fixWsdl4jBug(String str) {
		if ( str == null || str.equalsIgnoreCase("") ) {
			return null;
		}
		String result = str;
		int ind = result.indexOf("}");
		if ( ind > 0 ) {
			result = "http:" + result.substring(ind + 1);
		}
		return result;
	}

}
