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

import org.glassfish.jersey.server.mvc.Viewable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.sal.manager.RegistryManager;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

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
    private static final String ADMIN_JSP_PATH = "/jsp/admin.jsp";
    private static final String CONFIGURATION_JSP_PATH = "/jsp/configuration.jsp";
    private final RegistryManager manager;
    @Context
    UriInfo uriInfo;

    @Inject
    public RegistryResource(RegistryManager registryManager) {
        this.manager = registryManager;
    }

    @GET
    @Produces({MediaType.TEXT_HTML})
    public Response showAdminPage() {
        log.debug("Forwarding to {}", ADMIN_JSP_PATH);
        return Response.ok(new Viewable(ADMIN_JSP_PATH, null)).build();
    }

//    @GET
//    @Path("/configuration")
//    @Produces({MediaType.TEXT_HTML})
//    public Response showConfiguration() {
//        log.debug("Forwarding to {}", CONFIGURATION_JSP_PATH);
//        return Response.ok(new Viewable(CONFIGURATION_JSP_PATH, manager.getConfiguration())).build();
//    }

}
