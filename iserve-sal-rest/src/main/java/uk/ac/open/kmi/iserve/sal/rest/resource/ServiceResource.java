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
package uk.ac.open.kmi.iserve.sal.rest.resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.ontoware.rdf2go.model.Syntax;

import uk.ac.open.kmi.iserve.commons.vocabulary.LOG;
import uk.ac.open.kmi.iserve.sal.ServiceFormat;
import uk.ac.open.kmi.iserve.sal.exception.LogException;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;
import uk.ac.open.kmi.iserve.sal.rest.auth.AuthenticationException;

import com.sun.jersey.api.container.MappableContainerException;

/**
 * TODO: Refactor based on the Manager Singleton.
 * 
 * @author Dong Liu (Knowledge Media Institute - The Open University)
 */
@Path("/services")
public class ServiceResource {

	/**
	 * Relative path to data resources
	 */
	private static final String DATA_REL_PATH = "data";

	/**
	 * Relative path to html resources
	 */
	private static final String PAGE_REL_PATH = "page";
	
	/**
	 * Relative path to resources
	 */
	private static final String RESOURCE_REL_PATH = "resource";
	
	/**
	 * Relative path to services resources
	 */
	private static final String SERVICES_REL_PATH = "services";

	private static Syntax xmlSyntax = new Syntax("xml", "text/xml", ".xml");

	@Context
	UriInfo uriInfo;

	@Context
	SecurityContext security;

	public ServiceResource() {
		Syntax.register(xmlSyntax);
	}


	@POST
	@Consumes({MediaType.TEXT_HTML, MediaType.TEXT_XML, MediaType.APPLICATION_XML, "application/rdf+xml",
		"text/turtle", "text/n3", "text/rdf+n3", MediaType.TEXT_PLAIN})
	@Produces({MediaType.TEXT_HTML})
	public Response addService(String serviceContent, @HeaderParam("Content-Location") String locationUri,
			@HeaderParam("Content-Type") String contentType) throws ServiceException, URISyntaxException, LogException {
		
		if ( security.getUserPrincipal() == null ) {
			throw new MappableContainerException(
					new AuthenticationException(
							"Authentication credentials are required\r\n",
							"iServe SAL RESTful API"));
		}

		String userFoafId = security.getUserPrincipal().getName();
		URI serviceUri = null;
		ServiceFormat format = null;
		// TODO: Get the actual format of the service description
		
		try {
			if ( (locationUri != null) && ("".equalsIgnoreCase(locationUri) == false) ) {
				// There is a location. Just register, don't import
					serviceUri = ManagerSingleton.getInstance().registerService(URI.create(locationUri), format);
			} else {
				// There is no location. Import the entire service
				// TODO: Get the actual charset
				InputStream is = new ByteArrayInputStream(serviceContent.getBytes("utf-8"));
				serviceUri = ManagerSingleton.getInstance().importService(is, format);
			}
			//		String oauthConsumer = ((SecurityFilter.Authorizer) security).getOAuthConsumer();

			String htmlString = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
				"  <body>\nA service is created at <a href='" + serviceUri + "'>" + serviceUri + "</a>\n  </body>\n</html>";
			
			return Response.status(Status.CREATED).contentLocation(serviceUri).entity(htmlString).build();
		} catch (Exception e) {
			String error = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
					"  <body>\nThere was an error while creating the service. Contact the system administrator. \n  </body>\n</html>";
			
			// TODO: Add logging
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(error).build();
		}
	}

	@DELETE @Path("/{id}")
	@Produces({MediaType.TEXT_HTML})
	public Response deleteService() throws ServiceException, LogException {
		if ( security.getUserPrincipal() == null ) {
			throw new MappableContainerException(
					new AuthenticationException(
							"Authentication credentials are required\r\n",
							"iServe SAL RESTful API"));
		}

		String userFoafId = security.getUserPrincipal().getName();
		URI serviceUri =  uriInfo.getAbsolutePath();
		String response = null; 
		try {
			if ( ManagerSingleton.getInstance().unregisterService(serviceUri) ) {
				// The service was deleted
				response = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
					"  <body>\n The service <a href='" + serviceUri + "'>" + serviceUri + "</a> has been deleted from the server.\n  </body>\n</html>";
				
				return Response.status(Status.GONE).contentLocation(serviceUri).entity(response).build();
			} else {
				// The service was not deleted
				response = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
					"  <body>\n The service <a href='" + serviceUri + "'>" + serviceUri + "</a> could not be deleted from the server. Try again or contact a server administrator.\n  </body>\n</html>";
				
				return Response.status(Status.NOT_MODIFIED).contentLocation(serviceUri).entity(response).build();
			}
		} catch (SalException e) {
			response = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
					"  <body>\nThere was an error while deleting the service. Contact the system administrator. \n  </body>\n</html>";
			
			// TODO: Add logging
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(response).build();
		}

	}
}
