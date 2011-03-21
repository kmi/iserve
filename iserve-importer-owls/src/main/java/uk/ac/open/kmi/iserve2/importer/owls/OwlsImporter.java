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
package uk.ac.open.kmi.iserve2.importer.owls;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.Syntax;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.iserve2.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve2.importer.ImporterConfig;
import uk.ac.open.kmi.iserve2.importer.ImporterException;
import uk.ac.open.kmi.iserve2.importer.ServiceImporter;

public class OwlsImporter extends ServiceImporter {

	private static final String PREFIX = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n"
			+ "PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\n"
			+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"
			+ "PREFIX sawsdl:<http://www.w3.org/ns/sawsdl#>\n"
			+ "PREFIX service:<http://www.daml.org/services/owl-s/1.1/Service.owl#>\n"
			+ "PREFIX profile:<http://www.daml.org/services/owl-s/1.1/Profile.owl#>\n"
			+ "PREFIX process:<http://www.daml.org/services/owl-s/1.1/Process.owl#>\n"
			+ "PREFIX grounding:<http://www.daml.org/services/owl-s/1.1/Grounding.owl#>\n"
			+ "PREFIX expr:<http://www.daml.org/services/owl-s/1.1/generic/Expression.owl#>\n"
			+ "PREFIX wl:<http://www.wsmo.org/ns/wsmo-lite#>"
			+ "PREFIX msm:<"
			+ MSM.NS_URI + ">\n";

	private static final String QUERY = PREFIX
			+ "SELECT DISTINCT * WHERE {\n"
			+ "  ?service rdf:type service:Service .\n"
			+ "  ?service service:presents ?profile .\n"
			+ "  OPTIONAL { ?service service:describedBy ?processModel. ?processModel process:hasProcess ?process . } "
			+ "  OPTIONAL { ?service service:describedBy ?process . }"
			+ "  OPTIONAL { \n"
			+ "    ?process process:hasPrecondition ?condition . \n"
			+ "    OPTIONAL { ?condition rdfs:label ?conditionlabel . }\n"
			+ "    ?condition expr:expressionLanguage ?conditionExprLang .\n"
			+ "    ?condition expr:expressionBody ?conditionExprBody .\n"
			+ "  }\n"
			+ "  OPTIONAL {?profile profile:serviceClassification ?sclass .}\n"
			+ "  OPTIONAL {?profile profile:serviceProduct ?sproduct .}\n"
			+ "  OPTIONAL {\n"
			+ "    ?process process:hasResult ?result .\n"
			+ "    OPTIONAL {\n"
			+ "      ?result process:hasResultVar ?resultVar . \n"
			+ "      ?resultVar process:parameterType ?resultVarType . "
			+ "    }\n"
			+ "    OPTIONAL {\n"
			+ "      ?result process:inCondition ?inCondition .\n"
			+ "      ?inCondition expr:expressionBody ?inConditionExprBody .\n"
			+ "    }\n"
			+ "    ?result process:hasEffect ?effect .\n"
			+ "    ?effect expr:expressionBody ?effectExprBody .\n"
			+ "  }\n"
			+ "  OPTIONAL {\n"
			+ "   ?processGrounding grounding:owlsProcess ?process .\n"
			+ "   ?processGrounding grounding:wsdlOperation ?operationRef .\n"
			+ "   ?operationRef grounding:operation ?operation .\n"
			+ "  }\n"
			+ "  OPTIONAL {\n"
			+ "   ?processGrounding grounding:wsdlInputMessage ?inputMessage .\n"
			+ "   ?processGrounding grounding:wsdlInput ?inputMap .\n"
			+ "   ?inputMap grounding:wsdlMessagePart ?inputPart .\n"
			+ "   ?inputMap grounding:owlsParameter ?inputParameter .\n"
			+ "   ?inputParameter process:parameterType ?inputParameterType .\n"
			+ "   OPTIONAL {?inputMap grounding:xsltTransformationString ?inputTransform . }\n"
			+ "  }\n"
			+ "  OPTIONAL {\n"
			+ "   ?processGrounding grounding:wsdlOutputMessage ?outputMessage .\n"
			+ "   ?processGrounding grounding:wsdlOutput ?outputMap .\n"
			+ "   ?outputMap grounding:wsdlMessagePart ?outputPart .\n"
			+ "   ?outputMap grounding:owlsParameter ?outputParameter .\n"
			+ "   ?outputParameter process:parameterType ?outputParameterType .\n"
			+ "   OPTIONAL {?outputMap grounding:xsltTransformationString ?outputTransform . }\n"
			+ "  }\n"
			// + "  OPTIONAL {\n"
			+ "    OPTIONAL {?process process:hasInput ?inputParameter2 . }\n"
			+ "    OPTIONAL {?inputParameter2 process:parameterType ?inputParameterType2 . }\n"
			+ "    OPTIONAL {?process process:hasOutput ?outputParameter2 . }\n"
			+ "    OPTIONAL {?outputParameter2 process:parameterType ?outputParameterType2 . }\n"
			// + "  }"
			+ "}";

