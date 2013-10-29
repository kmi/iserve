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
import com.google.inject.assistedinject.FactoryModuleBuilder;
import junit.framework.Assert;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.ConfigurationModule;
import uk.ac.open.kmi.iserve.sal.manager.*;
import uk.ac.open.kmi.msm4j.io.MediaType;
import uk.ac.open.kmi.msm4j.io.Syntax;
import uk.ac.open.kmi.msm4j.io.Transformer;
import uk.ac.open.kmi.msm4j.io.util.FilenameFilterBySyntax;
import uk.ac.open.kmi.msm4j.io.util.FilenameFilterForTransformer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

/**
 * RegistryManagerImplTest
 * <p/>
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 06/06/2013
 * Time: 18:50
 */
@RunWith(JukitoRunner.class)
public class RegistryManagerImplTest {

    private static final Logger log = LoggerFactory.getLogger(RegistryManagerImplTest.class);

    // Limit the number of documents to upload to the registry
    private static final int MAX_DOCS = 25;

    private static final String OWLS_TC3_MSM = "/OWLS-TC3-MSM";
    private static final String OWLS_TC4_PDDL = "/OWLS-TC4_PDDL/htdocs/services/1.1";
    private static final Syntax SYNTAX = Syntax.TTL;
    private static final String OWLS_MEDIATYPE = "application/owl+xml";

    private FilenameFilter ttlFilter;
    private FilenameFilter owlsFilter;

    private int numServices;
    private File[] msmTtlTcFiles;
    private File[] owlsTcFiles;

    /**
     * JukitoModule.
     */
    public static class InnerModule extends JukitoModule {
        @Override
        protected void configureTest() {

            // Ensure configuration is loaded
            install(new ConfigurationModule());

            // Assisted Injection for the Graph Store Manager
            install(new FactoryModuleBuilder()
                    .implement(SparqlGraphStoreManager.class, ConcurrentSparqlGraphStoreManager.class)
                    .build(SparqlGraphStoreFactory.class));

            // Create the EventBus
            final EventBus eventBus = new EventBus("iServe");
            bind(EventBus.class).toInstance(eventBus);

            bind(DocumentManager.class).to(DocumentManagerFileSystem.class);
            bind(ServiceManager.class).to(ServiceManagerSparql.class);
            bind(KnowledgeBaseManager.class).to(KnowledgeBaseManagerSparql.class);
            bind(RegistryManager.class).to(RegistryManagerImpl.class);

            // Necessary to verify interaction with the real object
            bindSpy(RegistryManagerImpl.class);
        }
    }

    @Before
    public void setUp() throws Exception {
        URI msmTestFolder = RegistryManagerImplTest.class.getResource(OWLS_TC3_MSM).toURI();
        ttlFilter = new FilenameFilterBySyntax(Syntax.TTL);
        File dir = new File(msmTestFolder);
        msmTtlTcFiles = dir.listFiles(ttlFilter);

        URI owlsTestFolder = RegistryManagerImplTest.class.getResource(OWLS_TC4_PDDL).toURI();
        owlsFilter = new FilenameFilterForTransformer(Transformer.getInstance().getTransformer(OWLS_MEDIATYPE));
        dir = new File(owlsTestFolder);
        owlsTcFiles = dir.listFiles(owlsFilter);
    }

    @Test
    public void testImportService(RegistryManager registryManager) throws Exception {

        registryManager.clearRegistry();
        InputStream in;
        List<URI> servicesUris;
        int count = 0;
        log.info("Importing MSM TTL services");
        for (int i = 0; i < MAX_DOCS && i < msmTtlTcFiles.length; i++) {
            in = new FileInputStream(msmTtlTcFiles[i]);
            log.info("Adding service: {}", msmTtlTcFiles[i].getName());
            servicesUris = registryManager.importServices(in, MediaType.TEXT_TURTLE.getMediaType());
            Assert.assertNotNull(servicesUris);
            log.info("Service added: {}", servicesUris.get(0).toASCIIString());
            count++;
        }

        Assert.assertEquals(Math.min(MAX_DOCS, msmTtlTcFiles.length), count);

        // Test exploiting import plugins
        count = 0;
        log.info("Importing OWLS services");
        for (int i = 0; i < MAX_DOCS && i < owlsTcFiles.length; i++) {
            in = new FileInputStream(owlsTcFiles[i]);
            log.info("Adding service: {}", owlsTcFiles[i].getName());
            servicesUris = registryManager.importServices(in, OWLS_MEDIATYPE);
            Assert.assertNotNull(servicesUris);
            log.info("Service added: {}", servicesUris.get(0).toASCIIString());
            count++;
        }
        Assert.assertEquals(Math.min(MAX_DOCS, msmTtlTcFiles.length), count);
    }
}
