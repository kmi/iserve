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
package uk.ac.open.kmi.iserve2.importer.rdf;

import java.io.IOException;
import java.io.StringReader;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.Syntax;
import org.ontoware.rdf2go.model.node.Variable;
import org.ontoware.rdf2go.vocabulary.RDF;

import uk.ac.open.kmi.iserve2.commons.io.URIUtil;
import uk.ac.open.kmi.iserve2.commons.vocabulary.MSM;

public class RdfTransformer {

	private static String getBaseUri(String serviceUriString) {
		String baseUri = "";
		String localName = URIUtil.getLocalName(serviceUriString);
		baseUri = serviceUriString.substring(0, serviceUriString.length() - localName.length());// - 1);
		return baseUri;
	}

	public static String replaceBaseUri(String rdf, String serviceUri, String newBaseUri) {
		String oldBaseUri = getBaseUri(serviceUri);
//		return rdf.replaceAll(oldBaseUri, "");

		// FIXME: only replace NS of Service, Operation and Message
		// read line, lines contain "<rdf:Description", "hasInput", "hasOutput", "hasInputFault" and "hasOutputFault"
		String[] lines = rdf.split("\n");
		String newRdf = "";
		for ( String line : lines) {
			if ( line.contains("xml:base") ||
					line.contains("rdf:Description") ||
					line.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#Description") ||
					line.contains("msm:Service") ||
					line.contains("Service xmlns=\"http://cms-wg.sti2.org/ns/minimal-service-model#\"") ||
					line.contains("msm:Operation") ||
					line.contains("Operation xmlns=\"http://cms-wg.sti2.org/ns/minimal-service-model#\"") ||
					line.contains("msm:Message") ||
					line.contains("MessageContent xmlns=\"http://cms-wg.sti2.org/ns/minimal-service-model#\"") ||
					line.contains("MessagePart xmlns=\"http://cms-wg.sti2.org/ns/minimal-service-model#\"") ||
					line.contains("hasOperation") ||
					line.contains("hasInput") ||
					line.contains("hasOutput") ||
					line.contains("hasInputFault") ||
					line.contains("hasOutputFault") ||
					line.contains("hasPart") ||
					line.contains("hasOptionalPart") ||
					line.contains("hasMandatoryPart") ) {
				newRdf += line.replaceAll(oldBaseUri, newBaseUri) + "\n";
			} else {
				newRdf += line + "\n";
			}
		}
		return newRdf;
	}

	public static String getServiceUri(String rdf) throws ModelRuntimeException, IOException {
		String serviceUriString = "";
		StringReader stringReader = new StringReader(rdf);
		Model model = RDF2Go.getModelFactory().createModel();
		model.open();
		model.readFrom(stringReader, Syntax.RdfXml);
		ClosableIterator<org.ontoware.rdf2go.model.Statement> iter = model.findStatements(Variable.ANY, RDF.type, MSM.Service);
		if ( iter.hasNext() ) {
			Statement stmt = iter.next();
			serviceUriString = stmt.getSubject().toString();
		}
		iter.close();
		model.close();
		return serviceUriString;
	}

}
