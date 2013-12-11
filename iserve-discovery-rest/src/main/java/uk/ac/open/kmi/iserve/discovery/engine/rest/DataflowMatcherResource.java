package uk.ac.open.kmi.iserve.discovery.engine.rest;

import org.apache.abdera.model.Feed;
import uk.ac.open.kmi.iserve.discovery.api.DataflowMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * ConceptMatcherResource provides access to Concept Matching via REST
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 01/11/2013
 */
@Path("/dataflow")
public class DataflowMatcherResource {

    private final DataflowMatcher dataflowMatcher;

    // Base URI info for this object
    @Context
    UriInfo uriInfo;

    @Inject
    public DataflowMatcherResource(DataflowMatcher dataflowMatcher) {
        this.dataflowMatcher = dataflowMatcher;
    }

//    @GET
//    @Produces({MediaType.APPLICATION_ATOM_XML,
//            MediaType.APPLICATION_JSON,
//            MediaType.TEXT_XML})
//    public Response discoverMatchingOperations(
//            @QueryParam("uri") String operation,
//            @DefaultValue("Plugin") @QueryParam("minMatch") String minMatch,
//            @DefaultValue("Exact") @QueryParam("maxMatch") String maxMatch ) throws
//            WebApplicationException {
//
//        if (dataflowMatcher == null) {
//            throw new NotFoundException("No Dataflow matcher available.");
//        }
//
//        // Validate operation
//        if (operation == null || operation.isEmpty()) {
//           throw new BadRequestException("You must provide a operation to be matched.");
//        }
//
//        URI conceptUri;
//        try {
//            conceptUri = new URI(operation);
//        } catch (URISyntaxException e) {
//            throw new BadRequestException("You must provide a valid operation URI for the operation to be " +
//                    "matched.");
//        }
//
//        // Validate types
//        LogicConceptMatchType minType = LogicConceptMatchType.valueOf(minMatch);
//        LogicConceptMatchType maxType = LogicConceptMatchType.valueOf(maxMatch);
//        if (minType == null || maxType == null) {
//            LogicConceptMatchType[] typesSupported = LogicConceptMatchType.values();
//            StringBuilder builder = new StringBuilder();
//            for (LogicConceptMatchType type : typesSupported) {
//                builder.append(type.name()).append(" - ");
//            }
//
//            throw new BadRequestException("You must provide correct min and max match types. Correct " +
//                    "values are: " + builder.toString());
//        }
//
//        Map<URI, MatchResult> matchResults = dataflowMatcher.listMatchesWithinRange(conceptUri, minType, maxType);
//        Feed feed = new AbderaAtomFeedProvider().generateFeed(uriInfo.getRequestUri().toASCIIString(),
//                dataflowMatcher.getMatcherDescription() + " - " + dataflowMatcher.getMatcherVersion(),
//                matchResults);
//
//        return Response.ok(feed).build();
//    }

    @GET
    @Produces({MediaType.APPLICATION_ATOM_XML,
            MediaType.APPLICATION_JSON,
            MediaType.TEXT_XML})
    public Response matchingOperations(
            @QueryParam("originOp") String originOperation,
            @QueryParam("destinationOp") String destinationOperation) throws
            WebApplicationException {

        if (dataflowMatcher == null) {
            throw new NotFoundException("No Dataflow matcher available.");
        }

        // Validate operation
        if (originOperation == null || originOperation.isEmpty() ||
                destinationOperation == null || destinationOperation.isEmpty()) {
            throw new BadRequestException("You must provide two operations to be matched.");
        }

        URI originOpUri, destinationOpUri;
        try {
            originOpUri = new URI(originOperation);
            destinationOpUri = new URI(destinationOperation);
        } catch (URISyntaxException e) {
            throw new BadRequestException("You must provide valid origin and destination operation URIs.");
        }

        MatchResult matchResult = dataflowMatcher.match(originOpUri, destinationOpUri);
        Feed feed = new AbderaAtomFeedProvider().generateFeed(uriInfo.getRequestUri().toASCIIString(),
                dataflowMatcher.getMatcherDescription() + " - " + dataflowMatcher.getMatcherVersion(),
                matchResult);

        return Response.ok(feed).build();
    }

}