	private static final String TEMP_NS = "http://owls-transformer.baseuri/8965949584020236497#";

	public OwlsImporter(ImporterConfig config) throws RepositoryException {
		super(config);
	}

	@Override
	protected InputStream transformStream(String serviceDescription) throws ImporterException {
		// store the service into a temporary repository.
		Model tempModel = RDF2Go.getModelFactory().createModel();
		tempModel.open();
		String resultString = null;
		try {
			tempModel.readFrom(new ByteArrayInputStream(serviceDescription.getBytes()));
			resultString = transform(tempModel);
		} catch (ModelRuntimeException e) {
			throw new ImporterException(e);
		} catch (IOException e) {
			throw new ImporterException(e);
		} finally {
			tempModel.close();
		}
		if ( null == resultString ) {
			return null;
		}
		return new ByteArrayInputStream(resultString.getBytes());
	}

	private String transform(Model model) {
		QueryResultTable qrt = extractInformation(model);
		String triplePatterns = generateTriplePatterns(qrt);
		String constructString = createMsmInstance(triplePatterns);
		ClosableIterable<Statement> iterable = model.sparqlConstruct(constructString);

		Model resultModel = RDF2Go.getModelFactory().createModel();
		resultModel.open();
		resultModel.addAll(iterable.iterator());
		String result = resultModel.serialize(Syntax.RdfXml);
		resultModel.close();
		result = result.replaceAll(TEMP_NS, "#");
		return result;
	}

	private QueryResultTable extractInformation(Model model) {
		QueryResultTable qrt = model.sparqlSelect(QUERY);
		return qrt;
	}

