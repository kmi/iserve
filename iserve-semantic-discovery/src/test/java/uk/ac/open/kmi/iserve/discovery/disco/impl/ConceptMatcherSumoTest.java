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
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import junit.framework.Assert;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.sal.manager.RegistryManager;
import uk.ac.open.kmi.iserve.sal.manager.impl.RegistryManagementModule;

import javax.inject.Inject;
import java.net.URI;
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
public class ConceptMatcherSumoTest {

    private static final Logger log = LoggerFactory.getLogger(ConceptMatcherSumoTest.class);

    private static final String OWL_THING = "http://www.w3.org/2002/07/owl#Thing";
    private static final String OWL_NOTHING = "http://www.w3.org/2002/07/owl#Nothing";

    private static final String SUMO_ABSTRACT = "http://127.0.0.1:8000/ontology/SUMO.owl#Abstract";
    private static final String SUMO_PHYSICAL_QUANTITY = "http://127.0.0.1:8000/ontology/SUMO.owl#PhysicalQuantity";
    private static final String SUMO_CURRENCY_MEASURE = "http://127.0.0.1:8000/ontology/SUMO.owl#CurrencyMeasure";
    private static final String SUMO_QUANTITY = "http://127.0.0.1:8000/ontology/SUMO.owl#Quantity";
    private static final String SUMO_CONSTANT_QUANTITY = "http://127.0.0.1:8000/ontology/SUMO.owl#ConstantQuantity";
    private static final String SUMO_EURO_DOLLAR = "http://127.0.0.1:8000/ontology/SUMO.owl#EuroDollar";

    private static final String SUMO_THERAPEUTIC_PROCESS = "http://127.0.0.1:8000/ontology/SUMO.owl#TherapeuticProcess";
    private static final String SUMO_INTENTIONAL_PROCESS = "http://127.0.0.1:8000/ontology/SUMO.owl#IntentionalProcess";
    private static final String SUMO_ENTITY = "http://127.0.0.1:8000/ontology/SUMO.owl#Entity";
    private static final String SUMO_CM = "http://127.0.0.1:8000/ontology/SUMO.owl#Centimeter";
    private static final String SUMO_OWL = "http://127.0.0.1:8000/ontology/SUMO.owl";

    @Inject
    private ConceptMatcher conceptMatcher;

    @BeforeClass
    public static void setupTests() throws Exception {
        Injector injector = Guice.createInjector(new RegistryManagementModule());
        RegistryManager registryManager = injector.getInstance(RegistryManager.class);
        registryManager.clearRegistry();

        // Upload SUMO
        Model model = ModelFactory.createDefaultModel();
        model.read(SUMO_OWL);
        registryManager.getKnowledgeBaseManager().uploadModel(new URI(SUMO_OWL), model, true);
    }

    @Test
    public void testMatch() throws Exception {

        URI euro = URI.create(SUMO_EURO_DOLLAR);
        URI quantity = URI.create(SUMO_QUANTITY);
        URI cm = URI.create(SUMO_CM);

        // Obtain matches
        // Check Plugin
        Stopwatch stopwatch = new Stopwatch().start();
        MatchResult match = conceptMatcher.match(euro, quantity);
        stopwatch.stop();

        log.info("Obtained match in {} \n {}", stopwatch, match);
        Assert.assertEquals(match.getMatchType(), LogicConceptMatchType.Plugin);

        // Check Subsumes
        stopwatch = new Stopwatch().start();
        match = conceptMatcher.match(quantity, euro);
        stopwatch.stop();

        log.info("Obtained match in {} \n {}", stopwatch, match);
        Assert.assertEquals(match.getMatchType(), LogicConceptMatchType.Subsume);

        // Check Exact
        stopwatch = new Stopwatch().start();
        match = conceptMatcher.match(euro, euro);
        stopwatch.stop();

        log.info("Obtained match in {} \n {}", stopwatch, match);
        Assert.assertEquals(match.getMatchType(), LogicConceptMatchType.Exact);

        // Check Fail
        stopwatch = new Stopwatch().start();
        match = conceptMatcher.match(euro, cm);
        stopwatch.stop();

        log.info("Obtained match in {} \n {}", stopwatch, match);
        Assert.assertEquals(match.getMatchType(), LogicConceptMatchType.Fail);

        stopwatch = new Stopwatch().start();
        match = conceptMatcher.match(cm, euro);
        stopwatch.stop();

        log.info("Obtained match in {} \n {}", stopwatch, match);
        Assert.assertEquals(match.getMatchType(), LogicConceptMatchType.Fail);

    }

