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

import com.google.inject.assistedinject.FactoryModuleBuilder;
import junit.framework.Assert;
import org.jukito.All;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.ConfigurationModule;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;
import uk.ac.open.kmi.iserve.sal.manager.SparqlGraphStoreManager;
import uk.ac.open.kmi.msm4j.Service;
import uk.ac.open.kmi.msm4j.io.TransformationException;
import uk.ac.open.kmi.msm4j.io.impl.ServiceTransformationEngine;
import uk.ac.open.kmi.msm4j.io.impl.TransformerModule;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * ServiceManagerSparqlTest
 * TODO: Provide Description
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 11/10/2013
 */
@RunWith(JukitoRunner.class)
public class ServiceManagerTest {

    private static final Logger log = LoggerFactory.getLogger(ServiceManagerTest.class);

    private static final String MEDIATYPE = "text/xml";

    private static final String WSC08_01 = "/services/wsc08/01/";
    private static final String WSC08_01_SERVICES = WSC08_01 + "services.xml";

    /**
     * JukitoModule.
     */
    public static class InnerModule extends JukitoModule {
        @Override
        protected void configureTest() {
            // Get configuration
            install(new ConfigurationModule());

            install(new TransformerModule());

            // bind all the implementations
            bindMany(ServiceManager.class, ServiceManagerSparql.class, ServiceManagerIndexRdf.class);

//            bind(ServiceManager.class).to(ServiceManagerSparql.class);
            // Necessary to verify interaction with the real object
//            bindSpy(ServiceManagerSparql.class);

            // Assisted Injection for the Graph Store Manager
            install(new FactoryModuleBuilder()
                    .implement(SparqlGraphStoreManager.class, ConcurrentSparqlGraphStoreManager.class)
                    .build(SparqlGraphStoreFactory.class));
        }
    }

    // One of setup based on the use of a file to indicate if we need to initialise
    @BeforeClass
    public static void oneOfSetup() throws IOException, URISyntaxException {
        URL resource = ServiceManagerTest.class.getResource(".");
        File folder = new File(resource.toURI());
        if (folder.exists() && folder.isDirectory() && folder.canWrite()) {
            File file = new File(folder.getAbsolutePath() + "/init.check");
            if (!file.exists())
                file.createNewFile();
        }
    }

    // One of setup based on the use of a file to indicate if we need to initialise
    @AfterClass
    public static void oneOfTearDown() throws URISyntaxException {
        URL resource = ServiceManagerTest.class.getResource("init.check");
        if (resource != null) {
            File file = new File(resource.toURI());
            if (file != null && file.exists()) {
                file.delete();
            }
        }
    }

    public void checkAndImportServices(ServiceTransformationEngine transformationEngine,
                                       ServiceManager serviceManager) throws
            Exception {
        URL resource = ServiceManagerTest.class.getResource("init.check");
        if (resource != null) {
            File file = new File(resource.toURI());
            if (file.exists()) {
                serviceManager.clearServices();
                importWscServices(transformationEngine, serviceManager);
                file.delete();
            }
        }
    }

    private void importWscServices(ServiceTransformationEngine transformationEngine, ServiceManager serviceManager) throws
            TransformationException,
            ServiceException, URISyntaxException {
        log.info("Importing WSC Dataset");
        String file = this.getClass().getResource(WSC08_01_SERVICES).getFile();
        log.info("Services XML file {}", file);
        File servicesFile = new File(file);
        URL base = this.getClass().getResource(WSC08_01);
        log.info("Dataset Base URI {}", base.toURI().toASCIIString());
        List<Service> services = transformationEngine.transform(servicesFile, base.toURI().toASCIIString(), MEDIATYPE);
        //List<Service> result = transformationEngine.transform(services, null, MEDIATYPE);
        if (services.size() == 0) {
            fail("No services transformed!");
        }
        // Import all services
        int counter = 0;
        for (Service svc : services) {
            URI uri = serviceManager.addService(svc);
            Assert.assertNotNull(uri);
            log.info("Service added: " + uri.toASCIIString());
            counter++;
        }
        log.debug("Total services added {}", counter);
        Assert.assertEquals(services.size(), counter);
    }