	private String generateTriplePatterns(QueryResultTable queryResultTable) {
		if ( queryResultTable == null ) return "";
		ClosableIterator<QueryRow> iterator = queryResultTable.iterator();
		StringBuffer result = new StringBuffer();
		while (iterator.hasNext()) {
			QueryRow row = (QueryRow) iterator.next();
			if ( row.getValue("service") != null ) {
				String serviceUri = row.getValue("service").toString();
				String serviceLocalName = ":" + getLocalName(serviceUri);

				result.append(serviceLocalName);
				result.append(" rdf:type <" + MSM.SERVICE + "> .\n");
				result.append(serviceLocalName);
				result.append(" rdfs:label ?sname .\n");
				result.append(serviceLocalName);
				result.append(" rdfs:comment ?tdescription .\n");
				if ( row.getValue("sclass") != null ) {
					result.append(serviceLocalName);
					result.append(" sawsdl:modelReference <" + row.getLiteralValue("sclass") + "> .\n");
				}
				if ( row.getValue("sproduct") != null ) {
					result.append(serviceLocalName);
					result.append(" sawsdl:modelReference <" + row.getLiteralValue("sproduct") + "> .\n");
				}

				String operationUri = "";
				String operationLocalName = ":operation";
				if ( row.getValue("operation") != null ) {
					operationUri = row.getValue("operation").toString();
					operationLocalName = ":" + getLocalName(operationUri);
				} else {
					operationUri = row.getValue("process").toString();
					operationLocalName = ":" + getLocalName(operationUri);
				}

				result.append(operationLocalName);
				result.append(" rdf:type <" + MSM.OPERATION + "> .\n");
				result.append(serviceLocalName);
				result.append(" <" + MSM.HAS_OPERATION + "> ");
				result.append(operationLocalName);
				result.append(" .\n");

				if ( row.getValue("condition") != null ) {
					String conditionUri = row.getValue("condition").toString();
					result.append(operationLocalName + " sawsdl:modelReference :" + getLocalName(conditionUri) + " .\n");
					result.append(":" + getLocalName(conditionUri) + " rdf:type wl:Condition .\n");
					if ( row.getValue("conditionlabel") != null ) {
						result.append(":" + getLocalName(conditionUri) + " rdfs:label \"" + row.getValue("conditionlabel").asLiteral().toString() + "\" .\n");
					}
//					if ( row.getLiteralValue("conditionlabel") != null && row.getLiteralValue("conditionlabel") != "" ) {
//						result.append(":" + getLocalName(conditionUri) + " rdfs:label \"" + row.getLiteralValue("conditionlabel") + "\" .\n");
//					}
					if ( row.getValue("conditionExprLang") != null ) {
						result.append(":" + getLocalName(conditionUri) + " expr:expressionLanguage " + row.getValue("conditionExprLang").toSPARQL() + " .\n");
					}
					if ( row.getValue("conditionExprBody") != null ) {
						result.append(":" + getLocalName(conditionUri) + " expr:expressionBody " + row.getValue("conditionExprBody").toSPARQL() + " .\n");
					}
				}

				// effect
				if ( row.getValue("result") != null ) {
					String resultUri = row.getValue("result").toString();
					result.append(operationLocalName + " sawsdl:modelReference :" + getLocalName(resultUri) + " .\n");
					result.append(":" + getLocalName(resultUri) + " rdf:type wl:Effect .\n");
					if ( row.getValue("resultVar") != null ) {
						result.append(":" + getLocalName(resultUri) + " process:hasResultVar " + row.getValue("resultVar").toSPARQL() + " .\n");
					}
					if ( row.getValue("resultVarType") != null ) {
						result.append(":" + getLocalName(resultUri) + " process:parameterType " + row.getValue("resultVarType").toSPARQL() + " .\n");
					}
					if ( row.getValue("inCondition") != null ) {
						result.append(":" + getLocalName(resultUri) + " process:inCondition <" + row.getValue("inCondition").toString() + "> .\n");
						if ( row.getValue("inConditionExprBody") != null ) {
							result.append("<" + row.getValue("inCondition").toString() + "> expr:expressionBody " + row.getValue("inConditionExprBody").toSPARQL() + " .\n");
						}
					}
					if ( row.getValue("effect") != null ) {
						String effectUriString = row.getValue("effect").toString();
						if ( effectUriString.startsWith("_:") ) {
							effectUriString = ":" + getLocalName(resultUri) + "_Effect";
						} else {
							effectUriString = "<" + effectUriString + ">"; 
						}
						result.append(":" + getLocalName(resultUri) + " process:hasEffect " + effectUriString + " .\n");
						if ( row.getValue("effectExprBody") != null ) {
							result.append(effectUriString + " expr:expressionBody " + row.getValue("effectExprBody").toSPARQL() + " .\n");
						}
					}
				}


				if ( row.getValue("inputMessage") != null ) {
					String inputUri = row.getValue("inputMessage").toString();
					String inputLocalName = ":" + getLocalName(inputUri);

					result.append(inputLocalName);
					result.append(" rdf:type <" + MSM.MESSAGE + "> .\n");
					result.append(operationLocalName);
					result.append(" <" + MSM.HAS_INPUT_MESSAGE + "> ");
					result.append(inputLocalName);
					result.append(" .\n");

					String inputPartUri = row.getValue("inputPart").toString();
					String inputPartLocalName = ":" + getLocalName(inputPartUri);
			
					result.append(inputPartLocalName);
					result.append(" rdf:type <" + MSM.MESSAGE_PART + "> .\n");
					result.append(inputLocalName);
					result.append(" <" + MSM.HAS_PART + "> ");
					result.append(inputPartLocalName);
					result.append(" .\n");

					result.append(inputLocalName);
					result.append(" <" + MSM.HAS_PART_TRANSITIVE + "> ");
					result.append(inputPartLocalName);
					result.append(" .\n");

					result.append(inputPartLocalName);
					result.append(" sawsdl:modelReference <");
					result.append(row.getLiteralValue("inputParameterType"));
					result.append("> .\n");
				} else {
					// for OWL-S TC 2.x
					String inputLocalName = ":input";

					result.append(inputLocalName);
					result.append(" rdf:type <" + MSM.MESSAGE + "> .\n");
					result.append(operationLocalName);
					result.append(" <" + MSM.HAS_INPUT_MESSAGE + "> ");
					result.append(inputLocalName);
					result.append(" .\n");

					String inputPartUri = row.getValue("inputParameter2").toString();
					String inputPartLocalName = ":" + getLocalName(inputPartUri) + "Part";

					result.append(inputPartLocalName);
					result.append(" rdf:type <" + MSM.MESSAGE_PART + "> .\n");
					result.append(inputLocalName);
					result.append(" <" + MSM.HAS_PART + "> ");
					result.append(inputPartLocalName);
					result.append(" .\n");
					result.append(inputLocalName);
					result.append(" <" + MSM.HAS_PART_TRANSITIVE + "> ");
					result.append(inputPartLocalName);
					result.append(" .\n");

					result.append(inputPartLocalName);
					result.append(" sawsdl:modelReference <");
					result.append(row.getLiteralValue("inputParameterType2"));
					result.append("> .\n");					
				}

				if ( row.getValue("outputMessage") != null ) {
					String outputUri = row.getValue("outputMessage").toString();
					String outputLocalName = ":" + getLocalName(outputUri);
		
					result.append(outputLocalName);
					result.append(" rdf:type <" + MSM.MESSAGE + "> .\n");
					result.append(operationLocalName);
					result.append(" <" + MSM.HAS_OUTPUT_MESSAGE + "> ");
					result.append(outputLocalName);
					result.append(" .\n");
		
					String outputPartUri = row.getValue("outputPart").toString();
					String outputPartLocalName = ":" + getLocalName(outputPartUri);
		
					result.append(outputPartLocalName);
					result.append(" rdf:type <" + MSM.MESSAGE_PART + "> .\n");
					result.append(outputLocalName);
					result.append(" <" + MSM.HAS_PART + "> ");
					result.append(outputPartLocalName);
					result.append(" .\n");
					result.append(outputLocalName);
					result.append(" <" + MSM.HAS_PART_TRANSITIVE + "> ");
					result.append(outputPartLocalName);
					result.append(" .\n");

					result.append(outputPartLocalName);
					result.append(" sawsdl:modelReference <");
					result.append(row.getLiteralValue("outputParameterType"));
					result.append("> .\n");
				} else {
					// for OWL-S TC 2.x
					String outputLocalName = ":output";

					result.append(outputLocalName);
					result.append(" rdf:type <" + MSM.MESSAGE + "> .\n");
					result.append(operationLocalName);
					result.append(" <" + MSM.HAS_OUTPUT_MESSAGE + "> ");
					result.append(outputLocalName);
					result.append(" .\n");

					String outputPartUri = row.getValue("outputParameter2").toString();
					String outputPartLocalName = ":" + getLocalName(outputPartUri) + "Part";

					result.append(outputPartLocalName);
					result.append(" rdf:type <" + MSM.MESSAGE_PART + "> .\n");
					result.append(outputLocalName);
					result.append(" <" + MSM.HAS_PART + "> ");
					result.append(outputPartLocalName);
					result.append(" .\n");
					result.append(outputLocalName);
					result.append(" <" + MSM.HAS_PART_TRANSITIVE + "> ");
					result.append(outputPartLocalName);
					result.append(" .\n");

					result.append(outputPartLocalName);
					result.append(" sawsdl:modelReference <");
					result.append(row.getLiteralValue("outputParameterType2"));
					result.append("> .\n");
				}
			}
			result.append("\n");
		}
//		System.out.println("result: " + result.toString());
		return result.toString();
	}

