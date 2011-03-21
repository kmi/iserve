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
package uk.ac.open.kmi.iserve2.importer.sawsdl.schema;

import java.util.List;

import javax.wsdl.extensions.schema.Schema;

import org.apache.ws.commons.schema.XmlSchema;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SchemaParser {

	public SchemaParser() {
		
	}

	public SchemaMap parse(Schema schema, String baseUriString) {
		org.w3c.dom.Element doc = schema.getElement();
		return parse(doc, baseUriString);
	}

	public SchemaMap parse(XmlSchema xmlSchema, String baseUriString) {
		Document[] schemas = xmlSchema.getAllSchemas();
		SchemaMap result = new SchemaMap();
		for ( Document schema : schemas ) {
			if ( schema != null && schema.getDocumentElement() != null ) {
				result.addSchemaMap(parse(schema.getDocumentElement(), baseUriString));
			}
		}
		return result;
	}

	private SchemaMap parse(org.w3c.dom.Element xmlElement, String baseUriString) {
		NodeList nodes = xmlElement.getChildNodes();
		if ( null == nodes )
			return null;
		SchemaMap result = new SchemaMap();

		for ( int i = 0; i < nodes.getLength(); i++ ) {
			Node node = nodes.item(i);
			if ( node != null && node.getLocalName() != null ) {
				if ( node.getLocalName().equalsIgnoreCase("simpleType") ) {
					SimpleType simpleType = new SimpleType(node, baseUriString);
					result.putSimpleType(simpleType.getName(), simpleType);
				} else if ( node.getLocalName().equalsIgnoreCase("complexType") ) {
					ComplexType complexType = new ComplexType(node, baseUriString);
					result.putComplexType(complexType.getName(), complexType);
					List<Element> children = complexType.getSequence();
					if ( children != null && children.size() > 0 ) {
						for ( Element child : children ) {
							result.putElement(child.getName(), child);
						}
					}
				} else if ( node.getLocalName().equalsIgnoreCase("element") ) {
					Element element = new Element(node, baseUriString);
					result.putElement(element.getName(), element);
					Type noNameType = element.getType();
					if ( noNameType != null ) {
						if ( noNameType instanceof SimpleType ) {
							result.putSimpleType(noNameType.getName(), (SimpleType) noNameType);
						} else if ( noNameType instanceof ComplexType ) {
							result.putComplexType(noNameType.getName(), (ComplexType) noNameType);
						}
					}
				}
			}
		}
		return result;
	}

}
