package uk.ac.open.kmi.iserve.sal.rest.resource;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.glassfish.jersey.server.mvc.Viewable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * Registry resource is in charge of managing the registry (clear the registry, configure it, etc)
 * <p/>
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 27/06/2013
 * Time: 17:31
 */
@Path("/registry")
public class RegistryResource {

    private static final Logger log = LoggerFactory.getLogger(RegistryResource.class);

    @Context
    UriInfo uriInfo;

    public RegistryResource() {
    }

    @GET
    @Produces({MediaType.TEXT_HTML})
    public Response showAdminPage() {
        log.debug("Forwarding to /jsp/admin.jsp");
        return Response.ok(new Viewable("/jsp/admin.jsp", null)).build();
    }

    @GET
    @Path("/configuration")
    @Produces({MediaType.TEXT_HTML})
    public Response showConfiguration() {
        log.debug("Forwarding to /jsp/configuration.jsp");
        return Response.ok(new Viewable("/jsp/configuration.jsp", ManagerSingleton.getInstance().getConfiguration())).build();
    }

    /**
     * Clears the entire registry
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
    public Response clearRegistry() {

        // Check first that the user is allowed to upload a service
        Subject currentUser = SecurityUtils.getSubject();
        if (!currentUser.isPermitted("registry:delete")) {
            log.warn("User without the appropriate permissions attempted to clear the registry: " + currentUser.getPrincipal());

            String htmlString = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                    "  <body>\n You have not got the appropriate permissions for clearing the registry. Please login and ensure you have the correct permissions. </body>\n</html>";

            return Response.status(Response.Status.FORBIDDEN).entity(htmlString).build();
        }

        URI serviceUri = uriInfo.getRequestUri();

        String response;
        try {
            if (ManagerSingleton.getInstance().clearRegistry()) {
                // The registry was cleared
                response = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                        "  <body>\n The registry was cleared.\n  </body>\n</html>";

                return Response.status(Response.Status.OK).contentLocation(serviceUri).entity(response).build();
            } else {
                // The registry was not cleared
                response = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                        "  <body>\n The registry could not be cleared. Try again or contact a server administrator.\n  </body>\n</html>";

                return Response.status(Response.Status.NOT_MODIFIED).contentLocation(serviceUri).entity(response).build();
            }
        } catch (SalException e) {
            response = "<html>\n  <head>\n    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n  </head>\n" +
                    "  <body>\nThere was an error while clearing the registry. Contact the system administrator. \n  </body>\n</html>";

            // TODO: Add logging
            log.error("SAL Exception while deleting service", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
        }

    }

}
