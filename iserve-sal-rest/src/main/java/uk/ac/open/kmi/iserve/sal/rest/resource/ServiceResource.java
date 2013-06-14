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

import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.MultiPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.sal.ServiceFormat;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

/**
 * TODO: Refactor based on the Manager Singleton.
 *
 * @author Dong Liu (Knowledge Media Institute - The Open University)
 */
@Path("/services")
public class ServiceResource {

    private static final Logger log = LoggerFactory.getLogger(ServiceResource.class);

    @Context
    UriInfo uriInfo;

    @Context
    SecurityContext security;

    public ServiceResource() {

    }


//    @POST
//	@Consumes({MediaType.TEXT_HTML, MediaType.TEXT_XML, MediaType.APPLICATION_XML, "application/rdf+xml",
//		"text/turtle", "text/n3", "text/rdf+n3", MediaType.TEXT_PLAIN})
//    @Produces({MediaType.TEXT_HTML})
//    public Response addService(String serviceContent, @HeaderParam("Content-Location") String locationUri,
//			@HeaderParam("Content-Type") String contentType) throws ServiceException, URISyntaxException, LogException {

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.TEXT_HTML})
    public Response addService(MultiPart content,
                               @HeaderParam("Content-Location") String locationUri) {

        // TODO: implement again the security
//		if ( security.getUserPrincipal() == null ) {
//			throw new MappableContainerException(
//					new AuthenticationException(
//							"Authentication credentials are required\r\n",
//							"iServe SAL RESTful API"));
//		}
//
//		String userFoafId = security.getUserPrincipal().getName();


//        // Obtain content type and charset. Any reusable code for this?
//        int index = contentType.indexOf(';');
//        if (index > 0) {
//            mediaType = contentType.substring(0,index).trim();
//            String charsetPart = contentType.substring(index + 1);
//            index = charsetPart.indexOf("=");
//            charset = charsetPart.substring(index + 1).trim();
//        }

        String mediaType = null;
        URI serviceUri;
        InputStream input = null;

        List<BodyPart> parts = content.getBodyParts();
        if (parts != null && !parts.isEmpty()) {
            mediaType = parts.get(0).getMediaType().getType() + "/" + parts.get(0).getMediaType().getSubtype();
        } else {
            // TODO: we should obtain/guess the media type
        }

        ServiceFormat format = uk.ac.open.kmi.iserve.sal.MediaType.MEDIATYPE_FORMAT_MAP.get(mediaType);
//        ServiceFormat format = ServiceFormat.MSM_TTL; //FIXME
        if (format == null) {
            format = ServiceFormat.UNSUPPORTED;
        }

        try {
            if ((locationUri != null) && (!"".equalsIgnoreCase(locationUri))) {
                // There is a location. Just register, don't import
                log.info("Registering the service: " + locationUri);
                serviceUri = ManagerSingleton.getInstance().registerService(URI.create(locationUri), format);
            } else {
                // There is no location. Import the entire service
                log.info("Importing the service");
                BodyPartEntity bpe = (BodyPartEntity) parts.get(0).getEntity();
                input = bpe.getInputStream();
                serviceUri = ManagerSingleton.getInstance().importService(input, format);
            }
            //		String oauthConsumer = ((SecurityFilter.Authorizer) security).getOAuthConsumer();

            String htmlString = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                    "  <body>\nA service is created at <a href='" + serviceUri + "'>" + serviceUri + "</a>\n  </body>\n</html>";

            return Response.status(Status.CREATED).contentLocation(serviceUri).entity(htmlString).build();
        } catch (ServiceException e) {
            String error = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                    "  <body>\nThere was an error while transforming the service: " + e.getMessage() + "\n  </body>\n</html>";

            // TODO: Add logging
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } catch (SalException e) {
            String error = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                    "  <body>\nThere was an error while storing the service: " + e.getMessage() + "\n  </body>\n</html>";

            // TODO: Add logging
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    log.error("Problems while close the input stream with the service content", e);
                }
            }
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces({MediaType.TEXT_HTML})
    public Response deleteService() {

        // TODO: Get the actual format of the service description
//        if ( security.getUserPrincipal() == null ) {
//			throw new MappableContainerException(
//					new AuthenticationException(
//							"Authentication credentials are required\r\n",
//							"iServe SAL RESTful API"));
//		}
//
//		String userFoafId = security.getUserPrincipal().getName();

        URI serviceUri = uriInfo.getAbsolutePath();
        String response;
        try {
            if (ManagerSingleton.getInstance().unregisterService(serviceUri)) {
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
            log.error("SAL Exception while deleting service", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(response).build();
        }

    }
}
