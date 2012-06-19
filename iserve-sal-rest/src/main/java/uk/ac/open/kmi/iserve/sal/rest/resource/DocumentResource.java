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

import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.iserve.commons.io.URIUtil;
import uk.ac.open.kmi.iserve.commons.vocabulary.LOG;
import uk.ac.open.kmi.iserve.sal.exception.DocumentException;
import uk.ac.open.kmi.iserve.sal.exception.LogException;
import uk.ac.open.kmi.iserve.sal.manager.DocumentManager;
import uk.ac.open.kmi.iserve.sal.manager.LogManager;
import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;
import uk.ac.open.kmi.iserve.sal.rest.auth.AuthenticationException;
import uk.ac.open.kmi.iserve.sal.util.HtmlUtil;

import com.sun.jersey.api.container.MappableContainerException;

@Path("/documents")
public class DocumentResource {

	@Context
	UriInfo uriInfo;

	@Context
	SecurityContext security;

	public DocumentResource() throws IOException, RepositoryException { }

	@GET
	@Produces({MediaType.TEXT_HTML, MediaType.WILDCARD})
	public Response listDocumentsAsHtml() throws URISyntaxException {
		String absolutePath = uriInfo.getAbsolutePath().toString();
		return listDocuments(absolutePath, "page");
	}

	@GET
	@Produces({"application/rdf+xml", "text/turtle", "text/n3", "text/rdf+n3", "text/plain", "application/sparql-results+xml"})
	public Response listDocumentsAsRdf() throws URISyntaxException {
		String absolutePath = uriInfo.getAbsolutePath().toString();
		return listDocuments(absolutePath, "data");		
	}

	@GET @Path("/{id}")
	@Produces({MediaType.WILDCARD})
	public Response getDocumentById(@PathParam("id") String id) throws URISyntaxException, DocumentException {
		String absolutePath = uriInfo.getAbsolutePath().toString();
		return getDocument(absolutePath, id, "", "page");
	}

	@GET @Path("/{id}/{filename}")
	@Produces({MediaType.WILDCARD})
	public Response getDocument(@PathParam("id") String id, @PathParam("filename") String filename) throws URISyntaxException, DocumentException {
		String absolutePath = uriInfo.getAbsolutePath().toString();
		return getDocument(absolutePath, id, filename, "page");
	}

	@POST
	@Consumes({MediaType.TEXT_HTML, MediaType.TEXT_XML, MediaType.APPLICATION_XML, "application/rdf+xml",
		"text/turtle", "text/n3", "text/rdf+n3", MediaType.TEXT_PLAIN})
	@Produces({MediaType.TEXT_HTML})
	public Response addDocument(String serviceDescription, @HeaderParam("Content-Location") String locationUri,
			@HeaderParam("Content-Type") String contentType) throws DocumentException, URISyntaxException, LogException {
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
				} else if ( contentType.equalsIgnoreCase(MediaType.TEXT_XML) &&
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
		String uriString = ManagerSingleton.getInstance().addDocument(fileName, serviceDescription);
		// TODO: Push this down to the above method
		ManagerSingleton.getInstance().log(userFoafId, LOG.ITEM_CREATION, uriString, new Date(), "REST");
		String htmlString = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
			"  <body>\nA document is created at <a href='" + uriString + "'>" + uriString + "</a>\n  </body>\n</html>";
		return Response.status(Status.CREATED).contentLocation(new URI(uriString)).entity(htmlString).build();
	}

	@DELETE @Path("/{id}")
	@Produces({MediaType.TEXT_HTML})
	public String deleteDocumentById(@PathParam("id") String id) throws DocumentException, LogException {
		if ( security.getUserPrincipal() == null ) {
			throw new MappableContainerException(
					new AuthenticationException(
							"Authentication credentials are required\r\n",
							"iServe SAL RESTful API"));
		}

		String userFoafId = security.getUserPrincipal().getName();

		String uriString = ManagerSingleton.getInstance().deleteDocumentById(id);
		// TODO: Push this down to the above method
		ManagerSingleton.getInstance().log(userFoafId, LOG.ITEM_DELETING, uriString, new Date(), "REST");
		String htmlString = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
			"  <body>\nA document is removed from <a href='" + uriString + "'>" + uriString + "</a>\n  </body>\n</html>";
		return htmlString;
	}

