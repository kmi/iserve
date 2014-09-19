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

import com.wordnik.swagger.annotations.*;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.manager.RegistryManager;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

/**
 * Services Resource
 *
 * @author Dong Liu (Knowledge Media Institute - The Open University)
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
@Path("/services")
@Api(value = "/id/services", description = "Operations about services", basePath = "id")
public class ServicesResource {

    private static final Logger log = LoggerFactory.getLogger(ServicesResource.class);
    private final RegistryManager manager;
    @Context
    UriInfo uriInfo;
    @Context
    SecurityContext security;

    @Inject
    public ServicesResource(RegistryManager registryManager) {
        this.manager = registryManager;
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.TEXT_HTML})
    @ApiOperation(value = "Add a new service",
            notes = "Returns a HTML document which contains the URI of the added service")
    @ApiResponses(
            value = {@ApiResponse(code = 201, message = "Created document"),
                    @ApiResponse(code = 403, message = "You have not got the appropriate permissions for creating a service"),
                    @ApiResponse(code = 500, message = "Internal error")})
    public Response addService(
            @ApiParam(value = "Service description passed as body part")
            @FormDataParam("bodyPart") FormDataBodyPart bodyPart,
            @ApiParam(value = "Service description passed as file")
            @FormDataParam("file") InputStream file,
            @ApiParam(value = "Service description passed as location URI")
            @HeaderParam("Content-Location") String locationUri) {

        log.debug("Invocation to addService - bodyPart {}, file {}, content-location {}",
                bodyPart, file, locationUri);

        // Check first that the user is allowed to upload a service
//        Subject currentUser = SecurityUtils.getSubject();
//        if (!currentUser.isPermitted("services:create")) {
//            log.warn("User without the appropriate permissions attempted to create a service: " + currentUser.getPrincipal());
//
//            String htmlString = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
//                    "  <body>\n You have not got the appropriate permissions for creating a service. Please login and ensure you have the correct permissions. </body>\n</html>";
//
//            return Response.status(Status.FORBIDDEN).entity(htmlString).build();
//        }

        // The user is allowed to create services
        String mediaType = null;
        List<URI> servicesUris;

        if (bodyPart != null) {
            mediaType = bodyPart.getMediaType().toString();
        } else {
            // TODO: we should obtain/guess the media type
        }

        try {
            if ((locationUri != null) && (!"".equalsIgnoreCase(locationUri))) {
                // There is a location. Just register, don't import
                log.info("Registering the services from {} ", locationUri);
                servicesUris = manager.registerServices(URI.create(locationUri), mediaType);
            } else {
                // There is no location. Import the entire service
                log.info("Importing the services");
                servicesUris = manager.importServices(file, mediaType);
            }
            //		String oauthConsumer = ((SecurityFilter.Authorizer) security).getOAuthConsumer();

            StringBuilder responseBuilder = new StringBuilder()
                    .append("<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n")
                    .append("<body>\n")
                    .append(servicesUris.size()).append(" services added.");

            for (URI svcUri : servicesUris) {
                responseBuilder.append("Service created at <a href='").append(svcUri).append("'>").append(svcUri).append("</a>\n");
            }

            responseBuilder.append("</body>\n</html>");

            return Response.status(Status.CREATED).entity(responseBuilder.toString()).build();
        } catch (ServiceException e) {
            String error = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                    "  <body>\nThere was an error while transforming the service descriptions: " + e.getMessage() + "\n  </body>\n</html>";

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
    @ApiOperation(value = "Delete a service",
            notes = "Returns a HTML document which confirms the service deletion")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Service deleted"),
                    @ApiResponse(code = 404, message = "Service not found"),
                    @ApiResponse(code = 304, message = "The service could not be deleted from the server"),
                    @ApiResponse(code = 403, message = "You have not got the appropriate permissions for deleting a service"),
                    @ApiResponse(code = 500, message = "Internal error")})
    public Response deleteService(
            @ApiParam(value = "Service ID", required = true)
            @PathParam("uniqueId") String uniqueId,
            @ApiParam(value = "Service name", required = true)
            @PathParam("serviceName") String serviceName
    ) {

        // Check first that the user is allowed to upload a service
//        Subject currentUser = SecurityUtils.getSubject();
//        if (!currentUser.isPermitted("services:delete")) {
//            log.warn("User without the appropriate permissions attempted to delete a service: " + currentUser.getPrincipal());
//
//            String htmlString = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
//                    "  <body>\n You have not got the appropriate permissions for deleting a service. Please login and ensure you have the correct permissions. </body>\n</html>";
//
//            return Response.status(Status.FORBIDDEN).entity(htmlString).build();
//        }

        URI serviceUri = uriInfo.getRequestUri();

        String response;
        try {
            if (!manager.getServiceManager().serviceExists(serviceUri)) {
                // The service doesn't exist
                response = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                        "  <body>\n The service " + serviceUri + " is not present in the registry.\n  </body>\n</html>";

                return Response.status(Status.NOT_FOUND).contentLocation(serviceUri).entity(response).build();
            }

            if (manager.unregisterService(serviceUri)) {
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

    /**
     * Clears the services registry
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
    @Produces({MediaType.TEXT_HTML})
    @ApiOperation(value = "Delete all the registered services",
            notes = "BE CAREFUL! You can lose all your data. It returns a HTML document which confirms all the service deletions.")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Registry cleaned. All the services are deleted."),
                    @ApiResponse(code = 304, message = "The services could not be cleared."),
                    @ApiResponse(code = 403, message = "You have not got the appropriate permissions for clearing the services"),
                    @ApiResponse(code = 500, message = "Internal error")})
    public Response clearServices() {

        // Check first that the user is allowed to upload a service
//        Subject currentUser = SecurityUtils.getSubject();
//        if (!currentUser.isPermitted("services:delete")) {
//            log.warn("User without the appropriate permissions attempted to clear the services: " + currentUser.getPrincipal());
//
//            String htmlString = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
//                    "  <body>\n You have not got the appropriate permissions for clearing the services. Please login and ensure you have the correct permissions. </body>\n</html>";
//
//            return Response.status(Response.Status.FORBIDDEN).entity(htmlString).build();
//        }

        URI serviceUri = uriInfo.getRequestUri();

        String response;
        try {
            if (manager.getServiceManager().clearServices()) {
                // The registry was cleared
                response = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                        "  <body>\n The services have been cleared.\n  </body>\n</html>";

                return Response.status(Response.Status.OK).contentLocation(serviceUri).entity(response).build();
            } else {
                // The registry was not cleared
                response = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                        "  <body>\n The services could not be cleared. Try again or contact a server administrator.\n  </body>\n</html>";

                return Response.status(Response.Status.NOT_MODIFIED).contentLocation(serviceUri).entity(response).build();
            }
        } catch (SalException e) {
            response = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                    "  <body>\nThere was an error while clearing the services. Contact the system administrator. \n  </body>\n</html>";

            // TODO: Add logging
            log.error("SAL Exception while deleting service", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
        }

    }

    @GET
    @Path("/{uniqueId}/{serviceName}")
    @Produces
            ({
                    "text/javascript"
                    , "application/javascript"
                    , "application/rdf+xml"
                    , "application/atom+xml"
                    , "application/json"
                    , "application/xml"
                    , "text/turtle"
                    , "text/html"
                    , "text/xml"
                    , "text/plain"
            })
    @ApiOperation(value = "Get a service")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Service found"),
                    @ApiResponse(code = 404, message = "Service not found"),
                    @ApiResponse(code = 403, message = "You have not got the appropriate permissions for deleting a service"),
                    @ApiResponse(code = 500, message = "Internal error")})
    public Response getServices(
            @ApiParam(value = "Service ID", required = true)
            @PathParam("uniqueId") String uniqueId,
            @ApiParam(value = "Service name", required = true)
            @PathParam("serviceName") String serviceName) {
        URI serviceUri = uriInfo.getRequestUri();
        return Response.status(Response.Status.OK).contentLocation(serviceUri).entity(serviceUri).build();
    }

}
