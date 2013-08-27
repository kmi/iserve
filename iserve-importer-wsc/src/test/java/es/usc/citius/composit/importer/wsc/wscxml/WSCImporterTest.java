package es.usc.citius.composit.importer.wsc.wscxml;


import junit.framework.Assert;
import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.Transformer;
import uk.ac.open.kmi.iserve.commons.model.Service;
//import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;

import java.io.File;
import java.net.URI;
import java.util.List;

// TODO; Move test to sal-core or another project to avoid cyclic dependencies with the module.
// (if a test is added to sal-core using the WSC importer, the WSC importer cannot use sal-core!)
public class WSCImporterTest {
    private static final Logger log = LoggerFactory.getLogger(WSCImporterTest.class);

    @Before
    public void setUp() throws Exception {
        BasicConfigurator.configure();
        //ManagerSingleton.getInstance().clearRegistry();
    }

    @Test
    public void testImportService() throws Exception {

        int count = 0;
        log.info("Importing WSC 2008 services");
        File services = new File(getClass().getClassLoader().getResource("services.xml").getFile());
        List<Service> result = Transformer.getInstance().transform(services, null, WSCImporter.mediaType);
        /*
        for(Service s : result){
            URI uri = ManagerSingleton.getInstance().addService(s);
            Assert.assertNotNull(uri);
            log.info("Service added: " + uri.toASCIIString());
            count++;
        }*/

        Assert.assertEquals(count, 158);
    }

    @Test
    public void testTransform() throws Exception {
        // Add all the test collections
        log.info("Transforming test collections");
            File services = new File(getClass().getClassLoader().getResource("services.xml").getFile());

            log.info("Transforming service {}", services.getAbsolutePath());
                try {
                    List<Service> result = Transformer.getInstance().transform(services, null, WSCImporter.mediaType);
                    Assert.assertNotNull("Service collection should not be null", services);
                    Assert.assertEquals(158, result.size());
                } catch (Exception e) {
                    log.error("Problems transforming the service. Continuing", e);
                }
            }

    }

