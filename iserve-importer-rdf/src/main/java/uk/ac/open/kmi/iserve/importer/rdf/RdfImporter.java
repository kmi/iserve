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
package uk.ac.open.kmi.iserve.importer.rdf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.Syntax;
import org.openrdf.repository.RepositoryException;
import org.w3c.dom.Document;

import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.importer.ImporterConfig;
import uk.ac.open.kmi.iserve.sal.ServiceImporter;
import uk.ac.open.kmi.iserve.sal.exception.ImporterException;

public class RdfImporter implements ServiceImporter {

	private static final String TEMP_NS = "http://owls-transformer.baseuri/2878757605265003094#";

	private static final String PART_QUERY_STRING = "CONSTRUCT {\n" +
		"  ?m " + MSM.hasPartTransitive.toSPARQL() + " ?p .\n" +
		"} WHERE {\n" +
		"  ?m " + MSM.hasPart.toSPARQL() + " ?p .\n" +
		"}";

	private static final String OPTIONAL_PART_QUERY_STRING = "CONSTRUCT {\n" +
		"  ?m " + MSM.hasPartTransitive.toSPARQL() + " ?p .\n" +
		"} WHERE {\n" +
		"  ?m " + MSM.hasOptionalPart.toSPARQL() + " ?p .\n" +
		"}";

	private static final String MANDATORY_PART_QUERY_STRING = "CONSTRUCT {\n" +
		"  ?m " + MSM.hasPartTransitive.toSPARQL() + " ?p .\n" +
		"} WHERE {\n" +
		"  ?m " + MSM.hasMandatoryPart.toSPARQL() + " ?p .\n" +
		"}";

	private Model tempModel;

	public RdfImporter() throws RepositoryException {
		tempModel = RDF2Go.getModelFactory().createModel();
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.ServiceImporter#transformStream(java.io.InputStream)
	 */
	@Override
	public InputStream transformStream(InputStream originalDescription) throws ImporterException {
		try {
			String serviceUriInRdf = RdfTransformer.getServiceUri(originalDescription);
			String rdfToStore = RdfTransformer.replaceBaseUri(originalDescription, serviceUriInRdf, "#");
			// add hasPartTransitive
			rdfToStore = addHasPartTransitive(rdfToStore);
			return new ByteArrayInputStream(rdfToStore.getBytes());
		} catch (ModelRuntimeException e) {
			throw new ImporterException(e);
		} catch (IOException e) {
			throw new ImporterException(e);
		}
	}

	private String addHasPartTransitive(String rdfToStore) throws ModelRuntimeException, IOException {
		tempModel.open();
		ByteArrayInputStream is = new ByteArrayInputStream(rdfToStore.getBytes());
		tempModel.readFrom(is, Syntax.RdfXml, TEMP_NS);
		is.close();

		addConstructResult(PART_QUERY_STRING);
		addConstructResult(OPTIONAL_PART_QUERY_STRING);
		addConstructResult(MANDATORY_PART_QUERY_STRING);

		rdfToStore = tempModel.serialize(Syntax.RdfXml);
		rdfToStore = rdfToStore.replaceAll(TEMP_NS, "#");
		tempModel.removeAll();
		tempModel.close();
		return rdfToStore;
	}

	private void addConstructResult(String queryString) {
		ClosableIterable<Statement> stmts = tempModel.sparqlConstruct(queryString);
		if ( stmts != null ) {
			ClosableIterator<Statement> iter = stmts.iterator();
			if ( iter != null ) {
//				tempModel.addAll(iter);
				while( iter.hasNext() ) {
					Statement stmt = iter.next();
					tempModel.addStatement(stmt);
				}
				iter.close();
			}
		}
	}

}
