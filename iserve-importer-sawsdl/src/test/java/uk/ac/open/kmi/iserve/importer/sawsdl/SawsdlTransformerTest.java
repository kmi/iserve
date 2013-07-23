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
import uk.ac.open.kmi.iserve.commons.io.TransformationException;
import uk.ac.open.kmi.iserve.commons.io.Transformer;
import uk.ac.open.kmi.iserve.commons.model.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SawsdlTransformerTest {

    private static final Logger log = LoggerFactory.getLogger(SawsdlTransformerTest.class);
    private static final String SAWSDL_TC3_SERVICES = "/SAWSDL-TC3_WSDL11/htdocs/services/sawsdl_wsdl11/";

    private SawsdlTransformer importer;
    private ServiceWriter writer;
    private List<URI> testFolders;
    private FilenameFilter sawsdlFilter;

    public SawsdlTransformerTest() {
        try {
            importer = new SawsdlTransformer();
        } catch (TransformationException e) {
            log.error("Unable to initialise SAWSDL importer", e);
        }
    }

    @Before
    public void setUp() throws Exception {

        importer = new SawsdlTransformer();
        writer = new ServiceWriterImpl();
        testFolders = new ArrayList<URI>();
        testFolders.add(SawsdlTransformerTest.class.getResource(SAWSDL_TC3_SERVICES).toURI());

        sawsdlFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.endsWith(".wsdl") || name.endsWith(".sawsdl"));
            }
        };
    }

    @Test
    public void testTransformFile() {

        // Add all the test collections
        log.info("Transforming test collections");
        for (URI testFolder : testFolders) {
            File dir = new File(testFolder);
            log.info("Test collection: {} ", testFolder);

            // Test services
            Collection<Service> services;
            log.info("Transforming services");
            File[] sawsdlFiles = dir.listFiles(sawsdlFilter);
            for (File file : sawsdlFiles) {
                log.info("Transforming service {}", file.getAbsolutePath());
                try {
                    services = importer.transform(new FileInputStream(file), null);
                    Assert.assertNotNull("Service collection should not be null", services);
                    Assert.assertTrue("There should be at least one service", 1 >= services.size());
                } catch (Exception e) {
                    log.error("Problems transforming the service. Continuing", e);
                }
            }
        }
    }

    @Test
    public void testPluginBasedTransformation() {
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
                try {
                    services = Transformer.getInstance().transform(file, null, SawsdlTransformer.mediaType);
                    Assert.assertNotNull("Service collection should not be null", services);
                    Assert.assertEquals(1, services.size());
                } catch (Exception e) {
                    log.error("Problems transforming the service. Continuing", e);
                }
            }
        }

    }

}
