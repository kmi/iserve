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

import com.google.common.base.Stopwatch;
import com.google.common.collect.Table;
import es.usc.citius.composit.importer.wsc.wscxml.WSCImporter;
import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.MediaType;
import uk.ac.open.kmi.iserve.commons.io.Syntax;
import uk.ac.open.kmi.iserve.commons.io.Transformer;
import uk.ac.open.kmi.iserve.commons.io.util.FilenameFilterBySyntax;
import uk.ac.open.kmi.iserve.commons.model.Service;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.disco.DiscoMatchType;
import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DiscoMatcherTest
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 01/08/2013
 */
public class LogicConceptMatcherWSC08Test extends TestCase {

    private static final Logger log = LoggerFactory.getLogger(LogicConceptMatcherWSC08Test.class);
    private static final String WSC08_01_SERVICES = "/wsc08-dataset01/services/services.xml";
    private static final String MEDIATYPE = "text/xml";

    private LogicConceptMatcher matcher = new LogicConceptMatcher();

    public static TestSetup suite() {

        TestSetup setup = new TestSetup(new TestSuite(LogicConceptMatcherWSC08Test.class)) {
            protected void setUp() throws Exception {
                BasicConfigurator.configure();
                // do your one-time setup here

                // Clean the whole thing before testing
                ManagerSingleton.getInstance().clearRegistry();

                log.info("Importing WSC 2008 services");
                String file =  LogicConceptMatcherWSC08Test.class.getResource(WSC08_01_SERVICES).getFile();
                log.debug("Using " + file);
                File services = new File(file);

                //List<Service> result = new WSCImporter().transform(new FileInputStream(services), null);
                // Automatic plugin discovery
                List<Service> result = Transformer.getInstance().transform(services, null, MEDIATYPE);
                // Import all services
                for(Service s : result){
                    URI uri = ManagerSingleton.getInstance().importService(s);
                    Assert.assertNotNull(uri);
                    log.info("Service added: " + uri.toASCIIString());
                }

                /*

                URI testFolder = LogicConceptMatcherWSC08Test.class.getResource(WSC08_01_SERVICES).toURI();
                log.debug("Test folder {}", testFolder);
                FilenameFilter ttlFilter = new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return (name.endsWith(".owl") || name.endsWith(".owls"));
                    }
                };
                File dir = new File(testFolder);
                File[] msmTtlTcFiles = dir.listFiles(ttlFilter);
                log.debug("Valid services detected: {}", msmTtlTcFiles);

                FileInputStream in;
                // Upload every document and obtain their URLs
                for (File ttlFile : msmTtlTcFiles) {
                    log.debug("Importing {}", ttlFile.getAbsolutePath());
                    in = new FileInputStream(ttlFile);
                    ManagerSingleton.getInstance().importService(in, OWLS_MEDIATYPE);
                } */
            }

            protected void tearDown() throws Exception {
                // do your one-time tear down here!
            }
        };
        return setup;
    }

    public void testDirectPluginMatch() throws Exception {

        URI origin = URI.create("http://127.0.0.1/ontology/taxonomy.owl#con1655991159");
        URI destination = URI.create("http://127.0.0.1/ontology/taxonomy.owl#con409488015");

        // Obtain matches
        Stopwatch stopwatch = new Stopwatch().start();
        MatchResult match = matcher.match(origin, destination);
        stopwatch.stop();

        log.info("Obtained match in {} \n {}", stopwatch, match);
        Assert.assertEquals(DiscoMatchType.Plugin, match.getMatchType());
    }

    public void testDirectSubsumeMatch() throws Exception {

        URI origin = URI.create("http://127.0.0.1/ontology/taxonomy.owl#con1655991159");
        URI destination = URI.create("http://127.0.0.1/ontology/taxonomy.owl#con409488015");

        // Obtain matches
        Stopwatch stopwatch = new Stopwatch().start();
        MatchResult match = matcher.match(destination, origin);
        stopwatch.stop();

        log.info("Obtained match in {} \n {}", stopwatch, match);
        Assert.assertEquals(DiscoMatchType.Subsume, match.getMatchType());
    }

