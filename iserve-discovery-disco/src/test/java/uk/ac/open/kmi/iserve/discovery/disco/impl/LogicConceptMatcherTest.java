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

import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.MediaType;
import uk.ac.open.kmi.iserve.commons.io.Syntax;
import uk.ac.open.kmi.iserve.commons.io.util.FilenameFilterBySyntax;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * DiscoMatcherTest
 * TODO: Provide Description
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 01/08/2013
 */
public class LogicConceptMatcherTest extends TestCase {

    private static final Logger log = LoggerFactory.getLogger(LogicConceptMatcherTest.class);
    private static final String OWLS_TC_SERVICES = "/OWLS-TC3-MSM";
    private static final String JGD_SERVICES = "/jgd-services";
    private static final String SOA4RE_SERVICES = "/soa4re";

    private LogicConceptMatcher matcher = new LogicConceptMatcher();

    public static TestSetup suite() {

        TestSetup setup = new TestSetup(new TestSuite(LogicConceptMatcherTest.class)) {
            protected void setUp() throws Exception {
                // do your one-time setup here

                // Clean the whole thing before testing
                ManagerSingleton.getInstance().clearRegistry();

                URI testFolder = LogicConceptMatcherTest.class.getResource(OWLS_TC_SERVICES).toURI();
                FilenameFilter ttlFilter = new FilenameFilterBySyntax(Syntax.TTL);
                File dir = new File(testFolder);
                File[] msmTtlTcFiles = dir.listFiles(ttlFilter);

                FileInputStream in;
                // Upload every document and obtain their URLs
                for (File ttlFile : msmTtlTcFiles) {
                    in = new FileInputStream(ttlFile);
                    ManagerSingleton.getInstance().importService(in, MediaType.TEXT_TURTLE.getMediaType());
                }
            }

            protected void tearDown() throws Exception {
                // do your one-time tear down here!
            }
        };
        return setup;
    }

    @Test
    public void testGetMatcherDescription() throws Exception {
        Assert.fail("Not yet implemented");
    }

    @Test
    public void testGetMatcherVersion() throws Exception {
        Assert.fail("Not yet implemented");
    }

    @Test
    public void testGetMatchTypesSupported() throws Exception {
        Assert.fail("Not yet implemented");
    }

    @Test
    public void testMatch() throws Exception {
        Assert.fail("Not yet implemented");
    }

    @Test
    public void testMatchBySets() throws Exception {
        Assert.fail("Not yet implemented");
    }

    @Test
    public void testListMatchesOfType() throws Exception {
        Assert.fail("Not yet implemented");
    }

    @Test
    public void testListMatchesAtLeastOfType() throws Exception {
        Assert.fail("Not yet implemented");
    }

    @Test
    public void testListMatchesAtMostOfType() throws Exception {
        Assert.fail("Not yet implemented");
    }

    @Test
    public void testListMatchesWithinRange() throws Exception {
        Assert.fail("Not yet implemented");
    }

    @Test
    public void testGenerateAllMatchesQuery() throws Exception {

        SortedSet<URI> origins = new TreeSet<URI>();
        origins.add(URI.create("http://127.0.0.1/ontology/concept.owl#UntangibleObjects"));
//        origins.add(URI.create("http://127.0.0.1/ontology/SUMO.owl#Agent"));
        origins.add(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"));
        origins.add(URI.create("http://127.0.0.1/ontology/SUMO.owl#Repairing"));
        Map<URI, MatchResult> matches = matcher.obtainAllMatchResults(origins);
        Assert.fail("Not yet implemented");
    }
}
