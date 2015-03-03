package uk.ac.open.kmi.iserve.sal.manager.impl;

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

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.manager.NfpManager;
import uk.ac.open.kmi.iserve.sal.manager.RegistryManager;
import uk.ac.open.kmi.msm4j.Service;
import uk.ac.open.kmi.msm4j.io.ServiceReader;
import uk.ac.open.kmi.msm4j.io.Syntax;
import uk.ac.open.kmi.msm4j.vocabulary.SAWSDL;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.List;
import java.util.Set;


/**
 * Created by Luca Panziera on 01/05/2014.
 */
@RunWith(JukitoRunner.class)
public class NfpManagerTest {
    private static Logger logger = LoggerFactory.getLogger(NfpManager.class);

    @BeforeClass
    public static void setUp() throws Exception {


        Injector injector = Guice.createInjector(new RegistryManagementModule());

        RegistryManager registryManager = injector.getInstance(RegistryManager.class);
        ServiceReader serviceReader = injector.getInstance(ServiceReader.class);

        registryManager.clearRegistry();

        importPWapis(registryManager, serviceReader);

    }

    private static void importPWapis(RegistryManager registryManager, ServiceReader serviceReader) {

        File ontoFile = new File(NfpManagerTest.class.getResource("/google-maps.n3").getFile());

        try {
            List<Service> services = serviceReader.parse(new FileInputStream(ontoFile), "http://" + ontoFile.getName(), Syntax.N3);
            for (Service service : services) {
                logger.info("Loading {}", service.getUri());
                registryManager.getServiceManager().addService(service);
            }
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void writeReadTest(RegistryManager registryManager, NfpManager nfpManager) {
        Set<URI> services = registryManager.getServiceManager().listServices();
        for (URI service : services) {
            //get model reference
            try {
                Set<URI> modelReferences = (Set<URI>) nfpManager.getPropertyValue(service, URI.create(SAWSDL.modelReference.getURI()), URI.class);
                Assert.assertEquals(modelReferences.size(), 3);
                nfpManager.createPropertyValue(service, URI.create(SAWSDL.modelReference.getURI()), URI.create("http://schema.org/Action"));
                modelReferences = (Set<URI>) nfpManager.getPropertyValue(service, URI.create(SAWSDL.modelReference.getURI()), URI.class);
                Assert.assertEquals(modelReferences.size(), 4);
                nfpManager.deletePropertyValue(service, URI.create(SAWSDL.modelReference.getURI()), URI.create("http://schema.org/Action"));
                modelReferences = (Set<URI>) nfpManager.getPropertyValue(service, URI.create(SAWSDL.modelReference.getURI()), URI.class);
                Assert.assertEquals(modelReferences.size(), 3);
            } catch (SalException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * JukitoModule.
     */
    public static class InnerModule extends JukitoModule {
        @Override
        protected void configureTest() {

            install(new RegistryManagementModule());

        }

    }

}

