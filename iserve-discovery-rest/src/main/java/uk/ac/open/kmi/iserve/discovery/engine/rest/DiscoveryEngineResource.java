package uk.ac.open.kmi.iserve.discovery.engine.rest;

/**
 * Created by Luca Panziera on 29/05/2014.
 */

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.abdera.model.Feed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.discovery.api.DiscoveryEngine;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.OperationDiscoverer;
import uk.ac.open.kmi.iserve.discovery.api.ServiceDiscoverer;
import uk.ac.open.kmi.iserve.discovery.api.ranking.Filter;
import uk.ac.open.kmi.iserve.discovery.api.ranking.Ranker;
import uk.ac.open.kmi.iserve.discovery.api.ranking.ScoreComposer;
import uk.ac.open.kmi.iserve.discovery.api.ranking.Scorer;
import uk.ac.open.kmi.iserve.discovery.freetextsearch.FreeTextSearchPlugin;
import uk.ac.open.kmi.iserve.discovery.util.Pair;
import uk.ac.open.kmi.msm4j.vocabulary.MSM;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/{type}")
public class DiscoveryEngineResource {

    @Context
    UriInfo uriInfo;
    private Logger logger = LoggerFactory.getLogger(DiscoveryEngineResource.class);
    private DiscoveryEngine discoveryEngine;
    private DiscoveryResultsBuilderPlugin discoveryResultsBuilder;
    private FreeTextSearchPlugin freeTextSearchPlugin;

    @Inject
    DiscoveryEngineResource(ServiceDiscoverer serviceDiscoverer, OperationDiscoverer operationDiscoverer, Set<Filter> filters, Set<Scorer> scorers, ScoreComposer scoreComposer, Ranker ranker, DiscoveryResultsBuilderPlugin discoveryResultsBuilder, FreeTextSearchPlugin freeTextSearchPlugin) {
        discoveryEngine = new DiscoveryEngine(serviceDiscoverer, operationDiscoverer, filters, scorers, scoreComposer, ranker);
        this.discoveryResultsBuilder = discoveryResultsBuilder;
        this.freeTextSearchPlugin = freeTextSearchPlugin;
    }

    @Path("/func-rdfs")
    @GET
    @Produces("application/atom+xml")
    public Response classificationBasedDiscoveryAsAtom(
            @PathParam("type") String type,
            @QueryParam("f") String function,
            @QueryParam("class") List<String> resources,
            @QueryParam("ranking") String rankingType,
            @DefaultValue("disabled") @QueryParam("filtering") String filtering
    ) throws
            WebApplicationException {

        String query = buildClassQuery(type, function, resources, rankingType, filtering);

        Map<URI, Pair<Double, MatchResult>> result = discoveryEngine.discover(query);
        Map<URI, DiscoveryResult> discoveryResults = discoveryResultsBuilder.build(result,rankingType);

        Feed feed = new AbderaAtomFeedProvider().generateDiscoveryFeed(uriInfo.getRequestUri().toASCIIString(), discoveryEngine.toString(), discoveryResults);

        return Response.ok(feed).build();
    }

    @Path("/func-rdfs")
    @GET
    @Produces("application/json")
    public Response classificationBasedDiscoveryAsJson(
            @PathParam("type") String type,
            @QueryParam("f") String function,
            @QueryParam("class") List<String> resources,
            @QueryParam("ranking") String rankingType,
            @DefaultValue("disabled") @QueryParam("filtering") String filtering
    ) throws
            WebApplicationException {

        String query = buildClassQuery(type, function, resources, rankingType, filtering);

        Map<URI, Pair<Double, MatchResult>> result = discoveryEngine.discover(query);

        Map<URI, DiscoveryResult> discoveryResults = discoveryResultsBuilder.build(result,rankingType);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(discoveryResults);

        return Response.ok(json).build();
    }

    @Path("/io-rdfs")
    @GET
    @Produces("application/atom+xml")
    public Response IODiscoveryAsAtom(
            @PathParam("type") String type,
            @QueryParam("f") String function,
            @QueryParam("ranking") String rankingType,
            @DefaultValue("disabled") @QueryParam("filtering") String filtering,
            @QueryParam("i") List<String> inputs,
            @QueryParam("o") List<String> outputs
    ) throws
            WebApplicationException {

        String query = buildIOQuery(type, function, rankingType, filtering, inputs, outputs);

        Map<URI, Pair<Double, MatchResult>> result = discoveryEngine.discover(query);

        Map<URI, DiscoveryResult> discoveryResults = discoveryResultsBuilder.build(result,rankingType);

        Feed feed = new AbderaAtomFeedProvider().generateDiscoveryFeed(uriInfo.getRequestUri().toASCIIString(), discoveryEngine.toString(), discoveryResults);

        return Response.ok(feed).build();
    }

    @Path("/io-rdfs")
    @GET
    @Produces("application/json")
    public Response IODiscoveryAsJson(
            @PathParam("type") String type,
            @QueryParam("f") String function,
            @QueryParam("ranking") String rankingType,
            @DefaultValue("disabled") @QueryParam("filtering") String filtering,
            @QueryParam("i") List<String> inputs,
            @QueryParam("o") List<String> outputs
    ) throws
            WebApplicationException {

        String query = buildIOQuery(type, function, rankingType, filtering, inputs, outputs);

        Map<URI, Pair<Double, MatchResult>> result = discoveryEngine.discover(query);
        Map<URI, MatchResult> output = Maps.newLinkedHashMap();
        for (URI matchedResource : result.keySet()) {
            output.put(matchedResource, result.get(matchedResource).getRight());
        }
        Map<URI, DiscoveryResult> discoveryResults = discoveryResultsBuilder.build(result,rankingType);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(discoveryResults);

        return Response.ok(json).build();
    }

