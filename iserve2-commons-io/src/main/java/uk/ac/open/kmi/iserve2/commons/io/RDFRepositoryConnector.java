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
package uk.ac.open.kmi.iserve2.commons.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.ontoware.rdf2go.model.node.impl.URIImpl;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.rdf2go.RepositoryModel;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;

public class RDFRepositoryConnector {

	private Repository sesameRepository;

	public RDFRepositoryConnector(String repoServerUrl, String repoName) throws RepositoryException {
		sesameRepository  = new HTTPRepository(repoServerUrl, repoName);
		sesameRepository.initialize();
	}

	public RepositoryModel openRepositoryModel() {
		if ( null == sesameRepository )
			return null;
		RepositoryModel result = new RepositoryModel(sesameRepository);
		if ( result != null ) {
			result.open();
		}
		return result;
	}

	public RepositoryModel openRepositoryModel(String context) {
		if ( null == context || context.equalsIgnoreCase("") || null == sesameRepository )
			return null;
		RepositoryModel result = new RepositoryModel(new URIImpl(context), sesameRepository);
		if ( result != null ) {
			result.open();
		}
		return result;
	}

	public void closeRepositoryModel(RepositoryModel modelToClose) {
		if ( modelToClose != null ) {
			modelToClose.close();
		}
	}

	public HTTPRepository getRepository() {
		return (HTTPRepository) sesameRepository;
	}

	public String query(String queryString) throws RepositoryException, MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, RDFHandlerException, IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		RepositoryConnection con = getRepository().getConnection();

		SPARQLParser parser = new SPARQLParser();
		ParsedQuery parsedQuery = parser.parseQuery(queryString, null);
		if ( parsedQuery instanceof ParsedTupleQuery ) {
			SPARQLResultsXMLWriter writer = new SPARQLResultsXMLWriter(bout);
			TupleQuery query = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
			query.evaluate(writer);
		} else if ( parsedQuery instanceof ParsedGraphQuery ) {
			RDFXMLPrettyWriter writer = new RDFXMLPrettyWriter(bout);
			GraphQuery query = con.prepareGraphQuery(QueryLanguage.SPARQL, queryString);
			query.evaluate(writer);
		} else if ( parsedQuery instanceof ParsedBooleanQuery ) {
			BooleanQuery query = con.prepareBooleanQuery(QueryLanguage.SPARQL, queryString);
			boolean result = query.evaluate();
			String resultString = BOOLEAN_RESULT_RESULT_PREFIX + result + BOOLEAN_RESULT_RESULT_SUFFIX;
			bout.write(resultString.getBytes());
		}
		con.close();
		return bout.toString();
	}

	private static String BOOLEAN_RESULT_RESULT_PREFIX = "<?xml version=\"1.0\"?>\n" +
		"<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\"\n" +
		"        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
		"        xsi:schemaLocation=\"http://www.w3.org/2001/sw/DataAccess/rf1/result2.xsd\">\n" +
		"  <boolean>\n";

	private static String BOOLEAN_RESULT_RESULT_SUFFIX = "</boolean>\n" + "</sparql>";

}
