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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wordnik.swagger.annotations.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.sal.exception.DocumentException;
import uk.ac.open.kmi.iserve.sal.exception.LogException;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.manager.RegistryManager;
import uk.ac.open.kmi.msm4j.io.impl.ServiceTransformationEngine;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;

@Path("/documents")
@Api(value = "/id/documents", description = "Operations about service documents", basePath = "id")
public class DocumentsResource {

    private final RegistryManager registryManager;
    @Context
    ServletContext context;
    @Context
    UriInfo uriInfo;
    @Context
    SecurityContext security;
    private Logger logger = LoggerFactory.getLogger(DocumentsResource.class);

    @Inject
    public DocumentsResource(RegistryManager registryManager) {
        this.registryManager = registryManager;
    }


    @GET
    @ApiOperation(value = "List all the service documents",
            notes = "Returns a list of service documents")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Documents Found"),
            @ApiResponse(code = 500, message = "Internal error")})
    @Produces({"application/json", "text/html"})
    public Response listDocuments() {
        try {
            Set<URI> result = registryManager.getDocumentManager().listDocuments();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(result);
            return Response.status(Status.OK).entity(json).build();
        } catch (DocumentException e) {
            e.printStackTrace();
            String error = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                    "  <body>\nThere was an error while loading documents. Contact the system administrator. \n  </body>\n</html>";
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @GET
    @ApiOperation(value = "Get a service document",
            notes = "Returns a service document")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Document Found"),
            @ApiResponse(code = 404, message = "Document not found"),
            @ApiResponse(code = 500, message = "Internal error")})
    @Produces({"*/*"})
    @Path("/{id: .*}")
    public Response getDocument(@Context UriInfo uriInfo,
                                @ApiParam(value = "Description ID", required = true)
                                @PathParam("id") String id,
                                @ApiParam(value = "Document Media type")
                                @HeaderParam("Accept") String accept) {
        try {
            logger.debug("Request: {}", uriInfo.getRequestUri());
            logger.debug("Using absolute path for document: {}", uriInfo.getAbsolutePath());
            logger.debug("Accept: {}", accept);
            URI docUri = uriInfo.getAbsolutePath(); // Drop any query parameters
            InputStream is = registryManager.getDocumentManager().getDocument(docUri);
            String docMediaType = registryManager.getDocumentManager().getDocumentMediaType(docUri);
            if (is != null) {
                if (accept.contains(MediaType.TEXT_HTML) && docMediaType.equals(MediaType.APPLICATION_JSON)) {
                    logger.debug("Generating Swagger UI");
                    String result = generateSwaggerUI(docUri);
                    return Response.status(Status.OK).entity(result).build();
                } else {
                    logger.debug("Document found!");
                    String result = IOUtils.toString(is, "UTF-8");
                    return Response.status(Status.OK).header("Content-Type", docMediaType).entity(result).build();
                }

            } else {
                String error = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                        "  <body>\nDocument not found.\n  </body>\n</html>";

                return Response.status(Status.NOT_FOUND).entity(error).build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            String error = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                    "  <body>\nThere was an error while retrieving the document. Contact the system administrator. \n  </body>\n</html>";
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    private String generateSwaggerUI(URI requestUri) {
        StringBuilder swaggerUiBuilder = new StringBuilder();
        swaggerUiBuilder
                .append("<!DOCTYPE html>\n")
                .append("<html>\n")
                .append("<head>\n")
                .append("<title>Service Document</title>\n")
                .append("<link href='//fonts.googleapis.com/css?family=DroidSans:400,700' rel='stylesheet' type='text/css'/>\n")
                .append("<link href='").append(context.getContextPath()).append("/docs/css/reset.css' media='screen' rel='stylesheet' type='text/css'/>\n")
                .append("<link href='").append(context.getContextPath()).append("/docs/css/screen.css' media='screen' rel='stylesheet' type='text/css'/>\n")
                .append("<link href='").append(context.getContextPath()).append("/docs/css/reset.css' media='print' rel='stylesheet' type='text/css'/>\n")
                .append("<link href='").append(context.getContextPath()).append("/docs/css/screen.css' media='print' rel='stylesheet' type='text/css'/>\n")
                .append("    <script type=\"text/javascript\" src=\"").append(context.getContextPath()).append("/docs/lib/shred.bundle.js\"></script>\n")
                .append("    <script src='").append(context.getContextPath()).append("/docs/lib/jquery-1.8.0.min.js' type='text/javascript'></script>\n")
                .append("    <script src='").append(context.getContextPath()).append("/docs/lib/jquery.slideto.min.js' type='text/javascript'></script>\n")
                .append("    <script src='").append(context.getContextPath()).append("/docs/lib/jquery.wiggle.min.js' type='text/javascript'></script>\n")
                .append("    <script src='").append(context.getContextPath()).append("/docs/lib/jquery.ba-bbq.min.js' type='text/javascript'></script>\n")
                .append("    <script src='").append(context.getContextPath()).append("/docs/lib/handlebars-1.0.0.js' type='text/javascript'></script>\n")
                .append("    <script src='").append(context.getContextPath()).append("/docs/lib/underscore-min.js' type='text/javascript'></script>\n")
                .append("    <script src='").append(context.getContextPath()).append("/docs/lib/backbone-min.js' type='text/javascript'></script>\n")
                .append("    <script src='").append(context.getContextPath()).append("/docs/lib/swagger.js' type='text/javascript'></script>\n")
                .append("    <script src='").append(context.getContextPath()).append("/docs/swagger-ui.js' type='text/javascript'></script>\n")
                .append("    <script src='").append(context.getContextPath()).append("/docs/lib/highlight.7.3.pack.js' type='text/javascript'></script>\n")
                .append("\n")
                .append("    <!-- enabling this will enable oauth2 implicit scope support -->\n")
                .append("    <script src='").append(context.getContextPath()).append("/docs/lib/swagger-oauth.js' type='text/javascript'></script>\n")
                .append("\n")
                .append("    <script type=\"text/javascript\">\n")
                .append("        $(function () {\n")
                .append("            window.swaggerUi = new SwaggerUi({\n")
                .append("                url: \"").append(requestUri).append("\",\n")
                .append("                dom_id: \"swagger-ui-container\",\n")
                .append("                supportedSubmitMethods: ['get', 'post', 'put', 'delete'],\n")
                .append("                onComplete: function (swaggerApi, swaggerUi) {\n")
                .append("                    log(\"Loaded SwaggerUI\");\n")
                .append("\n")
                .append("                    if (typeof initOAuth == \"function\") {\n")
                .append("                        /*\n")
                .append("                         initOAuth({\n")
                .append("                         clientId: \"your-client-id\",\n")
                .append("                         realm: \"your-realms\",\n")
                .append("                         appName: \"your-app-name\"\n")
                .append("                         });\n")
                .append("                         */\n")
                .append("                    }\n")
                .append("                    $('pre code').each(function (i, e) {\n")
                .append("                        hljs.highlightBlock(e)\n")
                .append("                    });\n")
                .append("                },\n")
                .append("                onFailure: function (data) {\n")
                .append("                    log(\"Unable to Load SwaggerUI\");\n")
                .append("                },\n")
                .append("                docExpansion: \"none\",\n")
                .append("                sorter: \"alpha\"\n")
                .append("            });\n")
                .append("\n")
                .append("            $('#input_apiKey').change(function () {\n")
                .append("                var key = $('#input_apiKey')[0].value;\n")
                .append("                log(\"key: \" + key);\n")
                .append("                if (key && key.trim() != \"\") {\n")
                .append("                    log(\"added key \" + key);\n")
                .append("                    window.authorizations.add(\"key\", new ApiKeyAuthorization(\"api_key\", key, \"query\"));\n")
                .append("                }\n")
                .append("            })\n")
                .append("            window.swaggerUi.load();\n")
                .append("        });\n")
                .append("    </script>\n")
                .append("</head>\n")
                .append("\n")
                .append("<body class=\"swagger-section\">\n")
                .append("<div id='header'>\n")
                .append("    <div class=\"swagger-ui-wrap\">\n")
                .append("    </div>\n")
                .append("</div>\n")
                .append("<br/>\n")
                .append("<div id=\"message-bar\" class=\"swagger-ui-wrap\">&nbsp;</div>\n")
                .append("<div id=\"swagger-ui-container\" class=\"swagger-ui-wrap\"></div>\n")
                .append("</body>\n")
                .append("</html>");
        return swaggerUiBuilder.toString();
    }


    @POST
    @ApiOperation(value = "Add a new service document",
            notes = "Returns a HTML document which contains the URI of the added document")
    @ApiResponses(
            value = {@ApiResponse(code = 201, message = "Created document"),
                    @ApiResponse(code = 500, message = "Internal error")})
    @Consumes({MediaType.TEXT_HTML, MediaType.TEXT_XML, MediaType.APPLICATION_XML, "application/rdf+xml",
            "text/turtle", "text/n3", "text/rdf+n3", MediaType.TEXT_PLAIN, "application/json", "application/wsdl+xml"})
    @Produces({MediaType.TEXT_HTML})
    public Response addDocument(
            String document,
            @ApiParam(value = "Document location", required = true)
            @HeaderParam("Content-Location") String locationUri,
            @ApiParam(value = "Document Media type", required = true)
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
        try {

            URI docUri;
            ServiceTransformationEngine transformationEngine = registryManager.getServiceTransformationEngine();
            if (document != null && !document.equals("")) {
                InputStream is = new ByteArrayInputStream(document.getBytes("UTF-8"));
                docUri = registryManager.getDocumentManager().createDocument(is, transformationEngine.getFileExtension(contentType), contentType);
            } else {
                docUri = registryManager.getDocumentManager().createDocument(new URI(locationUri), transformationEngine.getFileExtension(contentType), contentType);
            }

            String htmlString = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                    "  <body>\nA document is created at <a href='" + docUri.toString() + "'>" + docUri.toString() + "</a>\n  </body>\n</html>";

            return Response.status(Status.CREATED).contentLocation(docUri).entity(htmlString).build();
        } catch (Exception e) {
            String error = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                    "  <body>\nThere was an error while creating a document. Contact the system administrator. \n  </body>\n</html>";
            e.printStackTrace();
            logger.error(e.toString());

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
    @ApiOperation(value = "Delete a service document and its sub-documents",
            notes = "Returns a HTML document which confirms the deletion of the document")
    @ApiResponses(
            value = {@ApiResponse(code = 401, message = "Deleted document"),
                    @ApiResponse(code = 500, message = "Internal error")})
    @Produces({MediaType.TEXT_HTML})
    public Response deleteDocument(
            @Context UriInfo uriInfo,
            @ApiParam(value = "Description ID", required = true)
            @PathParam("id") String id
    ) throws DocumentException, LogException {

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
            boolean result = registryManager.getDocumentManager().deleteDocument(uriInfo.getRequestUri());
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
