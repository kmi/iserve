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

import org.ontoware.rdf2go.model.Syntax;

import uk.ac.open.kmi.iserve.sal.exception.ServiceException;

/**
 * Interface that defines the operations a Service Manager should offer
 * If other implementations backed by different types of storage systems are 
 * needed we should update this.
 * 
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public interface ServiceManager {

	// Create Methods
	/**
	 * Creates a Service Description in the system.
	 * Only needs to be fed with an serialisation of some kind. At the moment
	 * this is to be limited to RDF descriptions.
	 * 
	 * Prior to storing the description the system should ensure that this is 
	 * a correct serialisation.
	 * 
	 * @param msmInputStream the input service description in terms of MSM
	 * @param sourceDocumentUri the source document this description has been 
	 * extracted from. Could be local or external
	 * @return the URI this service description was saved to
	 * @throws ServiceException
	 */
	public URI addService(InputStream msmInputStream,
			URI sourceDocumentUri) throws ServiceException;
	
	// Read Methods
	/**
	 * Obtains the service description of the service identified by the URI
	 * in the serialisation requested.
	 *  
	 * @param serviceUri the URI of the service to obtain
	 * @param syntax the format to use when serialising the service
	 * @return the serialised service description
	 * @throws ServiceException
	 */
	public abstract String getServiceSerialisation(URI serviceUri, Syntax syntax)
			throws ServiceException;
	
	// Delete Methods
	/**
	 * Deletes the given service
	 * 
	 * @param serviceUri the URI of the service to delete
	 * @return True if it was deleted or false otherwise
	 * @throws ServiceException
	 */
	public abstract boolean deleteService(URI serviceUri)
			throws ServiceException;

	// General Management Methods
	/**
	 * Obtains a list of service URIs with all the services known to the system
	 * 
	 * @return list of URIs with all the services in the registry
	 */
	public abstract List<URI> listServices();
	
	/**
	 * Determines whether a service is known to the registry
	 * 
	 * @param serviceUri the URI of the service being looked up
	 * @return True if it is registered in the server
	 * @throws ServiceException
	 */
	public abstract boolean serviceExists(URI serviceUri) throws ServiceException;

}