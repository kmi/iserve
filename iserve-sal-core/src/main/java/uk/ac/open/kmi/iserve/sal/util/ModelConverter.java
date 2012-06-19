package uk.ac.open.kmi.iserve.sal.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.Variable;
import org.ontoware.rdf2go.vocabulary.RDFS;

import uk.ac.open.kmi.iserve.commons.vocabulary.DC;
import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.sal.model.common.URI;
import uk.ac.open.kmi.iserve.sal.model.impl.MessageContentImpl;
import uk.ac.open.kmi.iserve.sal.model.impl.MessagePartImpl;
import uk.ac.open.kmi.iserve.sal.model.impl.ModelReferenceImpl;
import uk.ac.open.kmi.iserve.sal.model.impl.OperationImpl;
import uk.ac.open.kmi.iserve.sal.model.impl.QueryResultImpl;
import uk.ac.open.kmi.iserve.sal.model.impl.QueryRowImpl;
import uk.ac.open.kmi.iserve.sal.model.impl.ServiceImpl;
import uk.ac.open.kmi.iserve.sal.model.impl.URIImpl;
import uk.ac.open.kmi.iserve.sal.model.query.QueryResult;
import uk.ac.open.kmi.iserve.sal.model.query.QueryRow;
import uk.ac.open.kmi.iserve.sal.model.service.MessageContent;
import uk.ac.open.kmi.iserve.sal.model.service.MessagePart;
import uk.ac.open.kmi.iserve.sal.model.service.ModelReference;
import uk.ac.open.kmi.iserve.sal.model.service.Operation;
import uk.ac.open.kmi.iserve.sal.model.service.Service;

public class ModelConverter {

	public static Service coverterService(URI serviceUri, Model model) {
		Service result = new ServiceImpl(serviceUri);
		org.ontoware.rdf2go.model.node.URI uri = convertUri(serviceUri);
		result.setSources(getUriValues(uri, DC.source, model));
		result.setSeeAlso(getUriValues(uri, RDFS.seeAlso, model));
		result.setLabels(getStringValues(uri, RDFS.label, model));
		result.setModelReferences(getModelReferenceValues(uri, model));
		result.setOperations(getOperations(uri, model));
		return result;
	}

	private static List<Operation> getOperations(org.ontoware.rdf2go.model.node.URI serviceUri, Model model) {
		List<Operation> result = new ArrayList<Operation>();
		List<URI> operationUriList = getUriValues(serviceUri, MSM.hasOperation, model);
		for ( URI operationUri : operationUriList ) {
			Operation operation = new OperationImpl(operationUri);
			org.ontoware.rdf2go.model.node.URI uri = convertUri(operationUri);
			operation.setAddresses(getUriValues(uri, MSM.hasAddress, model));
			operation.setLabels(getStringValues(uri, RDFS.label, model));
			operation.setInputs(getMessages(uri, MSM.hasInput, model));
			operation.setOutputs(getMessages(uri, MSM.hasOutput, model));
			operation.setInputFaults(getMessages(uri, MSM.hasInputFault, model));
			operation.setOutputFaults(getMessages(uri, MSM.hasOutputFault, model));
			operation.setModelReferences(getModelReferenceValues(uri, model));
			result.add(operation);
		}
		return result;
	}

	private static List<MessageContent> getMessages(org.ontoware.rdf2go.model.node.URI operationUri,
			org.ontoware.rdf2go.model.node.URI messageType, Model model) {
		List<MessageContent> result = new ArrayList<MessageContent>();
		List<URI> messageUriList = getUriValues(operationUri, messageType, model);
		for ( URI messageUri : messageUriList ) {
			MessageContent message = new MessageContentImpl(messageUri);
			org.ontoware.rdf2go.model.node.URI uri = convertUri(messageUri);
			message.setLoweringSchemaMappings(getUriValues(uri, MSM.loweringSchemaMapping, model));
			message.setLiftingSchemaMappings(getUriValues(uri, MSM.liftingSchemaMapping, model));
			message.setLabels(getStringValues(uri, RDFS.label, model));
			message.setModelReferences(getModelReferenceValues(uri, model));
			message.setParts(getMessageParts(uri, model));
			result.add(message);
		}
		return result;
	}

	private static List<MessagePart> getMessageParts(org.ontoware.rdf2go.model.node.URI messageUri, Model model) {
		List<MessagePart> result = new ArrayList<MessagePart>();
		List<URI> messagePartUriList = getUriValues(messageUri, MSM.hasPart, model);
		for ( URI messagePartUri : messagePartUriList ) {
			MessagePart messagePart = new MessagePartImpl(messagePartUri);
			org.ontoware.rdf2go.model.node.URI uri = convertUri(messagePartUri);
			messagePart.setLabels(getStringValues(uri, RDFS.label, model));
			messagePart.setModelReferences(getModelReferenceValues(uri, model));
			result.add(messagePart);
		}
		return result;
	}

