/*
 * Copyright (c) 2013. Knowledge Media Institute - The Open University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.open.kmi.iserve.sal.rest.resource;

import uk.ac.open.kmi.iserve.sal.exception.DocumentException;
import uk.ac.open.kmi.iserve.sal.exception.LogException;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.manager.DocumentManager;
import uk.ac.open.kmi.iserve.sal.manager.impl.iServeFacade;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

@Path("/id/documents")
public class DocumentsResource {

    @Context
    UriInfo uriInfo;

    @Context
    SecurityContext security;

    private final DocumentManager docManager;

    @Inject
    public DocumentsResource() {
        this.docManager = iServeFacade.getInstance().getDocumentManager();
    }

    @POST
    @Consumes({MediaType.TEXT_HTML, MediaType.TEXT_XML, MediaType.APPLICATION_XML, "application/rdf+xml",
            "text/turtle", "text/n3", "text/rdf+n3", MediaType.TEXT_PLAIN})
    @Produces({MediaType.TEXT_HTML})
    public Response addDocument(String document, @HeaderParam("Content-Location") String locationUri,
                                @HeaderParam("Content-Type") String contentType) {

        // TODO: Re add security
//        if ( security.getUserPrincipal() == null ) {
//			throw new MappableContainerException(
//					new AuthenticationException(
//							"Authentication credentials are required\r\n",
//							"iServe SAL RESTful API"));
//		}
//
//		String userFoafId = security.getUserPrincipal().getName();

        // TODO check the actual encoding
        InputStream is;
        try {
            is = new ByteArrayInputStream(document.getBytes("UTF-8"));
            URI docUri = docManager.createDocument(is, contentType);

            String htmlString = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                    "  <body>\nA document is created at <a href='" + docUri.toString() + "'>" + docUri.toString() + "</a>\n  </body>\n</html>";

            return Response.status(Status.CREATED).contentLocation(docUri).entity(htmlString).build();
        } catch (Exception e) {
            String error = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                    "  <body>\nThere was an error while creating a document. Contact the system administrator. \n  </body>\n</html>";

            // TODO: Add logging

            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }

    }

    // TODO: Support delete
//	@DELETE @Path("/{id}")
//	@Produces({MediaType.TEXT_HTML})
//	public String deleteDocumentById(@PathParam("id") String id) throws DocumentException, LogException {
//		if ( security.getUserPrincipal() == null ) {
//			throw new MappableContainerException(
//					new AuthenticationException(
//							"Authentication credentials are required\r\n",
//							"iServe SAL RESTful API"));
//		}
//
//		String userFoafId = security.getUserPrincipal().getName();
//
//		String uriString = ManagerSingleton.getInstance().deleteDocumentById(id);
//		// TODO: Push this down to the above method
//		ManagerSingleton.getInstance().log(userFoafId, LOG.ITEM_DELETING, uriString, new Date(), "REST");
//		String htmlString = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
//			"  <body>\nA document is removed from <a href='" + uriString + "'>" + uriString + "</a>\n  </body>\n</html>";
//		return htmlString;
//	}

    @DELETE
    @Path("/{id}")
    @Produces({MediaType.TEXT_HTML})
    public Response deleteDocument(@Context UriInfo uriInfo) throws DocumentException, LogException {

        // TODO: Re add security
//        if ( security.getUserPrincipal() == null ) {
//			throw new MappableContainerException(
//					new AuthenticationException(
//							"Authentication credentials are required\r\n",
//							"iServe SAL RESTful API"));
//		}
//
//		String userFoafId = security.getUserPrincipal().getName();

        try {
            boolean result = docManager.deleteDocument(uriInfo.getRequestUri());
        } catch (SalException e) {
            String error = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                    "  <body>\nThere was an error while deleting a document. Contact the system administrator. \n  </body>\n</html>";

            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
        // TODO: Push this down to the above method
//		ManagerSingleton.getInstance().log(userFoafId, LOG.ITEM_DELETING, uriString, new Date(), "REST");

        String htmlString = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                "  <body>\nA document is removed from <a href='" + uriInfo.getRequestUri() + "'>" + uriInfo.getRequestUri() + "</a>\n  </body>\n</html>";

        return Response.status(Status.GONE).contentLocation(uriInfo.getRequestUri()).entity(htmlString).build();
    }
}