    @Test
    public void testMatchBySets() throws Exception {

        Set<URI> origins = new HashSet<URI>();
        origins.add(URI.create(SUMO_EURO_DOLLAR));
        origins.add(URI.create(SUMO_ABSTRACT));
        origins.add(URI.create(SUMO_THERAPEUTIC_PROCESS));

        Set<URI> destinations = new HashSet<URI>();
        destinations.add(URI.create(SUMO_QUANTITY));
        destinations.add(URI.create(SUMO_INTENTIONAL_PROCESS));
        destinations.add(URI.create(SUMO_ENTITY));

        // Obtain cross-matches
        Stopwatch stopwatch = new Stopwatch().start();
        Table<URI, URI, MatchResult> matches =
                conceptMatcher.match(origins, destinations);
        stopwatch.stop();

        log.info("Obtained all cross matches ({}) in {}", matches.size(), stopwatch);
        Assert.assertEquals(4, matches.size());
        stopwatch.reset();

    }

    @Test
    public void testListMatchesOfType() throws Exception {
        // Obtain only exact
        Stopwatch stopwatch = new Stopwatch().start();
        Map<URI, MatchResult> matches =
                conceptMatcher.listMatchesOfType(URI.create(SUMO_EURO_DOLLAR), LogicConceptMatchType.Exact);
        stopwatch.stop();

        log.info("Obtained ({}) exact matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertTrue(matches.size() == 1);
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_EURO_DOLLAR)));
        stopwatch.reset();

        // Obtain only plugin for Euro
        stopwatch.start();
        matches = conceptMatcher.listMatchesOfType(URI.create(SUMO_EURO_DOLLAR), LogicConceptMatchType.Plugin);
        stopwatch.stop();

        log.info("Obtained ({}) plugin matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertTrue(matches.size() == 23 || matches.size() == 24);
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_ABSTRACT)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_PHYSICAL_QUANTITY)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_CURRENCY_MEASURE)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_QUANTITY)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_CONSTANT_QUANTITY)));
        // Depending on the reasoning use there should also be "http://www.w3.org/2002/07/owl#Thing"
        if (matches.size() == 24) {
            Assert.assertTrue(matches.containsKey(URI.create(OWL_THING)));
        }

        stopwatch.reset();

        // Obtain only plugin
        stopwatch.start();
        matches = conceptMatcher.listMatchesOfType(URI.create(SUMO_EURO_DOLLAR), LogicConceptMatchType.Subsume);
        stopwatch.stop();

        log.info("Obtained ({}) plugin matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertTrue(matches.size() <= 1);
        // Depending on the reasoning there should be "http://www.w3.org/2002/07/owl#Nothing"
        if (matches.size() == 1) {
            Assert.assertTrue(matches.containsKey(URI.create(OWL_NOTHING)));
        }
        stopwatch.reset();
    }

    @Test
    public void testListMatchesAtLeastOfType() throws Exception {
        // Obtain at least exact
        Stopwatch stopwatch = new Stopwatch().start();
        Map<URI, MatchResult> matches =
                conceptMatcher.listMatchesAtLeastOfType(URI.create(SUMO_EURO_DOLLAR), LogicConceptMatchType.Exact);
        stopwatch.stop();

        log.info("Obtained ({}) At least Exact matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertTrue(matches.size() == 1);
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_EURO_DOLLAR)));

        stopwatch.reset();

        // Obtain at least plugin
        stopwatch.start();
        matches = conceptMatcher.listMatchesAtLeastOfType(URI.create(SUMO_EURO_DOLLAR), LogicConceptMatchType.Plugin);
        stopwatch.stop();

        log.info("Obtained ({}) At least Exact matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertTrue(matches.size() == 24 || matches.size() == 25);
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_ABSTRACT)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_PHYSICAL_QUANTITY)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_CURRENCY_MEASURE)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_QUANTITY)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_CONSTANT_QUANTITY)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_EURO_DOLLAR)));
        // Depending on the reasoning use there should also be "http://www.w3.org/2002/07/owl#Thing"
        if (matches.size() == 25) {
            Assert.assertTrue(matches.containsKey(URI.create(OWL_THING)));
        }

        stopwatch.reset();

        // Obtain at least subsumes
        stopwatch.start();
        matches = conceptMatcher.listMatchesAtLeastOfType(URI.create(SUMO_EURO_DOLLAR), LogicConceptMatchType.Subsume);
        stopwatch.stop();

        log.info("Obtained ({}) At least Plugin matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertTrue(matches.size() == 24 || matches.size() == 25);
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_ABSTRACT)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_PHYSICAL_QUANTITY)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_CURRENCY_MEASURE)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_QUANTITY)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_CONSTANT_QUANTITY)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_EURO_DOLLAR)));
        // Depending on the reasoning use there should also be "http://www.w3.org/2002/07/owl#Thing" and Nothing
        if (matches.size() == 25) {
            Assert.assertTrue(matches.containsKey(URI.create(OWL_THING)));
            Assert.assertTrue(matches.containsKey(URI.create(OWL_NOTHING)));
        }
        stopwatch.reset();

        // Obtain at least fail
        stopwatch.start();
        matches = conceptMatcher.listMatchesAtLeastOfType(URI.create(SUMO_EURO_DOLLAR), LogicConceptMatchType.Fail);
        stopwatch.stop();

        log.info("Obtained ({}) At least Fail matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertTrue(matches.size() == 24 || matches.size() == 26);
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_ABSTRACT)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_PHYSICAL_QUANTITY)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_CURRENCY_MEASURE)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_QUANTITY)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_CONSTANT_QUANTITY)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_EURO_DOLLAR)));
        // Depending on the reasoning use there should also be "http://www.w3.org/2002/07/owl#Thing" and
        // owl:Nothing
        if (matches.size() == 26) {
            Assert.assertTrue(matches.containsKey(URI.create(OWL_THING)));
            Assert.assertTrue(matches.containsKey(URI.create(OWL_NOTHING)));
        }
        stopwatch.reset();
    }

    @Test
    public void testListMatchesAtMostOfType() throws Exception {
        // Obtain at least exact
        Stopwatch stopwatch = new Stopwatch().start();
        Map<URI, MatchResult> matches =
                conceptMatcher.listMatchesAtMostOfType(URI.create(SUMO_EURO_DOLLAR), LogicConceptMatchType.Exact);
        stopwatch.stop();

        log.info("Obtained ({}) At most Exact matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertTrue(matches.size() == 24 || matches.size() == 26);
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_ABSTRACT)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_PHYSICAL_QUANTITY)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_CURRENCY_MEASURE)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_QUANTITY)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_CONSTANT_QUANTITY)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_EURO_DOLLAR)));
        // Depending on the reasoning use there should also be "http://www.w3.org/2002/07/owl#Thing" and
        // owl:Nothing
        if (matches.size() == 26) {
            Assert.assertTrue(matches.containsKey(URI.create(OWL_THING)));
            Assert.assertTrue(matches.containsKey(URI.create(OWL_NOTHING)));
        }
        stopwatch.reset();

        // Obtain at most plugin
        stopwatch.start();
        matches = conceptMatcher.listMatchesAtMostOfType(URI.create(SUMO_EURO_DOLLAR), LogicConceptMatchType.Plugin);
        stopwatch.stop();

        log.info("Obtained ({}) At most Plugin matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertTrue(matches.size() == 23 || matches.size() == 25);
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_ABSTRACT)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_PHYSICAL_QUANTITY)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_CURRENCY_MEASURE)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_QUANTITY)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_CONSTANT_QUANTITY)));
        // Depending on the reasoning use there should also be "http://www.w3.org/2002/07/owl#Thing" and
        // owl:Nothing
        if (matches.size() == 25) {
            Assert.assertTrue(matches.containsKey(URI.create(OWL_THING)));
            Assert.assertTrue(matches.containsKey(URI.create(OWL_NOTHING)));
        }
        stopwatch.reset();

        // Obtain at most subsumes
        stopwatch.start();
        matches = conceptMatcher.listMatchesAtMostOfType(URI.create(SUMO_EURO_DOLLAR), LogicConceptMatchType.Subsume);
        stopwatch.stop();

        log.info("Obtained ({}) At most Subsume matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertTrue(matches.size() == 0 || matches.size() == 1);
        // Depending on the reasoning use there could be owl:Nothing
        if (matches.size() == 1) {
            Assert.assertTrue(matches.containsKey(URI.create(OWL_NOTHING)));
        }
        stopwatch.reset();

        // Obtain at least fail
        stopwatch.start();
        matches = conceptMatcher.listMatchesAtMostOfType(URI.create(SUMO_EURO_DOLLAR), LogicConceptMatchType.Fail);
        stopwatch.stop();

        log.info("Obtained ({}) At most Fail matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertTrue(matches.size() == 0);
        stopwatch.reset();
    }

    @Test
    public void testListMatchesWithinRange() throws Exception {

        // Obtain all matches
        Stopwatch stopwatch = new Stopwatch().start();
        Map<URI, MatchResult> matches =
                conceptMatcher.listMatchesWithinRange(URI.create(SUMO_EURO_DOLLAR), LogicConceptMatchType.Fail, LogicConceptMatchType.Exact);
        stopwatch.stop();

        log.info("Obtained ({}) all matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertTrue(matches.size() == 24 || matches.size() == 26);
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_ABSTRACT)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_PHYSICAL_QUANTITY)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_CURRENCY_MEASURE)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_QUANTITY)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_CONSTANT_QUANTITY)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_EURO_DOLLAR)));
        // Depending on the reasoning use there should also be "http://www.w3.org/2002/07/owl#Thing" and
        // owl:Nothing
        if (matches.size() == 26) {
            Assert.assertTrue(matches.containsKey(URI.create(OWL_THING)));
            Assert.assertTrue(matches.containsKey(URI.create(OWL_NOTHING)));
        }
        stopwatch.reset();

        // Obtain only exact
        stopwatch.start();
        matches = conceptMatcher.listMatchesWithinRange(URI.create(SUMO_EURO_DOLLAR), LogicConceptMatchType.Exact, LogicConceptMatchType.Exact);
        stopwatch.stop();

        log.info("Obtained ({}) exact matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertEquals(1, matches.size());
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_EURO_DOLLAR)));
        stopwatch.reset();

        // Obtain from Plugin up
        stopwatch.start();
        matches = conceptMatcher.listMatchesWithinRange(URI.create(SUMO_EURO_DOLLAR), LogicConceptMatchType.Plugin, LogicConceptMatchType.Exact);
        stopwatch.stop();

        log.info("Obtained ({}) matches >= Plugin - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertTrue(matches.size() == 24 || matches.size() == 25);
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_ABSTRACT)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_PHYSICAL_QUANTITY)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_CURRENCY_MEASURE)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_QUANTITY)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_CONSTANT_QUANTITY)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_EURO_DOLLAR)));
        // Depending on the reasoning use there should also be "http://www.w3.org/2002/07/owl#Thing"
        if (matches.size() == 25) {
            Assert.assertTrue(matches.containsKey(URI.create(OWL_THING)));
        }
        stopwatch.reset();

        // Obtain Plugin and subsumes
        stopwatch.start();
        matches = conceptMatcher.listMatchesWithinRange(URI.create(SUMO_EURO_DOLLAR), LogicConceptMatchType.Subsume, LogicConceptMatchType.Plugin);
        stopwatch.stop();

        log.info("Obtained ({}) Subsumes >= matches >= Plugin - {} - in {} \n", matches.size(),
                matches, stopwatch);
        Assert.assertTrue(matches.size() == 23 || matches.size() == 24);
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_ABSTRACT)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_PHYSICAL_QUANTITY)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_CURRENCY_MEASURE)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_QUANTITY)));
        Assert.assertTrue(matches.containsKey(URI.create(SUMO_CONSTANT_QUANTITY)));
        // Depending on the reasoning use there should also be "http://www.w3.org/2002/07/owl#Thing" and
        // owl:Nothing
        if (matches.size() == 24) {
            Assert.assertTrue(matches.containsKey(URI.create(OWL_THING)));
        }
        stopwatch.reset();

        // Invert limits
        stopwatch.start();
        matches = conceptMatcher.listMatchesWithinRange(URI.create(SUMO_EURO_DOLLAR), LogicConceptMatchType.Exact, LogicConceptMatchType.Plugin);
        stopwatch.stop();

        log.info("Obtained ({}) Exact >= matches >= Plugin - {} - in {} \n", matches.size(),
                matches, stopwatch);
        Assert.assertEquals(0, matches.size());
        stopwatch.reset();

    }

    /**
     * JukitoModule.
     */
    public static class InnerModule extends JukitoModule {
        @Override
        protected void configureTest() {
            // Get the Registry Management
            install(new RegistryManagementModule());

            // bind
            bind(ConceptMatcher.class).to(SparqlLogicConceptMatcher.class);

            // Necessary to verify interaction with the real object
            bindSpy(SparqlLogicConceptMatcher.class);
        }
    }
}
