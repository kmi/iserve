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
package uk.ac.open.kmi.iserve.importer;

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

public abstract class ServiceImporter {

	private ImporterConfig config;

	private RDFRepositoryConnector connector;

	public ServiceImporter(ImporterConfig config) throws RepositoryException {
		this.config = config;
		this.connector = new RDFRepositoryConnector(config.getRepoServerUrl(), config.getRepoName());
	}

	public String importService(String fileName, String serviceDescription, String sourceUri) throws ImporterException {
		String uuid = generateUniqueId();
		return importService(fileName, serviceDescription, sourceUri, uuid);
	}

	public String importService(String fileName, String serviceDescription, String sourceUri, String uuid) throws ImporterException {
		if ( null == fileName || fileName.equalsIgnoreCase("") ) {
			throw new ImporterException("File name of service description is null");
		}

		if ( null == serviceDescription || serviceDescription.equalsIgnoreCase("")  ) {
			throw new ImporterException("Content of service description is null");
		}

		String documentBaseURI = config.getUriPrefix() + MSM.DOCUMENT_INFIX + uuid + "/";
		String serviceBaseURI = config.getUriPrefix() + MSM.SERVICE_INFIX + uuid;
		String newServiceUri = null;

		InputStream msmServiceDescriptionStream = transformStream(serviceDescription);
		if ( null == msmServiceDescriptionStream ) {
			throw new ImporterException("Fail to be transformed into MSM, and the result is null");
		}

		// save RDF to OWLim
		RepositoryModel model = connector.openRepositoryModel(documentBaseURI + fileName);
		try {
			model.readFrom(msmServiceDescriptionStream, Syntax.RdfXml, serviceBaseURI);
			ClosableIterator<Statement> serviceURIs = model.findStatements(Variable.ANY, RDF.type, MSM.Service);
			if ( null == serviceURIs ) {
				throw new ImporterException("Errors in transformation results, because MSM service cannot be found");
			}
			Statement stmt = serviceURIs.next();
			if ( null == stmt || null == stmt.getSubject() ) {
				throw new ImporterException("Errors in transformation results, because MSM service cannot be found");
			}
			serviceURIs.close();
			model.addStatement(stmt.getSubject(), DC.source, model.createURI(documentBaseURI + fileName));
			if ( sourceUri != null && sourceUri.equalsIgnoreCase("") == false ) {
				model.addStatement(stmt.getSubject(), DC.source, model.createURI(sourceUri));
			}
			newServiceUri = stmt.getSubject().toString();
		} catch (ModelRuntimeException e) {
			throw new ImporterException(e);
		} catch (IOException e) {
			throw new ImporterException(e);
		} finally {
			connector.closeRepositoryModel(model);
		}

		// save file to disk
		try {
			FileUtil.createDirIfNotExists(new File(config.getDocFolderPath() + uuid + "/" ));
			IOUtil.writeString(serviceDescription, new File(config.getDocFolderPath() + uuid + "/" + fileName));
		} catch (IOException e) {
			model.open();
			// FIXME: May remove ALL the statement in the repository, when using early version of Swift OWLim!
			model.removeAll();
			connector.closeRepositoryModel(model);
			throw new ImporterException(e);
		}

		return newServiceUri;
	}

	/**
	 * Transform a service description, e.g. SAWSDL, OWL-S, hRESTS, etc., into
	 * Minimal Service Model (MSM).
	 * 
	 * @param serviceDescription
	 *        The semantic Web service description
	 * @return An input stream of the RDF describing the service and conforming to MSM model
	 */
	protected abstract InputStream transformStream(String serviceDescription) throws ImporterException;

	private String generateUniqueId() {
		// FIXME: UUID may be too long in this case.
		String uid = UUID.randomUUID().toString();
		return uid;
	}

}