    private String buildClassQuery(String type, String function, List<String> resources, String rankingType, String filtering) {
        StringBuilder queryBuilder = new StringBuilder();

        if (resources == null || resources.isEmpty()) {
            if (type.equals("svc")) {
                queryBuilder.append("service-class ");
            } else if (type.equals("op")) {
                queryBuilder.append("operation-class ");
            }
            queryBuilder.append("http://www.w3.org/2002/07/owl#Thing ");
        } else {
            List<String> copyOfRes = Lists.newArrayList(resources);
            for (String resource : resources) {
                if (type.equals("svc")) {
                    queryBuilder.append("service-class ");
                } else if (type.equals("op")) {
                    queryBuilder.append("operation-class ");
                }
                queryBuilder.append(URI.create(resource)).append(" ");
                copyOfRes.remove(resource);
                if (!copyOfRes.isEmpty()) {
                    if (function == null || function.equals("") || function.equals("or")) {
                        queryBuilder.append("union ");
                    } else if (function.equals("and")) {
                        queryBuilder.append("intersection ");
                    }
                }
            }

        }

        if (filtering.equals("enabled")) {
            queryBuilder.append("filtering ");
        }
        if (rankingType != null) {
            if (rankingType.equals("standard")) {
                queryBuilder.append("rank");
            } else if (rankingType.equals("inverse")) {
                queryBuilder.append("inverse-rank");
            }
        }

        String query = queryBuilder.toString();

        logger.info("Query: {}", query);

        return query;
    }

    private String buildIOQuery(String type, String function, String rankingType, String filtering, List<String> inputs, List<String> outputs) {
        StringBuilder queryBuilder = new StringBuilder();
        if ((inputs == null || inputs.isEmpty()) && (outputs == null || outputs.isEmpty())) {
            if (type.equals("svc")) {
                queryBuilder.append("service-input http://www.w3.org/2002/07/owl#Thing ");
            } else if (type.equals("op")) {
                queryBuilder.append("operation-input http://www.w3.org/2002/07/owl#Thing ");
            }

        } else {
            List<String> copyOfinputs = Lists.newArrayList(inputs);
            for (String resource : inputs) {
                if (type.equals("svc")) {
                    queryBuilder.append("service-input ");
                } else if (type.equals("op")) {
                    queryBuilder.append("operation-input ");
                }
                queryBuilder.append(URI.create(resource)).append(" ");
                copyOfinputs.remove(resource);
                if (!copyOfinputs.isEmpty()) {
                    if (function == null || function.equals("") || function.equals("or")) {
                        queryBuilder.append("union ");
                    } else if (function.equals("and")) {
                        queryBuilder.append("intersection ");
                    }
                }
            }

            List<String> copyOfoutputs = Lists.newArrayList(outputs);
            for (String resource : outputs) {
                if (type.equals("svc")) {
                    queryBuilder.append("service-output ");
                } else if (type.equals("op")) {
                    queryBuilder.append("operation-output ");
                }
                queryBuilder.append(URI.create(resource)).append(" ");
                copyOfoutputs.remove(resource);
                if (!copyOfoutputs.isEmpty()) {
                    if (function == null || function.equals("") || function.equals("or")) {
                        queryBuilder.append("union ");
                    } else if (function.equals("and")) {
                        queryBuilder.append("intersection ");
                    }
                }
            }

        }

        if (filtering.equals("enabled")) {
            queryBuilder.append("filtering ");
        }
        if (rankingType != null) {
            if (rankingType.equals("standard")) {
                queryBuilder.append("rank");
            } else if (rankingType.equals("inverse")) {
                queryBuilder.append("inverse-rank");
            }
        }

        String query = queryBuilder.toString();

        logger.info("Query: {}", query);

        return query;
    }

    @Path("/search")
    @GET
    @Produces("application/json")
    public Response searchAsJson(@PathParam("type") String type, @QueryParam("q") String query) {
        logger.info("Searching {} by keywords: {}", type, query);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Set<URI> result;
        if (type.equals("svc")) {
            result = freeTextSearchPlugin.search(query, URI.create(MSM.Service.getURI()));
        } else if (type.equals("op")) {
            result = freeTextSearchPlugin.search(query, URI.create(MSM.Operation.getURI()));
        } else {
            result = freeTextSearchPlugin.search(query);
        }
        String json = gson.toJson(result);
        return Response.ok(json).build();
    }

    @Path("/search")
    @GET
    @Produces("application/atom+xml")
    public Response searchAsAtom(@PathParam("type") String type, @QueryParam("q") String query) {
        logger.info("Searching {} by keywords: {}", type, query);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Set<URI> result;
        if (type.equals("svc")) {
            result = freeTextSearchPlugin.search(query, URI.create(MSM.Service.getURI()));
        } else if (type.equals("op")) {
            result = freeTextSearchPlugin.search(query, URI.create(MSM.Operation.getURI()));
        } else {
            result = freeTextSearchPlugin.search(query);
        }
        Feed feed = new AbderaAtomFeedProvider().generateSearchFeed(uriInfo.getRequestUri().toASCIIString(), freeTextSearchPlugin.getClass().toString(), result);
        return Response.ok(feed).build();
    }

}
