package uk.ac.open.kmi.iserve.sal.rest;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.mvc.jsp.JspMvcFeature;
import uk.ac.open.kmi.iserve.sal.rest.resource.DocumentsResource;
import uk.ac.open.kmi.iserve.sal.rest.resource.RegistryResource;
import uk.ac.open.kmi.iserve.sal.rest.resource.ServicesResource;

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
        classes.add(RegistryResource.class);
        classes.add(ServicesResource.class);
        classes.add(DocumentsResource.class);
        classes.add(LoggingFilter.class);
        return classes;
    }
}