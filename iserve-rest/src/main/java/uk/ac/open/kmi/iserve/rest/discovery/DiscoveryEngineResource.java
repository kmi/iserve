package uk.ac.open.kmi.iserve.rest.discovery;

/**
 * Created by Luca Panziera on 29/05/2014.
 */

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.wordnik.swagger.annotations.*;
import org.apache.abdera.model.Feed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.discovery.api.DiscoveryEngine;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.OperationDiscoverer;
import uk.ac.open.kmi.iserve.discovery.api.ServiceDiscoverer;
import uk.ac.open.kmi.iserve.discovery.api.freetextsearch.FreeTextSearchPlugin;
import uk.ac.open.kmi.iserve.discovery.api.ranking.*;
import uk.ac.open.kmi.iserve.discovery.util.Pair;
import uk.ac.open.kmi.iserve.rest.util.AbderaAtomFeedProvider;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/")
@Api(value = "/discovery", description = "Service discovery operations", basePath = "discovery")
@Produces({"application/atom+xml", "application/json"})
public class DiscoveryEngineResource {

    @Context
    UriInfo uriInfo;
    @Context
    HttpServletRequest request;
    private Logger logger = LoggerFactory.getLogger(DiscoveryEngineResource.class);
    private DiscoveryEngine discoveryEngine;
    private DiscoveryResultsBuilderPlugin discoveryResultsBuilder;

    @Inject
    DiscoveryEngineResource(ServiceDiscoverer serviceDiscoverer, OperationDiscoverer operationDiscoverer, Set<Filter> filters, Set<AtomicFilter> atomicFilters, Set<Scorer> scorers, Set<AtomicScorer> atomicScorers, ScoreComposer scoreComposer, DiscoveryResultsBuilderPlugin discoveryResultsBuilder, FreeTextSearchPlugin freeTextSearchPlugin) {
        discoveryEngine = new DiscoveryEngine(serviceDiscoverer, operationDiscoverer, freeTextSearchPlugin, filters, atomicFilters, scorers, atomicScorers, scoreComposer);
        this.discoveryResultsBuilder = discoveryResultsBuilder;
    }

    @GET
    @Path("{type}/func-rdfs")
    @ApiOperation(
            value = "Get services or operations classified by specific RDF classes.",
            notes = "The search is performed through RDFS-based reasoning.",
            response = DiscoveryResult.class

    )
    @Produces({"application/atom+xml", "application/json"})
    public Response classificationBasedDiscovery(
            @ApiParam(value = "Parameter indicating the type of item to discover. The only values accepted are \"op\" for discovering operations, and \"svc\" for discovering services.", required = true, allowableValues = "svc,op")
            @PathParam("type") String type,
            @ApiParam(value = "Type of matching. The value should be either \"and\" or \"or\". The result should be the set- based conjunction or disjunction depending on the classes selected.", allowableValues = "and,or")
            @QueryParam("f") String function,
            @ApiParam(value = "Multivalued parameter indicating the functional classifications to match. The class should be the URL of the concept to match. This URL should be URL encoded.", required = true, allowMultiple = true)
            @QueryParam("class") List<String> resources,
            @ApiParam(value = "Filtering according to specific criteria.", allowableValues = "disabled,enabled")
            @DefaultValue("disabled") @QueryParam("filtering") String filtering,
            @ApiParam(value = "Popularity-based ranking. The value should be \"standard\" to rank the results according the popularity of the provider.", allowableValues = "standard,inverse")
            @QueryParam("ranking") String rankingType
    ) throws
            WebApplicationException {

        if (request.getHeader("Accept") != null && request.getHeader("Accept").equals("application/json")) {
            return classificationBasedDiscoveryAsJson(type, function, resources, rankingType, filtering);
        }
        return classificationBasedDiscoveryAsAtom(type, function, resources, rankingType, filtering);

    }

    public Response classificationBasedDiscoveryAsAtom(
            String type,
            String function,
            List<String> resources,
            String rankingType,
            String filtering
    ) throws
            WebApplicationException {

        JsonElement query = buildClassQuery(type, function, resources, rankingType, filtering);

        Map<URI, Pair<Double, MatchResult>> result = discoveryEngine.discover(query);
        Map<URI, DiscoveryResult> discoveryResults = discoveryResultsBuilder.build(result, rankingType);

        Feed feed = new AbderaAtomFeedProvider().generateDiscoveryFeed(uriInfo.getRequestUri().toASCIIString(), discoveryEngine.toString(), discoveryResults);

        return Response.ok(feed).build();
    }

    public Response classificationBasedDiscoveryAsJson(
            String type,
            String function,
            List<String> resources,
            String rankingType,
            String filtering
    ) throws
            WebApplicationException {

        JsonElement query = buildClassQuery(type, function, resources, rankingType, filtering);

        Map<URI, Pair<Double, MatchResult>> result = discoveryEngine.discover(query);

        Map<URI, DiscoveryResult> discoveryResults = discoveryResultsBuilder.build(result, rankingType);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(discoveryResults);

        return Response.ok(json).build();
    }


