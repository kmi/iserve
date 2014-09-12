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

package uk.ac.open.kmi.iserve.elda;

import com.epimorphics.lda.restlets.*;
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
public class EldaWebApplication extends ResourceConfig {

    private static final Logger log = LoggerFactory.getLogger(EldaWebApplication.class);

    @Inject
    public EldaWebApplication(ServiceLocator serviceLocator) {
        // Set package to look for resources in
        log.debug("Loading Elda Web App");
        register(EldaRouterRestlet.class);

        // Add Elda's resources except for RouterRestlet as it is already covered by our own R/W Servlet
        register(ControlRestlet.class);
        register(ConfigRestlet.class);
        register(ClearCache.class);
        register(MetadataRestlet.class);
        register(ResetCacheCounts.class);
        register(ShowCache.class);
        register(ShowStats.class);

        register(MultiPartFeature.class);
        register(JspMvcFeature.class);

        GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);

        GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
        Injector injector = Guice.createInjector(new ConfigurationModule());
        guiceBridge.bridgeGuiceInjector(injector);

    }
}