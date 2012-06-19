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
package uk.ac.open.kmi.iserve.sal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.Syntax;
import org.ontoware.rdf2go.model.node.Variable;
import org.ontoware.rdf2go.vocabulary.RDF;
import org.openrdf.rdf2go.RepositoryModel;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.iserve.commons.io.FileUtil;
import uk.ac.open.kmi.iserve.commons.io.IOUtil;
import uk.ac.open.kmi.iserve.commons.io.RDFRepositoryConnector;
import uk.ac.open.kmi.iserve.commons.vocabulary.DC;
import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.sal.exception.ImporterException;

/**
 * Abstract class to be extended by Service Importers
 * 
 * @author Dong Liu (Knowledge Media Institute - The Open University)
 */
public interface ServiceImporter {

	/**
	 * Transform a service description, e.g. SAWSDL, OWL-S, hRESTS, etc., into
	 * Minimal Service Model (MSM).
	 * 
	 * @param serviceDescription
	 *        The semantic Web service description
	 * @return An input stream of the RDF describing the service and conforming to MSM model
	 */
	public abstract InputStream transformStream(String serviceDescription) throws ImporterException;



}
