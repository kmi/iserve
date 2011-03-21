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
package uk.ac.open.kmi.iserve.importer.sawsdl;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLElement;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Syntax;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.vocabulary.RDF;
import org.ontoware.rdf2go.vocabulary.RDFS;
import org.xml.sax.InputSource;

import uk.ac.open.kmi.iserve.importer.sawsdl.schema.Element;
import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.importer.sawsdl.schema.SchemaMap;
import uk.ac.open.kmi.iserve.importer.sawsdl.schema.SchemaParser;
import uk.ac.open.kmi.iserve.importer.sawsdl.util.ModelReferenceExtractor;

public class Sawsdl11Transformer {

	private static final String TEMP_BASE_URI = "http://sawsdl-transformer.baseuri/8965949584020236497#";

	private WSDLReader reader;

	private SchemaParser schemaParser;

	private List<SchemaMap> schemaMaps;

	public Sawsdl11Transformer() throws WSDLException {
		reader = WSDLFactory.newInstance().newWSDLReader();
		schemaParser = new SchemaParser();
		schemaMaps = new ArrayList<SchemaMap>();
	}

	public String transform(String serviceDescription) throws WSDLException {
		InputSource is = new InputSource(new StringReader(serviceDescription));
		Definition definition = reader.readWSDL(null, is);
		Model tempModel = RDF2Go.getModelFactory().createModel();
		tempModel.open();
		Types types = definition.getTypes();
		// TODO: replace "types with the name of schema"
		processTypes(types, tempModel, TEMP_BASE_URI + "types/");

		processServices(definition.getServices(), tempModel);
		String result = tempModel.serialize(Syntax.RdfXml);
		result = result.replaceAll(TEMP_BASE_URI, "#");
		tempModel.close();
		tempModel = null;
//		System.out.println(result);
		schemaMaps = null;
		schemaMaps = new ArrayList<SchemaMap>();
		reader = null;
		reader = WSDLFactory.newInstance().newWSDLReader();
		return result;
	}

	private void processTypes(Types types, Model tempModel, String baseUriString) {
		if ( types == null ) {
			return;
		}
		List<Schema> schemas = types.getExtensibilityElements();
		if ( schemas != null ) {
			for ( Schema schema : schemas ) {
				Map<String, Vector> importsMap = schema.getImports();
				// process imported schemas
				SchemaMap importedSchema = processImports(importsMap, baseUriString);
				if ( importedSchema != null ) {
					schemaMaps.add(importedSchema);
				}
				SchemaMap schemaMap = schemaParser.parse(schema, baseUriString);
				if ( schemaMap != null ) {
					schemaMaps.add(schemaMap);
				}
			}
		}
	}

	private SchemaMap processImports(Map<String, Vector> importsMap, String baseUriString) {
		SchemaMap result = null;
		Set<String> keys = importsMap.keySet();
		if ( keys != null ) {
			result = new SchemaMap();
			Iterator<String> iter = keys.iterator();
			while ( iter.hasNext() ) {
				String key = iter.next();
				Vector<SchemaImport> vector = importsMap.get(key);
				if ( vector != null && !vector.isEmpty() ) {
					Iterator<SchemaImport> schemaImportsIter = vector.iterator();
					while ( schemaImportsIter != null && schemaImportsIter.hasNext() ) {
						SchemaImport schemaImport = schemaImportsIter.next();
						if ( schemaImport != null && schemaImport.getReferencedSchema() != null ) {
							SchemaMap schemaMap = schemaParser.parse(schemaImport.getReferencedSchema(), baseUriString);
							if ( schemaMap != null ) {
								result.addSchemaMap(schemaMap);
							}
						}
					}
				}
			}
		}
		return result;
	}

	private void processServices(Map<QName, Service> services, Model tempModel) {
		for ( SchemaMap schemaMap : schemaMaps ) {
			schemaMap.toRdf(tempModel);
		}
		Set<QName> keys = services.keySet();
		Iterator<QName> iter = keys.iterator();
		while (iter.hasNext()) {
			QName key = iter.next();
			Service service = services.get(key);
			URI serviceUri = tempModel.createURI(TEMP_BASE_URI + "wsdl.service(" + service.getQName().getLocalPart() + ")");
			tempModel.addStatement(serviceUri, RDF.type, MSM.Service);
			tempModel.addStatement(serviceUri, RDFS.label, service.getQName().getLocalPart());
			ModelReferenceExtractor.extractModelReferences((WSDLElement) service, tempModel, serviceUri);
			processPortType(service, tempModel, serviceUri);
		}
	}

