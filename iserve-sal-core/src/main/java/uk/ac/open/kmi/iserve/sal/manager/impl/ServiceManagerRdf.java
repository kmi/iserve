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
package uk.ac.open.kmi.iserve.sal.manager.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.Syntax;
import org.ontoware.rdf2go.model.node.Variable;
import org.ontoware.rdf2go.vocabulary.RDF;
import org.openrdf.rdf2go.RepositoryModel;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.iserve.commons.io.FileUtil;
import uk.ac.open.kmi.iserve.commons.io.IOUtil;
import uk.ac.open.kmi.iserve.commons.io.RDFRepositoryConnector;
import uk.ac.open.kmi.iserve.commons.io.StringUtil;
import uk.ac.open.kmi.iserve.commons.vocabulary.DC;
import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.imatcher.IServeIMatcher;
import uk.ac.open.kmi.iserve.sal.ServiceFormat;
import uk.ac.open.kmi.iserve.sal.ServiceFormatDetector;
import uk.ac.open.kmi.iserve.sal.ServiceImporter;
import uk.ac.open.kmi.iserve.sal.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.exception.ImporterException;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;
import uk.ac.open.kmi.iserve.sal.model.common.URI;
import uk.ac.open.kmi.iserve.sal.model.service.Service;
import uk.ac.open.kmi.iserve.sal.util.ModelConverter;

public class ServiceManagerRdf implements ServiceManager {
	
	private ServiceFormatDetector formatDetector;

	private RDFRepositoryConnector repoConnector;

	private SystemConfiguration configuration;

