package uk.ac.open.kmi.iserve.sal.manager.impl;

import com.google.inject.internal.util.$Sets;
import es.usc.citius.composit.importer.wsc.wscxml.WSCImporter;
import junit.framework.Assert;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.Transformer;
import uk.ac.open.kmi.iserve.commons.model.Service;
import uk.ac.open.kmi.iserve.sal.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class ServiceManagerRdfTest {
    private static final Logger log = LoggerFactory.getLogger(ServiceManagerRdfTest.class);
    //private static final String DATASET = "/wsc08-dataset01/services/services.xml";
    private static final String DATASET = "/simple-datasets/03/services.xml";
    private static final String MEDIATYPE = "text/xml";
    private static final String CONFIG_PROPERTIES_FILENAME = "config.properties";
    private static ServiceManager manager = ManagerSingleton.getInstance().getServiceManager();

    @BeforeClass
    public static void setUp() throws Exception {
        BasicConfigurator.configure();
        org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);

        ManagerSingleton.getInstance().clearRegistry();

        log.info("Importing");
        String file =  ServiceManagerRdfTest.class.getResource(DATASET).getFile();
        log.debug("Using " + file);
        File services = new File(file);

        // TODO; Change for using the plugin system instead manual instantiation.
        List<Service> result = Transformer.getInstance().transform(services, null, MEDIATYPE);
        //WSCImporter imp = new WSCImporter();
        //List<Service> result = imp.transform(new FileInputStream(services),"");
        // Import all services
        for(Service s : result){
            URI uri = ManagerSingleton.getInstance().importService(s);
            Assert.assertNotNull(uri);
            log.info("Service added: " + uri.toASCIIString());
        }
    }


    @Test
    public void testListServices() throws Exception {
        String[] expected={"serv1323166560", "serv1392598793", "serv1462031026", "serv699915007", "serv7231183", "serv630482774", "serv2015850384", "serv769347240", "serv1253734327", "serv1531463259"};
        List<URI> services = manager.listServices();
        assertTrue(expected.length==services.size());
        for(URI service : services){
            boolean exists = false;
            for(String valid : expected){
                if (service.toASCIIString().contains(valid)){
                    exists=true;
                    break;
                }
            }
            assertTrue(exists);
        }

    }

    /**
     * Finds the URI with the first coincidence with the service name
     * @param opName  Service name
     * @return first coincident URI
     */
    public URI findServiceURI(String opName){
        List<URI> services = manager.listServices();
        for(URI service : services){
            if (service.toASCIIString().contains(opName)){
                return service;
            }
        }
        return null;
    }

    @Test
    public void testListOperations() throws Exception {
        URI op = findServiceURI("serv1323166560");
        if (op!=null){
            List<URI> ops = manager.listOperations(op);
            assertTrue(ops.size()==1);
        } else {
            fail();
        }
    }

    @Test
    public void testListInputs() throws Exception {
        URI op = findServiceURI("serv1323166560");
        if (op!=null){
            List<URI> ops = manager.listOperations(op);
            List<URI> inputs = manager.listInputs(ops.get(0));
            assertTrue(inputs.size()==1);
        } else {
            fail();
        }
    }

    @Test
    public void testInputParts() throws Exception {
        URI op = findServiceURI("serv1323166560");
        String[] expected = {"con241744282","con1849951292","con1653328292"};
        if (op!=null){
            List<URI> ops = manager.listOperations(op);
            List<URI> inputs = manager.listInputs(ops.get(0));
            Set<URI> parts = new HashSet<URI>(manager.listMandatoryParts(inputs.get(0)));
            // [http://localhost:9090/iserve/id/services/92783016-66aa-41a7-a2da-a4b2422037cb/serv1323166560/Operation/MessageContent_input/MessagePart_con241744282,
            // http://localhost:9090/iserve/id/services/92783016-66aa-41a7-a2da-a4b2422037cb/serv1323166560/Operation/MessageContent_input/MessagePart_con1849951292,
            // http://localhost:9090/iserve/id/services/92783016-66aa-41a7-a2da-a4b2422037cb/serv1323166560/Operation/MessageContent_input/MessagePart_con1653328292]
            assertTrue(parts.size()==3);
            for(URI part : parts){
                boolean valid = false;
                for(String expectedInput : expected){
                    if (part.toASCIIString().contains(expectedInput)){
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
        if (op!=null){
            List<URI> ops = manager.listOperations(op);
            List<URI> inputs = manager.listOutputs(ops.get(0));
            assertTrue(inputs.size()==1);
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