    @GET
    @Path("{type}/io-rdfs")
    @ApiOperation(
            value = "Get services or operations that have inputs or/and outputs classified by specific RDF classes.",
            notes = "The search is performed through RDFS-based reasoning.",
            response = DiscoveryResult.class
    )
    @Produces({"application/atom+xml", "application/json"})
    public Response ioDiscovery(
            @ApiParam(value = "Parameter indicating the type of item to discover. The only values accepted are \"op\" for discovering operations, and \"svc\" for discovering services.", required = true, allowableValues = "svc,op")
            @PathParam("type") String type,
            @ApiParam(value = "type of matching. The value should be either \"and\" or \"or\". The result should be the set- based conjunction or disjunction depending on the value selected between the services matching the inputs and those matching the outputs.", allowableValues = "and,or")
            @QueryParam("f") String function,
            @ApiParam(value = "Filtering according to specific criteria.", allowableValues = "disabled,enabled")
            @DefaultValue("disabled") @QueryParam("filtering") String filtering,
            @ApiParam(value = "Popularity-based ranking. The value should be \"standard\" to rank the results according the popularity of the provider.", allowableValues = "standard,inverse")
            @QueryParam("ranking") String rankingType,
            @ApiParam(value = "Multivalued parameter indicating the classes that the input of the service should match to. The classes are indicated with the URL of the concept to match. This URL should be URL encoded.", required = true, allowMultiple = true)
            @QueryParam("i") List<String> inputs,
            @ApiParam(value = "Multivalued parameter indicating the classes that the output of the service should match to. The classes are indicated with the URL of the concept to match. This URL should be URL encoded.", allowMultiple = true)
            @QueryParam("o") List<String> outputs
    ) throws
            WebApplicationException {
        if (request.getHeader("Accept") != null && request.getHeader("Accept").equals("application/json")) {
            return ioDiscoveryAsJson(type, function, rankingType, filtering, inputs, outputs);
        }

        return ioDiscoveryAsAtom(type, function, rankingType, filtering, inputs, outputs);
    }


    public Response ioDiscoveryAsAtom(
            String type,
            String function,
            String rankingType,
            String filtering,
            List<String> inputs,
            List<String> outputs
    ) throws
            WebApplicationException {

        JsonElement query = buildIOQuery(type, function, rankingType, filtering, inputs, outputs);

        Map<URI, Pair<Double, MatchResult>> result = discoveryEngine.discover(query);

        Map<URI, DiscoveryResult> discoveryResults = discoveryResultsBuilder.build(result, rankingType);

        Feed feed = new AbderaAtomFeedProvider().generateDiscoveryFeed(uriInfo.getRequestUri().toASCIIString(), discoveryEngine.toString(), discoveryResults);

        return Response.ok(feed).build();
    }

    public Response ioDiscoveryAsJson(
            String type,
            String function,
            String rankingType,
            String filtering,
            List<String> inputs,
            List<String> outputs
    ) throws
            WebApplicationException {

        JsonElement query = buildIOQuery(type, function, rankingType, filtering, inputs, outputs);

        Map<URI, Pair<Double, MatchResult>> result = discoveryEngine.discover(query);
        Map<URI, MatchResult> output = Maps.newLinkedHashMap();
        for (URI matchedResource : result.keySet()) {
            output.put(matchedResource, result.get(matchedResource).getRight());
        }
        Map<URI, DiscoveryResult> discoveryResults = discoveryResultsBuilder.build(result, rankingType);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(discoveryResults);

        return Response.ok(json).build();
    }

    private JsonElement buildClassQuery(String type, String operator, List<String> resources, String rankingType, String filtering) {

        JsonObject query = new JsonObject();
        JsonArray parameters = new JsonArray();
        for (String resource : resources) {
            parameters.add(new JsonPrimitive(resource));
        }
        JsonObject operatorObject = new JsonObject();
        if (operator == null || operator.equals("")) {
            operator = "or";
        }
        operatorObject.add(operator, parameters);

        JsonObject functionDiscoveryObject = new JsonObject();
        functionDiscoveryObject.add("classes", operatorObject);
        functionDiscoveryObject.add("type", new JsonPrimitive(type));

        JsonObject discoveryObject = new JsonObject();

        discoveryObject.add("func-rdfs", functionDiscoveryObject);

        query.add("discovery", discoveryObject);

        if (filtering.equals("enabled")) {
            query.add("filtering", new JsonArray());
        }
        if (rankingType != null && !rankingType.equals("")) {
            query.add("ranking", new JsonPrimitive(rankingType));
        }

        logger.debug("Query: {}", query);

        return query;
    }

