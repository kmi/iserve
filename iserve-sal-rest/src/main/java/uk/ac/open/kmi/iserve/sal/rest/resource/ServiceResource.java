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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
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

/**
 * TODO: Refactor based on the Manager Singleton.
 *
 * @author Dong Liu (Knowledge Media Institute - The Open University)
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
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


    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.TEXT_HTML})
    public Response addService(@FormDataParam("file") FormDataBodyPart bodyPart,
                               @FormDataParam("file") InputStream file,
                               @HeaderParam("Content-Location") String locationUri) {

        // Check first that the user is allowed to upload a service
        Subject currentUser = SecurityUtils.getSubject();
        if (!currentUser.isPermitted("services:create")) {
            log.warn("User without the appropriate permissions attempted to create a service: " + currentUser.getPrincipal());

            String htmlString = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                    "  <body>\n You have not got the appropriate permissions for creating a service. Please login and ensure you have the correct permissions. </body>\n</html>";

            return Response.status(Status.FORBIDDEN).entity(htmlString).build();
        }

        // The user is allowed to create services
        String mediaType = null;
        URI serviceUri;

        if (bodyPart != null) {
            mediaType = bodyPart.getMediaType().toString();
        } else {
            // TODO: we should obtain/guess the media type
        }

        ServiceFormat format = uk.ac.open.kmi.iserve.sal.MediaType.MEDIATYPE_FORMAT_MAP.get(mediaType);
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
                serviceUri = ManagerSingleton.getInstance().importService(file, format);
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
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    log.error("Problems while close the input stream with the service content", e);
                }
            }
        }
    }

    /**
     * Delete a given service
     * <p/>
     * From HTTP Method definition
     * 9.7 DELETE
     * A successful response SHOULD be
     * 200 (OK) if the response includes an entity describing the status,
     * 202 (Accepted) if the action has not yet been enacted, or
     * 204 (No Content) if the action has been enacted but the response does not include an entity.
     *
     * @return
     */
    @DELETE
    @Path("/{uniqueId}/{serviceName}")
    @Produces({MediaType.TEXT_HTML})
    public Response deleteService() {

        // Check first that the user is allowed to upload a service
        Subject currentUser = SecurityUtils.getSubject();
        if (!currentUser.isPermitted("services:delete")) {
            log.warn("User without the appropriate permissions attempted to delete a service: " + currentUser.getPrincipal());

            String htmlString = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                    "  <body>\n You have not got the appropriate permissions for deleting a service. Please login and ensure you have the correct permissions. </body>\n</html>";

            return Response.status(Status.FORBIDDEN).entity(htmlString).build();
        }

        URI serviceUri = uriInfo.getRequestUri();

        String response;
        try {
            if (!ManagerSingleton.getInstance().serviceExists(serviceUri)) {
                // The service doesn't exist
                response = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                        "  <body>\n The service " + serviceUri + " is not present in the registry.\n  </body>\n</html>";

                return Response.status(Status.NOT_FOUND).contentLocation(serviceUri).entity(response).build();
            }

            if (ManagerSingleton.getInstance().unregisterService(serviceUri)) {
                // The service was deleted
                response = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                        "  <body>\n The service <a href='" + serviceUri + "'>" + serviceUri + "</a> has been deleted from the server.\n  </body>\n</html>";

                return Response.status(Status.OK).contentLocation(serviceUri).entity(response).build();
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

//    @DELETE
//    @Produces({MediaType.TEXT_HTML})
//    public Response clearService() {
//
//    }

}