	/**
	 * Constructor for the Service Manager. Protected to avoid external access.
	 * Any access to this should take place through the iServeManager
	 * 
	 * @param configuration
	 * @throws RepositoryException
	 * @throws TransformerConfigurationException
	 * @throws WSDLException
	 * @throws ParserConfigurationException
	 */
	protected ServiceManagerRdf(SystemConfiguration configuration) throws RepositoryException {
		this.configuration = configuration;
		repoConnector = new RDFRepositoryConnector(configuration.getRepoServerUrl(), configuration.getRepoName());		
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#listService()
	 */
	@Override
	public List<String> listService() {
		List<String> result = new ArrayList<String>();
		String queryString = "select DISTINCT ?s where { \n" +
			"?s " + RDF.type.toSPARQL() + " " + MSM.Service.toSPARQL() +
			" . }";

		RepositoryModel model = repoConnector.openRepositoryModel();
		QueryResultTable qrt = model.sparqlSelect(queryString);
		if ( qrt != null ) {
			ClosableIterator<org.ontoware.rdf2go.model.QueryRow> iter = qrt.iterator();
			if ( iter != null ) {
				while ( iter.hasNext() ) {
					org.ontoware.rdf2go.model.QueryRow qr = iter.next();
					String valueString = qr.getValue("s").toString();
					result.add(valueString);
				}
				iter.close();
				iter = null;
			}
		}
		repoConnector.closeRepositoryModel(model);
		model = null;
		return result;
	}

	
	/**
	 * Generate a unique identifier for a new service to be stored.
	 * 
	 * @return
	 */
	private String generateUniqueId() {
		// FIXME: UUID may be too long in this case.
		String uid = UUID.randomUUID().toString();
		return uid;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#addService(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String addService(String fileName, String serviceDescription,
			String sourceUri) throws ServiceException {
		// TODO: We are assuming here a concrete charset 
		try {
			return addService(fileName, new ByteArrayInputStream(serviceDescription.getBytes("UTF-8")), sourceUri);
		} catch (UnsupportedEncodingException e) {
			throw new ServiceException("Unsupported encoding for the service", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#addService(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String addService(String fileName, InputStream msmServiceDescriptionStream, String sourceUri) throws ServiceException {

		if (msmServiceDescriptionStream == null || !isValid(msmServiceDescriptionStream)) {
			throw new ServiceException("The service is not valid MSM.");
		}
		
		String uuid = generateUniqueId();
		String documentBaseURI = configuration.getUriPrefix() + MSM.DOCUMENT_INFIX + uuid + "/";
		String serviceBaseURI = configuration.getUriPrefix() + MSM.SERVICE_INFIX + uuid;
		String newServiceUri = null;
		
		// save RDF to OWLim
		RepositoryModel model = repoConnector.openRepositoryModel(documentBaseURI + fileName);
		try {
			model.readFrom(msmServiceDescriptionStream, Syntax.RdfXml, serviceBaseURI);
			ClosableIterator<Statement> serviceURIs = model.findStatements(Variable.ANY, RDF.type, MSM.Service);
			if ( null == serviceURIs ) {
				throw new ServiceException("Errors in transformation results: msm:Service cannot be found");
			}
			Statement stmt = serviceURIs.next();
			if ( null == stmt || null == stmt.getSubject() ) {
				throw new ServiceException("Errors in transformation results: msm:Service cannot be found");
			}
			serviceURIs.close();
			
			// Only store the document and include the dc:source if no original
			// source for the document is given
			if ( sourceUri != null && sourceUri.equalsIgnoreCase("") ) {
				model.addStatement(stmt.getSubject(), DC.source, model.createURI(sourceUri));
			} else {
				model.addStatement(stmt.getSubject(), DC.source, model.createURI(documentBaseURI + fileName));
			}
			newServiceUri = stmt.getSubject().toString();
		} catch (ModelRuntimeException e) {
			throw new ServiceException(e);
		} catch (IOException e) {
			throw new ServiceException(e);
		} finally {
			repoConnector.closeRepositoryModel(model);
		}

		//TODO: This should be done directly using the Document Manager from
		// iServe Manager 
		if ( sourceUri == null || sourceUri.equalsIgnoreCase("") ) {
			// save file to disk
			try {
				FileUtil.createDirIfNotExists(new File(configuration.getDocFolderPath() + uuid + "/" ));
				IOUtil.writeString(msmServiceDescriptionStream.toString(), new File(configuration.getDocFolderPath() + uuid + "/" + fileName));
			} catch (IOException e) {
				model.open();
				// FIXME: May remove ALL the statement in the repository, when using early version of Swift OWLim!
				model.removeAll();
				repoConnector.closeRepositoryModel(model);
				throw new ServiceException(e);
			}
		}		

		// add to iServe iMatcher
		// Replace with an observer mechanism so that anybody can register 
		// push this up to iServeManager though
//		Model model = getServiceAsModel(serviceUri);
//		model.open();
//		String serviceInRdf = model.serialize(Syntax.RdfXml);
//		IServeIMatcher.getInstance().addRdf(serviceInRdf, serviceUri);
//		model.close();
//		model = null;

		return newServiceUri;
	}

	/**
	 * @param msmServiceDescriptionStream
	 * @return
	 */
	private boolean isValid(InputStream msmServiceDescriptionStream) {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#deleteService(java.lang.String)
	 */
	@Override
	public String deleteService(String serviceUri) throws ServiceException {
		String contextUri = getContextUri(serviceUri);
		if ( null == contextUri ) {
			throw new ServiceException("Cannot find service identified by " + serviceUri);
		}

		// delete from hard disk
		// FIXME: This should probably take place via the Document Manager
		// TODO: Dont use the configuration directly
		String prefix = configuration.getUriPrefix() + MSM.DOCUMENT_INFIX;
		String relativePath = StringUtil.subStrings(contextUri, prefix); 
		String filePath = configuration.getDocFolderPath() + relativePath;

		File file = new File(filePath);
		File docFolder = new File(file.getParent());
		FileUtil.deltree(docFolder);

		// delete from OWLim
		RepositoryModel model = repoConnector.openRepositoryModel(contextUri);
		ClosableIterator<Statement> iter = model.iterator();
		while ( iter.hasNext() ) {
			Statement stmt = iter.next();
			model.removeStatement(stmt);
			// Remove from iServe iMatcher
			IServeIMatcher.getInstance().removeStatement(stmt);
		}
		iter.close();
		iter = null;
		repoConnector.closeRepositoryModel(model);
		model = null;
		return serviceUri;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#deleteServiceById(java.lang.String)
	 */
	@Override
	public String deleteServiceById(String serviceId) throws ServiceException {
		return deleteService(getServiceUri(serviceId));
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#getService(java.lang.String, org.ontoware.rdf2go.model.Syntax)
	 */
	@Override
	public String getService(String serviceUri, Syntax syntax) throws ServiceException {
		String contextUri = getContextUri(serviceUri);
		if ( null == contextUri ) {
			throw new ServiceException("Cannot find service identified by " + serviceUri);
		}
		RepositoryModel model = repoConnector.openRepositoryModel(contextUri);
		String result = model.serialize(syntax);
		repoConnector.closeRepositoryModel(model);
		model = null;
		return result;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#getServiceAsModel(java.lang.String)
	 */
	@Override
	public Model getServiceAsModel(String serviceUri) throws ServiceException {
		String contextUri = getContextUri(serviceUri);
		if ( null == contextUri ) {
			throw new ServiceException("Cannot find service identified by " + serviceUri);
		}
		RepositoryModel model = repoConnector.openRepositoryModel(contextUri);
		return model;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#getServiceAsModelById(java.lang.String)
	 */
	@Override
	public Model getServiceAsModelById(String serviceId) throws ServiceException {
		String contextUri = getContextUriById(serviceId);
		if ( null == contextUri ) {
			throw new ServiceException("Cannot find service identified by " + serviceId);
		}
		RepositoryModel model = repoConnector.openRepositoryModel(contextUri);
		return model;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#getService(uk.ac.open.kmi.iserve.sal.model.common.URI)
	 */
	@Override
	public Service getService(URI serviceUri) throws ServiceException {
		Service result = null;
		Model model = getServiceAsModel(serviceUri.toString());
		result = ModelConverter.coverterService(serviceUri, model);
		model.close();
		model = null;
		return result;
	}

	private ServiceFormat detectFormat(String serviceDescription) throws IOException {
		return formatDetector.detect(serviceDescription);
	}

	// TODO: Dont use the configuration directly
	private String getContextUri(String serviceUri) {
		String queryString = "select ?c where { \n" +
			"graph ?c { \n" +
			"<" + serviceUri + "> " + RDF.type.toSPARQL() + " " + MSM.Service.toSPARQL() +
			" . } }";

		RepositoryModel model = repoConnector.openRepositoryModel();
		QueryResultTable qrt = model.sparqlSelect(queryString);
		String defUri = null;
		if ( qrt != null ) {
			ClosableIterator<org.ontoware.rdf2go.model.QueryRow> iter = qrt.iterator();
			if ( iter != null ) {
				while ( iter.hasNext() ) {
					org.ontoware.rdf2go.model.QueryRow qr = iter.next();
					String valueString = qr.getValue("c").toString();
					if ( valueString.toLowerCase().contains(configuration.getUriPrefix()) ) {
						defUri = valueString;
					}
				}
				iter.close();
				iter = null;
			}
		}
		repoConnector.closeRepositoryModel(model);
		model = null;
		return defUri;
	}

	// TODO: Dont use the configuration directly
	private String getContextUriById(String serviceId) {
		String queryString = "select ?c where { \n" +
			"graph ?c { \n" +
			"?s " + RDF.type.toSPARQL() + " " + MSM.Service.toSPARQL() + "\n" +
			"FILTER regex(str(?s), \"" + serviceId + "\", \"i\")}\n}";

		RepositoryModel model = repoConnector.openRepositoryModel();
		QueryResultTable qrt = model.sparqlSelect(queryString);
		String defUri = null;
		if ( qrt != null ) {
			ClosableIterator<org.ontoware.rdf2go.model.QueryRow> iter = qrt.iterator();
			if ( iter != null ) {
				while ( iter.hasNext() ) {
					org.ontoware.rdf2go.model.QueryRow qr = iter.next();
					String valueString = qr.getValue("c").toString();
					if ( valueString.toLowerCase().contains(configuration.getUriPrefix()) ) {
						defUri = valueString;
					}
				}
				iter.close();
				iter = null;
			}
		}
		repoConnector.closeRepositoryModel(model);
		model = null;
		return defUri;
	}

	// TODO: Dont use the configuration directly
	private String getServiceUri(String serviceId) {
		String queryString = "select ?s where { \n" +
			"?s " + RDF.type.toSPARQL() + " " + MSM.Service.toSPARQL() + "\n" +
			"FILTER regex(str(?s), \"" + serviceId + "\", \"i\")\n}";

		RepositoryModel model = repoConnector.openRepositoryModel();
		QueryResultTable qrt = model.sparqlSelect(queryString);
		String defUri = null;
		if ( qrt != null ) {
			ClosableIterator<org.ontoware.rdf2go.model.QueryRow> iter = qrt.iterator();
			if ( iter != null ) {
				while ( iter.hasNext() ) {
					org.ontoware.rdf2go.model.QueryRow qr = iter.next();
					String valueString = qr.getValue("s").toString();
					if ( valueString.toLowerCase().contains(configuration.getUriPrefix()) ) {
						defUri = valueString;
					}
				}
				iter.close();
				iter = null;
			}
		}
		repoConnector.closeRepositoryModel(model);
		model = null;
		return defUri;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#getRepositoryConnector()
	 */
	@Override
	public RDFRepositoryConnector getServicesRepositoryConnector() {
		return repoConnector;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#querySelect(java.lang.String, java.lang.String)
	 */
//	@Override
//	public QueryResultTable querySelect(String query, String language) {
//		Model model = repoConnector.openRepositoryModel();
//		return model.querySelect(query, language);
//	}

}
