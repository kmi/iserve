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
package uk.ac.open.kmi.iserve.sal.manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.Syntax;
import org.ontoware.rdf2go.vocabulary.RDF;
import org.openrdf.rdf2go.RepositoryModel;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.iserve.commons.io.FileUtil;
import uk.ac.open.kmi.iserve.commons.io.StringUtil;
import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.imatcher.IServeIMatcher;
import uk.ac.open.kmi.iserve.importer.ImporterException;
import uk.ac.open.kmi.iserve.importer.ServiceImporter;
import uk.ac.open.kmi.iserve.importer.hrests.HrestsImporter;
import uk.ac.open.kmi.iserve.importer.owls.OwlsImporter;
import uk.ac.open.kmi.iserve.importer.rdf.RdfImporter;
import uk.ac.open.kmi.iserve.importer.sawsdl.SawsdlImporter;
import uk.ac.open.kmi.iserve.sal.ServiceFormat;
import uk.ac.open.kmi.iserve.sal.ServiceFormatDetector;
import uk.ac.open.kmi.iserve.sal.config.SalConfig;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.model.common.URI;
import uk.ac.open.kmi.iserve.sal.model.service.Service;
import uk.ac.open.kmi.iserve.sal.util.ModelConverter;

public class ServiceManager extends BaseSemanticManager {

	private SalConfig config;

	private Map<ServiceFormat, ServiceImporter> importerMap;

	private ServiceFormatDetector formatDetector;

	public ServiceManager(SalConfig config) throws RepositoryException, TransformerConfigurationException, WSDLException, ParserConfigurationException {
		super(config.getImporterConfig().getRepoServerUrl(), config.getImporterConfig().getRepoName());

		this.config = config;
		importerMap = new HashMap<ServiceFormat, ServiceImporter>();

		ServiceImporter importer = new HrestsImporter(config.getImporterConfig(), config.getXsltPath());
		importerMap.put(ServiceFormat.HTML, importer);

		importer = new OwlsImporter(config.getImporterConfig());
		importerMap.put(ServiceFormat.OWLS, importer);

		importer = new SawsdlImporter(config.getImporterConfig());
		importerMap.put(ServiceFormat.WSDL, importer);

		importer = new RdfImporter(config.getImporterConfig());
		importerMap.put(ServiceFormat.RDFXML, importer);

		formatDetector = new ServiceFormatDetector();
	}

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

	public String addService(String fileName, String serviceDescription, String sourceUri) throws ServiceException {
		// detect type
		ServiceFormat format = ServiceFormat.UNSUPPORTED;
		try {
			format = detectFormat(serviceDescription);
		} catch (IOException e1) {
			throw new ServiceException(e1);
		}

		// find corresponding importer
		if ( format.equals(ServiceFormat.UNSUPPORTED) ) {
			throw new ServiceException("The service is described in an unsupported format");
		}
		if ( fileName == null ) {
			// determine file name
			if ( format.equals(ServiceFormat.HTML) ) {
				fileName = "service.html";
			} else if ( format.equals(ServiceFormat.OWLS) ) {
				fileName = "service.owls";
			} else if ( format.equals(ServiceFormat.WSDL) ) {
				fileName = "service.wsdl";
			} else if ( format.equals(ServiceFormat.RDFXML) ) {
				fileName = "service.rdf.xml";
			}
		}
		ServiceImporter importer = importerMap.get(format);
		String serviceUri = null;
		try {
			serviceUri = importer.importService(fileName, serviceDescription, sourceUri);
		} catch (ImporterException e) {
			throw new ServiceException(e);
		}

		// add to iServe iMatcher
		Model model = getServiceAsModel(serviceUri);
		model.open();
		String serviceInRdf = model.serialize(Syntax.RdfXml);
		IServeIMatcher.getInstance().addRdf(serviceInRdf, serviceUri);
		model.close();
		model = null;

		return serviceUri;
	}

	public String deleteService(String serviceUri) throws ServiceException {
		String contextUri = getContextUri(serviceUri);
		if ( null == contextUri ) {
			throw new ServiceException("Cannot find service identified by " + serviceUri);
		}

		// delete from hard disk
		String prefix = config.getImporterConfig().getUriPrefix() + MSM.DOCUMENT_INFIX;
		String relativePath = StringUtil.subStrings(contextUri, prefix); 
		String filePath = config.getImporterConfig().getDocFolderPath() + relativePath;

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

	public String deleteServiceById(String serviceId) throws ServiceException {
		return deleteService(getServiceUri(serviceId));
	}

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

	public RepositoryModel getServiceAsModel(String serviceUri) throws ServiceException {
		String contextUri = getContextUri(serviceUri);
		if ( null == contextUri ) {
			throw new ServiceException("Cannot find service identified by " + serviceUri);
		}
		RepositoryModel model = repoConnector.openRepositoryModel(contextUri);
		return model;
	}

	public Model getServiceAsModelById(String serviceId) throws ServiceException {
		String contextUri = getContextUriById(serviceId);
		if ( null == contextUri ) {
			throw new ServiceException("Cannot find service identified by " + serviceId);
		}
		RepositoryModel model = repoConnector.openRepositoryModel(contextUri);
		return model;
	}

	public Service getService(URI serviceUri) throws ServiceException {
		Service result = null;
		RepositoryModel model = getServiceAsModel(serviceUri.toString());
		result = ModelConverter.coverterService(serviceUri, model);
		model.close();
		model = null;
		return result;
	}

	private ServiceFormat detectFormat(String serviceDescription) throws IOException {
		return formatDetector.detect(serviceDescription);
	}

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
					if ( valueString.toLowerCase().contains(config.getImporterConfig().getUriPrefix()) ) {
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
					if ( valueString.toLowerCase().contains(config.getImporterConfig().getUriPrefix()) ) {
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
					if ( valueString.toLowerCase().contains(config.getImporterConfig().getUriPrefix()) ) {
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

}