	private int getLocalNameIndex(String uri) {
		int separatorIdx = uri.indexOf('#');

		if (separatorIdx < 0) {
			separatorIdx = uri.lastIndexOf('/');
		}

		if (separatorIdx < 0) {
			separatorIdx = uri.lastIndexOf(':');
		}

		if (separatorIdx < 0) {
			throw new IllegalArgumentException("No separator character founds in URI: " + uri);
		}

		return separatorIdx + 1;
	}

	private String getLocalName(String uriString) {
		int idx = uriString.indexOf('^');
		if ( idx >= 0 ) {
			uriString = uriString.substring(0, idx);
		}
		int localNameIdx = getLocalNameIndex(uriString);
		return uriString.substring(localNameIdx);
	}

	private String createMsmInstance(String triplePatterns) {
		if ( null == triplePatterns || "".endsWith(triplePatterns) )
			return "";
		String prefix = PREFIX + "PREFIX :<" + TEMP_NS + ">\n";
		String construct = prefix 
			+ "CONSTRUCT {"
			+ triplePatterns
			+ "} WHERE {\n"
			+ "?service rdf:type service:Service .\n"
			+ "?service service:presents ?profile .\n"
			+ "OPTIONAL {?profile profile:serviceName ?sname .}\n"
			+ "OPTIONAL {?profile profile:textDescription ?tdescription .}\n"
//			+ "OPTIONAL {?profile profile:serviceClassification ?sclass .}\n"
//			+ "OPTIONAL {?profile profile:serviceProduct ?sproduct .}\n"
			+ "}";
//		System.out.println("construct: " + construct);
		return construct;		
	}

}
