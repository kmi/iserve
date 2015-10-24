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

    private static final String SUMO_OWL_STR = "http://127.0.0.1:8000/ontology/SUMO.owl";

    private static final URI OWL_THING = URI.create("http://www.w3.org/2002/07/owl#Thing");
    private static final URI OWL_NOTHING = URI.create("http://www.w3.org/2002/07/owl#Nothing");
    private static final URI SUMO_OWL = URI.create("http://127.0.0.1:8000/ontology/SUMO.owl");
    private static final URI SUMO_ABSTRACT = URI.create(SUMO_OWL_STR + "#Abstract");
    private static final URI SUMO_PHYSICAL_QUANTITY = URI.create(SUMO_OWL_STR + "#PhysicalQuantity");
    private static final URI SUMO_CURRENCY_MEASURE = URI.create(SUMO_OWL_STR + "#CurrencyMeasure");
    private static final URI SUMO_QUANTITY = URI.create(SUMO_OWL_STR + "#Quantity");
    private static final URI SUMO_CONSTANT_QUANTITY = URI.create(SUMO_OWL_STR + "#ConstantQuantity");
    private static final URI SUMO_EURO_DOLLAR = URI.create(SUMO_OWL_STR + "#EuroDollar");
    private static final URI SUMO_THERAPEUTIC_PROCESS = URI.create(SUMO_OWL_STR + "#TherapeuticProcess");
    private static final URI SUMO_INTENTIONAL_PROCESS = URI.create(SUMO_OWL_STR + "#IntentionalProcess");
    private static final URI SUMO_ENTITY = URI.create(SUMO_OWL_STR + "#Entity");
    private static final URI SUMO_CM = URI.create(SUMO_OWL_STR + "#Centimeter");

    @Inject
    private ConceptMatcher conceptMatcher;

    /**
     * JukitoModule.
     */
    public static class InnerModule extends JukitoModule {
        @Override
        protected void configureTest() {
            // Get the Registry Management
            install(new RegistryManagementModule());

//            // bind
//            bind(ConceptMatcher.class).to(SparqlLogicConceptMatcher.class);
//
//            // Necessary to verify interaction with the real object
//            bindSpy(SparqlLogicConceptMatcher.class);

            // bind
            bind(ConceptMatcher.class).to(SparqlIndexedLogicConceptMatcher.class);

            // Necessary to verify interaction with the real object
            bindSpy(SparqlIndexedLogicConceptMatcher.class);
        }
    }

    @BeforeClass
    public static void setupTests() throws Exception {
        Injector injector = Guice.createInjector(new RegistryManagementModule());
        RegistryManager registryManager = injector.getInstance(RegistryManager.class);
        registryManager.clearRegistry();

        // Upload SUMO
        Model model = ModelFactory.createDefaultModel();
        model.read(SUMO_OWL_STR);
        registryManager.getKnowledgeBaseManager().uploadModel(SUMO_OWL, model, true);
    }

    @Test
    public void testMatch() throws Exception {

        // Obtain matches
        // Check Plugin
        Stopwatch stopwatch = new Stopwatch().start();
        MatchResult match = conceptMatcher.match(SUMO_EURO_DOLLAR, SUMO_QUANTITY);
        stopwatch.stop();

        log.info("Obtained match in {} \n {}", stopwatch, match);
        Assert.assertEquals(LogicConceptMatchType.Plugin, match.getMatchType());

        // Check Subsumes
        stopwatch = new Stopwatch().start();
        match = conceptMatcher.match(SUMO_QUANTITY, SUMO_EURO_DOLLAR);
        stopwatch.stop();

        log.info("Obtained match in {} \n {}", stopwatch, match);
        Assert.assertEquals(LogicConceptMatchType.Subsume, match.getMatchType());

        // Check Exact
        stopwatch = new Stopwatch().start();
        match = conceptMatcher.match(SUMO_EURO_DOLLAR, SUMO_EURO_DOLLAR);
        stopwatch.stop();

        log.info("Obtained match in {} \n {}", stopwatch, match);
        Assert.assertEquals(LogicConceptMatchType.Exact, match.getMatchType());

        // Check Fail
        stopwatch = new Stopwatch().start();
        match = conceptMatcher.match(SUMO_EURO_DOLLAR, SUMO_CM);
        stopwatch.stop();

        log.info("Obtained match in {} \n {}", stopwatch, match);
        Assert.assertEquals(LogicConceptMatchType.Fail, match.getMatchType());

        stopwatch = new Stopwatch().start();
        match = conceptMatcher.match(SUMO_CM, SUMO_EURO_DOLLAR);
        stopwatch.stop();

        log.info("Obtained match in {} \n {}", stopwatch, match);
        Assert.assertEquals(LogicConceptMatchType.Fail, match.getMatchType());

    }

    @Test
    public void testMatchBySets() throws Exception {

        Set<URI> origins = new HashSet<URI>();
        origins.add(SUMO_EURO_DOLLAR);
        origins.add(SUMO_ABSTRACT);
        origins.add(SUMO_THERAPEUTIC_PROCESS);

        Set<URI> destinations = new HashSet<URI>();
        destinations.add(SUMO_QUANTITY);
        destinations.add(SUMO_INTENTIONAL_PROCESS);
        destinations.add(SUMO_ENTITY);

        // Obtain cross-matches
        Stopwatch stopwatch = new Stopwatch().start();
        Table<URI, URI, MatchResult> matches =
                conceptMatcher.match(origins, destinations);
        stopwatch.stop();

        log.info("Obtained all cross matches ({}) in {}", matches.size(), stopwatch);
        log.info("List of matches: {}", matches.toString());
        Assert.assertEquals(9, matches.size());
        Assert.assertEquals(LogicConceptMatchType.Plugin, matches.get(SUMO_EURO_DOLLAR, SUMO_QUANTITY).getMatchType());
        Assert.assertEquals(LogicConceptMatchType.Fail, matches.get(SUMO_EURO_DOLLAR, SUMO_INTENTIONAL_PROCESS).getMatchType());
        Assert.assertEquals(LogicConceptMatchType.Fail, matches.get(SUMO_EURO_DOLLAR, SUMO_ENTITY).getMatchType());

        Assert.assertEquals(LogicConceptMatchType.Subsume, matches.get(SUMO_ABSTRACT, SUMO_QUANTITY).getMatchType());
        Assert.assertEquals(LogicConceptMatchType.Fail, matches.get(SUMO_ABSTRACT, SUMO_INTENTIONAL_PROCESS).getMatchType());
        Assert.assertEquals(LogicConceptMatchType.Fail, matches.get(SUMO_ABSTRACT, SUMO_ENTITY).getMatchType());

        Assert.assertEquals(LogicConceptMatchType.Fail, matches.get(SUMO_THERAPEUTIC_PROCESS, SUMO_QUANTITY).getMatchType());
        Assert.assertEquals(LogicConceptMatchType.Plugin, matches.get(SUMO_THERAPEUTIC_PROCESS, SUMO_INTENTIONAL_PROCESS).getMatchType());
        Assert.assertEquals(LogicConceptMatchType.Plugin, matches.get(SUMO_THERAPEUTIC_PROCESS, SUMO_ENTITY).getMatchType());
        stopwatch.reset();

    }

    @Test
    public void testListMatchesOfType() throws Exception {
        // Obtain only exact
        Stopwatch stopwatch = new Stopwatch().start();
        Map<URI, MatchResult> matches =
                conceptMatcher.listMatchesOfType(SUMO_EURO_DOLLAR, LogicConceptMatchType.Exact);
        stopwatch.stop();

        log.info("Obtained ({}) exact matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertTrue(matches.size() == 1);
        Assert.assertTrue(matches.containsKey(SUMO_EURO_DOLLAR));
        stopwatch.reset();

        // Obtain only plugin for Euro
        stopwatch.start();
        matches = conceptMatcher.listMatchesOfType(SUMO_EURO_DOLLAR, LogicConceptMatchType.Plugin);
        stopwatch.stop();

        log.info("Obtained ({}) plugin matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertTrue(matches.size() == 5 || matches.size() == 6);
        Assert.assertTrue(matches.containsKey(SUMO_ABSTRACT));
        Assert.assertTrue(matches.containsKey(SUMO_PHYSICAL_QUANTITY));
        Assert.assertTrue(matches.containsKey(SUMO_CURRENCY_MEASURE));
        Assert.assertTrue(matches.containsKey(SUMO_QUANTITY));
        Assert.assertTrue(matches.containsKey(SUMO_CONSTANT_QUANTITY));
        // Depending on the reasoning use there should also be "http://www.w3.org/2002/07/owl#Thing"
        if (matches.size() == 6) {
            Assert.assertTrue(matches.containsKey(OWL_THING));
        }

        stopwatch.reset();

        // Obtain only plugin
        stopwatch.start();
        matches = conceptMatcher.listMatchesOfType(SUMO_EURO_DOLLAR, LogicConceptMatchType.Subsume);
        stopwatch.stop();

        log.info("Obtained ({}) plugin matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertTrue(matches.size() <= 1);
        // Depending on the reasoning there should be "http://www.w3.org/2002/07/owl#Nothing"
        if (matches.size() == 1) {
            Assert.assertTrue(matches.containsKey(OWL_NOTHING));
        }
        stopwatch.reset();
    }

    @Test
    public void testListMatchesAtLeastOfType() throws Exception {
        // Obtain at least exact
        Stopwatch stopwatch = new Stopwatch().start();
        Map<URI, MatchResult> matches =
                conceptMatcher.listMatchesAtLeastOfType(SUMO_EURO_DOLLAR, LogicConceptMatchType.Exact);
        stopwatch.stop();

        log.info("Obtained ({}) At least Exact matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertTrue(matches.size() == 1);
        Assert.assertTrue(matches.containsKey(SUMO_EURO_DOLLAR));

        stopwatch.reset();

        // Obtain at least plugin
        stopwatch.start();
        matches = conceptMatcher.listMatchesAtLeastOfType(SUMO_EURO_DOLLAR, LogicConceptMatchType.Plugin);
        stopwatch.stop();

        log.info("Obtained ({}) At least Exact matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertTrue(matches.size() == 6 || matches.size() == 7);
        Assert.assertTrue(matches.containsKey(SUMO_ABSTRACT));
        Assert.assertTrue(matches.containsKey(SUMO_PHYSICAL_QUANTITY));
        Assert.assertTrue(matches.containsKey(SUMO_CURRENCY_MEASURE));
        Assert.assertTrue(matches.containsKey(SUMO_QUANTITY));
        Assert.assertTrue(matches.containsKey(SUMO_CONSTANT_QUANTITY));
        Assert.assertTrue(matches.containsKey(SUMO_EURO_DOLLAR));
        // Depending on the reasoning use there should also be "http://www.w3.org/2002/07/owl#Thing"
        if (matches.size() == 7) {
            Assert.assertTrue(matches.containsKey(OWL_THING));
        }

        stopwatch.reset();

        // Obtain at least subsumes
        stopwatch.start();
        matches = conceptMatcher.listMatchesAtLeastOfType(SUMO_EURO_DOLLAR, LogicConceptMatchType.Subsume);
        stopwatch.stop();

        log.info("Obtained ({}) At least Plugin matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertTrue(matches.size() == 6 || matches.size() == 8);
        Assert.assertTrue(matches.containsKey(SUMO_ABSTRACT));
        Assert.assertTrue(matches.containsKey(SUMO_PHYSICAL_QUANTITY));
        Assert.assertTrue(matches.containsKey(SUMO_CURRENCY_MEASURE));
        Assert.assertTrue(matches.containsKey(SUMO_QUANTITY));
        Assert.assertTrue(matches.containsKey(SUMO_CONSTANT_QUANTITY));
        Assert.assertTrue(matches.containsKey(SUMO_EURO_DOLLAR));
        // Depending on the reasoning use there should also be "http://www.w3.org/2002/07/owl#Thing" and Nothing
        if (matches.size() == 7) {
            Assert.assertTrue(matches.containsKey(OWL_THING));
            Assert.assertTrue(matches.containsKey(OWL_NOTHING));
        }
        stopwatch.reset();

        // Obtain at least fail
        stopwatch.start();
        matches = conceptMatcher.listMatchesAtLeastOfType(SUMO_EURO_DOLLAR, LogicConceptMatchType.Fail);
        stopwatch.stop();

        log.info("Obtained ({}) At least Fail matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertTrue(matches.size() == 6 || matches.size() == 8);
        Assert.assertTrue(matches.containsKey(SUMO_ABSTRACT));
        Assert.assertTrue(matches.containsKey(SUMO_PHYSICAL_QUANTITY));
        Assert.assertTrue(matches.containsKey(SUMO_CURRENCY_MEASURE));
        Assert.assertTrue(matches.containsKey(SUMO_QUANTITY));
        Assert.assertTrue(matches.containsKey(SUMO_CONSTANT_QUANTITY));
        Assert.assertTrue(matches.containsKey(SUMO_EURO_DOLLAR));
        // Depending on the reasoning use there should also be "http://www.w3.org/2002/07/owl#Thing" and
        // owl:Nothing
        if (matches.size() == 8) {
            Assert.assertTrue(matches.containsKey(OWL_THING));
            Assert.assertTrue(matches.containsKey(OWL_NOTHING));
        }
        stopwatch.reset();
    }

    @Test
    public void testListMatchesAtMostOfType() throws Exception {
        // Obtain at least exact
        Stopwatch stopwatch = new Stopwatch().start();
        Map<URI, MatchResult> matches =
                conceptMatcher.listMatchesAtMostOfType(SUMO_EURO_DOLLAR, LogicConceptMatchType.Exact);
        stopwatch.stop();

        log.info("Obtained ({}) At most Exact matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertTrue(matches.size() == 6 || matches.size() == 8);
        Assert.assertTrue(matches.containsKey(SUMO_ABSTRACT));
        Assert.assertTrue(matches.containsKey(SUMO_PHYSICAL_QUANTITY));
        Assert.assertTrue(matches.containsKey(SUMO_CURRENCY_MEASURE));
        Assert.assertTrue(matches.containsKey(SUMO_QUANTITY));
        Assert.assertTrue(matches.containsKey(SUMO_CONSTANT_QUANTITY));
        Assert.assertTrue(matches.containsKey(SUMO_EURO_DOLLAR));
        // Depending on the reasoning use there should also be "http://www.w3.org/2002/07/owl#Thing" and
        // owl:Nothing
        if (matches.size() == 8) {
            Assert.assertTrue(matches.containsKey(OWL_THING));
            Assert.assertTrue(matches.containsKey(OWL_NOTHING));
        }
        stopwatch.reset();

        // Obtain at most plugin
        stopwatch.start();
        matches = conceptMatcher.listMatchesAtMostOfType(SUMO_EURO_DOLLAR, LogicConceptMatchType.Plugin);
        stopwatch.stop();

        log.info("Obtained ({}) At most Plugin matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertTrue(matches.size() == 5 || matches.size() == 7);
        Assert.assertTrue(matches.containsKey(SUMO_ABSTRACT));
        Assert.assertTrue(matches.containsKey(SUMO_PHYSICAL_QUANTITY));
        Assert.assertTrue(matches.containsKey(SUMO_CURRENCY_MEASURE));
        Assert.assertTrue(matches.containsKey(SUMO_QUANTITY));
        Assert.assertTrue(matches.containsKey(SUMO_CONSTANT_QUANTITY));
        // Depending on the reasoning use there should also be "http://www.w3.org/2002/07/owl#Thing" and
        // owl:Nothing
        if (matches.size() == 7) {
            Assert.assertTrue(matches.containsKey(OWL_THING));
            Assert.assertTrue(matches.containsKey(OWL_NOTHING));
        }
        stopwatch.reset();

        // Obtain at most subsumes
        stopwatch.start();
        matches = conceptMatcher.listMatchesAtMostOfType(SUMO_EURO_DOLLAR, LogicConceptMatchType.Subsume);
        stopwatch.stop();

        log.info("Obtained ({}) At most Subsume matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertTrue(matches.size() == 0 || matches.size() == 1);
        // Depending on the reasoning use there could be owl:Nothing
        if (matches.size() == 1) {
            Assert.assertTrue(matches.containsKey(OWL_NOTHING));
        }
        stopwatch.reset();

        // Obtain at least fail
        stopwatch.start();
        matches = conceptMatcher.listMatchesAtMostOfType(SUMO_EURO_DOLLAR, LogicConceptMatchType.Fail);
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
                conceptMatcher.listMatchesWithinRange(SUMO_EURO_DOLLAR, LogicConceptMatchType.Fail, LogicConceptMatchType.Exact);
        stopwatch.stop();

        log.info("Obtained ({}) all matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertTrue(matches.size() == 6 || matches.size() == 8);
        Assert.assertTrue(matches.containsKey(SUMO_ABSTRACT));
        Assert.assertTrue(matches.containsKey(SUMO_PHYSICAL_QUANTITY));
        Assert.assertTrue(matches.containsKey(SUMO_CURRENCY_MEASURE));
        Assert.assertTrue(matches.containsKey(SUMO_QUANTITY));
        Assert.assertTrue(matches.containsKey(SUMO_CONSTANT_QUANTITY));
        Assert.assertTrue(matches.containsKey(SUMO_EURO_DOLLAR));
        // Depending on the reasoning use there should also be "http://www.w3.org/2002/07/owl#Thing" and
        // owl:Nothing
        if (matches.size() == 8) {
            Assert.assertTrue(matches.containsKey(OWL_THING));
            Assert.assertTrue(matches.containsKey(OWL_NOTHING));
        }
        stopwatch.reset();

        // Obtain only exact
        stopwatch.start();
        matches = conceptMatcher.listMatchesWithinRange(SUMO_EURO_DOLLAR, LogicConceptMatchType.Exact, LogicConceptMatchType.Exact);
        stopwatch.stop();

        log.info("Obtained ({}) exact matches - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertEquals(1, matches.size());
        Assert.assertTrue(matches.containsKey(SUMO_EURO_DOLLAR));
        stopwatch.reset();

        // Obtain from Plugin up
        stopwatch.start();
        matches = conceptMatcher.listMatchesWithinRange(SUMO_EURO_DOLLAR, LogicConceptMatchType.Plugin, LogicConceptMatchType.Exact);
        stopwatch.stop();

        log.info("Obtained ({}) matches >= Plugin - {} - in {} \n", matches.size(), matches, stopwatch);
        Assert.assertTrue(matches.size() == 6 || matches.size() == 7);
        Assert.assertTrue(matches.containsKey(SUMO_ABSTRACT));
        Assert.assertTrue(matches.containsKey(SUMO_PHYSICAL_QUANTITY));
        Assert.assertTrue(matches.containsKey(SUMO_CURRENCY_MEASURE));
        Assert.assertTrue(matches.containsKey(SUMO_QUANTITY));
        Assert.assertTrue(matches.containsKey(SUMO_CONSTANT_QUANTITY));
        Assert.assertTrue(matches.containsKey(SUMO_EURO_DOLLAR));
        // Depending on the reasoning use there should also be "http://www.w3.org/2002/07/owl#Thing"
        if (matches.size() == 7) {
            Assert.assertTrue(matches.containsKey(OWL_THING));
        }
        stopwatch.reset();

        // Obtain Plugin and subsumes
        stopwatch.start();
        matches = conceptMatcher.listMatchesWithinRange(SUMO_EURO_DOLLAR, LogicConceptMatchType.Subsume, LogicConceptMatchType.Plugin);
        stopwatch.stop();

        log.info("Obtained ({}) Subsumes >= matches >= Plugin - {} - in {} \n", matches.size(),
                matches, stopwatch);
        Assert.assertTrue(matches.size() == 5 || matches.size() == 6);
        Assert.assertTrue(matches.containsKey(SUMO_ABSTRACT));
        Assert.assertTrue(matches.containsKey(SUMO_PHYSICAL_QUANTITY));
        Assert.assertTrue(matches.containsKey(SUMO_CURRENCY_MEASURE));
        Assert.assertTrue(matches.containsKey(SUMO_QUANTITY));
        Assert.assertTrue(matches.containsKey(SUMO_CONSTANT_QUANTITY));
        // Depending on the reasoning use there should also be "http://www.w3.org/2002/07/owl#Thing" and
        // owl:Nothing
        if (matches.size() == 6) {
            Assert.assertTrue(matches.containsKey(OWL_THING));
        }
        stopwatch.reset();

        // Invert limits
        stopwatch.start();
        matches = conceptMatcher.listMatchesWithinRange(SUMO_EURO_DOLLAR, LogicConceptMatchType.Exact, LogicConceptMatchType.Plugin);
        stopwatch.stop();

        log.info("Obtained ({}) Exact >= matches >= Plugin - {} - in {} \n", matches.size(),
                matches, stopwatch);
        Assert.assertEquals(0, matches.size());
        stopwatch.reset();

    }

}
