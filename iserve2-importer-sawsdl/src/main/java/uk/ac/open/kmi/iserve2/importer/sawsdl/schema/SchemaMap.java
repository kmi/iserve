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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.node.URI;

public class SchemaMap {

	private Map<String, SimpleType> simpleTypeMap;

	private Map<String, ComplexType> complexTypeMap;

	private Map<String, Element> elementMap;

	public SchemaMap() {
		simpleTypeMap = new HashMap<String, SimpleType>();
		complexTypeMap = new HashMap<String, ComplexType>();
		elementMap = new HashMap<String, Element>();
	}

	public void putSimpleType(String name, SimpleType simpleType) {
		simpleTypeMap.put(name, simpleType);
	}

	public void putComplexType(String name, ComplexType complexType) {
		complexTypeMap.put(name, complexType);
	}

	public void putElement(String name, Element element) {
		elementMap.put(name, element);
	}

	public Element findElementType(String name) {
		return elementMap.get(name);
	}

	public void typeToRdf(String typeName, Model model, URI partUri) {
		Type type = findType(typeName);
		if ( null == type ) return;
		type.toRdf(model, partUri);
	}

	private Type findType(String typeName) {
		Type type = simpleTypeMap.get(typeName);
		if ( type == null ) {
			return complexTypeMap.get(typeName);
		}
		return type;
	}

	public void toRdf(Model model) {
		Set<String> keys = elementMap.keySet();
		Iterator<String> iter = keys.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			Element element = elementMap.get(key);
			element.toRdf(model);
			String typeName = element.getTypeName();
			typeToRdf(typeName, model, model.createURI(element.getUriString()));
		}
	}

	public Map<String, SimpleType> getSimpleTypeMap() {
		return simpleTypeMap;
	}

	public Map<String, ComplexType> getComplexTypeMap() {
		return complexTypeMap;
	}

	public Map<String, Element> getElementMap() {
		return elementMap;
	}

	public void addSchemaMap(SchemaMap schemaMap) {
		if ( schemaMap != null ) {
			simpleTypeMap.putAll(schemaMap.getSimpleTypeMap());
			complexTypeMap.putAll(schemaMap.getComplexTypeMap());
			elementMap.putAll(schemaMap.getElementMap());
		}
	}

}
