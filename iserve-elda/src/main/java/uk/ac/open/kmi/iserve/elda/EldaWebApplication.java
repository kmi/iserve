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
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public EldaWebApplication() {

        log.debug("Loading Elda Web App");
        register(EldaRouterRestlet.class);
        register(ControlRestlet.class);
        register(ConfigRestlet.class);
        register(ClearCache.class);
        register(MetadataRestlet.class);
        register(ResetCacheCounts.class);
        register(ShowCache.class);
        register(ShowStats.class);
        ;

    }
}