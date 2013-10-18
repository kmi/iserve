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
import junit.framework.Assert;
import org.jukito.JukitoRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.MediaType;
import uk.ac.open.kmi.iserve.commons.io.Syntax;
import uk.ac.open.kmi.iserve.commons.io.util.FilenameFilterBySyntax;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.manager.impl.iServeFacade;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ConceptMatcherOwlTcTest tests the Concept Matchers using some data from OWLS TC
 * Ensures that ontologies are actually loaded and reasoned upon.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 01/08/2013
 */
@RunWith(JukitoRunner.class)
public class ConceptMatcherOwlTcTest {

    private static final Logger log = LoggerFactory.getLogger(ConceptMatcherOwlTcTest.class);

    private static final String OWLS_TC_SERVICES = "/OWLS-TC3-MSM";

    @Inject
    private ConceptMatcher conceptMatcher;

    /**
     * JukitoModule.
     */
    public static class InnerModule extends ConfiguredTestModule {
        @Override
        protected void configureTest() {
            // Get properties
            super.configureTest();
            // bind
            bind(ConceptMatcher.class).to(SparqlLogicConceptMatcher.class);

            // Necessary to verify interaction with the real object
            bindSpy(SparqlLogicConceptMatcher.class);
        }
    }

    @BeforeClass
    public static void setupTests() throws Exception {
        iServeFacade.getInstance().clearRegistry();
        uploadOwlsTc();
    }

    private static void uploadOwlsTc() throws URISyntaxException, FileNotFoundException, SalException {
        // Obtain service documents
        URI testFolder = ConceptMatcherOwlTcTest.class.getResource(OWLS_TC_SERVICES).toURI();
        FilenameFilter ttlFilter = new FilenameFilterBySyntax(Syntax.TTL);
        File dir = new File(testFolder);
        File[] msmTtlTcFiles = dir.listFiles(ttlFilter);

        FileInputStream in;
        // Upload every document and obtain their URLs
        for (File ttlFile : msmTtlTcFiles) {
            log.debug("Importing {}", ttlFile.getAbsolutePath());
            in = new FileInputStream(ttlFile);
            iServeFacade.getInstance().importServices(in, MediaType.TEXT_TURTLE.getMediaType());
        }
        log.debug("Ready");
    }

    public void testMatch() throws Exception {

        URI origin = URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar");
        URI destination = URI.create("http://127.0.0.1/ontology/SUMO.owl#Quantity");

        // Obtain matches
        Stopwatch stopwatch = new Stopwatch().start();
        MatchResult match = conceptMatcher.match(origin, destination);
        stopwatch.stop();

        log.info("Obtained match in {} \n {}", stopwatch, match);
        Assert.assertEquals(match.getMatchType(), LogicConceptMatchType.Plugin);
    }

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
                conceptMatcher.match(origins, destinations);
        stopwatch.stop();