	private static List<URI> getUriValues(org.ontoware.rdf2go.model.node.URI subjectUri, org.ontoware.rdf2go.model.node.URI propertyUri, Model model) {
		List<URI> result = new ArrayList<URI>();
		ClosableIterator<Statement> stmts = model.findStatements(subjectUri, propertyUri, Variable.ANY);
		if ( stmts != null ) {
			while ( stmts.hasNext() ) {
				Statement stmt = stmts.next();
				result.add(new URIImpl(stmt.getObject().toString()));
			}
			stmts.close();
			stmts = null;
		}
		return result;
	}

	private static List<String> getStringValues(org.ontoware.rdf2go.model.node.URI subjectUri, org.ontoware.rdf2go.model.node.URI propertyUri, Model model) {
		List<String> result = new ArrayList<String>();
		ClosableIterator<Statement> stmts = model.findStatements(subjectUri, propertyUri, Variable.ANY);
		if ( stmts != null ) {
			while ( stmts.hasNext() ) {
				Statement stmt = stmts.next();
				Node node = stmt.getObject();
				result.add(node.asLiteral().getValue());
			}
			stmts.close();
			stmts = null;
		}
		return result;
	}

	private static List<ModelReference> getModelReferenceValues(org.ontoware.rdf2go.model.node.URI subjectUri, Model model) {
		List<ModelReference> result = new ArrayList<ModelReference>();
		ClosableIterator<Statement> stmts = model.findStatements(subjectUri, MSM.modelReference, Variable.ANY);
		if ( stmts != null ) {
			while ( stmts.hasNext() ) {
				Statement stmt = stmts.next();
				ModelReferenceImpl modelref = new ModelReferenceImpl(new URIImpl(stmt.getObject().asURI().toString()));
				result.add(modelref);
			}
			stmts.close();
			stmts = null;
		}
		return result;
	}

	private static org.ontoware.rdf2go.model.node.URI convertUri(URI uri) {
		org.ontoware.rdf2go.model.node.URI result = new org.ontoware.rdf2go.model.node.impl.URIImpl(uri.toString());
		return result;
	}

	public static QueryResult convertQueryResultTable(QueryResultTable qrt) {
		if ( null == qrt )
			return null;

		QueryResult result = new QueryResultImpl();
		List<String> vars = new ArrayList<String>();
		for ( String var : qrt.getVariables() ) {
			vars.add(var);
		}
		result.setVariables(vars);
		List<QueryRow> queryRows = new ArrayList<QueryRow>();
		for ( org.ontoware.rdf2go.model.QueryRow row : qrt ) {
			queryRows.add(converQureyRow(vars, row));
		}
		result.setQueryRows(queryRows);
		return result;
	}

	private static QueryRow converQureyRow(List<String> vars, org.ontoware.rdf2go.model.QueryRow qr) {
		if ( qr == null )
			return null;
		QueryRow result = new QueryRowImpl();
		Map<String, String> rowData = new HashMap<String, String>();
		Map<String, String> rowDatatype = new HashMap<String, String>();
		for ( String var : vars ) {
			Node node = qr.getValue(var);
			String value = "";
			if ( node instanceof org.ontoware.rdf2go.model.node.URI ) {
				rowDatatype.put(var, "uri");
				value = node.asURI().toString();
			} else if ( node instanceof org.ontoware.rdf2go.model.node.DatatypeLiteral ) {
				rowDatatype.put(var, "datatype:" + node.asDatatypeLiteral().getDatatype().toString());
				value = node.asDatatypeLiteral().getValue();
			} else if ( node instanceof org.ontoware.rdf2go.model.node.LanguageTagLiteral ) {
				rowDatatype.put(var, "lang:" + node.asLanguageTagLiteral().getLanguageTag());
				value = node.asLanguageTagLiteral().getValue();
			} else if ( node instanceof org.ontoware.rdf2go.model.node.BlankNode ) {
				rowDatatype.put(var, "blank");
				value = node.toString();
			} else {
				rowDatatype.put(var, "literal");
				if ( node != null ) {
					value = node.toString();
				} else {
					value = "";
				}
			}
			rowData.put(var, value);
		}
		result.setQueryRow(rowDatatype, rowData);
		return result;
	}

}
