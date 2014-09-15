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
 * Portable JAX-RS application.
 * Apparently we need to create an app an inject support for multipart forms.
 * <p/>
 *
 * @author Carlos Pedrinaci (KMi - The Open University)
 *         Date: 25/06/2013
 *         Time: 17:58
 */
public class RegistryManagementWebApplication extends ResourceConfig {

    private static final Logger log = LoggerFactory.getLogger(RegistryManagementWebApplication.class);


    @Inject
    public RegistryManagementWebApplication(ServiceLocator serviceLocator) {
        // Set package to look for resources in
        packages("uk.ac.open.kmi.iserve.sal.rest.resource");

        // Add Elda's resources except for RouterRestlet as it is already covered by our own R/W Servlet
        register(ControlRestlet.class);
        register(ConfigRestlet.class);
        register(ClearCache.class);
        register(MetadataRestlet.class);
        register(ResetCacheCounts.class);
        register(ShowCache.class);
        register(ShowStats.class);

        // Register MultiPart and JspMVC features
        register(MultiPartFeature.class);
        register(JspMvcFeature.class);

        packages("com.wordnik.swagger.jaxrs.json").
                register(ApiListingResourceJSON.class).
                register(JerseyApiDeclarationProvider.class).
                register(JerseyResourceListingProvider.class);

        log.info("Registering injectables");
        GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);

        GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
        ConfigurationModule configurationModule = new ConfigurationModule();
        Injector configInjector = Guice.createInjector(configurationModule);
        Injector injector = Guice.createInjector(configurationModule, configInjector.getInstance(SalRestModule.class));
        guiceBridge.bridgeGuiceInjector(injector);

    }

//    @Override
//    public Set<Class<?>> getClasses() {
//        final Set<Class<?>> classes = new HashSet<Class<?>>();
//        // register resources and features
//        classes.add(MultiPartFeature.class);
//        // MVC feature to serve JSPs
//        classes.add(JspMvcFeature.class);
//
//        // Add ELDA's specific resources
//        classes.add(ControlRestlet.class);
//        classes.add(ConfigRestlet.class);
//        classes.add(ClearCache.class);
//        classes.add(MetadataRestlet.class);
//        classes.add(ResetCacheCounts.class);
//        classes.add(ShowCache.class);
//        classes.add(ShowStats.class);
//
//        // Add iServe specific resources
//        classes.add(ReadWriteRouterServlet.class);
//        classes.add(RegistryResource.class);
////        classes.add(ServicesResource.class);
//        classes.add(DocumentsResource.class);
//        classes.add(LoggingFilter.class);
//
//        return classes;
//    }
}