    private JsonElement buildIOQuery(String type, String operator, String rankingType, String filtering, List<String> inputs, List<String> outputs) {
        JsonObject query = new JsonObject();
        JsonArray inputParameters = new JsonArray();
        for (String resource : inputs) {
            inputParameters.add(new JsonPrimitive(resource));
        }
        JsonArray outputParameters = new JsonArray();
        for (String resource : outputs) {
            outputParameters.add(new JsonPrimitive(resource));
        }
        JsonObject inputObject = null;
        if (!inputs.isEmpty()) {
            inputObject = new JsonObject();
            inputObject.add(operator, inputParameters);
        }
        JsonObject outputObject = null;
        if (!outputs.isEmpty()) {
            outputObject = new JsonObject();
            outputObject.add(operator, outputParameters);
        }

        JsonObject functionObject = new JsonObject();
        if (inputObject != null) {
            functionObject.add("input", inputObject);
        }
        if (outputObject != null) {
            functionObject.add("output", outputObject);
        }

        JsonObject operatorObject = new JsonObject();
        if (operator.equals("")) {
            operator = "or";
        }
        operatorObject.add(operator, functionObject);
        JsonObject functionDiscoveryObject = new JsonObject();
        functionDiscoveryObject.add("expression", operatorObject);
        functionDiscoveryObject.add("type", new JsonPrimitive(type));

        JsonObject discoveryObject = new JsonObject();

        discoveryObject.add("io-rdfs", functionDiscoveryObject);

        query.add("discovery", discoveryObject);

        if (filtering.equals("enabled")) {
            query.add("filtering", new JsonArray());
        }
        if (rankingType != null && !rankingType.equals("")) {
            query.add("ranking", new JsonPrimitive(rankingType));
        }

        logger.debug("Query: {}", query);

        return query;
    }


    @GET
    @Path("{type}/search")
    @ApiOperation(
            value = "Free text-based search of services, operations or service parts",
            response = DiscoveryResult.class
    )
    @Produces({"application/atom+xml", "application/json"})
    public Response search(
            @ApiParam(value = "Parameter indicating the type of item to discover. The only values accepted are \"op\" for discovering operations, \"svc\" for discovering services and \"all\" for any kind of service component", required = true, allowableValues = "svc,op,all")
            @PathParam("type") String type,
            @ApiParam(value = "Parameter indicating a query that specifies keywords to search. Regular expressions are allowed.", required = true)
            @QueryParam("q") String query
    ) {
        logger.info("Searching {} by keywords: {}", type, query);

        //building discovery Query
        JsonObject discoveryRequest = new JsonObject();
        JsonObject functionObject = new JsonObject();
        functionObject.add("query", new JsonPrimitive(query));
        functionObject.add("type", new JsonPrimitive(type));
        discoveryRequest.add("discovery", functionObject);

        // Run discovery
        Map<URI, Pair<Double, MatchResult>> result = discoveryEngine.discover(discoveryRequest);
        Map<URI, DiscoveryResult> discoveryResults = discoveryResultsBuilder.build(result, "");

        if (request.getHeader("Accept") != null && request.getHeader("Accept").equals("application/json")) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(discoveryResults);

            return Response.ok(json).build();

        }
        Feed feed = new AbderaAtomFeedProvider().generateDiscoveryFeed(uriInfo.getRequestUri().toASCIIString(), discoveryEngine.toString(), discoveryResults);
        return Response.ok(feed).build();
    }

    @POST
    @ApiOperation(
            value = "Advanced discovery",
            notes = "Discovery performed by submitting a complete discovery request as JSON",
            response = DiscoveryResult.class
    )
    @ApiResponses(
            value = {@ApiResponse(code = 200, message = "Discovery successful"),
                    @ApiResponse(code = 500, message = "Internal error")})
    @Consumes("application/json")
    @Produces({"application/atom+xml", "application/json"})
    public Response advancedDiscovery(
            @ApiParam(value = "JSON document that specifies the discovery strategy", required = true)
            String discoveryRequest
    ) {
        try {
            logger.debug("Advanced discovery");
            Map<URI, Pair<Double, MatchResult>> result = discoveryEngine.discover(new JsonParser().parse(discoveryRequest));
            Map<URI, DiscoveryResult> discoveryResults;
            if (discoveryRequest.contains("\"ranking\"") || discoveryRequest.contains("\"scoring\"")) {
                discoveryResults = discoveryResultsBuilder.build(result, "standard");
            } else {
                discoveryResults = discoveryResultsBuilder.build(result, "");
            }

            if (request.getHeader("Accept") != null && request.getHeader("Accept").equals("application/json")) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(discoveryResults);
                return Response.status(200).entity(json).build();
            }
            Feed feed = new AbderaAtomFeedProvider().generateDiscoveryFeed(uriInfo.getRequestUri().toASCIIString(), discoveryEngine.toString(), discoveryResults);
            return Response.status(200).entity(feed).build();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return Response.status(500).entity("Parsing error! The request is not properly defined.").build();
        }

    }

}
