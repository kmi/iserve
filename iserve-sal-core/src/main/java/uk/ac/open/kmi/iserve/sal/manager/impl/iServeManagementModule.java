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

package uk.ac.open.kmi.iserve.sal.manager.impl;

import com.google.common.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.ConfiguredModule;
import uk.ac.open.kmi.iserve.sal.manager.DocumentManager;
import uk.ac.open.kmi.iserve.sal.manager.KnowledgeBaseManager;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;

/**
 * iServeModule is in charge of adequately initialising iServe by obtaining the configuration parameters
 * and binding each of the interfaces to the chosen implementation.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 06/09/2013
 */
public class iServeManagementModule extends ConfiguredModule {

    private static final Logger log = LoggerFactory.getLogger(iServeManagementModule.class);

    @Override
    protected void configure() {
        // Ensure configuration is loaded
        super.configure();

        // Create the EventBus
        final EventBus eventBus = new EventBus("iServe");
        bind(EventBus.class).toInstance(eventBus);

        // Bind each of the managers
        bind(DocumentManager.class).to(DocumentManagerFileSystem.class);
//        bind(ServiceManager.class).to(ServiceManagerRdf.class);
        bind(ServiceManager.class).to(ServiceManagerIndexRdf.class);
        bind(KnowledgeBaseManager.class).to(ConcurrentSparqlKnowledgeBaseManager.class);
    }
}
