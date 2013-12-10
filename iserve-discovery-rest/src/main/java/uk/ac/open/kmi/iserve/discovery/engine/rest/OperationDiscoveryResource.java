package uk.ac.open.kmi.iserve.discovery.engine.rest;

import org.apache.abdera.model.Feed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.OperationDiscoverer;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * OperationDiscoveryResource
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 29/11/2013
 */
@Path("/operation")
public class OperationDiscoveryResource {

    private static final Logger log = LoggerFactory.getLogger(OperationDiscoveryResource.class);
    private final OperationDiscoverer opDiscoverer;

    // Base URI info for this object
    @Context
    UriInfo uriInfo;

    @Inject
    public OperationDiscoveryResource(OperationDiscoverer opDiscoverer) {
        this.opDiscoverer = opDiscoverer;
    }

    @GET
    @Produces({MediaType.APPLICATION_ATOM_XML,
            MediaType.APPLICATION_JSON,
            MediaType.TEXT_XML})
    public Response discoverOperations(
            @DefaultValue("some") @QueryParam("mode") String mode,
            @QueryParam("in") List<String> inputs,
            @QueryParam("out") List<String> outputs ) throws
            WebApplicationException {

        if (opDiscoverer == null) {
            throw new NotFoundException("No Operation Discoverer available.");
        }

        // Validate params
        if ( (inputs == null || inputs.isEmpty()) && (outputs == null || outputs.isEmpty()) ) {
            throw new BadRequestException("You must provide either inputs or outputs that characterise the " +
                    "services to discover");
        }


        log.info("Discoverying operations with mode {}, having these inputs \n {} \n and these outputs \n " +
                "{}", mode, inputs, outputs );


        Set<URI> inputsSet = new HashSet<URI>();
        if (!inputs.isEmpty()) {
            for(String input : inputs) {
                try {
                    inputsSet.add(new URI(input));
                } catch (URISyntaxException e) {
                    throw new BadRequestException("You must provide a valid concept URI for the concept to be " +
                            "matched. The wrong URI is: " + input);
                }
            }
        }

        Set<URI> outputsSet = new HashSet<URI>();
        if (!outputs.isEmpty()) {
            for(String output : outputs) {
                try {
                    outputsSet.add(new URI(output));
                } catch (URISyntaxException e) {
                    throw new BadRequestException("You must provide a valid concept URI for the concept to be " +
                            "matched. The wrong URI is: " + output);
                }
            }
        }

        Map<URI, MatchResult> matchResults = opDiscoverer.findOperationsConsumingSome(inputsSet);

        Feed feed = new AbderaAtomFeedProvider().generateFeed(uriInfo.getRequestUri().toASCIIString(),
                opDiscoverer.getClass().getCanonicalName(),
                matchResults);

        return Response.ok(feed).build();
    }

}