    @Test
    public void testListServices(ServiceTransformationEngine transformationEngine, @All ServiceManager serviceManager) throws Exception {

        //check and import services
        checkAndImportServices(transformationEngine, serviceManager);

        Set<URI> services = serviceManager.listServices();
        // Check the original list of retrieved URIs
        assertEquals(158, services.size());
        // Check if there are no duplications
        assertEquals(new HashSet<URI>(services).size(), services.size());

        serviceManager.shutdown();
    }


    /**
     * Finds the URI with the first coincidence with the service name
     *
     * @param opName Service name
     * @return first coincident URI
     */
    public URI findServiceURI(ServiceManager serviceManager, String opName) {
        Set<URI> services = serviceManager.listServices();
        for (URI service : services) {
            if (service.toASCIIString().contains(opName)) {
                return service;
            }
        }
        return null;
    }

    @Test
    //@Ignore
    public void testListOperations(ServiceTransformationEngine transformationEngine, @All ServiceManager serviceManager) throws Exception {

        //check and import services
        checkAndImportServices(transformationEngine, serviceManager);

        URI op = findServiceURI(serviceManager, "serv1323166560");
        if (op != null) {
            Set<URI> ops = serviceManager.listOperations(op);
            assertTrue(ops.size() == 1);
        } else {
            fail();
        }

        serviceManager.shutdown();
    }

    @Test
    //@Ignore
    public void testListInputs(ServiceTransformationEngine transformationEngine, @All ServiceManager serviceManager) throws Exception {

        //check and import services
        checkAndImportServices(transformationEngine, serviceManager);

        URI op = findServiceURI(serviceManager, "serv1323166560");
        if (op != null) {
            Set<URI> ops = serviceManager.listOperations(op);
            Set<URI> inputs = serviceManager.listInputs(ops.iterator().next());
            assertTrue(inputs.size() == 1);
        } else {
            fail();
        }

        serviceManager.shutdown();
    }

    @Test
    public void testInputParts(ServiceTransformationEngine transformationEngine, @All ServiceManager serviceManager) throws Exception {

        //check and import services
        checkAndImportServices(transformationEngine, serviceManager);

        URI op = findServiceURI(serviceManager, "serv1323166560");
        String[] expected = {"con241744282", "con1849951292", "con1653328292"};
        if (op != null) {
            Set<URI> ops = serviceManager.listOperations(op);
            Set<URI> inputs = serviceManager.listInputs(ops.iterator().next());
            Set<URI> parts = new HashSet<URI>(serviceManager.listMandatoryParts(inputs.iterator().next()));
            assertTrue(parts.size() == 3);
            for (URI part : parts) {
                boolean valid = false;
                for (String expectedInput : expected) {
                    if (part.toASCIIString().contains(expectedInput)) {
                        valid = true;
                        break;
                    }
                }
                assertTrue(valid);
            }
        } else {
            fail();
        }

        serviceManager.shutdown();
    }

    @Test
    public void testListOutputs(ServiceTransformationEngine transformationEngine, @All ServiceManager serviceManager) throws Exception {

        //check and import services
        checkAndImportServices(transformationEngine, serviceManager);

        URI op = findServiceURI(serviceManager, "serv1323166560");
        if (op != null) {
            Set<URI> ops = serviceManager.listOperations(op);
            Set<URI> inputs = serviceManager.listOutputs(ops.iterator().next());
            assertTrue(inputs.size() == 1);
        } else {
            fail();
        }

        serviceManager.shutdown();
    }

