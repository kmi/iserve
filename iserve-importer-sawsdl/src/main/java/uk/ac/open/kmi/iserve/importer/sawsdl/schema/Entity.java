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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class Entity {

	protected NamedNodeMap attributes;

	private String baseUriString;

	private String name;

	private String modelref;

	private String lowering;

	private String lifting;

	public Entity(Node node, String baseUriString) {
		this.baseUriString = baseUriString;
		attributes = node.getAttributes();
		if ( attributes != null ) {
			int len = attributes.getLength();
			for ( int i = 0; i < len; i++ ) {
				if ( attributes.item(i) != null ) {
					String attrName = null;
					if (attributes.item(i).getLocalName() != null && attributes.item(i).getLocalName() != "" ) {
						attrName = attributes.item(i).getLocalName();
					} else if ( attributes.item(i).getNodeName() != null && attributes.item(i).getNodeName() != "" ) {
						attrName = attributes.item(i).getNodeName();
					}
					if ( attrName != null ) {
						if ( attrName.contains("name") ) {
							this.name = attributes.item(i).getNodeValue();
						} else if ( attrName.contains("modelReference") ){
							this.modelref = attributes.item(i).getNodeValue();
						} else if ( attrName.contains("loweringSchemaMapping") ){
							lowering = attributes.item(i).getNodeValue();
						} else if ( attrName.contains("liftingSchemaMapping") ){
							lifting = attributes.item(i).getNodeValue();
						}
					}
				}
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLoweringSchemaMapping() {
		return lowering;
	}

	public String getLiftingSchemaMapping() {
		return lifting;
	}

	public String getModelReference() {
		return modelref;
	}

	public String getUriString() {
		return baseUriString + name;
	}

}
