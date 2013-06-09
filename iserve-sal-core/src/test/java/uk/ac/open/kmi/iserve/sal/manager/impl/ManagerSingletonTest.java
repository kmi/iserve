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

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.open.kmi.iserve.commons.io.FilenameFilterBySyntax;
import uk.ac.open.kmi.iserve.commons.io.Syntax;
import uk.ac.open.kmi.iserve.sal.ServiceFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

/**
 * ManagerSingletonTest
 * <p/>
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 06/06/2013
 * Time: 18:50
 */
public class ManagerSingletonTest {

    private static final String OWLS_TC3_SERVICES = "/OWLS-TC3-MSM";
    private static final Syntax SYNTAX = Syntax.TTL;

    private URI testFolder;
    private FilenameFilter ttlFilter;

    private int numServices;
    private String dataUpdateEndpoint;
    private File[] msmTtlTcFiles;

    @Before
    public void setUp() throws Exception {
        testFolder = ManagerSingletonTest.class.getResource(OWLS_TC3_SERVICES).toURI();
        ttlFilter = new FilenameFilterBySyntax(Syntax.TTL);
        File dir = new File(testFolder);
        msmTtlTcFiles = dir.listFiles(ttlFilter);
        numServices = msmTtlTcFiles.length;

        dataUpdateEndpoint = ManagerSingleton.getInstance().getConfiguration().getSparqlUpdateUri().toASCIIString();
    }

    @Test
    public void testImportService() throws Exception {

        ManagerSingleton.getInstance().clearRegistry();
        InputStream in;
        URI serviceUri;
        int count = 0;
        for (File ttlFile : msmTtlTcFiles) {
            in = new FileInputStream(ttlFile);
            System.out.println("Adding service: " + ttlFile.getName());
            serviceUri = ManagerSingleton.getInstance().importService(in, ServiceFormat.MSM_TTL);
            Assert.assertNotNull(serviceUri);
            System.out.println("Service added: " + serviceUri.toASCIIString());
            count++;
        }
        Assert.assertEquals(numServices, count);

    }

    @Test
    public void testListServices() throws Exception {
        // The test depends on the number of services previously updloaded
        // TODO: Upload services before
        List<URI> services = ManagerSingleton.getInstance().listServices();
        Assert.assertEquals(numServices, services.size());
    }

//
//    @Test
//    public void testRegisterService() throws Exception {
//
//    }
//
//    @Test
//    public void testUnregisterService() throws Exception {
//
//    }
//
//    @Test
//    public void testExportService() throws Exception {
//
//    }
//
//    @Test
//    public void testGetService() throws Exception {
//
//    }
//
//    @Test
//    public void testGetServices() throws Exception {
//
//    }
//
//    @Test
//    public void testAddRelatedDocumentToService() throws Exception {
//
//    }
//
//    @Test
//    public void testGetDocument() throws Exception {
//
//    }
//
//    @Test
//    public void testCreateDocument() throws Exception {
//
//    }
//
//    @Test
//    public void testListDocuments() throws Exception {
//
//    }
//
//    @Test
//    public void testListDocumentsForService() throws Exception {
//
//    }
//
//    @Test
//    public void testDeleteDocument() throws Exception {
//
//    }
}
