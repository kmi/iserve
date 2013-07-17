
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

package uk.ac.open.kmi.iserve.importer.owls;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.open.kmi.iserve.commons.io.ServiceWriter;
import uk.ac.open.kmi.iserve.commons.io.ServiceWriterImpl;
import uk.ac.open.kmi.iserve.commons.model.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Test class for the OWLS Importer
 * <p/>
 * User: Carlos Pedrinaci (KMi - The Open University)
 * Date: 22/05/2013
 * Time: 16:49
 */
public class OwlsImporterTest {

    private static final String OWLS_TC4_PDDL = "/OWLS-TC4_PDDL/htdocs/services/1.1/";
    private static final String OWLS_TC3_SERVICES_1_1 = "/OWLS-TC3/htdocs/services/1.1/";
    private static final String OWLS_TC3_SERVICES_1_0 = "/OWLS-TC3/htdocs/services/1.0/";

    private OwlsImporter importer;
    private ServiceWriter writer;
    private List<URI> testFolders;
    private FilenameFilter owlsFilter;

    @Before
    public void setUp() throws Exception {

        importer = new OwlsImporter();
        writer = new ServiceWriterImpl();
        testFolders = new ArrayList<URI>();
        testFolders.add(OwlsImporterTest.class.getResource(OWLS_TC3_SERVICES_1_1).toURI());

        owlsFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.endsWith(".owl") || name.endsWith(".owls"));
            }
        };
    }

    @Test
    public void testTransformFile() throws Exception {

        // Add all the test collections
        System.out.println("Transforming test collections");
        for (URI testFolder : testFolders) {
            File dir = new File(testFolder);
            System.out.println("Test collection: " + testFolder);

            // Test services
            Collection<Service> services;
            System.out.println("Transforming services");
            File[] owlsFiles = dir.listFiles(owlsFilter);
            for (File file : owlsFiles) {
                services = importer.transform(file);
                Assert.assertNotNull("Service collection should not be null", services);
                Assert.assertEquals(1, services.size());
            }
        }

    }

    @Test
    public void testTransformInputStream() throws Exception {

        // Add all the test collections
        System.out.println("Transforming test collections");
        for (URI testFolder : testFolders) {
            File dir = new File(testFolder);
            System.out.println("Test collection: " + testFolder);

            // Test services
            Collection<Service> services;
            System.out.println("Transforming services");
            File[] owlsFiles = dir.listFiles(owlsFilter);
            for (File file : owlsFiles) {
                InputStream in = new FileInputStream(file);
                services = importer.transform(in);
                Assert.assertNotNull("Service collection should not be null", services);
                Assert.assertEquals(1, services.size());
            }
        }

    }
}
