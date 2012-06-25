/*
   Copyright 2012  Knowledge Media Institute - The Open University

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

import java.io.InputStream;
import java.util.List;

import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.Syntax;

import uk.ac.open.kmi.iserve.commons.io.RDFRepositoryConnector;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.model.common.URI;
import uk.ac.open.kmi.iserve.sal.model.service.Service;

/**
 * Interface that defines the operations a Service Manager should offer
 * Currently this is bound to RDF2GO and therefore is RDF-oriented
 * If other implementations backed by different types of storage systems are 
 * needed we should update this 
 * 
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public interface ServiceManager {

	public abstract List<String> listService();

	public abstract String addService(String fileName,
			String serviceDescription, String sourceUri)
			throws ServiceException;

	public abstract String deleteService(String serviceUri)
			throws ServiceException;

	public abstract String deleteServiceById(String serviceId)
			throws ServiceException;

	public abstract String getService(String serviceUri, Syntax syntax)
			throws ServiceException;

	public abstract Model getServiceAsModel(String serviceUri)
			throws ServiceException;

	public abstract Model getServiceAsModelById(String serviceId)
			throws ServiceException;

	public abstract Service getService(URI serviceUri) throws ServiceException;

	/**
	 * @param fileName
	 * @param msmServiceDescriptionStream
	 * @param sourceUri
	 * @return
	 * @throws ServiceException
	 */
	public String addService(String fileName, InputStream msmServiceDescriptionStream,
			String sourceUri) throws ServiceException;

}