        log.info("Obtained all cross matches ({}) in {}", matches.size(), stopwatch);
        Assert.assertEquals(matches.size(), 4);
        stopwatch.reset();

    }

    @Test
    public void testListMatchesOfType() throws Exception {
        // Obtain only exact
        Stopwatch stopwatch = new Stopwatch().start();
        Map<URI, MatchResult> matches =
                conceptMatcher.listMatchesOfType(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), LogicConceptMatchType.Exact);
        stopwatch.stop();

        log.info("Obtained ({}) exact matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertEquals(1, matches.size());
        stopwatch.reset();

        // Obtain only plugin
        stopwatch.start();
        matches = conceptMatcher.listMatchesOfType(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), LogicConceptMatchType.Plugin);
        stopwatch.stop();

        log.info("Obtained ({}) plugin matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertEquals(6, matches.size());
        stopwatch.reset();

        // Obtain only plugin
        stopwatch.start();
        matches = conceptMatcher.listMatchesOfType(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), LogicConceptMatchType.Subsume);
        stopwatch.stop();

        log.info("Obtained ({}) plugin matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertEquals(1, matches.size());
        Assert.assertTrue(matches.containsKey(URI.create("http://www.w3.org/2002/07/owl#Nothing")));
        stopwatch.reset();
    }

    @Test
    public void testListMatchesAtLeastOfType() throws Exception {
        // Obtain at least exact
        Stopwatch stopwatch = new Stopwatch().start();
        Map<URI, MatchResult> matches =
                conceptMatcher.listMatchesAtLeastOfType(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), LogicConceptMatchType.Exact);
        stopwatch.stop();

        log.info("Obtained ({}) At least Exact matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertEquals(1, matches.size());
        stopwatch.reset();

        // Obtain at least plugin
        stopwatch.start();
        matches = conceptMatcher.listMatchesAtLeastOfType(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), LogicConceptMatchType.Plugin);
        stopwatch.stop();

        log.info("Obtained ({}) At least Plugin matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertEquals(7, matches.size());
        stopwatch.reset();

        // Obtain at least subsumes
        stopwatch.start();
        matches = conceptMatcher.listMatchesAtLeastOfType(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), LogicConceptMatchType.Subsume);
        stopwatch.stop();

        log.info("Obtained ({}) At least Subsumes matches - {] - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertEquals(8, matches.size());
        stopwatch.reset();

        // Obtain at least fail
        stopwatch.start();
        matches = conceptMatcher.listMatchesAtLeastOfType(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), LogicConceptMatchType.Fail);
        stopwatch.stop();

        log.info("Obtained ({}) At least Fail matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertEquals(8, matches.size());
        stopwatch.reset();
    }

    @Test
    public void testListMatchesAtMostOfType() throws Exception {
        // Obtain at least exact
        Stopwatch stopwatch = new Stopwatch().start();
        Map<URI, MatchResult> matches =
                conceptMatcher.listMatchesAtMostOfType(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), LogicConceptMatchType.Exact);
        stopwatch.stop();

        log.info("Obtained ({}) At most Exact matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertEquals(8, matches.size());
        stopwatch.reset();

        // Obtain at least plugin
        stopwatch.start();
        matches = conceptMatcher.listMatchesAtMostOfType(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), LogicConceptMatchType.Plugin);
        stopwatch.stop();

        log.info("Obtained ({}) At most Plugin matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertEquals(7, matches.size());
        stopwatch.reset();

        // Obtain at least subsumes
        stopwatch.start();
        matches = conceptMatcher.listMatchesAtMostOfType(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), LogicConceptMatchType.Subsume);
        stopwatch.stop();

        log.info("Obtained ({}) At most Subsumes matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertEquals(1, matches.size());
        stopwatch.reset();

        // Obtain at least fail
        stopwatch.start();
        matches = conceptMatcher.listMatchesAtMostOfType(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), LogicConceptMatchType.Fail);
        stopwatch.stop();

        log.info("Obtained ({}) At most Fail matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertEquals(0, matches.size());
        stopwatch.reset();
    }

    @Test
    public void testListMatchesWithinRange() throws Exception {

        // Obtain all matches
        Stopwatch stopwatch = new Stopwatch().start();
        Map<URI, MatchResult> matches =
                conceptMatcher.listMatchesWithinRange(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), LogicConceptMatchType.Fail, LogicConceptMatchType.Exact);
        stopwatch.stop();

        log.info("Obtained ({}) all matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertEquals(8, matches.size());
        stopwatch.reset();

        // Obtain only exact
        stopwatch.start();
        matches = conceptMatcher.listMatchesWithinRange(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), LogicConceptMatchType.Exact, LogicConceptMatchType.Exact);
        stopwatch.stop();

        log.info("Obtained ({}) exact matches - {} - in {} \n", matches.size(), stopwatch);
        Assert.assertEquals(1, matches.size());
        stopwatch.reset();

        // Obtain from Plugin up
        stopwatch.start();
        matches = conceptMatcher.listMatchesWithinRange(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), LogicConceptMatchType.Plugin, LogicConceptMatchType.Exact);
        stopwatch.stop();

        log.info("Obtained ({}) matches >= Plugin - {} - in {} \n", matches.size(), stopwatch);
        Assert.assertEquals(7, matches.size());
        stopwatch.reset();

        // Obtain Plugin and subsumes
        stopwatch.start();
        matches = conceptMatcher.listMatchesWithinRange(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), LogicConceptMatchType.Subsume, LogicConceptMatchType.Plugin);
        stopwatch.stop();

        log.info("Obtained ({}) Subsumes >= matches >= Plugin - {} - in {} \n", matches.size(), stopwatch);
        Assert.assertEquals(7, matches.size());
        stopwatch.reset();

        // Invert limits
        stopwatch.start();
        matches = conceptMatcher.listMatchesWithinRange(URI.create("http://127.0.0.1/ontology/SUMO.owl#EuroDollar"), LogicConceptMatchType.Exact, LogicConceptMatchType.Plugin);
        stopwatch.stop();

        log.info("Obtained ({}) Exact >= matches >= Plugin - {} - in {} \n", matches.size(), stopwatch);
        Assert.assertEquals(0, matches.size());
        stopwatch.reset();

    }
}
