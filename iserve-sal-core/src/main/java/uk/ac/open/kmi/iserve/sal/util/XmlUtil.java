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
package uk.ac.open.kmi.iserve.sal.util;

/*public class XmlUtil {

	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<Q1:Service xmlns:Q1=\"http://iserve.kmi.open.ac.uk/2011/01/ServiceInfo\"\n" +
		"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
		"	xsi:schemaLocation=\"http://iserve.kmi.open.ac.uk/2011/01/ServiceInfo http://iserve.kmi.open.ac.uk/2011/01/ServiceInfo.xsd\">\n";

	private static final String XML_FOOTER = "</Q1:Service>\n";

	public static String serializeService(Model model) throws ServiceException {
		if ( null == model || model.isEmpty() == true ) return null;
		if ( model.isOpen() == false ) {
			model.open();
		}
		StringBuffer sb = new StringBuffer();

		String serviceUri = findServiceUri(model);
		sb.append(XML_HEADER);

		serializeNameDescription(model, serviceUri, "Service", sb);

		sb.append("<Q1:ServiceUrl>" + serviceUri + "</Q1:ServiceUrl>\n");

		serializeOperation(model, serviceUri, sb);

		sb.append(XML_FOOTER);
		return sb.toString();
	}

	private static String findServiceUri(Model model) {
		ClosableIterator<Statement> stmts = model.findStatements(Variable.ANY, RDF.type, MSM.Service);
		if ( stmts == null )
			return null;
		if ( stmts.hasNext() ) {
			Statement stmt = stmts.next();
			stmts.close();
			stmts = null;
			return stmt.getSubject().asURI().toString();
		}
		return null;
	}

	private static void serializeOperation(Model model, String serviceUri, StringBuffer sb) throws ServiceException {
		ClosableIterator<Statement> iter = model.findStatements(new URIImpl(serviceUri), MSM.hasOperation, Variable.ANY);
		while ( iter.hasNext() ) {
			Statement stmt = iter.next();
			URI opUri = stmt.getObject().asURI();
			sb.append("<Q1:ServiceOperation>\n");
			serializeNameDescription(model, opUri.toString(), "Op", sb);
			sb.append("	<Q1:OpUrl>" + opUri.toString() + "</Q1:OpUrl>\n");
			serializeIO(model, opUri, sb);
			sb.append("</Q1:ServiceOperation>\n");
		}
		iter.close();
	}

	private static void serializeIO(Model model, URI opUri, StringBuffer sb) throws ServiceException {
		ClosableIterator<Statement> iter = model.findStatements(opUri, MSM.hasInput, Variable.ANY);
		while ( iter.hasNext() ) {
			Statement stmt = iter.next();
			String inputUri = stmt.getObject().asURI().toString();
			serializeMessageContent(model, inputUri, "Input", sb);
		}
		iter.close();

		iter = model.findStatements(opUri, MSM.hasOutput, Variable.ANY);
		while ( iter.hasNext() ) {
			Statement stmt = iter.next();
			String outputUri = stmt.getObject().asURI().toString();
			serializeMessageContent(model, outputUri, "Output", sb);
		}
		iter.close();
	}

	private static void serializeMessageContent(Model model, String msgContentUri, String ioType, StringBuffer sb) throws ServiceException {
		if ( hasModelReference(model, msgContentUri) == true ) {
			serializeIOTypes(model, msgContentUri, sb, ioType);
		} else {
			List<String> parts = getParts(model, msgContentUri);
			// if does not have parts or none of the sub-parts has a model reference
			if ( parts == null || parts.size() == 0 || partsHasModelReference(model, msgContentUri) == false ) {
				sb.append("	<Q1:Op" + ioType);
				serializeIONameDescription(model, msgContentUri, sb);
				sb.append("Type=\"" + XSD._string + "\" />\n");
			} else {
				for (String part : parts) {
					serializeMessageContent(model, part, ioType, sb);
				}
			}
		}
	}

	private static void serializeIONameDescription(Model model, String msgUri, StringBuffer sb) {
		String localName = URIUtil.getLocalName(msgUri);

		List<String> stringValues = getStringValues(model, msgUri, RDFS.label);
		if ( stringValues == null || stringValues.size() <= 0 ) {
			// use the local name as service name
			sb.append(" Name=\"" + localName + "\" ");
		} else {
			sb.append(" Name=\"" + stringValues.get(0) + "\" ");
		}

		stringValues = getStringValues(model, msgUri, RDFS.comment);
		if ( stringValues == null || stringValues.size() <= 0 ) {
			// use the local name as service description
			sb.append("Description=\"" + localName + "\" ");
		} else {
			sb.append("Description=\"" + stringValues.get(0) + "\" ");
		}
	}

	private static boolean partsHasModelReference(Model model, String msgContentUri) {
		return model.sparqlAsk("ASK {\n" +
				"<" + msgContentUri + "> " + MSM.hasPartTransitive.toSPARQL() + " ?part .\n" +
				" ?part " + MSM.modelReference.toSPARQL() + " ?mr .\n" +
				"}");
	}

	private static boolean hasModelReference(Model model, String msgUri) {
		return model.sparqlAsk("ASK { <" +
				msgUri + "> " + MSM.modelReference.toSPARQL() + " " + "?mr . " +
				"}");
	}

	private static List<String> getParts(Model model, String msgUri) {
		List<String> result = new ArrayList<String>();
		QueryResultTable qrt = model.sparqlSelect("SELECT ?p { <" +
				msgUri + "> " + MSM.hasPart.toSPARQL() + " ?p ." +
				"}");
		ClosableIterator<QueryRow> iter = qrt.iterator();
		while ( iter.hasNext() ) {
			QueryRow row = iter.next();
			result.add(row.getValue("p").toString());
		}
		iter.close();
		return result;
	}

	private static void serializeIOTypes(Model model, String msgUri, StringBuffer sb, String type) throws ServiceException {
		String queryString = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" +
			"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"PREFIX msm:<http://cms-wg.sti2.org/ns/minimal-service-model#>\n" +
			"PREFIX sawsdl:<http://www.w3.org/ns/sawsdl#>\n" +
			"SELECT DISTINCT ?msgmr WHERE {\n" +
			"	<" + msgUri + "> sawsdl:modelReference ?msgmr .\n " +
			"}\n";
		QueryResultTable qrt = model.sparqlSelect(queryString);
		ClosableIterator<QueryRow> iter = qrt.iterator();
		if ( iter == null )
			return;
		while ( iter.hasNext() ) {
			QueryRow row = iter.next();
			if ( row.getValue("msgmr") != null ) {
				String mrUri = row.getValue("msgmr").asURI().toString();
				try {
					ModelReferenceUtil.getInstance().inputModelReference(mrUri);
				} catch (Exception e) {
					throw new ServiceException("Errors in processing model reference: " + mrUri + ". " + e.getMessage());
				} finally {
					iter.close();
				}
				String label = ModelReferenceUtil.getInstance().getLabel(mrUri);
				if ( label != null ) {
					sb.append("	<Q1:Op" + type + " Name=\"" + label + "\" ");
				} else {
					sb.append("	<Q1:Op" + type + " Name=\"" + URIUtil.getLocalName(mrUri) + "\" ");
				}
				String comment = ModelReferenceUtil.getInstance().getComment(mrUri);
				if ( comment != null ) {
					sb.append("Description=\"" + comment.trim().replaceAll("\n", " ") + "\" ");
				} else {
					sb.append("Description=\"" + URIUtil.getLocalName(mrUri) + "\" ");
				}
				String xsdType = ModelReferenceUtil.getInstance().getDatatype(mrUri);
				sb.append("Type=\"" + xsdType + "\" />\n");
			}
		}
		iter.close();
	}

	private static void serializeNameDescription(Model model, String serviceUri, String type, StringBuffer sb) {
		String localName = URIUtil.getLocalName(serviceUri);

		List<String> stringValues = getStringValues(model, serviceUri, RDFS.label);
		if ( type.equalsIgnoreCase("Op") ) {
			sb.append("	");
		}
		if ( stringValues == null || stringValues.size() <= 0 ) {
			// use the local name as service name
			sb.append("<Q1:" + type + "Name>" + localName + "</Q1:" + type + "Name>\n");
		} else {
			sb.append("<Q1:" + type + "Name>" + stringValues.get(0) + "</Q1:" + type + "Name>\n");
		}

		stringValues = getStringValues(model, serviceUri, RDFS.comment);
		if ( type.equalsIgnoreCase("Op") ) {
			sb.append("	");
		}
		if ( stringValues == null || stringValues.size() <= 0 ) {
			// use the local name as service description
			sb.append("<Q1:" + type + "Description>" + localName + "</Q1:" + type + "Description>\n");
		} else {
			sb.append("<Q1:" + type + "Description>" + stringValues.get(0) + "</Q1:" + type + "Description>\n");
		}
	}

	private static List<String> getStringValues(Model model, String serviceUri, URI predicate) {
		ClosableIterator<Statement> iter = model.findStatements(new URIImpl(serviceUri), predicate, Variable.ANY);
		if ( iter == null ) {
			return null;
		}
		List<String> result = new ArrayList<String>();
		while ( iter.hasNext() ) {
			Statement stmt = iter.next();
			String value = stmt.getObject().asLiteral().getValue();
			result.add(value);
		}
		iter.close();
		return result;
	}

//	public static void main(String[] args) throws IOException, RepositoryException, TransformerConfigurationException, ParserConfigurationException, ServiceException, WSDLException {
//		Properties prop = IOUtil.readProperties(new File("/Users/dl3962/Workspace/JWS/iserve-parent/iserve-sal-rest/src/main/webapp/WEB-INF/config.properties"));
//		prop.setProperty(SalConfig.XSLT_PATH, "/Users/dl3962/Workspace/JWS/iserve-webapp/src/main/webapp/servicebrowser/im/hrests.xslt");
//		SalConfig config = new SalConfig(prop);
//		ServiceManager serviceManager = new ServiceManagerRdf(config);
//		RepositoryModel model = serviceManager.getServiceAsModel("http://service-repository.kmi.open.ac.uk:8080/iserve/resource/services/0f59a383-6324-4fce-865a-506517c801c1#NestoriaService");
////		RepositoryModel model = serviceManager.getServiceAsModel("http://service-repository.kmi.open.ac.uk:8080/iserve/resource/services/7c985dba-143b-43e3-b081-a45a8c87f09e#PoliceAPIService");
//		String xml = serializeService(model);
//		System.out.println(xml);
//	}

}
*/