package uk.ac.open.kmi.iserve.discovery.engine.rest;

import org.apache.abdera.model.Feed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * ConceptMatcherResource provides access to Concept Matching via REST
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 01/11/2013
 */
@Path("/concept")
public class ConceptMatcherResource {

    private static final Logger log = LoggerFactory.getLogger(ConceptMatcherResource.class);
    private final ConceptMatcher conceptMatcher;

    // Base URI info for this object
    @Context
    UriInfo uriInfo;

    @Inject
    public ConceptMatcherResource(ConceptMatcher conceptMatcher) {
        this.conceptMatcher = conceptMatcher;
    }

    @GET
    @Produces({MediaType.APPLICATION_ATOM_XML,
            MediaType.APPLICATION_JSON,
            MediaType.TEXT_XML})
    public Response matchConcepts(
            @QueryParam("uri") String concept,
            @DefaultValue("Plugin") @QueryParam("minMatch") String minMatch,
            @DefaultValue("Exact") @QueryParam("maxMatch") String maxMatch) throws
            WebApplicationException {

        if (conceptMatcher == null) {
            throw new NotFoundException("No concept matcher available.");
        }

        // Validate concept
        if (concept == null || concept.isEmpty()) {
            throw new BadRequestException("You must provide a concept to be matched.");
        }

        URI conceptUri;
        try {
            conceptUri = new URI(concept);
        } catch (URISyntaxException e) {
            throw new BadRequestException("You must provide a valid concept URI for the concept to be " +
                    "matched.");
        }

        // Validate types
        LogicConceptMatchType minType = LogicConceptMatchType.valueOf(minMatch);
        LogicConceptMatchType maxType = LogicConceptMatchType.valueOf(maxMatch);
        if (minType == null || maxType == null) {
            LogicConceptMatchType[] typesSupported = LogicConceptMatchType.values();
            StringBuilder builder = new StringBuilder();
            for (LogicConceptMatchType type : typesSupported) {
                builder.append(type.name()).append(" - ");
            }

            throw new BadRequestException("You must provide correct min and max match types. Correct " +
                    "values are: " + builder.toString());
        }

        log.info("Obtaining Concept matches for {}, within range {} - {}", conceptUri, minType, maxType);

        Map<URI, MatchResult> matchResults = conceptMatcher.listMatchesWithinRange(conceptUri, minType, maxType);
        Feed feed = new AbderaAtomFeedProvider().generateFeed(uriInfo.getRequestUri().toASCIIString(),
                conceptMatcher.getMatcherDescription() + " - " + conceptMatcher.getMatcherVersion(),
                matchResults);

        return Response.ok(feed).build();
    }

}
