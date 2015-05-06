package uk.ac.open.kmi.iserve.rest.sal.resource;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gson.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.wordnik.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.rest.util.KnowledgeBaseResult;
import uk.ac.open.kmi.iserve.sal.manager.KnowledgeBaseManager;
import uk.ac.open.kmi.iserve.sal.manager.NfpManager;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * Created by Luca Panziera on 28/04/15.
 */
@Path("/")
@Api(value = "/kb", description = "Operations about knowledge base", basePath = "kb")
public class KnowledgeBaseResource {
    private KnowledgeBaseManager kbManager;
    private NfpManager nfpManager;
    private Logger log = LoggerFactory.getLogger(KnowledgeBaseResource.class);

    @Inject
    KnowledgeBaseResource(KnowledgeBaseManager knowledgeBaseManager, NfpManager nfpManager) {
        this.kbManager = knowledgeBaseManager;
        this.nfpManager = nfpManager;
    }

    @DELETE
    @Produces({"application/json"})
    @ApiOperation(value = "Clear all knowledge base",
            notes = "BE CAREFUL! You can lose all your data. It returns a message which confirms that the knowledge base is empty.")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Knowledge base cleaned. All the concepts are deleted."),
                    @ApiResponse(code = 304, message = "Knowledge base could not be cleared."),
                    @ApiResponse(code = 403, message = "You have not got the appropriate permissions for clearing the knowledge base"),
                    @ApiResponse(code = 500, message = "Internal error")})
    public Response clearServices() {
        if (kbManager.clearKnowledgeBase()) {
            return Response.status(Response.Status.OK).entity(createJsonMessage("Knowledge base cleaned. All the concepts are deleted.")).build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createJsonMessage("Internal error")).build();
        }

    }

    @GET
    @Path("subclasses")
    @Produces({"application/json"})
    @ApiOperation(value = "Get subclasses of a concept",
            notes = "It returns subclasses of a specific concept")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "List of subclasses"),
                    @ApiResponse(code = 400, message = "Malformed input"),
                    @ApiResponse(code = 500, message = "Internal error")})
    public Response getSubclasses(
            @ApiParam(value = "URI of the concept (e.g., http://schema.org/Action)", required = true)
            @QueryParam("uri") String uri,
            @ApiParam(value = "If this parameter is set as TRUE, the function will return only direct subclasses", allowableValues = "true,false")
            @QueryParam("direct") String direct) {
        try {
            Set<URI> subclasses = kbManager.listSubClasses(new URI(uri), Boolean.parseBoolean(direct));
            return Response.status(Response.Status.OK).entity(createJsonMessage(createResultSet(subclasses))).build();
        } catch (URISyntaxException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(createJsonMessage(e.getMessage())).build();
        }
    }


    @GET
    @Path("superclasses")
    @Produces({"application/json"})
    @ApiOperation(value = "Get superclasses of a concept",
            notes = "It returns superclasses of a specific concept")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "List of superclasses"),
                    @ApiResponse(code = 400, message = "Malformed input"),
                    @ApiResponse(code = 500, message = "Internal error")})
    public Response getSuperclasses(
            @ApiParam(value = "URI of the concept (e.g., http://schema.org/Action)", required = true)
            @QueryParam("uri") String uri,
            @ApiParam(value = "If this parameter is set as TRUE, the function will return only direct superclasses", allowableValues = "true,false")
            @QueryParam("direct") String direct) {
        try {
            Set<URI> classes = kbManager.listSuperClasses(new URI(uri), Boolean.parseBoolean(direct));
            return Response.status(Response.Status.OK).entity(createJsonMessage(createResultSet(classes))).build();
        } catch (URISyntaxException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(createJsonMessage(e.getMessage())).build();
        }
    }

    @GET
    @Path("equivalentClasses")
    @Produces({"application/json"})
    @ApiOperation(value = "List classes equivalent to a specific concept",
            notes = "It returns a list of classes")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "List of equivalent classes"),
                    @ApiResponse(code = 400, message = "Malformed input"),
                    @ApiResponse(code = 500, message = "Internal error")})
    public Response getEquivalentClasses(
            @ApiParam(value = "URI of the concept (e.g., http://schema.org/Action)", required = true)
            @QueryParam("uri") String uri
    ) {
        try {
            Set<URI> classes = kbManager.listEquivalentClasses(new URI(uri));
            return Response.status(Response.Status.OK).entity(createJsonMessage(createResultSet(classes))).build();
        } catch (URISyntaxException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(createJsonMessage(e.getMessage())).build();
        }
    }

    @GET
    @Produces({"application/json"})
    @ApiOperation(value = "List concepts in the knowledge base",
            notes = "It returns a list of concepts")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "List of concepts"),
                    @ApiResponse(code = 400, message = "Malformed input"),
                    @ApiResponse(code = 500, message = "Internal error")})
    public Response listConcepts(
            @ApiParam(value = "URI of the model. If empty it will return all the concepts in the Knowledge Base")
            @QueryParam("uri") String uri
    ) {
        try {
            Set<URI> classes;
            if (uri != null && !uri.equals("")) {
                classes = kbManager.listConcepts(new URI(uri));
            } else {
                classes = kbManager.listConcepts(null);
            }
            return Response.status(Response.Status.OK).entity(createJsonMessage(createResultSet(classes))).build();
        } catch (URISyntaxException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(createJsonMessage(e.getMessage())).build();
        }
    }

    @GET
    @Path("linkedConcepts")
    @Produces({"application/json"})
    @ApiOperation(value = "List concepts linked by a chain of properties",
            notes = "It returns subclasses of a specific concept")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Map of linked concepts"),
                    @ApiResponse(code = 400, message = "Malformed input"),
                    @ApiResponse(code = 500, message = "Internal error")})
    public Response listLinkedConcepts(
            @ApiParam(value = "Chain of properties that specify the link between concepts", required = true)
            @QueryParam("property") List<String> properties,
            @ApiParam(value = "Class of the source concept")
            @QueryParam("sourceClass") String sourceClass,
            @ApiParam(value = "Class of the source concept")
            @QueryParam("targetClass") String targetClass) {
        try {
            URI sourceClassUri = null;
            URI targetClassUri = null;
            Multimap<KnowledgeBaseResult, KnowledgeBaseResult> r = HashMultimap.create();
            if (sourceClass != null) {
                sourceClassUri = new URI(sourceClass);
            }
            if (targetClass != null) {
                targetClassUri = new URI(targetClass);
            }
            List<URI> propertyUris = Lists.newArrayList();
            for (String property : properties) {
                propertyUris.add(new URI(property));
            }
            Multimap<URI, URI> linkedConcepts = kbManager.listLinkedConcepts(propertyUris, sourceClassUri, targetClassUri);
            createResultSet(linkedConcepts.keySet());
            createResultSet(linkedConcepts.values());
            for (URI source : linkedConcepts.keySet()) {
                r.putAll(createResultSet(source), createResultSet(linkedConcepts.get(source)));
            }

            return Response.status(Response.Status.OK).entity(createJsonMessage(sourceClassUri, targetClassUri, propertyUris, r)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(createJsonMessage(e.getMessage())).build();
        }
    }

    @GET
    @Path("models")
    @Produces({"application/json"})
    @ApiOperation(value = "List models available in the knowledge base",
            notes = "It returns a list of models")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "List of models"),
                    @ApiResponse(code = 500, message = "Internal error")})
    public Response getLoadedModels() {
        Set<URI> r = kbManager.getLoadedModels();
        return Response.status(Response.Status.OK).entity(createJsonMessage(r)).build();
    }

    @POST
    @Path("models")
    @Consumes({"application/rdf+xml", "text/turtle", "text/n3", "text/rdf+n3"})
    @Produces({"application/json"})
    @ApiOperation(value = "Upload a model in the knowledge base",
            notes = "It returns a list of models")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Model uploaded"),
                    @ApiResponse(code = 400, message = "Malformed input"),
                    @ApiResponse(code = 500, message = "Internal error")})
    public Response uploadModel(@ApiParam(value = "URI of the remote ontology or URI that identifies a local model", required = true)
                                @QueryParam("uri") String uri,
                                @ApiParam(value = "Ontology to by uploaded in the knowledge base")
                                String ontology,
                                @ApiParam(value = "Force the upload of the model", allowableValues = "true,false")
                                @QueryParam("forceUpdate") String forceUpdate) {

        try {
            Model model = ModelFactory.createDefaultModel();
            if (ontology == null || ontology.equals("")) {
                model.read(uri);
            } else {
                model.read(new ByteArrayInputStream(ontology.getBytes(StandardCharsets.UTF_8)), uri);
            }

            kbManager.uploadModel(new URI(uri), model, Boolean.parseBoolean(forceUpdate));
            return Response.status(Response.Status.OK).entity(createJsonMessage("Model uploaded with success")).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity(createJsonMessage(e.getMessage())).build();
        }

    }

    @DELETE
    @Path("models")
    @Produces({"application/json"})
    @ApiOperation(value = "Delete a model from the knowledge base",
            notes = "It returns a list of models")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "List of models"),
                    @ApiResponse(code = 400, message = "Malformed input"),
                    @ApiResponse(code = 500, message = "Internal error")})
    public Response deleteModel(@ApiParam(value = "URI of the remote ontology or URI that identifies a local model", required = true)
                                @QueryParam("uri") String uri) {
        try {
            if (kbManager.deleteModel(new URI(uri))) {
                return Response.status(Response.Status.OK).entity(createJsonMessage("Model deleted with success")).build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity(createJsonMessage("Model not found")).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity(createJsonMessage(e.getMessage())).build();
        }

    }

    private String createJsonMessage(String text) {
        JsonObject message = new JsonObject();
        message.add("message", new JsonPrimitive(text));
        return message.toString();
    }

    private String createJsonMessage(Object o) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(o);
    }

    private String createJsonMessage(URI source, URI target, List<URI> properties, Multimap<KnowledgeBaseResult, KnowledgeBaseResult> map) {
        Gson gson = new Gson();
        JsonObject message = new JsonObject();
        if (source != null) {
            message.add("source", gson.toJsonTree(source));
        }
        if (target != null) {
            message.add("target", gson.toJsonTree(target));
        }
        if (properties != null) {
            message.add("properties", gson.toJsonTree(properties));
        }
        JsonArray resultList = new JsonArray();
        message.add("result", resultList);
        for (KnowledgeBaseResult s : map.keySet()) {
            JsonObject entry = new JsonObject();
            resultList.add(entry);
            JsonElement sJson = gson.toJsonTree(s);
            entry.add("source", sJson);
            JsonElement tJson = gson.toJsonTree(map.get(s));
            entry.add("target", tJson);
        }
        return message.toString();
    }

    private Set<KnowledgeBaseResult> createResultSet(Collection<URI> uris) {
        Set<KnowledgeBaseResult> r = Sets.newLinkedHashSet();
        Map<URI, Object> labels = nfpManager.getPropertyValueOfResources(new HashSet<URI>(uris), URI.create(RDFS.label.getURI()), String.class);
        for (URI uri : uris) {
            r.add(new KnowledgeBaseResult(uri, (String) labels.get(uri)));
        }
        return r;
    }

    private KnowledgeBaseResult createResultSet(URI uri) {
        String label = (String) nfpManager.getPropertyValue(uri, URI.create(RDFS.label.getURI()), String.class);
        return new KnowledgeBaseResult(uri, label);
    }

}