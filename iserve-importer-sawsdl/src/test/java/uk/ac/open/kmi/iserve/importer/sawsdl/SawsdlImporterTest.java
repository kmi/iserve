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
package uk.ac.open.kmi.iserve.importer.sawsdl;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.ServiceWriter;
import uk.ac.open.kmi.iserve.commons.io.ServiceWriterImpl;
import uk.ac.open.kmi.iserve.commons.model.Service;

import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SawsdlImporterTest {

    private static final Logger log = LoggerFactory.getLogger(SawsdlImporterTest.class);
    private static final String SAWSDL_TC3_SERVICES = "/SAWSDL-TC3/htdocs/services/sawsdl_wsdl11/";

    private SawsdlImporter importer;
    private ServiceWriter writer;
    private List<URI> testFolders;
    private FilenameFilter sawsdlFilter;

    public SawsdlImporterTest() throws WSDLException, ParserConfigurationException {
        importer = new SawsdlImporter();
    }

    @Before
    public void setUp() throws Exception {

        importer = new SawsdlImporter();
//        importer.setProxy("http://wwwcache.open.ac.uk", "80");
        writer = new ServiceWriterImpl();
        testFolders = new ArrayList<URI>();
        testFolders.add(SawsdlImporterTest.class.getResource(SAWSDL_TC3_SERVICES).toURI());

        sawsdlFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.endsWith(".wsdl") || name.endsWith(".sawsdl"));
            }
        };
    }

    @Test
    public void testTransformFile() throws Exception {

        // Add all the test collections
        log.info("Transforming test collections");
        for (URI testFolder : testFolders) {
            File dir = new File(testFolder);
            log.info("Test collection: {} ", testFolder);

            // Test services
            Collection<Service> services;
            log.info("Transforming services");
            File[] owlsFiles = dir.listFiles(sawsdlFilter);
            for (File file : owlsFiles) {
                log.info("Transforming service {}", file.getAbsolutePath());
                services = importer.transform(file);
                Assert.assertNotNull("Service collection should not be null", services);
                Assert.assertEquals(1, services.size());
            }
        }

    }

    @Test
    public void testTransformInputStream() throws Exception {

        // Add all the test collections
        log.info("Transforming test collections");
        for (URI testFolder : testFolders) {
            File dir = new File(testFolder);
            log.info("Test collection: {} ", testFolder);

            // Test services
            Collection<Service> services;
            log.info("Transforming services");
            File[] owlsFiles = dir.listFiles(sawsdlFilter);
            for (File file : owlsFiles) {
                log.info("Transforming service {}", file.getAbsolutePath());
                InputStream in = new FileInputStream(file);
                services = importer.transform(in);
                Assert.assertNotNull("Service collection should not be null", services);
                Assert.assertEquals(1, services.size());
            }
        }

    }


}
