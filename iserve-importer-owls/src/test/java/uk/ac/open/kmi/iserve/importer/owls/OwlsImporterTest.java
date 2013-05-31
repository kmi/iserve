package uk.ac.open.kmi.iserve.importer.owls;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.open.kmi.iserve.commons.model.Service;
import uk.ac.open.kmi.iserve.commons.model.ServiceWriter;
import uk.ac.open.kmi.iserve.commons.model.ServiceWriterImpl;
import uk.ac.open.kmi.iserve.commons.model.Syntax;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Test class for the OWLS Importer
 *
 * User: Carlos Pedrinaci (KMi - The Open University)
 * Date: 22/05/2013
 * Time: 16:49
 */
public class OwlsImporterTest {

    private static final String TEST_RESOURCES_PATH = "/src/test/resources/";
    private static final String OWLS_TC3_SERVICES_1_1 = "OWLS-TC3/htdocs/services/1.1";

    private OwlsImporter importer;
    private ServiceWriter writer;
    private String workingDir;
    private List<String> testFolders;
    private FilenameFilter owlsFilter;

    @Before
    public void setUp() throws Exception {

        importer = new OwlsImporter();
        writer = new ServiceWriterImpl();
        workingDir = System.getProperty("user.dir");
        testFolders = new ArrayList<String>();
        testFolders.add(workingDir + TEST_RESOURCES_PATH + OWLS_TC3_SERVICES_1_1);

        owlsFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.endsWith(".owl") || name.endsWith(".owls")) ;
            }
        };
    }

    @Test
    public void testExtractServicesFromStream() throws Exception {

        // Add all the test collections
        System.out.println("Transforming test collections");
        for (String testFolder : testFolders) {
            File dir = new File(testFolder);
            System.out.println("Test collection: " + testFolder);

            // Test services
            Collection<Service> services;
            System.out.println("Transforming services");
            File[] owlsFiles = dir.listFiles(owlsFilter);
            for (File file : owlsFiles) {
                InputStream in = new FileInputStream(file);
                services = importer.extractServicesFromStream(in);
                Assert.assertNotNull("Service collection should not be null", services);
                Assert.assertEquals(1, services.size());

                if (services != null) {
                    System.out.println("Services obtained: " + services.size());
                    for (Service service : services) {
                        writer.serialise(service, System.out, Syntax.TTL);
                    }
                }
            }
        }

    }


}