    public void testIndirectPluginMatch() throws Exception {

        URI origin = URI.create("http://127.0.0.1/ontology/taxonomy.owl#con1901563774");
        URI destination = URI.create("http://127.0.0.1/ontology/taxonomy.owl#con241744282");

        // Obtain matches
        Stopwatch stopwatch = new Stopwatch().start();
        MatchResult match = matcher.match(origin, destination);
        stopwatch.stop();

        log.info("Obtained match in {} \n {}", stopwatch, match);
        Assert.assertEquals(DiscoMatchType.Plugin, match.getMatchType());
    }

    /*
    @Test
    public void testMatchBySets() throws Exception {

        Set<URI> origins = new HashSet<URI>();
        origins.add(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"));
        origins.add(URI.create("http://127.0.0.1/ontology/SUMO.owl#Abstract"));
        origins.add(URI.create("http://127.0.0.1/ontology/SUMO.owl#TherapeuticProcess"));

        Set<URI> destinations = new HashSet<URI>();
        destinations.add(URI.create("http://127.0.0.1/ontology/SUMO.owl#Quantity"));
        destinations.add(URI.create("http://127.0.0.1/ontology/SUMO.owl#IntentionalProcess"));
        destinations.add(URI.create("http://127.0.0.1/ontology/SUMO.owl#Entity"));

        // Obtain cross-matches
        Stopwatch stopwatch = new Stopwatch().start();
        Table<URI, URI, MatchResult> matches =
                matcher.match(origins, destinations);
        stopwatch.stop();

        log.info("Obtained all cross matches ({}) in {} \n {}", matches.size(), stopwatch, matches);
        Assert.assertEquals(matches.size(), 4);
        stopwatch.reset();

    }

    @Test
    public void testListMatchesOfType() throws Exception {
        // Obtain only exact
        Stopwatch stopwatch = new Stopwatch().start();
        Map<URI, MatchResult> matches =
                matcher.listMatchesOfType(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), DiscoMatchType.Exact);
        stopwatch.stop();

        log.info("Obtained all exact matches ({}) in {} \n", matches.size(), stopwatch, matches);
        Assert.assertEquals(matches.size(), 1);
        stopwatch.reset();

        // Obtain only plugin
        stopwatch.start();
        matches = matcher.listMatchesOfType(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), DiscoMatchType.Plugin);
        stopwatch.stop();

        log.info("Obtained all plugin matches ({}) in {} \n", matches.size(), stopwatch, matches);
        Assert.assertEquals(matches.size(), 5);
        stopwatch.reset();

        // Obtain only plugin
        stopwatch.start();
        matches = matcher.listMatchesOfType(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), DiscoMatchType.Subsume);
        stopwatch.stop();

        log.info("Obtained all plugin matches ({}) in {} \n", matches.size(), stopwatch, matches);
        Assert.assertEquals(matches.size(), 0);
        stopwatch.reset();
    }

    @Test
    public void testListMatchesAtLeastOfType() throws Exception {
        // Obtain at least exact
        Stopwatch stopwatch = new Stopwatch().start();
        Map<URI, MatchResult> matches =
                matcher.listMatchesAtLeastOfType(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), DiscoMatchType.Exact);
        stopwatch.stop();

        log.info("Obtained at least Exact matches ({}) in {} \n", matches.size(), stopwatch, matches);
        Assert.assertEquals(matches.size(), 1);
        stopwatch.reset();

        // Obtain at least plugin
        stopwatch.start();
        matches = matcher.listMatchesAtLeastOfType(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), DiscoMatchType.Plugin);
        stopwatch.stop();

        log.info("Obtained at least Plugin matches ({}) in {} \n", matches.size(), stopwatch, matches);
        Assert.assertEquals(matches.size(), 6);
        stopwatch.reset();

        // Obtain at least subsumes
        stopwatch.start();
        matches = matcher.listMatchesAtLeastOfType(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), DiscoMatchType.Subsume);
        stopwatch.stop();

        log.info("Obtained at least Subsumes matches ({}) in {} \n", matches.size(), stopwatch, matches);
        Assert.assertEquals(matches.size(), 6);
        stopwatch.reset();

        // Obtain at least fail
        stopwatch.start();
        matches = matcher.listMatchesAtLeastOfType(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), DiscoMatchType.Fail);
        stopwatch.stop();

        log.info("Obtained at least Fail matches ({}) in {} \n", matches.size(), stopwatch, matches);
        Assert.assertEquals(matches.size(), 6);
        stopwatch.reset();
    }

    @Test
    public void testListMatchesAtMostOfType() throws Exception {
        // Obtain at least exact
        Stopwatch stopwatch = new Stopwatch().start();
        Map<URI, MatchResult> matches =
                matcher.listMatchesAtMostOfType(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), DiscoMatchType.Exact);
        stopwatch.stop();

        log.info("Obtained at most Exact matches ({}) in {} \n", matches.size(), stopwatch, matches);
        Assert.assertEquals(matches.size(), 6);
        stopwatch.reset();

        // Obtain at least plugin
        stopwatch.start();
        matches = matcher.listMatchesAtMostOfType(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), DiscoMatchType.Plugin);
        stopwatch.stop();

        log.info("Obtained at most Plugin matches ({}) in {} \n", matches.size(), stopwatch, matches);
        Assert.assertEquals(matches.size(), 5);
        stopwatch.reset();

        // Obtain at least subsumes
        stopwatch.start();
        matches = matcher.listMatchesAtMostOfType(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), DiscoMatchType.Subsume);
        stopwatch.stop();

        log.info("Obtained at most Subsumes matches ({}) in {} \n", matches.size(), stopwatch, matches);
        Assert.assertEquals(matches.size(), 0);
        stopwatch.reset();

        // Obtain at least fail
        stopwatch.start();
        matches = matcher.listMatchesAtMostOfType(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), DiscoMatchType.Fail);
        stopwatch.stop();

        log.info("Obtained at most Fail matches ({}) in {} \n", matches.size(), stopwatch, matches);
        Assert.assertEquals(matches.size(), 0);
        stopwatch.reset();
    }

    @Test
    public void testListMatchesWithinRange() throws Exception {

        // Obtain all matches
        Stopwatch stopwatch = new Stopwatch().start();
        Map<URI, MatchResult> matches =
                matcher.listMatchesWithinRange(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), DiscoMatchType.Fail, DiscoMatchType.Exact);
        stopwatch.stop();

        log.info("Obtained all matches ({}) in {} \n", matches.size(), stopwatch, matches);
        Assert.assertEquals(matches.size(), 6);
        stopwatch.reset();

        // Obtain only exact
        stopwatch.start();
        matches = matcher.listMatchesWithinRange(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), DiscoMatchType.Exact, DiscoMatchType.Exact);
        stopwatch.stop();

        log.info("Obtained exact matches ({}) in {} \n", matches.size(), stopwatch, matches);
        Assert.assertEquals(matches.size(), 1);
        stopwatch.reset();

        // Obtain from Plugin up
        stopwatch.start();
        matches = matcher.listMatchesWithinRange(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), DiscoMatchType.Plugin, DiscoMatchType.Exact);
        stopwatch.stop();

        log.info("Obtained matches >= Plugin ({}) in {} \n", matches.size(), stopwatch, matches);
        Assert.assertEquals(matches.size(), 6);
        stopwatch.reset();

        // Obtain Plugin and subsumes
        stopwatch.start();
        matches = matcher.listMatchesWithinRange(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), DiscoMatchType.Subsume, DiscoMatchType.Plugin);
        stopwatch.stop();

        log.info("Obtained Subsumes >= matches >= Plugin ({}) in {} \n", matches.size(), stopwatch, matches);
        Assert.assertEquals(matches.size(), 5);
        stopwatch.reset();

        // Invert limits
        stopwatch.start();
        matches = matcher.listMatchesWithinRange(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), DiscoMatchType.Exact, DiscoMatchType.Plugin);
        stopwatch.stop();

        log.info("Obtained Exact >= matches >= Plugin ({}) in {} \n", matches.size(), stopwatch, matches);
        Assert.assertEquals(matches.size(), 0);
        stopwatch.reset();

    }*/
}
