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
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.inject.Guice;
import com.google.inject.Injector;
import junit.framework.Assert;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.Transformer;
import uk.ac.open.kmi.iserve.commons.model.*;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.sal.manager.iServeManager;
import uk.ac.open.kmi.iserve.sal.manager.impl.iServeManagementModule;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * LogicConceptMatcherWSC08Test
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @author <a href="mailto:pablo.rodriguez.mier@usc.es">Pablo Rodriguez Mier</a> (CiTIUS, University of Santiago de Compostela)
 * @since 01/08/2013
 */
public class SparqlLogicConceptMatcherWSC08Test {

    private static final Logger log = LoggerFactory.getLogger(SparqlLogicConceptMatcherWSC08Test.class);
    private static final String WSC08_01_SERVICES = "/WSC08/wsc08_datasets/01/services.xml";
    private static final String MEDIATYPE = "text/xml";

    private static final String SPARQL_ENDPOINT = "http://localhost:8080/openrdf-sesame/repositories/Test";

    private static SparqlLogicConceptMatcher conceptMatcher;
    private static iServeManager manager;

    @BeforeClass
    public static void setUp() throws Exception {
        BasicConfigurator.configure();
        org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);
        // do your one-time setup here
        Injector injector = Guice.createInjector(new iServeManagementModule());
        manager = injector.getInstance(iServeManager.class);

        // Clean the whole thing before testing
        manager.clearRegistry();

        conceptMatcher = new SparqlLogicConceptMatcher(SPARQL_ENDPOINT);

        log.info("Importing WSC 2008 services");
        String file = SparqlLogicConceptMatcherWSC08Test.class.getResource(WSC08_01_SERVICES).getFile();
        log.debug("Using " + file);
        File services = new File(file);

