
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.ServiceWriter;
import uk.ac.open.kmi.iserve.commons.io.Transformer;
import uk.ac.open.kmi.iserve.commons.io.impl.ServiceWriterImpl;
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
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 18/07/2013
 */
public class OwlsTransformerTest {

    private static final Logger log = LoggerFactory.getLogger(OwlsTransformerTest.class);
    private static final String OWLS_TC4_PDDL = "/OWLS-TC4_PDDL/htdocs/services/1.1/";
    private static final String OWLS_TC3_SERVICES_1_1 = "/OWLS-TC3/htdocs/services/1.1/";
    private static final String OWLS_TC3_SERVICES_1_0 = "/OWLS-TC3/htdocs/services/1.0/";

    private OwlsTransformer importer;
    private ServiceWriter writer;
    private List<URI> testFolders;
    private FilenameFilter owlsFilter;

    @Before
    public void setUp() throws Exception {

        importer = new OwlsTransformer();
        writer = new ServiceWriterImpl();
        testFolders = new ArrayList<URI>();
        testFolders.add(OwlsTransformerTest.class.getResource(OWLS_TC3_SERVICES_1_1).toURI());

        owlsFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.endsWith(".owl") || name.endsWith(".owls"));
            }
        };
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
            File[] owlsFiles = dir.listFiles(owlsFilter);
            for (File file : owlsFiles) {
                log.info("Transforming service {}", file.getAbsolutePath());
                try {
                    InputStream in = new FileInputStream(file);
                    services = importer.transform(in, null);
                    Assert.assertNotNull("Service collection should not be null", services);
                    Assert.assertEquals(1, services.size());
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
            File[] owlsFiles = dir.listFiles(owlsFilter);
            for (File file : owlsFiles) {
                log.info("Transforming service {}", file.getAbsolutePath());
                try {
                    services = Transformer.getInstance().transform(file, null, OwlsTransformer.mediaType);
                    Assert.assertNotNull("Service collection should not be null", services);
                    Assert.assertEquals(1, services.size());
                } catch (Exception e) {
                    log.error("Problems transforming the service. Continuing", e);
                }
            }
        }

    }
}
