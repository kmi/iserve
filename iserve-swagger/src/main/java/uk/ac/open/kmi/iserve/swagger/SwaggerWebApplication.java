package uk.ac.open.kmi.iserve.swagger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.wordnik.swagger.jersey.listing.ApiListingResourceJSON;
import com.wordnik.swagger.jersey.listing.JerseyApiDeclarationProvider;
import com.wordnik.swagger.jersey.listing.JerseyResourceListingProvider;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.jsp.JspMvcFeature;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.ConfigurationModule;

import javax.inject.Inject;

/**
 * Created by Luca Panziera on 17/09/2014.
 */
public class SwaggerWebApplication extends ResourceConfig {
    private static final Logger log = LoggerFactory.getLogger(SwaggerWebApplication.class);

    @Inject
    public SwaggerWebApplication(ServiceLocator serviceLocator) {
        // Set package to look for resources in
//        packages("uk.ac.open.kmi.iserve.sal.rest.resource");
        packages("uk.ac.open.kmi.iserve.swagger");

        log.info("Registering injectables");
        GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);
        GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
        Injector firstStage = Guice.createInjector(new ConfigurationModule());
        Injector secondStage = firstStage.createChildInjector(new SwaggerModule(firstStage));
        guiceBridge.bridgeGuiceInjector(secondStage);

        // Register MultiPart and JspMVC features
        register(MultiPartFeature.class);
        register(JspMvcFeature.class);

        packages("com.wordnik.swagger.jaxrs.json").
                register(ApiListingResourceJSON.class).
                register(JerseyApiDeclarationProvider.class).
                register(JerseyResourceListingProvider.class);
    }
}