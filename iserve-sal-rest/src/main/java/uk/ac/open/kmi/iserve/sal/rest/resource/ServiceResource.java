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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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

import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Syntax;
import org.ontoware.rdf2go.vocabulary.RDF;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.rdf2go.RepositoryModel;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;

import uk.ac.open.kmi.iserve.commons.io.RDFRepositoryConnector;
import uk.ac.open.kmi.iserve.commons.io.URIUtil;
import uk.ac.open.kmi.iserve.commons.vocabulary.LOG;
import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.sal.exception.LogException;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;
import uk.ac.open.kmi.iserve.sal.rest.auth.AuthenticationException;
import uk.ac.open.kmi.iserve.sal.util.HtmlUtil;
import uk.ac.open.kmi.iserve.sal.util.ModelReferenceUtil;
import uk.ac.open.kmi.iserve.sal.util.XmlUtil;

import com.sun.jersey.api.container.MappableContainerException;

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

	@GET
	@Produces({MediaType.TEXT_HTML, MediaType.WILDCARD})
	public Response listServicesAsHtml() throws URISyntaxException, RepositoryException, MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, RDFHandlerException, IOException {
		String absolutePath = uriInfo.getAbsolutePath().toString();
		return listServices(absolutePath, PAGE_REL_PATH);
	}

	@GET
	@Produces({"application/rdf+xml", "text/turtle", "text/n3", "text/rdf+n3", "text/plain", "application/sparql-results+xml"})
	public Response listServicesAsRdf() throws URISyntaxException, RepositoryException, MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, RDFHandlerException, IOException {
		String absolutePath = uriInfo.getAbsolutePath().toString();
		return listServices(absolutePath, DATA_REL_PATH);
	}

	@GET @Path("/{id}")
	@Produces({MediaType.TEXT_HTML, MediaType.WILDCARD})
	public Response getServiceAsHtml(@PathParam("id") String id) throws URISyntaxException, ServiceException {
		String absolutePath = uriInfo.getAbsolutePath().toString();
		return getService(absolutePath, id, PAGE_REL_PATH, null);
	}

	@GET @Path("/{id}")
	@Produces({MediaType.TEXT_XML})
	public Response getServiceAsXml(@PathParam("id") String id) throws URISyntaxException, ServiceException {
		String absolutePath = uriInfo.getAbsolutePath().toString();
		return getService(absolutePath, id, DATA_REL_PATH, xmlSyntax);
	}

	@GET @Path("/{id}")
	@Produces({"application/rdf+xml"})
	public Response getServiceAsRdfXml(@PathParam("id") String id) throws ServiceException, URISyntaxException {
		String absolutePath = uriInfo.getAbsolutePath().toString();
		return getService(absolutePath, id, DATA_REL_PATH, Syntax.RdfXml);
	}

	@GET @Path("/{id}")
	@Produces({"text/turtle", "text/n3", "text/rdf+n3"})
	public Response getServiceAsTurtle(@PathParam("id") String id) throws ServiceException, URISyntaxException {
		String absolutePath = uriInfo.getAbsolutePath().toString();
		return getService(absolutePath, id, DATA_REL_PATH, Syntax.Turtle);
	}

	@GET @Path("/{id}")
	@Produces({MediaType.TEXT_PLAIN})
	public Response getServiceAsNtriples(@PathParam("id") String id) throws ServiceException, URISyntaxException {
		String absolutePath = uriInfo.getAbsolutePath().toString();
		return getService(absolutePath, id, DATA_REL_PATH, Syntax.Ntriples);
	}

	@POST
	@Consumes({MediaType.TEXT_HTML, MediaType.TEXT_XML, MediaType.APPLICATION_XML, "application/rdf+xml",
		"text/turtle", "text/n3", "text/rdf+n3", MediaType.TEXT_PLAIN})
	@Produces({MediaType.TEXT_HTML})
	public Response addService(String html, @HeaderParam("Content-Location") String locationUri,
			@HeaderParam("Content-Type") String contentType) throws ServiceException, URISyntaxException, LogException {
		
		if ( security.getUserPrincipal() == null ) {
			throw new MappableContainerException(
					new AuthenticationException(
							"Authentication credentials are required\r\n",
							"iServe SAL RESTful API"));
		}

		String userFoafId = security.getUserPrincipal().getName();
		String fileName = null;
		if ( (locationUri != null) && ("".equalsIgnoreCase(locationUri) == false) ) {
			fileName = URIUtil.getLocalName(locationUri);
		}

		if ( fileName == null ) {
			if ( (contentType != null) && ("".equalsIgnoreCase(contentType) == false) ) {
				if ( contentType.equalsIgnoreCase(MediaType.TEXT_HTML) ) {
					fileName = "service.html";
				} else if ( contentType.equalsIgnoreCase(MediaType.TEXT_XML) ||
						contentType.equalsIgnoreCase(MediaType.APPLICATION_XML) ) {
					fileName = "service.xml";
				} else if ( contentType.equalsIgnoreCase("application/rdf+xml") ) {
					fileName = "service.rdf.xml";
				} else if ( contentType.equalsIgnoreCase("text/turtle")  ) {
					fileName = "service.ttl";
				} else if ( contentType.equalsIgnoreCase("text/rdf+n3") &&
						contentType.equalsIgnoreCase("text/n3") ) {
					fileName = "service.n3";
				} else {
					fileName = "service.txt";
				}
			}
		}
		String uriString = ManagerSingleton.getInstance().addService(fileName, html, locationUri);
//		String oauthConsumer = ((SecurityFilter.Authorizer) security).getOAuthConsumer();
		// TODO: Push this down to the above method
		ManagerSingleton.getInstance().log(userFoafId, LOG.ITEM_CREATION, uriString, new Date(), "REST");
		String htmlString = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
			"  <body>\nA service is created at <a href='" + uriString + "'>" + uriString + "</a>\n  </body>\n</html>";
		return Response.status(Status.CREATED).contentLocation(new URI(uriString)).entity(htmlString).build();
	}

	@DELETE @Path("/{id}")
	@Produces({MediaType.TEXT_HTML})
	public String deleteService(@PathParam("id") String id) throws ServiceException, LogException {
		if ( security.getUserPrincipal() == null ) {
			throw new MappableContainerException(
					new AuthenticationException(
							"Authentication credentials are required\r\n",
							"iServe SAL RESTful API"));
		}

		String userFoafId = security.getUserPrincipal().getName();
		String uriString = ManagerSingleton.getInstance().deleteServiceById(id);
		// TODO: Push this down to the above method
		ManagerSingleton.getInstance().log(userFoafId, LOG.ITEM_DELETING, uriString, new Date(), "REST");
		String htmlString = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
			"  <body>\nA service is removed from <a href='" + uriString + "'>" + uriString + "</a>\n  </body>\n</html>";
		return htmlString;
	}

	private Response listServices(String absolutePath, String redirect) throws URISyntaxException, RepositoryException, MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException, RDFHandlerException, IOException {
		if ( absolutePath.endsWith("/") == false ) {
			absolutePath += "/";
		}
		if ( absolutePath.endsWith("resource/services/") ) {
			String newPath = absolutePath.substring(0, absolutePath.length() - "resource/services/".length() );
			newPath += redirect + "/" + SERVICES_REL_PATH ;
			return Response.seeOther(new URI(newPath)).build();
		} else if ( absolutePath.endsWith("page/services/") ) {
			StringBuffer sb = new StringBuffer();
			sb.append(HtmlUtil.LIST_HTML_PREFIX.replaceAll("<<<title>>>", "Service List"));
			List<String> serviceList = ManagerSingleton.getInstance().listService();
			sb.append(HtmlUtil.uriListToTable(serviceList));
			sb.append(HtmlUtil.LIST_HTML_SUFFIX);
			return Response.ok(sb.toString(), MediaType.TEXT_HTML).build();
		} else if ( absolutePath.endsWith("data/services/") ) {
			RDFRepositoryConnector connector = ManagerSingleton.getInstance().getServicesRepositoryConnector();
			String queryString = "SELECT ?service WHERE { ?service " + RDF.type.toSPARQL() + " " + MSM.Service.toSPARQL() + "}";
			String result = connector.query(queryString);
			return Response.ok(result, "application/sparql-results+xml").build();
		}
		return Response.status(Status.BAD_REQUEST).build();		
	}

	private Response getService(String absolutePath, String id, String redirect, Syntax syntax) throws URISyntaxException, ServiceException {
		if (absolutePath.endsWith("/") == false) {
			absolutePath += "/";
		}
		if (absolutePath.endsWith("resource/services/" + id + "/")) {
			String newPath = absolutePath.substring(0, absolutePath.length() - ("resource/services/" + id + "/").length());
			newPath += redirect + "/" + SERVICES_REL_PATH + "/" + id;
			return Response.seeOther(new URI(newPath)).build();
		} else if (absolutePath.endsWith("page/services/" + id + "/")) {
			StringBuffer sb = new StringBuffer();
			sb.append(HtmlUtil.LIST_HTML_PREFIX.replaceAll("<<<title>>>", "Service Description"));
			Model model = ManagerSingleton.getInstance().getServiceAsModelById(id);
			sb.append(HtmlUtil.modelToTable(model, id));
			sb.append(HtmlUtil.LIST_HTML_SUFFIX);
			return Response.ok(sb.toString(), MediaType.TEXT_HTML).build();
		} else if (absolutePath.endsWith("data/services/" + id + "/")) {
			Model model = ManagerSingleton.getInstance().getServiceAsModelById(id);			
			if ( syntax != null ) {
				if ( syntax.equals(Syntax.Ntriples) ) {
					String result = model.serialize(Syntax.Ntriples);
					return Response.ok(result, MediaType.TEXT_PLAIN).build();
				} else if ( syntax.equals(Syntax.Turtle) ) {
					String result = model.serialize(Syntax.Turtle);
					return Response.ok(result, "text/turtle").build();
				} else if ( syntax.equals(Syntax.Trig) ) {
					String result = model.serialize(Syntax.Trig);
					return Response.ok(result, "application/rdf+xml").build();
				} else if ( syntax.equals(Syntax.Trix) ) {
					String result = model.serialize(Syntax.Trix);
					return Response.ok(result, "application/rdf+xml").build();
				} else if ( syntax.equals(xmlSyntax) ) {
					RepositoryModel m = (RepositoryModel) ManagerSingleton.getInstance().getServiceAsModelById(id);
					ModelReferenceUtil.getInstance().setRDFRepositoryConnector(ManagerSingleton.getInstance().getServicesRepositoryConnector());
					String result = XmlUtil.serializeService(m);
					return Response.ok(result, "text/xml").build();
				}
			}
			String result = model.serialize(Syntax.RdfXml);
			return Response.ok(result, "application/rdf+xml").build();
		}
		return Response.status(Status.BAD_REQUEST).build();
	}

}
