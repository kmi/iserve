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
import java.net.URI;
import java.util.List;

import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Syntax;

import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
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

	// Create Methods
	public URI addService(String serviceId, InputStream msmInputStream,
			URI sourceDocumentUri) throws ServiceException;
	
	// Read Methods
	public abstract String getServiceSerialisation(URI serviceUri, Syntax syntax)
			throws ServiceException;

	public abstract Model getServiceAsModel(URI serviceUri)
			throws ServiceException;

	public abstract Model getServiceAsModelById(String serviceId)
			throws ServiceException;

	public abstract Service getService(URI serviceUri) throws ServiceException;
	
	// Delete Methods
	public abstract boolean deleteService(URI serviceUri)
			throws ServiceException;

	public abstract boolean deleteServiceById(String serviceId)
			throws ServiceException;

	
	// General Management Methods
	public String generateUniqueServiceId();
	
	public abstract List<URI> listServices();
	
	public abstract boolean serviceExists(URI serviceUri) throws ServiceException;
	
	public abstract boolean serviceExists(String serviceId) throws ServiceException;

}