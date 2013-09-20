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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.MediaType;
import uk.ac.open.kmi.iserve.commons.io.Syntax;
import uk.ac.open.kmi.iserve.commons.io.Transformer;
import uk.ac.open.kmi.iserve.commons.io.util.FilenameFilterBySyntax;
import uk.ac.open.kmi.iserve.commons.io.util.FilenameFilterForTransformer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

/**
 * iServeFacadeTest
 * <p/>
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 06/06/2013
 * Time: 18:50
 */
public class iServeFacadeTest {

    private static final Logger log = LoggerFactory.getLogger(iServeFacadeTest.class);

    private static final String OWLS_TC3_MSM = "/OWLS-TC3-MSM";
    private static final String OWLS_TC4_PDDL = "/OWLS-TC4_PDDL/htdocs/services/1.1";
    private static final Syntax SYNTAX = Syntax.TTL;
    private static final String OWLS_MEDIATYPE = "application/owl+xml";

    private FilenameFilter ttlFilter;
    private FilenameFilter owlsFilter;

    private int numServices;
    private File[] msmTtlTcFiles;
    private File[] owlsTcFiles;

    @Before
    public void setUp() throws Exception {
        URI msmTestFolder = iServeFacadeTest.class.getResource(OWLS_TC3_MSM).toURI();
        ttlFilter = new FilenameFilterBySyntax(Syntax.TTL);
        File dir = new File(msmTestFolder);
        msmTtlTcFiles = dir.listFiles(ttlFilter);

        URI owlsTestFolder = iServeFacadeTest.class.getResource(OWLS_TC4_PDDL).toURI();
        owlsFilter = new FilenameFilterForTransformer(Transformer.getInstance().getTransformer(OWLS_MEDIATYPE));
        dir = new File(owlsTestFolder);
        owlsTcFiles = dir.listFiles(owlsFilter);
        numServices = msmTtlTcFiles.length + owlsTcFiles.length;
    }

    @Test
    public void testImportService() throws Exception {

        iServeFacade.getInstance().clearRegistry();
        InputStream in;
        List<URI> servicesUris;
        int count = 0;
        log.info("Importing MSM TTL services");
        for (File ttlFile : msmTtlTcFiles) {
            in = new FileInputStream(ttlFile);
            log.info("Adding service: {}", ttlFile.getName());
            servicesUris = iServeFacade.getInstance().importServices(in, MediaType.TEXT_TURTLE.getMediaType());
            Assert.assertNotNull(servicesUris);
            log.info("Service added: {}", servicesUris.get(0).toASCIIString());
            count++;
        }
        Assert.assertEquals(msmTtlTcFiles.length, count);

        // Test exploiting import plugins
        count = 0;
        log.info("Importing OWLS services");
        for (File owlsFile : owlsTcFiles) {
            in = new FileInputStream(owlsFile);
            log.info("Adding service: {}", owlsFile.getName());
            servicesUris = iServeFacade.getInstance().importServices(in, OWLS_MEDIATYPE);
            Assert.assertNotNull(servicesUris);
            log.info("Service added: {}", servicesUris.get(0).toASCIIString());
            count++;
        }
        Assert.assertEquals(owlsTcFiles.length, count);
    }
}
