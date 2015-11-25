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

package uk.ac.open.kmi.iserve.rest.sal.resource;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.wordnik.swagger.annotations.*;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.manager.NfpManager;
import uk.ac.open.kmi.iserve.sal.manager.RegistryManager;
import uk.ac.open.kmi.iserve.sal.util.Triplet;
import uk.ac.open.kmi.msm4j.Service;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Services Resource
 *
 * @author Dong Liu (Knowledge Media Institute - The Open University)
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
@Path("services")
@Api(value = "/id/services", description = "Operations about services", basePath = "id")
public class ServicesResource {

    private static final Logger log = LoggerFactory.getLogger(ServicesResource.class);
    private final RegistryManager manager;
    private final NfpManager nfpManager;
    @Context
    UriInfo uriInfo;
    @Context
    SecurityContext security;

    @Inject
    public ServicesResource(RegistryManager registryManager, NfpManager nfpManager) {
        this.manager = registryManager;
        this.nfpManager = nfpManager;
    }

    @POST
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.TEXT_HTML, "application/json"})
    @ApiOperation(value = "Add a new service",
            notes = "Returns a message which contains the URI of the added service")
    @ApiResponses(
            value = {@ApiResponse(code = 201, message = "Created document"),
                    @ApiResponse(code = 403, message = "You have not got the appropriate permissions for creating a service"),
                    @ApiResponse(code = 500, message = "Internal error")})
    public Response addService(
            @ApiParam(value = "Service description passed as body part")
            @FormDataParam("file") FormDataBodyPart bodyPart,
            @ApiParam(value = "Service description passed as file")
            @FormDataParam("file") InputStream file,
            @ApiParam(value = "Service description passed as location URI")
            @QueryParam("store") Boolean store,
            @ApiParam(value = "Response message media type", allowableValues = "application/json,text/html")
            @HeaderParam("Accept") String accept
    ) {

        log.debug("Invocation to addService - bodyPart {}, file {}",
                bodyPart, file);

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

            if (store != null && !store) {
                log.info("Registering the services");
                servicesUris = manager.registerServices(file, mediaType);
            } else {
                log.info("Importing the services");
                servicesUris = manager.importServices(file, mediaType);
            }

            //		String oauthConsumer = ((SecurityFilter.Authorizer) security).getOAuthConsumer();
            if (accept.contains(MediaType.TEXT_HTML)) {
                StringBuilder responseBuilder = new StringBuilder()
                        .append("<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n")
                        .append("<body>\n")
                        .append(servicesUris.size()).append(" service(s) added.");

                for (URI svcUri : servicesUris) {
                    responseBuilder.append("Service created at <a href='").append(svcUri).append("'>").append(svcUri).append("</a>\n");
                }

                responseBuilder.append("</body>\n</html>");

                return Response.status(Status.CREATED).entity(responseBuilder.toString()).build();
            } else {
                Gson gson = new Gson();
                JsonObject message = new JsonObject();
                message.add("message", new JsonPrimitive(servicesUris.size() + " services added"));
                message.add("uris", gson.toJsonTree(servicesUris));
                return Response.status(Status.CREATED).entity(message.toString()).build();
            }
        } catch (ServiceException e) {
            String error;
            if (accept.contains(MediaType.TEXT_HTML)) {
                error = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                        "  <body>\nThere was an error while transforming the service descriptions: " + e.getMessage() + "\n  </body>\n</html>";
            } else {
                JsonObject message = new JsonObject();
                message.add("message", new JsonPrimitive("There was an error while transforming the service descriptions: " + e.getMessage()));
                error = message.toString();
            }
            // TODO: Add logging
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } catch (SalException e) {
            String error;
            if (accept.contains(MediaType.TEXT_HTML)) {
                error = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                        "  <body>\nThere was an error while storing the service: " + e.getMessage() + "\n  </body>\n</html>";
            } else {
                JsonObject message = new JsonObject();
                message.add("message", new JsonPrimitive("TThere was an error while storing the service: " + e.getMessage()));
                error = message.toString();
            }
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

    @POST
    @Consumes({MediaType.TEXT_HTML, MediaType.TEXT_XML, MediaType.APPLICATION_XML, "application/rdf+xml",
            "text/turtle", "text/n3", "text/rdf+n3", MediaType.TEXT_PLAIN, "application/json", "application/wsdl+xml"})
    @Produces({MediaType.TEXT_HTML, "application/json"})
    @ApiOperation(value = "Add a new service from a document available on the Web",
            notes = "Returns a massage which contains the URI of the added service")
    @ApiResponses(
            value = {@ApiResponse(code = 201, message = "Created document"),
                    @ApiResponse(code = 403, message = "You have not got the appropriate permissions for creating a service"),
                    @ApiResponse(code = 500, message = "Internal error")})
    public Response addService(
            @ApiParam(value = "URI of service description", required = true)
            @HeaderParam("Content-Location") String locationUri,
            @ApiParam(value = "Service description Media type", required = true)
            @HeaderParam("Content-Type") String mediaType,
            @ApiParam(value = "Service description passed as location URI")
            @QueryParam("store") Boolean store,
            @ApiParam(value = "Response message media type", allowableValues = "application/json,text/html")
            @HeaderParam("Accept") String accept
    ) {

        log.debug("Invocation to addService from {}", locationUri);

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
        List<URI> servicesUris;

        try {
            if (store != null && store) {
                log.info("Importing the services");
                servicesUris = manager.importServices(URI.create(locationUri), mediaType);
            } else {
                log.info("Registering the services from {} ", locationUri);
                servicesUris = manager.registerServices(URI.create(locationUri), mediaType);
            }
            //		String oauthConsumer = ((SecurityFilter.Authorizer) security).getOAuthConsumer();
            if (accept.contains(MediaType.TEXT_HTML)) {
                StringBuilder responseBuilder = new StringBuilder()
                        .append("<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n")
                        .append("<body>\n")
                        .append(servicesUris.size()).append(" services added.");

                for (URI svcUri : servicesUris) {
                    responseBuilder.append("Service created at <a href='").append(svcUri).append("'>").append(svcUri).append("</a>\n");
                }

                responseBuilder.append("</body>\n</html>");

                return Response.status(Status.CREATED).entity(responseBuilder.toString()).build();
            } else {
                Gson gson = new Gson();
                JsonObject message = new JsonObject();
                message.add("message", new JsonPrimitive(servicesUris.size() + " services added"));
                message.add("uris", gson.toJsonTree(servicesUris));
                return Response.status(Status.CREATED).entity(message.toString()).build();
            }

        } catch (ServiceException e) {
            String error;
            if (accept.contains(MediaType.TEXT_HTML)) {
                error = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                        "  <body>\nThere was an error while transforming the service descriptions: " + e.getMessage() + "\n  </body>\n</html>";
            } else {
                JsonObject message = new JsonObject();
                message.add("message", new JsonPrimitive("There was an error while transforming the service descriptions: " + e.getMessage()));
                error = message.toString();
            }
            // TODO: Add logging
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } catch (SalException e) {
            String error;
            if (accept.contains(MediaType.TEXT_HTML)) {
                error = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                        "  <body>\nThere was an error while storing the service: " + e.getMessage() + "\n  </body>\n</html>";
            } else {
                JsonObject message = new JsonObject();
                message.add("message", new JsonPrimitive("TThere was an error while storing the service: " + e.getMessage()));
                error = message.toString();
            }
            // TODO: Add logging
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }


    /**
     * Delete a given service
     *
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
    @Produces({MediaType.TEXT_HTML, "application/json"})
    @ApiOperation(value = "Delete a service",
            notes = "Returns a message which confirms the service deletion")
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
            @PathParam("serviceName") String serviceName,
            @ApiParam(value = "Response message media type", allowableValues = "application/json,text/html")
            @HeaderParam("Accept") String accept
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
                if (accept.contains(MediaType.TEXT_HTML)) {
                    response = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                            "  <body>\n The service " + serviceUri + " is not present in the registry.\n  </body>\n</html>";
                } else {
                    JsonObject message = new JsonObject();
                    message.add("message", new JsonPrimitive("The service " + serviceUri + " is not present in the registry."));
                    response = message.toString();
                }
                return Response.status(Status.NOT_FOUND).contentLocation(serviceUri).entity(response).build();
            }

            if (manager.unregisterService(serviceUri)) {
                // The service was deleted
                if (accept.contains(MediaType.TEXT_HTML)) {
                    response = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                            "  <body>\n The service <a href='" + serviceUri + "'>" + serviceUri + "</a> has been deleted from the server.\n  </body>\n</html>";

                } else {
                    JsonObject message = new JsonObject();
                    message.add("message", new JsonPrimitive("The service " + serviceUri + " has been deleted from the server."));
                    message.add("uri", new JsonPrimitive(serviceUri.toASCIIString()));
                    response = message.toString();
                }

                return Response.status(Status.OK).contentLocation(serviceUri).entity(response).build();
            } else {
                // The service was not deleted
                if (accept.contains(MediaType.TEXT_HTML)) {
                    response = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                            "  <body>\n The service <a href='" + serviceUri + "'>" + serviceUri + "</a> could not be deleted from the server. Try again or contact a server administrator.\n  </body>\n</html>";
                } else {
                    JsonObject message = new JsonObject();
                    message.add("message", new JsonPrimitive("The service " + serviceUri + " could not be deleted from the server. Try again or contact a server administrator."));
                    response = message.toString();
                }

                return Response.status(Status.NOT_MODIFIED).contentLocation(serviceUri).entity(response).build();
            }
        } catch (SalException e) {
            if (accept.contains(MediaType.TEXT_HTML)) {
                response = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                        "  <body>\nThere was an error while deleting the service. Contact the system administrator. \n  </body>\n</html>";
            } else {
                JsonObject message = new JsonObject();
                message.add("message", new JsonPrimitive("There was an error while deleting the service. Contact the system administrator."));
                response = message.toString();
            }
            // TODO: Add logging
            log.error("SAL Exception while deleting service", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(response).build();
        }

    }

    /**
     * Clears the services registry
     *
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
    @Produces({MediaType.TEXT_HTML, "application/json"})
    @ApiOperation(value = "Delete all the registered services",
            notes = "BE CAREFUL! You can lose all your data. It returns a message which confirms all the service deletions.")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Registry cleaned. All the services are deleted."),
                    @ApiResponse(code = 304, message = "The services could not be cleared."),
                    @ApiResponse(code = 403, message = "You have not got the appropriate permissions for clearing the services"),
                    @ApiResponse(code = 500, message = "Internal error")})
    public Response clearServices(
            @ApiParam(value = "Response message media type", allowableValues = "application/json,text/html")
            @HeaderParam("Accept") String accept) {

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
                if (accept.contains(MediaType.TEXT_HTML)) {
                    response = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                            "  <body>\n The services have been cleared.\n  </body>\n</html>";
                } else {
                    JsonObject message = new JsonObject();
                    message.add("message", new JsonPrimitive("The services have been cleared."));
                    response = message.toString();
                }
                return Response.status(Status.OK).contentLocation(serviceUri).entity(response).build();
            } else {
                // The registry was not cleared
                if (accept.contains(MediaType.TEXT_HTML)) {
                response = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                        "  <body>\n The services could not be cleared. Try again or contact a server administrator.\n  </body>\n</html>";
                } else {
                    JsonObject message = new JsonObject();
                    message.add("message", new JsonPrimitive("The services could not be cleared. Try again or contact a server administrator."));
                    response = message.toString();
                }
                return Response.status(Status.NOT_MODIFIED).contentLocation(serviceUri).entity(response).build();
            }
        } catch (SalException e) {
            if (accept.contains(MediaType.TEXT_HTML)) {
                response = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                        "<body>\nThere was an error while clearing the services. Contact the system administrator. \n  </body>\n</html>";
            } else {
                JsonObject message = new JsonObject();
                message.add("message", new JsonPrimitive("There was an error while clearing the services. Contact the system administrator."));
                response = message.toString();
            }
            // TODO: Add logging
            log.error("SAL Exception while deleting service", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(response).build();
        }

    }


    @GET
    @Produces
            ({
                    "application/rdf+xml"
                    , "application/atom+xml"
                    , "application/json"
                    , "application/xml"
                    , "text/turtle"
                    , "text/html"
                    , "text/xml"
                    , "text/plain"
            })
    @ApiOperation(value = "List all services", response = Service.class)
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Services found"),
                    @ApiResponse(code = 404, message = "Services not found"),
                    @ApiResponse(code = 403, message = "You have not got the appropriate permissions for reading services"),
                    @ApiResponse(code = 500, message = "Internal error")})
    public Response getServices() {
        URI serviceUri = uriInfo.getRequestUri();
        return Response.status(Status.OK).contentLocation(serviceUri).entity(serviceUri).build();
    }

    @GET
    @Path("/{uniqueId}/{serviceName}")
    @Produces
            ({
                    "application/rdf+xml"
                    , "application/atom+xml"
                    , "application/json"
                    , "application/xml"
                    , "text/turtle"
                    , "text/html"
                    , "text/xml"
                    , "text/plain"
            })
    @ApiOperation(value = "Get a service", response = Service.class)
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Service found"),
                    @ApiResponse(code = 404, message = "Service not found"),
                    @ApiResponse(code = 403, message = "You have not got the appropriate permissions for reading the service"),
                    @ApiResponse(code = 500, message = "Internal error")})
    public Response getService(
            @ApiParam(value = "Service ID", required = true)
            @PathParam("uniqueId") String uniqueId,
            @ApiParam(value = "Service name", required = true)
            @PathParam("serviceName") String serviceName) {
        URI serviceUri = uriInfo.getRequestUri();
        return Response.status(Status.OK).contentLocation(serviceUri).entity(serviceUri).build();
    }

    @POST
    @Path("/{uniqueId}/{serviceName}/property")
    @Produces({MediaType.TEXT_HTML, "application/json"})
    @ApiOperation(value = "Add a new property to the service",
            notes = "Returns a message which confirms the property storage")
    @ApiResponses(
            value = {@ApiResponse(code = 201, message = "Stored property"),
                    @ApiResponse(code = 403, message = "You have not got the appropriate permissions for storing the property"),
                    @ApiResponse(code = 500, message = "Internal error")})
    public Response setProperty(@ApiParam(value = "Service ID", required = true)
                                @PathParam("uniqueId") String uniqueId,
                                @ApiParam(value = "Service name", required = true)
                                @PathParam("serviceName") String serviceName,
                                @ApiParam(value = "URI that identifies a service resource to be associated with the property (e.g., operation or input message). If not specifies the property will be associated with the main service resource.")
                                @QueryParam("resource") String resource,
                                @ApiParam(value = "URI that identifies a property (e.g., http://www.w3.org/ns/sawsdl#modelReference)", required = true)
                                @QueryParam("property") String property,
                                @ApiParam(value = "Value of the property. The value can be a URI, a string or a number", required = true)
                                @QueryParam("value") String value,
                                @ApiParam(value = "Response message media type", allowableValues = "application/json,text/html")
                                @HeaderParam("Accept") String accept) {
        URI resourceUri;
        if (resource != null && !resource.equals("")) {
            try {
                resourceUri = new URI(resource);
            } catch (URISyntaxException e) {
                return Response.status(Status.BAD_REQUEST).entity(buildErrorMessageByMediaType("The service resource must be specified as a URI", accept)).build();
            }
        } else {
            resourceUri = uriInfo.getBaseUri().resolve("services/" + uniqueId + "/" + serviceName);
        }

        URI propertyUri;
        try {
            propertyUri = new URI(property);
        } catch (URISyntaxException e) {
            return Response.status(Status.BAD_REQUEST).entity(buildErrorMessageByMediaType("The service property must be specified as a URI", accept)).build();
        }
        Object valueObj;
        try {
            valueObj = new Double(value);
        } catch (NumberFormatException nfe) {
            try {
                valueObj = new URI(value);
                if (!((URI) valueObj).isAbsolute()) {
                    valueObj = value;
                }
            } catch (URISyntaxException e) {
                valueObj = value;
            }
        }

        try {
            nfpManager.createPropertyValue(resourceUri, propertyUri, valueObj);

        } catch (SalException e) {
            return Response.status(Status.BAD_REQUEST).entity(buildErrorMessageByMediaType(e.getMessage(), accept)).build();
        }

        return Response.status(Status.CREATED).entity(buildSuccessMessageForPropertyStorage(resourceUri.toASCIIString(), property, value, accept)).build();
    }

    private String buildSuccessMessageForPropertyStorage(String resource, String property, String value, String accept) {
        String response;
        if (accept != null && accept.contains(MediaType.TEXT_HTML)) {
            response = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                    "<body>\nProperty stored.\n</body>\n</html>";
        } else {
            JsonObject messageObj = new JsonObject();
            messageObj.add("message", new JsonPrimitive("Property stored"));
            JsonObject triple = new JsonObject();
            triple.add("resource", new JsonPrimitive(resource));
            triple.add("property", new JsonPrimitive(property));
            triple.add("value", new JsonPrimitive(value));
            messageObj.add("statement", triple);
            response = messageObj.toString();
        }
        return response;
    }

    private String buildErrorMessageByMediaType(String message, String accept) {
        String response;
        if (accept.contains(MediaType.TEXT_HTML)) {
            response = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                    "<body>\n" + message + "\n</body>\n</html>";
        } else {
            JsonObject messageObj = new JsonObject();
            messageObj.add("message", new JsonPrimitive(message));
            response = messageObj.toString();
        }
        return response;
    }

    @POST
    @Path("/{uniqueId}/{serviceName}/properties")
    @Produces({MediaType.TEXT_HTML, "application/json"})
    @ApiOperation(value = "Add a new property to the service",
            notes = "Returns a message which confirms the property storage")
    @ApiResponses(
            value = {@ApiResponse(code = 201, message = "Stored property"),
                    @ApiResponse(code = 403, message = "You have not got the appropriate permissions for storing the property"),
                    @ApiResponse(code = 500, message = "Internal error")})
    public Response setProperties(
            @ApiParam(value = "JSON document that specifies the graphURI and the subject-property-object updates", required = true)
            String jsonReq,
            @ApiParam(value = "Service ID", required = true)
            @PathParam("uniqueId") String uniqueId,
            @ApiParam(value = "Service name", required = true)
            @PathParam("serviceName") String serviceName,
            @ApiParam(value = "Response message media type", allowableValues = "application/json,text/html")
            @HeaderParam("Accept") String accept) {

        JsonElement jsonSpoEntries = new JsonParser().parse(jsonReq);

        // Obtain the graphUri
        URI graphUri = uriInfo.getBaseUri().resolve("services/" + uniqueId);
        List<Triplet<URI, URI, Object>> spoEntries = parseSpoUpdates(jsonSpoEntries);

        log.debug("Uploading {} properties to graph", spoEntries.size());

        if (graphUri != null && spoEntries != null && !spoEntries.isEmpty()) {
            try {
                nfpManager.createPropertyValues(graphUri, spoEntries);
            } catch (SalException e) {
                return Response.status(Status.BAD_REQUEST).entity(buildErrorMessageByMediaType(e.getMessage(), accept)).build();
            }
            return Response.status(Status.CREATED).entity(buildSuccessMessageForPropertiesStorage(graphUri, spoEntries, accept)).build();
        } else {
            return Response.status(Status.BAD_REQUEST).entity(buildErrorMessageByMediaType("No content to update.", accept)).build();
        }
    }


    /**
     * Given a JSON Element parse it into a Table of SPO Entries.
     * The JSON received should be of the form:
     *  {updates: [
     *      {subject: .., property: ..., object: ...},
     *      {subject: .., property: ..., object: ...},
     *      {subject: .., property: ..., object: ...}
     *  ]}
     *
     * @param jsonSpoEntries
     * @return
     */
    private List<Triplet<URI, URI, Object>> parseSpoUpdates(JsonElement jsonSpoEntries) {

        if (jsonSpoEntries == null || jsonSpoEntries.isJsonNull()) {
            return ImmutableList.of();
        }

        ImmutableList.Builder<Triplet<URI, URI, Object>> result = ImmutableList.builder();
        JsonObject request = jsonSpoEntries.getAsJsonObject();
        JsonArray updates = request.getAsJsonArray("updates");

        JsonObject update;
        URI subject;
        URI property;
        Object object = null;
        for (JsonElement updateEntry : updates) {
            update = updateEntry.getAsJsonObject();
            if (update.has("subject") && update.has("property") && update.has("object")) {
                try {
                    subject = new URI(update.get("subject").getAsString());
                    property = new URI(update.get("property").getAsString());
                    JsonPrimitive objectElmt = update.get("object").getAsJsonPrimitive();
                    if (objectElmt.isNumber()) {
                        object = (Double) objectElmt.getAsDouble();
                    } else if (objectElmt.isString()) {
                        // Try with a URL and fallback to String if it is not
                        object = new URI(objectElmt.getAsString());
                        if (!((URI)object).isAbsolute()) {
                            object = objectElmt.getAsString();
                        }
                    }

                    result.add(new Triplet(subject, property, object));

                } catch (URISyntaxException e) {
                    log.warn("Skipping entry...");
                    e.printStackTrace();
                }
            }
        }

        // Build the result
        return result.build();
    }

    private String buildSuccessMessageForPropertiesStorage(URI graphUri, List<Triplet<URI, URI, Object>> spoEntries, String accept) {
        String response;
        StringBuilder builder = new StringBuilder();
        if (accept != null && accept.contains(MediaType.TEXT_HTML)) {
            builder.append("<html>\n").
                    append("<head>\n").
                    append("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"> \n").
                    append("</head>\n").
                    append("<body>\n").
                    append("Graph ").append(graphUri.toASCIIString()).
                    append(" updated with ").append(spoEntries.size()).append(" new property values").
                    append("</body>\n").
                    append("</html>");
            response = builder.toString();
        } else {
            JsonObject messageObj = new JsonObject();
            messageObj.add("message", new JsonPrimitive(spoEntries.size() + " properties stored in graph " + graphUri.toASCIIString()));
//            JsonObject triple = new JsonObject();
//            triple.add("resource", new JsonPrimitive(resource));
//            triple.add("property", new JsonPrimitive(property));
//            triple.add("value", new JsonPrimitive(value));
//            messageObj.add("statement", triple);
            response = messageObj.toString();
        }
        return response;
    }

    @DELETE
    @Path("/{uniqueId}/{serviceName}/property")
    @Produces({MediaType.TEXT_HTML, "application/json"})
    @ApiOperation(value = "Delete a property of the service",
            notes = "Returns a message which confirms the property deletion")
    @ApiResponses(
            value = {@ApiResponse(code = 200, message = "Property deleted"),
                    @ApiResponse(code = 403, message = "You have not got the appropriate permissions for deleting the property"),
                    @ApiResponse(code = 500, message = "Internal error")})
    public Response deleteProperty(@ApiParam(value = "Service ID", required = true)
                                   @PathParam("uniqueId") String uniqueId,
                                   @ApiParam(value = "Service name", required = true)
                                   @PathParam("serviceName") String serviceName,
                                   @ApiParam(value = "URI that identifies a service resource to be associated with the property (e.g., operation or input message). If not specifies the property will be associated with the main service resource.")
                                   @QueryParam("resource") String resource,
                                   @ApiParam(value = "URI that identifies a property (e.g., http://www.w3.org/ns/sawsdl#modelReference)", required = true)
                                   @QueryParam("property") String property,
                                   @ApiParam(value = "Value of the property. The value can be a URI, a string or a number", required = true)
                                   @QueryParam("value") String value,
                                   @ApiParam(value = "Response message media type", allowableValues = "application/json,text/html")
                                   @HeaderParam("Accept") String accept) {
        URI resourceUri;
        if (resource != null && !resource.equals("")) {
            try {
                resourceUri = new URI(resource);
            } catch (URISyntaxException e) {
                return Response.status(Status.BAD_REQUEST).entity(buildErrorMessageByMediaType("The service resource must be specified as a URI", accept)).build();
            }
        } else {
            resourceUri = uriInfo.getBaseUri().resolve("services/" + uniqueId + "/" + serviceName);
        }

        URI propertyUri;
        try {
            propertyUri = new URI(property);
        } catch (URISyntaxException e) {
            return Response.status(Status.BAD_REQUEST).entity(buildErrorMessageByMediaType("The service property must be specified as a URI", accept)).build();
        }
        Object valueObj;
        try {
            valueObj = new Double(value);
        } catch (NumberFormatException nfe) {
            try {
                valueObj = new URI(value);
            } catch (URISyntaxException e) {
                valueObj = value;
            }
        }

        try {
            nfpManager.deletePropertyValue(resourceUri, propertyUri, valueObj);

        } catch (SalException e) {
            return Response.status(Status.BAD_REQUEST).entity(buildErrorMessageByMediaType(e.getMessage(), accept)).build();
        }

        return Response.status(Status.OK).entity(buildSuccessMessageForPropertyDelete(resourceUri.toASCIIString(), property, value, accept)).build();
    }

    private String buildSuccessMessageForPropertyDelete(String resource, String property, String value, String accept) {
        String response;
        if (accept.contains(MediaType.TEXT_HTML)) {
            response = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                    "<body>\nProperty deleted.\n</body>\n</html>";
        } else {
            JsonObject messageObj = new JsonObject();
            messageObj.add("message", new JsonPrimitive("Property deleted"));
            JsonObject triple = new JsonObject();
            triple.add("resource", new JsonPrimitive(resource));
            triple.add("property", new JsonPrimitive(property));
            triple.add("value", new JsonPrimitive(value));
            messageObj.add("statement", triple);
            response = messageObj.toString();
        }
        return response;
    }
}