    @Test
    public void testListOperationsWithInputType(ServiceTransformationEngine transformationEngine, @All ServiceManager serviceManager)
            throws Exception {

        //check and import services
        checkAndImportServices(transformationEngine, serviceManager);

        Set<URI> result;

        result = serviceManager.listOperationsWithInputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con332477359"));
        assertEquals(1, result.size());

        result = serviceManager.listOperationsWithInputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con1023932779"));
        assertEquals(1, result.size());

        result = serviceManager.listOperationsWithInputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con33243578"));
        assertEquals(1, result.size());

        result = serviceManager.listOperationsWithInputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con246045693"));
        assertEquals(0, result.size());

        result = serviceManager.listOperationsWithInputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con1557475654"));
        assertEquals(1, result.size());

        result = serviceManager.listOperationsWithInputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con996692056"));
        assertEquals(1, result.size());

        result = serviceManager.listOperationsWithInputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con38363177"));
        assertEquals(0, result.size());

        serviceManager.shutdown();
    }


    @Test
    public void testListOperationsWithOutputType(ServiceTransformationEngine transformationEngine, @All ServiceManager serviceManager)
            throws Exception {

        //check and import services
        checkAndImportServices(transformationEngine, serviceManager);

        Set<URI> result;

        result = serviceManager.listOperationsWithOutputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con332477359"));
        assertEquals(0, result.size());

        result = serviceManager.listOperationsWithOutputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con1023932779"));
        assertEquals(1, result.size());

        result = serviceManager.listOperationsWithOutputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con33243578"));
        assertEquals(3, result.size());

        result = serviceManager.listOperationsWithOutputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con246045693"));
        assertEquals(3, result.size());

        result = serviceManager.listOperationsWithOutputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con1557475654"));
        assertEquals(0, result.size());

        result = serviceManager.listOperationsWithOutputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con996692056"));
        assertEquals(2, result.size());

        result = serviceManager.listOperationsWithOutputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con38363177"));
        assertEquals(1, result.size());

        serviceManager.shutdown();
    }

    @Test
    public void testListServicesWithInputType(ServiceTransformationEngine transformationEngine, @All ServiceManager serviceManager) throws
            Exception {

        //check and import services
        checkAndImportServices(transformationEngine, serviceManager);

        Set<URI> result;

        result = serviceManager.listServicesWithInputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con332477359"));
        assertEquals(1, result.size());

        result = serviceManager.listServicesWithInputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con1023932779"));
        assertEquals(1, result.size());

        result = serviceManager.listServicesWithInputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con33243578"));
        assertEquals(1, result.size());

        result = serviceManager.listServicesWithInputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con246045693"));
        assertEquals(0, result.size());

        result = serviceManager.listServicesWithInputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con1557475654"));
        assertEquals(1, result.size());

        result = serviceManager.listServicesWithInputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con996692056"));
        assertEquals(1, result.size());

        result = serviceManager.listServicesWithInputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con38363177"));
        assertEquals(0, result.size());

        serviceManager.shutdown();
    }


    @Test
    public void testListServicesWithOutputType(ServiceTransformationEngine transformationEngine, @All ServiceManager serviceManager)
            throws Exception {

        //check and import services
        checkAndImportServices(transformationEngine, serviceManager);

        Set<URI> result;

        result = serviceManager.listServicesWithOutputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con332477359"));
        assertEquals(0, result.size());

        result = serviceManager.listServicesWithOutputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con1023932779"));
        assertEquals(1, result.size());

        result = serviceManager.listServicesWithOutputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con33243578"));
        assertEquals(3, result.size());

        result = serviceManager.listServicesWithOutputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con246045693"));
        assertEquals(3, result.size());

        result = serviceManager.listServicesWithOutputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con1557475654"));
        assertEquals(0, result.size());

        result = serviceManager.listServicesWithOutputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con996692056"));
        assertEquals(2, result.size());

        result = serviceManager.listServicesWithOutputType(URI.create("http://localhost/wsc/01/taxonomy.owl#con38363177"));
        assertEquals(1, result.size());

        serviceManager.shutdown();
    }

}
