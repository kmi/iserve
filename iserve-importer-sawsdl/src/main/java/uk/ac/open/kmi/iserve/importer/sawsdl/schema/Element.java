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
package uk.ac.open.kmi.iserve.importer.sawsdl.schema;

import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.vocabulary.RDF;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.importer.sawsdl.util.ModelReferenceExtractor;

public class Element extends Entity {

	private String typeName;

	private Type type;

	public Element(Node node, String baseUriString) {
		super(node, baseUriString);
		this.type = null;
		if ( attributes != null ) {
			int len = attributes.getLength();
			for ( int i = 0; i < len; i++ ) {
				if ( attributes.item(i) != null ) {
					String attrName = null;
					if ( attributes.item(i).getLocalName() != null && attributes.item(i).getLocalName() != "" ) {
						attrName = attributes.item(i).getLocalName();
					} else if ( attributes.item(i).getNodeName() != null && attributes.item(i).getNodeName() != "" ) {
						attrName = attributes.item(i).getNodeName();
					}
					if ( attrName.contains("type") ) {
						typeName = attributes.item(i).getNodeValue();
						if ( typeName.contains(":") ) {
							int index = typeName.indexOf(":");
							typeName = typeName.substring(index + 1);
						} else if ( typeName.contains("#") ) {
							int index = typeName.indexOf("#");
							typeName = typeName.substring(index + 1);
						}
					}
				}
			}
		}
		/**
		 *  deal with things like
		 *  <element>
		 *    <complexType>
		 *    ...
		 *    </complexType>
		 *  <element>
		 */
		NodeList children = node.getChildNodes();
		if ( children != null ) {
			for ( int i = 0; i < children.getLength(); i++ ) {
				if ( children.item(i) != null ) {
					Node child = children.item(i);
					if ( child != null ) {
						if ( child.getNodeName().contains("complexType") ) {
							this.type = new ComplexType(child, baseUriString);
							// FIXME: might not be unique
							this.typeName = getName() + "Type";
							this.type.setName(this.typeName);
						} else if ( child.getNodeName().contains("simpleType") ) {
							this.type = new SimpleType(child, baseUriString);
							this.typeName = getName() + "Type";
							this.type.setName(this.typeName);
						}
					}
				}
			}
		}
	}

	public String getTypeName() {
		return typeName;
	}

	public Type getType() {
		return type;
	}

	public void toRdf(Model model) {
		URI elementUri = model.createURI(getUriString());
		model.addStatement(elementUri, RDF.type, MSM.MessagePart);
		String modelRefString = getModelReference();
		ModelReferenceExtractor.processModelRefString(modelRefString, model, elementUri);
		ModelReferenceExtractor.processLiLoString(getLoweringSchemaMapping(), model, elementUri, MSM.loweringSchemaMapping);
		ModelReferenceExtractor.processLiLoString(getLiftingSchemaMapping(), model, elementUri, MSM.liftingSchemaMapping);
	}

}
