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
package uk.ac.open.kmi.iserve2.imatcher;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.URI;
import org.openrdf.rdf2go.RepositoryModel;

import simpack.measure.weightingscheme.StringTFIDF;
import uk.ac.open.kmi.iserve2.commons.io.RDFRepositoryConnector;
import uk.ac.open.kmi.iserve2.imatcher.strategy.api.IStrategy;
import uk.ac.open.kmi.iserve2.imatcher.tool.MITPHv1CorpusForIServe;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.wcohen.ss.api.StringWrapper;

public class IServeIMatcher {
	
	private static IServeIMatcher INSTANCE = null;

	protected Model model = ModelFactory.createDefaultModel();

	public StringTFIDF TFIDF;

	private IServeIMatcher() {

	}

	public static IServeIMatcher getInstance() {
		if ( INSTANCE == null ) {
			INSTANCE = new IServeIMatcher();
		}
		return INSTANCE;
	}

	public void init(RDFRepositoryConnector rdfRepositoryConnector) {
		RepositoryModel iServeModel = rdfRepositoryConnector.openRepositoryModel();
		if ( iServeModel == null || iServeModel.iterator() == null ) {
			return;
		}
		ClosableIterator<Statement> iter = iServeModel.iterator();
		while ( iter.hasNext() ) {
			Statement stmt = iter.next();
			com.hp.hpl.jena.rdf.model.Statement jenaStmt = convertStatement(stmt);
			if ( jenaStmt != null ) {
				this.model.add(jenaStmt);
			}
		}
		iter.close();
		rdfRepositoryConnector.closeRepositoryModel(iServeModel);

		buildCorpus();
	}

	private void buildCorpus() {
		// build corpus
		ArrayList<StringWrapper> corpus = MITPHv1CorpusForIServe.run(model);
		TFIDF = new StringTFIDF(corpus);
		TFIDF.calculate();
		corpus.clear();
		corpus = null;
	}

	public void addRdf(String rdfString, String baseUri) {
		StringReader stringReader = new StringReader(rdfString);
		model.read(stringReader, baseUri);
		buildCorpus();
//		model.write(System.out);
	}

	public void addStatement(Statement stmt) {
		model.add(convertStatement(stmt));
	}

	public void removeStatement(Statement stmt) {
		model.remove(convertStatement(stmt));
	}

	public ResultSet query(IStrategy strategy, List<String> parameters) {
		String queryString = strategy.getStrategy(parameters);
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet rs = ResultSetFactory.makeRewindable(qexec.execSelect());
		qexec.close();
		return rs;
	}

	private com.hp.hpl.jena.rdf.model.Statement convertStatement(Statement stmt) {
		com.hp.hpl.jena.rdf.model.Statement result = null;
		org.ontoware.rdf2go.model.node.Resource sub = stmt.getSubject();
		Resource jenaSub = model.createResource(sub.toString());
		URI pre = stmt.getPredicate();
		Property jenaPre = model.createProperty(pre.toString());
		if ( stmt.getObject() instanceof org.ontoware.rdf2go.model.node.impl.DatatypeLiteralImpl || 
				stmt.getObject() instanceof org.ontoware.rdf2go.model.node.impl.PlainLiteralImpl) {
			result = model.createStatement(jenaSub, jenaPre, stmt.getObject().asLiteral().getValue());
//			IOHelper.printToFile(stmt.getObject().asLiteral().getValue().toString());
		} else if ( stmt.getObject() instanceof org.ontoware.rdf2go.model.node.impl.URIImpl ) {
			URI objUri = stmt.getObject().asURI();
			Resource jenaObj = model.createResource(objUri.toString());
			result = model.createStatement(jenaSub, jenaPre, jenaObj);
		} else if ( stmt.getObject() instanceof org.ontoware.rdf2go.model.node.impl.LanguageTagLiteralImpl ) {
			result = model.createStatement(jenaSub, jenaPre, stmt.getObject().asLiteral().getValue());
//			IOHelper.printToFile(stmt.getObject().asLiteral().getValue().toString());
		} else {
//			IOHelper.printToFile(stmt.getObject().getClass().toString());
		}
		return result;
	}

}
