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
import junit.framework.Assert;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.TransformationException;
import uk.ac.open.kmi.iserve.commons.io.Transformer;
import uk.ac.open.kmi.iserve.commons.model.Service;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class ServiceManagerRdfTest {

    private static final Logger log = LoggerFactory.getLogger(ServiceManagerRdfTest.class);
    private static final String DATASET = "/WSC08/wsc08_datasets/01/";

    private static final String MEDIATYPE = "text/xml";
    private static final String CONFIG_PROPERTIES_FILENAME = "config.properties";

    // TODO: Change this URL!
    //private static final String ISERVE_TEST_URI = "file:///tmp/iserve/service-docs";
    private static final String ISERVE_TEST_URI = "http://localhost:9090/iserve";
    private static final String ISERVE_TEST_QUERY_URI = "http://localhost:8080/openrdf-sesame/repositories/Test";
    private static final String ISERVE_TEST_UPDATE_URI = "http://localhost:8080/openrdf-sesame/repositories/Test/statements";
    private static final String ISERVE_TEST_SERVICE_URI = "http://localhost:8080/openrdf-sesame/repositories/Test/rdf-graphs/service";


    private static ServiceManager serviceManager;

    @BeforeClass
    public static void setUp() throws Exception {
        BasicConfigurator.configure();
        org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);

        EventBus eventBus = new EventBus();
        serviceManager = new ServiceManagerRdf(eventBus, ISERVE_TEST_URI, ISERVE_TEST_QUERY_URI, ISERVE_TEST_UPDATE_URI, ISERVE_TEST_UPDATE_URI);
    }


    @Test
    public void testListServices() throws Exception {

        serviceManager.clearServices();
        importWscServices();

        List<URI> services = serviceManager.listServices();
        // Check the original list of retrieved URIs
        assertEquals(158, services.size());
        // Check if there are no duplications
        assertEquals(158, new HashSet<URI>(services).size());
    }

    private void importWscServices() throws TransformationException, ServiceException, URISyntaxException {
        log.info("Importing");
        String file = ServiceManagerRdfTest.class.getResource(DATASET + "services.xml").getFile();
        log.debug("Using " + file);
        File services = new File(file);
        URL base = this.getClass().getResource(DATASET);
        List<Service> result = Transformer.getInstance().transform(services, base.toURI().toASCIIString(), MEDIATYPE);
        //List<Service> result = Transformer.getInstance().transform(services, null, MEDIATYPE);

        // Import all services
        int counter=0;
        for (Service s : result) {
            URI uri = serviceManager.addService(s);
            Assert.assertNotNull(uri);
            log.info("Service added: " + uri.toASCIIString());
            counter++;
        }
        log.debug("Total services added {}", counter);
    }

    /**
     * Finds the URI with the first coincidence with the service name
     *
     * @param opName Service name
     * @return first coincident URI
     */
    public URI findServiceURI(String opName) {
        List<URI> services = serviceManager.listServices();
        for (URI service : services) {
            if (service.toASCIIString().contains(opName)) {
                return service;
            }
        }
        return null;
    }

    @Test
    public void testListOperations() throws Exception {
        URI op = findServiceURI("serv1323166560");
        if (op != null) {
            List<URI> ops = serviceManager.listOperations(op);
            assertTrue(ops.size() == 1);
        } else {
            fail();
        }
    }

    @Test
    public void testListInputs() throws Exception {
        URI op = findServiceURI("serv1323166560");
        if (op != null) {
            List<URI> ops = serviceManager.listOperations(op);
            List<URI> inputs = serviceManager.listInputs(ops.get(0));
            assertTrue(inputs.size() == 1);
        } else {
            fail();
        }
    }

    @Test
    public void testInputParts() throws Exception {
        URI op = findServiceURI("serv1323166560");
        String[] expected = {"con241744282", "con1849951292", "con1653328292"};
        if (op != null) {
            List<URI> ops = serviceManager.listOperations(op);
            List<URI> inputs = serviceManager.listInputs(ops.get(0));
            Set<URI> parts = new HashSet<URI>(serviceManager.listMandatoryParts(inputs.get(0)));
            // [http://localhost:9090/iserve/id/services/92783016-66aa-41a7-a2da-a4b2422037cb/serv1323166560/Operation/MessageContent_input/MessagePart_con241744282,
            // http://localhost:9090/iserve/id/services/92783016-66aa-41a7-a2da-a4b2422037cb/serv1323166560/Operation/MessageContent_input/MessagePart_con1849951292,
            // http://localhost:9090/iserve/id/services/92783016-66aa-41a7-a2da-a4b2422037cb/serv1323166560/Operation/MessageContent_input/MessagePart_con1653328292]
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
            //System.out.println(parts);
        } else {
            fail();
        }
    }

    @Test
    public void testListOutputs() throws Exception {
        URI op = findServiceURI("serv1323166560");
        if (op != null) {
            List<URI> ops = serviceManager.listOperations(op);
            List<URI> inputs = serviceManager.listOutputs(ops.get(0));
            assertTrue(inputs.size() == 1);
        } else {
            fail();
        }
    }

    // TODO; Add testOutputParts method

    @Test
    public void testGetService() throws Exception {

    }

    @Test
    public void testGetServices() throws Exception {

    }


}