        //List<Service> result = new WSCImporter().transform(new FileInputStream(services), null);
        // Automatic plugin discovery
        List<Service> result = Transformer.getInstance().transform(services, null, MEDIATYPE);
        // Import all services
        for (Service s : result) {
            URI uri = manager.getServiceManager().addService(s);
            Assert.assertNotNull(uri);
            log.info("Service added: " + uri.toASCIIString());
        }
    }

    @Test
    public void testDirectPluginMatch() throws Exception {

        URI origin = URI.create("http://localhost/ontology/taxonomy.owl#con1655991159");
        URI destination = URI.create("http://localhost/ontology/taxonomy.owl#con409488015");

        // Obtain matches
        Stopwatch stopwatch = new Stopwatch().start();
        MatchResult match = this.conceptMatcher.match(origin, destination);
        stopwatch.stop();

        log.info("Obtained match in {} \n {}", stopwatch, match);
        Assert.assertEquals(LogicConceptMatchType.Plugin, match.getMatchType());
    }

    @Test
    public void testDirectSubsumeMatch() throws Exception {

        URI origin = URI.create("http://localhost/ontology/taxonomy.owl#con1655991159");
        URI destination = URI.create("http://localhost/ontology/taxonomy.owl#con409488015");

        // Obtain matches
        Stopwatch stopwatch = new Stopwatch().start();
        MatchResult match = this.conceptMatcher.match(destination, origin);
        stopwatch.stop();

        log.info("Obtained match in {} \n {}", stopwatch, match);
        Assert.assertEquals(LogicConceptMatchType.Subsume, match.getMatchType());
    }

    @Test
    public void testIndirectPluginMatch() throws Exception {

        URI origin = URI.create("http://localhost/ontology/taxonomy.owl#con1901563774");
        URI destination = URI.create("http://localhost/ontology/taxonomy.owl#con241744282");

        // Obtain matches
        Stopwatch stopwatch = new Stopwatch().start();
        MatchResult match = this.conceptMatcher.match(origin, destination);
        stopwatch.stop();

        log.info("Obtained match in {} \n {}", stopwatch, match);
        Assert.assertEquals(LogicConceptMatchType.Plugin, match.getMatchType());
    }


    @Test
    @Ignore("Integration test (not a proper unit test), takes too long to complete")
    public void testMultipleDiscovery() throws Exception {
        // Define the available inputs
        Set<URI> available = new HashSet<URI>();

        available.add(URI.create("http://localhost/ontology/taxonomy.owl#con1233457844"));
        available.add(URI.create("http://localhost/ontology/taxonomy.owl#con1849951292"));
        available.add(URI.create("http://localhost/ontology/taxonomy.owl#con864995873"));

        String[] expected = {
                "serv213889376",
                "serv1668689219",
                "serv1323166560",
                "serv75024910",
                "serv1253734327",
                "serv1462031026",
                "serv144457143",
                "serv561050541",
                "serv1667050675",
                "serv212250832",
                "serv7231183",
                "serv1529824753",
                "serv2015850384",
                "serv837140929",
                "serv906573162",
                "serv1599256986"};

        Set<String> expectedServices = Sets.newHashSet(expected);

        // Discover executable services
        Set<String> candidates = new HashSet<String>();
        for (URI service : this.manager.getServiceManager().listServices()) {
            // Load the service
            Service srv = this.manager.getServiceManager().getService(service);
            // Load operations
            opLoop:
            for (Operation op : srv.getOperations()) {
                // Get inputs
                // System.out.println("> Checking " + op.getUri());
                for (URI to : getInputs(op)) {
                    // System.out.println("\tChecking input " + to);
                    for (URI from : available) {
                        // Try to match
                        MatchResult result = conceptMatcher.match(from, to);
                        if (result.getMatchType().compareTo(LogicConceptMatchType.Plugin) >= 0) {
                            log.info("Service operation " + op.getUri() + " matched.");
                            log.info("\t> Match " + from + "->" + to + ":" + result.getMatchType());
                            candidates.add(srv.getLabel());
                            // Check if the candidate is invokable
                            System.out.println(isInvokable(available, op));
                            assertTrue(expectedServices.contains(srv.getLabel()));
                            break opLoop;
                        }
                    }

                }
            }
        }
        System.out.println("Total services " + candidates.size());
    }

    @Test
    @Ignore("Integration test (not a proper unit test), takes too long to complete")
    public void discoverAllCandidates() throws Exception {

        String[][] expectedServices = {{"serv1529824753", "serv1253734327", "serv1462031026", "serv212250832", "serv906573162", "serv144457143", "serv1599256986", "serv75024910", "serv561050541", "serv2015850384", "serv1668689219", "serv213889376", "serv837140929", "serv1667050675", "serv7231183", "serv1323166560"},
                {"serv1043799122", "serv1736482908", "serv281683065", "serv974366889", "serv1805915141", "serv351115298", "serv769347240", "serv976005395", "serv1392598793", "serv630482774", "serv76663416", "serv2085282617"},
                {"serv420547531", "serv283321609", "serv900019062", "serv1113231355", "serv346199742", "serv1531463259", "serv699915007"},
                {"serv1807553685", "serv1114869861", "serv489979764", "serv422186075", "serv352753842", "serv1944779607", "serv1738121452", "serv1182663588", "serv1045437628", "serv1875347374"},
                {"serv559411997", "serv1876985918", "serv1252095821"},
                {"serv491618308", "serv1946418151", "serv2014211840", "serv1184302094"},
                {"serv1321528054"},
                {"serv628844230"},
                {"serv2083644073"},
                {"serv1460392520", "serv767708696", "serv5592677", "serv698276463", "serv1390960287"}};

        // Define the available inputs
        Set<URI> availableInputs = new HashSet<URI>();
        Set<URI> newInputs = new HashSet<URI>();
        Set<Operation> allRelevantOps = new HashSet<Operation>();

        availableInputs.add(URI.create("http://localhost/ontology/taxonomy.owl#con1233457844"));
        availableInputs.add(URI.create("http://localhost/ontology/taxonomy.owl#con1849951292"));
        availableInputs.add(URI.create("http://localhost/ontology/taxonomy.owl#con864995873"));
        newInputs.addAll(availableInputs);

        // Preload servide models
        // TODO; Discovery operations without loading the entire service model.
        Set<Service> services = new HashSet<Service>();
        for (URI srvURI : this.manager.getServiceManager().listServices()) {
            services.add(this.manager.getServiceManager().getService(srvURI));
        }
        int pass = 0;
        while (!newInputs.isEmpty()) {
            Stopwatch passWatch = new Stopwatch().start();
            Set<Operation> relevantOps = new HashSet<Operation>();
            Set<URI> relevantOutputs = new HashSet<URI>();
            Set<Service> relevantServices = new HashSet<Service>();
            for (Service srv : services) {
                log.debug("Checking {}", srv.getUri());
                // Load operations
                operations:
                for (Operation op : srv.getOperations()) {
                    if (allRelevantOps.contains(op)) continue;
                    // Fast check if there is some input that matches
                    if (consumesAny(newInputs, op)) {
                        if (isInvokable2(availableInputs, op)) {
                            log.debug(" >> Invokable!");
                            relevantOps.add(op);
                            Set<URI> outputs = getOutputs(op);
                            relevantOutputs.addAll(outputs);
                            relevantServices.add(srv);
                            break operations;
                        }
                    }
                }
            }
            // The new inputs:
            newInputs.clear();
            // Remove concepts that were used before
            relevantOutputs.removeAll(availableInputs);
            // New inputs that potentially lead to new discovered services
            newInputs.addAll(relevantOutputs);
            availableInputs.addAll(newInputs);
            allRelevantOps.addAll(relevantOps);
            if (!newInputs.isEmpty()) {
                assertTrue(expectedServices[pass].length == relevantOps.size() &&
                        serviceNamesToList(relevantServices).containsAll(Sets.newHashSet(expectedServices[pass])));
            }
            pass++;
            log.info("{} Pass, total candidates {}. Available inputs {}. New Inputs {}. Iteration time {}", pass, relevantOps.size(), availableInputs.size(), newInputs.size(), passWatch.toString());
            passWatch.reset();
        }


    }

    private List<String> serviceNamesToList(Set<Service> services) {
        List<String> names = new ArrayList<String>();
        for (Service s : services) {
            names.add(s.getLabel());
        }
        return names;
    }

    private boolean consumesAny(Set<URI> inputs, Operation op) {
        Set<URI> opInputs = getInputs(op);
        for (URI from : inputs) {
            for (URI to : opInputs) {
                MatchResult match = this.conceptMatcher.match(from, to);
                if (match.getMatchType().compareTo(LogicConceptMatchType.Plugin) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isInvokable2(Set<URI> availableInputs, Operation op) {
        Set<URI> opInputs = getInputs(op);
        Set<URI> matched = new HashSet<URI>();
        for (URI from : availableInputs) {
            for (URI to : opInputs) {
                // skip if already matched
                if (matched.contains(to)) continue;
                MatchResult match = this.conceptMatcher.match(from, to);
                if (match.getMatchType().compareTo(LogicConceptMatchType.Plugin) >= 0) {
                    matched.add(to);
                    if (matched.size() == opInputs.size()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isInvokable(Set<URI> availableInputs, Operation op) {
        Table<URI, URI, MatchResult> result = this.conceptMatcher.match(availableInputs, getInputs(op));
        for (URI column : result.columnKeySet()) {
            // Each column (destination) should contain a valid match.
            boolean hasValidMatch = false;
            for (MatchResult mr : result.column(column).values()) {
                if (mr.getMatchType().compareTo(LogicConceptMatchType.Plugin) >= 0) {
                    hasValidMatch = true;
                    break;
                }
            }
            if (!hasValidMatch) {
                return false;
            }
        }
        return true;
    }

    private Set<URI> getInputs(Operation op) {
        Set<URI> models = new HashSet<URI>();
        for (MessageContent c : op.getInputs()) {
            models.addAll(getModelReferences(c));
        }
        return models;
    }

    private Set<URI> getOutputs(Operation op) {
        Set<URI> models = new HashSet<URI>();
        for (MessageContent c : op.getOutputs()) {
            models.addAll(getModelReferences(c));
        }
        return models;
    }


    private Set<URI> getModelReferences(MessageContent msg) {
        Set<URI> uris = new HashSet<URI>();
        for (MessagePart p : msg.getMandatoryParts()) {
            for (Resource r : p.getModelReferences()) {
                uris.add(r.getUri());
            }
        }
        return uris;
    }
}
