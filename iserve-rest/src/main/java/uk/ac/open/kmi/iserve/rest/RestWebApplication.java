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

package uk.ac.open.kmi.iserve.rest;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.jsp.JspMvcFeature;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class RestWebApplication extends ResourceConfig {

    private static final Logger log = LoggerFactory.getLogger(RestWebApplication.class);

    @Inject
    public RestWebApplication(ServiceLocator serviceLocator) {
        // Set package to look for resources in
        packages("uk.ac.open.kmi.iserve.rest");

        // Register MultiPart and JspMVC features
        register(MultiPartFeature.class);
        register(JspMvcFeature.class);

        log.info("Registering injectables");
        GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);

        GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
        Injector injector = Guice.createInjector(new RestModule());
        guiceBridge.bridgeGuiceInjector(injector);

    }
}