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

package uk.ac.open.kmi.iserve.discovery.disco.impl;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.open.kmi.iserve.commons.io.MediaType;
import uk.ac.open.kmi.iserve.commons.io.Syntax;
import uk.ac.open.kmi.iserve.commons.io.util.FilenameFilterBySyntax;
import uk.ac.open.kmi.iserve.commons.model.Service;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.net.URI;
import java.net.URL;
import java.util.Map;

/**
 * AllServicesPluginTest
 * Tests for uploading services to the server should be cleared for these tests
 * <p/>
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 15/06/2013
 * Time: 18:01
 */
public class AllServicesPluginTest {

    private static final String OWLS_TC_SERVICES = "/OWLS-TC3-MSM";
    private static final String JGD_SERVICES = "/jgd-services";
    private static final String SOA4RE_SERVICES = "/soa4re";

    private URI testFolder;

    private File[] msmTtlTcFiles;

    private AllServicesPlugin plugin;

    private int numServices = 0;
    private int numOperations = 0;

    @Before
    public void setUp() throws Exception {

        // Clean the whole thing before testing
        ManagerSingleton.getInstance().clearRegistry();

        testFolder = AllServicesPluginTest.class.getResource(OWLS_TC_SERVICES).toURI();
        FilenameFilter ttlFilter = new FilenameFilterBySyntax(Syntax.TTL);
        File dir = new File(testFolder);
        msmTtlTcFiles = dir.listFiles(ttlFilter);

        URI uri;
        Service svc;
        FileInputStream in;
        // Upload every document and obtain their URLs
        for (File ttlFile : msmTtlTcFiles) {
            in = new FileInputStream(ttlFile);
            uri = ManagerSingleton.getInstance().importService(in, MediaType.TEXT_TURTLE.getMediaType());
            svc = ManagerSingleton.getInstance().getService(uri);
            numServices++;
            numOperations += svc.getOperations().size();
        }

        plugin = new AllServicesPlugin();
    }

    @Test
    public void testDiscoverServices() throws Exception {
        Map<URL, MatchResult> result = plugin.discoverServices(null);
        Assert.assertNotNull(result);
        Assert.assertEquals(numServices, result.size());
    }

    @Test
    public void testDiscoverOperations() throws Exception {
        Map<URL, MatchResult> result = plugin.discoverOperations(null);
        Assert.assertNotNull(result);
        Assert.assertEquals(numOperations, result.size());
    }
}