	private void processPortType(Service service, Model tempModel, URI serviceUri) {
		Map<String, Port> ports = service.getPorts();
		if ( null == ports )
			return;
		Set<String> keys = ports.keySet();
		if ( null == keys )
			return;
		Iterator<String> iter = keys.iterator();
		if ( null == iter )
			return;
		while (iter.hasNext()) {
			String key = iter.next();
			Port port = ports.get(key);
			// TODO: hasAddress
			PortType portType = port.getBinding().getPortType();
			ModelReferenceExtractor.extractModelReferences((WSDLElement) portType, tempModel, serviceUri);
			processOperations(portType.getOperations(), tempModel, serviceUri);
		}
	}

	private void processOperations(List<Operation> operations, Model tempModel, URI serviceUri) {
		if ( null == operations || operations.size() <= 0 )
			return;
		for ( Operation operation : operations ) {
			URI operationUri = tempModel.createURI(serviceUri.toString() + "/" + operation.getName());
			tempModel.addStatement(operationUri, RDF.type, MSM.Operation);
			tempModel.addStatement(serviceUri, MSM.hasOperation, operationUri);
			ModelReferenceExtractor.extractModelReferences(operation, tempModel, operationUri);
			Input input = operation.getInput();
			if ( input != null ) {
				processMessage(input.getMessage(), tempModel, operationUri, MSM.hasInput);
			}
			Output output = operation.getOutput();
			if ( output != null ) {
				processMessage(output.getMessage(), tempModel, operationUri, MSM.hasOutput);
			}
		}
	}

	private void processMessage(Message message, Model tempModel, URI operationUri, URI typeUri) {
		URI messageUri = tempModel.createURI(operationUri.toString() + "/" + message.getQName().getLocalPart());
		tempModel.addStatement(messageUri, RDF.type, MSM.Message);
		tempModel.addStatement(operationUri, typeUri, messageUri);
		ModelReferenceExtractor.extractModelReferences(message, tempModel, messageUri);
		ModelReferenceExtractor.extractLiLoSchema(message, tempModel, messageUri);
		processMessageParts(message, tempModel, messageUri);
	}

	private void processMessageParts(Message message, Model tempModel, URI messageUri) {
		Map<String, Part> parts = message.getParts();
		Set<String> keys = parts.keySet();
		Iterator<String> iter = keys.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			Part part = parts.get(key);
			URI partUri = tempModel.createURI(messageUri.toString() + "/" + part.getName());
			tempModel.addStatement(partUri, RDF.type, MSM.MessagePart);
			tempModel.addStatement(messageUri, MSM.hasPart, partUri);
			tempModel.addStatement(messageUri, MSM.hasPartTransitive, partUri);
			ModelReferenceExtractor.extractModelReferences(part, tempModel, partUri);
			ModelReferenceExtractor.extractLiLoSchema(part, tempModel, partUri);
			QName elementName = part.getElementName();
			if ( elementName != null && elementName.getLocalPart() != null && elementName.getLocalPart() != "" ) {
//				System.out.println(part.getElementName().getLocalPart());
				for ( SchemaMap schemaMap : schemaMaps ) {
					Element element = schemaMap.findElementType(elementName.getLocalPart());
					if ( element != null && element.getUriString() != null && element.getUriString() != "" ) {
//						System.out.println("element.getUriString(): " + element.getUriString());
						tempModel.addStatement(partUri, MSM.hasPart, tempModel.createURI(element.getUriString()));
						tempModel.addStatement(partUri, MSM.hasPartTransitive, tempModel.createURI(element.getUriString()));
					}
				}
			} else {
				QName typeName = part.getTypeName();
				if ( typeName != null && typeName.getLocalPart() != null && typeName.getLocalPart() != "" ) {
					for ( SchemaMap schemaMap : schemaMaps ) {
						schemaMap.typeToRdf(typeName.getLocalPart(), tempModel, partUri);
					}
				}
			}
		}
	}

}
