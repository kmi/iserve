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

package uk.ac.open.kmi.iserve.commons.io;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.util.FilenameFilterBySyntax;
import uk.ac.open.kmi.iserve.commons.model.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * ServiceReaderImplTest
 * TODO: Provide Description
 * <p/>
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 03/06/2013
 * Time: 23:43
 */
public class ServiceReaderImplTest {

    private static final Logger log = LoggerFactory.getLogger(ServiceReaderImplTest.class);

    private static final String OWLS_TC3_SERVICES = "/OWLS-TC3-MSM";
    private static final Syntax SYNTAX = Syntax.TTL;

    //    private List<String> testFolders;
    private List<URI> testFolders;
    private FilenameFilter ttlFilter;
    private ServiceReader reader;
    private ServiceWriter writer;

    @Before
    public void setUp() throws Exception {

        reader = new ServiceReaderImpl();
        writer = new ServiceWriterImpl(); //useful for testing correctness
        testFolders = new ArrayList<URI>();
        testFolders.add(ServiceReaderImplTest.class.getResource(OWLS_TC3_SERVICES).toURI());

        ttlFilter = new FilenameFilterBySyntax(SYNTAX);
    }

    @Test
    public void testParse() throws Exception {

        // Add all the test collections
        log.info("Reading test collections");
        for (URI testFolder : testFolders) {
            log.info("Test collection: " + testFolder);
            File dir = new File(testFolder);

            // Test reading services
            List<Service> services;
            InputStream in = null;
            log.info("Reading services");
            File[] ttlFiles = dir.listFiles(ttlFilter);
            for (File file : ttlFiles) {
                try {
                    log.info("Parsing file: " + file.getName());

                    in = new FileInputStream(file);
                    services = reader.parse(in, null, Syntax.TTL);
                    // Run checks
                    Assert.assertNotNull("Service collection should not be null", services);
                    Assert.assertEquals(1, services.size());
                    // The model read from file and that serialised from the parsed model
                    // should be isomorphic. This should be the case as this test collection
                    // is also generated thanks to the serialiser.
                    // NOTE: Ensure you generate a new test collection after any change on the
                    // serialiser
                    // TODO: Run automatically the test collection generation the first time
                    Model generatedModel = null;
                    Model originalModel = null;
                    try {
                        generatedModel = ((ServiceWriterImpl) writer).generateModel(services.get(0));
                        originalModel = ModelFactory.createDefaultModel();
                        // Reopen since reset is not directly supported
                        in = new FileInputStream(file);
                        originalModel.read(in, null, SYNTAX.getName());

                        Assert.assertTrue("The two RDF models are not isomorphic", generatedModel.isIsomorphicWith(originalModel));
                    } finally {
                        if (generatedModel != null)
                            generatedModel.close();
                        if (originalModel != null)
                            originalModel.close();
                    }
                } finally {
                    if (in != null)
                        in.close();
                }
            }
        }

    }
}
