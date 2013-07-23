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

package uk.ac.open.kmi.iserve.sal.rest;

import com.epimorphics.lda.restlets.*;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.mvc.jsp.JspMvcFeature;
import uk.ac.open.kmi.iserve.sal.rest.resource.DocumentsResource;
import uk.ac.open.kmi.iserve.sal.rest.resource.ReadWriteRouterServlet;
import uk.ac.open.kmi.iserve.sal.rest.resource.RegistryResource;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * Portable JAX-RS application.
 * Apparently we need to create an app an inject support for multipart forms.
 * <p/>
 *
 * @author Carlos Pedrinaci (KMi - The Open University)
 *         Date: 25/06/2013
 *         Time: 17:58
 */
public class iServeApp extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        // register resources and features
        classes.add(MultiPartFeature.class);
        // MVC feature to serve JSPs
        classes.add(JspMvcFeature.class);

        // Add ELDA's specific resources
        classes.add(ControlRestlet.class);
        classes.add(ConfigRestlet.class);
        classes.add(ClearCache.class);
        classes.add(MetadataRestlet.class);
        classes.add(ResetCacheCounts.class);
        classes.add(ShowCache.class);
        classes.add(ShowStats.class);

        // Add iServe specific resources
        classes.add(ReadWriteRouterServlet.class);
        classes.add(RegistryResource.class);
//        classes.add(ServicesResource.class);
        classes.add(DocumentsResource.class);
        classes.add(LoggingFilter.class);

        return classes;
    }
}