	@DELETE @Path("/{id}/{filename}")
	@Produces({MediaType.TEXT_HTML})
	public String deleteDocument(@PathParam("id") String id) throws DocumentException, LogException {
		if ( security.getUserPrincipal() == null ) {
			throw new MappableContainerException(
					new AuthenticationException(
							"Authentication credentials are required\r\n",
							"iServe SAL RESTful API"));
		}

		String userFoafId = security.getUserPrincipal().getName();

		String uriString = ManagerSingleton.getInstance().deleteDocumentById(id);
		// TODO: Push this down to the above method
		ManagerSingleton.getInstance().log(userFoafId, LOG.ITEM_DELETING, uriString, new Date(), "REST");
		String htmlString = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
			"  <body>\nA document is removed from <a href='" + uriString + "'>" + uriString + "</a>\n  </body>\n</html>";
		return htmlString;
	}

	private Response getDocument(String absolutePath, String id, String filename, String redirect) throws URISyntaxException, DocumentException {
		if (absolutePath.endsWith("/") == false) {
			absolutePath += "/";
		}
		if (absolutePath.endsWith("resource/documents/" + id + "/")) {
			String newPath = absolutePath.substring(0, absolutePath.length() - ("resource/documents/" + id + "/").length());
			newPath += redirect + "/documents/" + id;
			return Response.seeOther(new URI(newPath)).build();
		} else if (absolutePath.endsWith("page/documents/" + id + "/")) {
			String result = ManagerSingleton.getInstance().getDocumentById(id);
			return Response.ok(result).build();
		} else if (absolutePath.endsWith("data/documents/" + id + "/")) {
			String result = ManagerSingleton.getInstance().getDocumentById(id);
			return Response.ok(result).build();
		}
		if (absolutePath.endsWith("resource/documents/" + id + "/" + filename + "/")) {
			String newPath = absolutePath.substring(0, absolutePath.length() - ("resource/documents/" + id + "/" + filename + "/").length());
			newPath += redirect + "/documents/" + id + "/" + filename;
			return Response.seeOther(new URI(newPath)).build();
		} else if (absolutePath.endsWith("page/documents/" + id + "/" + filename + "/")) {
			String result = ManagerSingleton.getInstance().getDocumentById(id);
			return Response.ok(result).build();
		} else if (absolutePath.endsWith("data/documents/" + id + "/" + filename + "/")) {
			String result = ManagerSingleton.getInstance().getDocumentById(id);
			return Response.ok(result).build();
		}
		return Response.status(Status.BAD_REQUEST).build();
	}

	private Response listDocuments(String absolutePath, String redirect) throws URISyntaxException {
		if ( absolutePath.endsWith("/") == false ) {
			absolutePath += "/";
		}
		if ( absolutePath.endsWith("resource/documents/") ) {
			String newPath = absolutePath.substring(0, absolutePath.length() - "resource/documents/".length() );
			newPath += redirect + "/documents";
			return Response.seeOther(new URI(newPath)).build();
		} else if ( absolutePath.endsWith("page/documents/") ) {
			StringBuffer sb = new StringBuffer();
			sb.append(HtmlUtil.LIST_HTML_PREFIX.replaceAll("<<<title>>>", "Document List"));
			List<String> serviceList = ManagerSingleton.getInstance().listDocument();
			sb.append(HtmlUtil.uriListToTable(serviceList));
			sb.append(HtmlUtil.LIST_HTML_SUFFIX);
			return Response.ok(sb.toString(), MediaType.TEXT_HTML).build();
		} else if (absolutePath.endsWith("data/documents/")) {
			String header = "<?xml version='1.0' encoding='UTF-8'?>\n"
				+ "<sparql xmlns='http://www.w3.org/2005/sparql-results#'>\n"
				+ "	<head>\n"
				+ "		<variable name='document'/>\n"
				+ "	</head>\n"
				+ "	<results>\n";
			String tail = " 	</results>\n"
				+ "</sparql>";
			String uriHeader = "		<result>\n"
				+ "			<binding name='document'>\n"
				+ "				<uri>";
			String uriTail = "</uri>\n"
				+ "			</binding>\n"
				+ "		</result>\n";

			StringBuffer sb = new StringBuffer();
			sb.append(header);
			List<String> uriList = ManagerSingleton.getInstance().listDocument();
			for (String uri : uriList) {
				sb.append(uriHeader + uri + uriTail);
			}
			sb.append(tail);
			return Response.ok(sb.toString(), "application/sparql-results+xml").build();
		}
		return Response.status(Status.BAD_REQUEST).build();
	}

	
}
