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

import com.google.common.eventbus.EventBus;
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
import uk.ac.open.kmi.iserve.sal.exception.DocumentException;
import uk.ac.open.kmi.iserve.sal.manager.DocumentManager;

import java.io.*;
import java.net.URI;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 * DocumentManagerFileSystemTest
 * <p/>
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 06/06/2013
 * Time: 18:50
 */
public class DocumentManagerFileSystemTest {

    private static final Logger log = LoggerFactory.getLogger(DocumentManagerFileSystemTest.class);

    private static final String OWLS_TC3_MSM = "/OWLS-TC3-MSM";
    private static final String OWLS_TC4_PDDL = "/OWLS-TC4_PDDL/htdocs/services/1.1";
    private static final Syntax SYNTAX = Syntax.TTL;
    private static final String OWLS_MEDIATYPE = "application/owl+xml";
    private static final String ISERVE_TEST_URI = "http://localhost:9090/iserve";
    private static final String ISERVE_TEST_DOCS_FLD = "file:///tmp/iserve/service-docs";

    private FilenameFilter ttlFilter;
    private FilenameFilter owlsFilter;

    private int numServices;
    private File[] msmTtlTcFiles;
    private File[] owlsTcFiles;

    private static DocumentManager documentManager;


    @Before
    public void setUp() throws Exception {
        URI msmTestFolder = DocumentManagerFileSystemTest.class.getResource(OWLS_TC3_MSM).toURI();
        ttlFilter = new FilenameFilterBySyntax(Syntax.TTL);
        File dir = new File(msmTestFolder);
        msmTtlTcFiles = dir.listFiles(ttlFilter);

        URI owlsTestFolder = DocumentManagerFileSystemTest.class.getResource(OWLS_TC4_PDDL).toURI();
        owlsFilter = new FilenameFilterForTransformer(Transformer.getInstance().getTransformer(OWLS_MEDIATYPE));
        dir = new File(owlsTestFolder);
        owlsTcFiles = dir.listFiles(owlsFilter);
        numServices = msmTtlTcFiles.length + owlsTcFiles.length;

//        documentManager = ManagerSingleton.getInstance().getDocumentManager();
        EventBus eventBus = new EventBus();
        documentManager = new DocumentManagerFileSystem(eventBus, ISERVE_TEST_URI, ISERVE_TEST_DOCS_FLD);
    }


    @Test
    public void testCreateDocument() throws Exception {

        // Clear and upload data
        documentManager.clearDocuments();
        int count = uploadMsmFiles();
        Assert.assertEquals(msmTtlTcFiles.length, count);

        // TODO: We should check the content is correct
    }

    private int uploadMsmFiles() throws FileNotFoundException, DocumentException {

        InputStream in;
        URI docUri;
        int count = 0;
        // Upload every document and obtain their URLs
        for (File ttlFile : msmTtlTcFiles) {
            in = new FileInputStream(ttlFile);
            log.info("Adding document: {}", ttlFile.getName());
            String fileExt = MediaType.NATIVE_MEDIATYPE_SYNTAX_MAP.get(MediaType.TEXT_TURTLE.getMediaType()).getExtension();
            docUri = documentManager.createDocument(in, fileExt);
            Assert.assertNotNull(docUri);
            log.info("Service added: {}", docUri.toASCIIString());
            count++;
        }
        return count;
    }

    @Test
    public void testDeleteDocument() throws Exception {

        // Clear and upload data
        documentManager.clearDocuments();
        uploadMsmFiles();

        boolean result;
        URI docUri;
        Random rand = new Random();
        Set<URI> documents = documentManager.listDocuments();
        int numDocs = documents.size();
        int delta = numDocs / 10;
        int index = rand.nextInt(10 - 0 + 1) + 0;
        int count = numDocs;
        Iterator<URI> docsIter = documents.iterator();
        while (docsIter.hasNext() && index < numDocs) {
            docUri = docsIter.next();
            log.info("Deleting document: {}", docUri);
            result = documentManager.deleteDocument(docUri);
            Assert.assertTrue(result);
            index += delta;
            count--;
        }
        // Now ensure that the number of docs was reduced accordingly;
        documents = documentManager.listDocuments();
        Assert.assertEquals(count, documents.size());
    }

    @Test
    public void testListDocuments() throws Exception {

        // Clear and upload data
        documentManager.clearDocuments();
        int count = uploadMsmFiles();

        Set<URI> documents = documentManager.listDocuments();
        Assert.assertEquals(count, documents.size());
    }

    @Test
    public void testGetDocument() throws Exception {

        // Clear and upload data
        documentManager.clearDocuments();
        int count = uploadMsmFiles();

        InputStream is;
        URI docUri;
        Random rand = new Random();
        Set<URI> documents = documentManager.listDocuments();
        int numDocs = documents.size();
        int delta = numDocs / 10;
        int index = rand.nextInt(10 - 0 + 1) + 0;
        Iterator<URI> docsIter = documents.iterator();
        while (docsIter.hasNext() && index < numDocs) {
            docUri = docsIter.next();
            log.info("Obtaining document: {}", docUri);
            is = documentManager.getDocument(docUri);
            Assert.assertNotNull(is);
            index += delta;
        }
    }

}
