/*
 * Copyright (c) 2014. Knowledge Media Institute - The Open University
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

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.ConfigurationModule;
import uk.ac.open.kmi.iserve.sal.manager.*;
import uk.ac.open.kmi.iserve.sal.util.caching.Cache;
import uk.ac.open.kmi.iserve.sal.util.caching.CacheFactory;
import uk.ac.open.kmi.iserve.sal.util.caching.impl.InMemoryCache;
import uk.ac.open.kmi.iserve.sal.util.caching.impl.RedisCache;
import uk.ac.open.kmi.msm4j.io.impl.TransformerModule;

import java.util.concurrent.Executors;

/**
 * iServeModule is in charge of adequately initialising iServe by obtaining the configuration parameters
 * and binding each of the interfaces to the chosen implementation.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 06/09/2013
 */
public class RegistryManagementModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(RegistryManagementModule.class);
    private static final EventBus eventBus = new AsyncEventBus("iServe", Executors.newCachedThreadPool());
    private final String configFile;

    public RegistryManagementModule() {
        this.configFile = null;
    }

    public RegistryManagementModule(String configFile) {
        this.configFile = configFile;
    }

    @Override
    protected void configure() {
        // Include Configuration Module
        if (configFile == null) {
            install(new ConfigurationModule());
        } else {
            install(new ConfigurationModule(configFile));
        }

        setupRegistry();
    }

    private void setupRegistry() {
        // Include transformation support
        install(new TransformerModule());

        // Assisted Injection for the Graph Store Manager
        install(new FactoryModuleBuilder()
                .implement(SparqlGraphStoreManager.class, ConcurrentSparqlGraphStoreManager.class)
                .build(SparqlGraphStoreFactory.class));

        // Create the EventBus
        bind(EventBus.class).toInstance(eventBus);

        // Bind each of the managers
//        bind(DocumentManager.class).to(DocumentManagerFileSystem.class).in(Singleton.class);
//        bind(ServiceManager.class).to(ServiceManagerIndexRdf.class).in(Singleton.class);
//        bind(KnowledgeBaseManager.class).to(KnowledgeBaseManagerSparql.class).in(Singleton.class);

        // Cache injection
        install(new FactoryModuleBuilder()
                .implement(Cache.class, Names.named("in-memory"), InMemoryCache.class)
                .implement(Cache.class, Names.named("persistent"), RedisCache.class)
                .build(CacheFactory.class));

        bind(DocumentManager.class).to(DocumentManagerFileSystem.class);
        bind(ServiceManager.class).to(ServiceManagerSparql.class);
        bind(KnowledgeBaseManager.class).to(KnowledgeBaseManagerSparql.class);
        bind(RegistryManager.class).to(RegistryManagerImpl.class);
        bind(NfpManager.class).to(NfpManagerSparql.class).asEagerSingleton();

    }
}
