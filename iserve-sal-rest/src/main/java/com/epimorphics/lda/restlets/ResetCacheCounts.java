/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.restlets;

import com.epimorphics.lda.cache.Cache;
import com.epimorphics.util.URIUtils;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/control/reset-counts")
public class ResetCacheCounts {

    @POST
    @Produces("text/html")
    public Response resetCache() {
        Cache.Registry.resetCounts();
        return Response.seeOther(URIUtils.newURI("/control/show-cache")).build();
    }

}
