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
import junit.framework.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.Transformer;
import uk.ac.open.kmi.iserve.commons.model.*;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.disco.DiscoMatchType;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;

import java.io.File;
import java.net.URI;
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
public class LogicConceptMatcherWSC08Test {

    private static final Logger log = LoggerFactory.getLogger(LogicConceptMatcherWSC08Test.class);
    private static final String WSC08_01_SERVICES = "/wsc08-dataset01/services/services.xml";
    private static final String MEDIATYPE = "text/xml";

    private LogicConceptMatcher matcher = new LogicConceptMatcher();

    @BeforeClass
    public static void setUp() throws Exception {
        //BasicConfigurator.configure();
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
    }

    @Test
    public void testDirectPluginMatch() throws Exception {

        URI origin = URI.create("http://localhost/ontology/taxonomy.owl#con1655991159");
        URI destination = URI.create("http://localhost/ontology/taxonomy.owl#con409488015");

        // Obtain matches
        Stopwatch stopwatch = new Stopwatch().start();
        MatchResult match = matcher.match(origin, destination);
        stopwatch.stop();

        log.info("Obtained match in {} \n {}", stopwatch, match);
        Assert.assertEquals(DiscoMatchType.Plugin, match.getMatchType());
    }

    @Test
    public void testDirectSubsumeMatch() throws Exception {

        URI origin = URI.create("http://localhost/ontology/taxonomy.owl#con1655991159");
        URI destination = URI.create("http://localhost/ontology/taxonomy.owl#con409488015");

        // Obtain matches
        Stopwatch stopwatch = new Stopwatch().start();
        MatchResult match = matcher.match(destination, origin);
        stopwatch.stop();

        log.info("Obtained match in {} \n {}", stopwatch, match);
        Assert.assertEquals(DiscoMatchType.Subsume, match.getMatchType());
    }

    @Test
    public void testIndirectPluginMatch() throws Exception {

        URI origin = URI.create("http://localhost/ontology/taxonomy.owl#con1901563774");
        URI destination = URI.create("http://localhost/ontology/taxonomy.owl#con241744282");

        // Obtain matches
        Stopwatch stopwatch = new Stopwatch().start();
        MatchResult match = matcher.match(origin, destination);
        stopwatch.stop();

        log.info("Obtained match in {} \n {}", stopwatch, match);
        Assert.assertEquals(DiscoMatchType.Plugin, match.getMatchType());
    }


    @Test
    @Ignore
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
        for(URI service : ManagerSingleton.getInstance().listServices()){
            // Load the service
            Service srv = ManagerSingleton.getInstance().getService(service);
            // Load operations
            opLoop:
            for(Operation op : srv.getOperations()) {
                // Get inputs
                // System.out.println("> Checking " + op.getUri());
                for(URI to : getInputs(op)){
                    // System.out.println("\tChecking input " + to);
                    for(URI from : available){
                        // Try to match
                        MatchResult result = matcher.match(from, to);
                        if (result.getMatchType().compareTo(DiscoMatchType.Plugin)>=0){
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
    public void discoverAllCandidates() throws Exception {
        // Define the available inputs
        Set<URI> availableInputs = new HashSet<URI>();
        Set<URI> newInputs = new HashSet<URI>();
        Set<Operation> allCandidates = new HashSet<Operation>();

        availableInputs.add(URI.create("http://localhost/ontology/taxonomy.owl#con1233457844"));
        availableInputs.add(URI.create("http://localhost/ontology/taxonomy.owl#con1849951292"));
        availableInputs.add(URI.create("http://localhost/ontology/taxonomy.owl#con864995873"));
        newInputs.addAll(availableInputs);

        // Preload servide models
        // TODO; Discovery operations without loading the entire service model.
        Set<Service> services = new HashSet<Service>();
        for(URI srvURI : ManagerSingleton.getInstance().listServices()){
            services.add(ManagerSingleton.getInstance().getService(srvURI));
        }

        int pass = 0;
        while(!newInputs.isEmpty()){
            Set<Operation> candidates = new HashSet<Operation>();
            Set<URI> candidateOutputs = new HashSet<URI>();
            for(Service srv : services){
                System.out.println("Checking " + srv.getUri());
                // Load operations
                operations:
                for(Operation op : srv.getOperations()) {
                    if (allCandidates.contains(op)) continue;
                    // Fast check if there is some input that matches
                    if (consumesAny(newInputs, op)){
                        if (isInvokable2(availableInputs, op)){
                            System.out.println(" >> Invokable!");
                            candidates.add(op);
                            Set<URI> outputs = getOutputs(op);
                            candidateOutputs.addAll(outputs);
                            break operations;
                        }
                    }
                }
            }
            // The new inputs:
            newInputs.clear();
            candidateOutputs.removeAll(availableInputs);
            newInputs.addAll(candidateOutputs);
            availableInputs.addAll(newInputs);
            allCandidates.addAll(candidates);

            pass++;
            System.out.println(pass + " Pass, total candidates " + candidates.size() + ". Available inputs: " + availableInputs.size() + ". New Inputs " + newInputs.size());
        }


    }

    private boolean consumesAny(Set<URI> inputs, Operation op){
        Set<URI> opInputs = getInputs(op);
        for(URI from : inputs){
            for(URI to : opInputs){
                MatchResult match = this.matcher.match(from, to);
                if (match.getMatchType().compareTo(DiscoMatchType.Plugin)>=0){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isInvokable2(Set<URI> availableInputs, Operation op){
        Set<URI> opInputs = getInputs(op);
        Set<URI> matched = new HashSet<URI>();
        for(URI from : availableInputs){
            for(URI to : opInputs){
                // skip if already matched
                if (matched.contains(to)) continue;
                MatchResult match = this.matcher.match(from, to);
                if (match.getMatchType().compareTo(DiscoMatchType.Plugin)>=0){
                    matched.add(to);
                    if (matched.size()==opInputs.size()){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isInvokable(Set<URI> availableInputs, Operation op){
        Table<URI,URI,MatchResult> result = matcher.match(availableInputs, getInputs(op));
        for(URI column : result.columnKeySet()){
            // Each column (destination) should contain a valid match.
            boolean hasValidMatch=false;
            for(MatchResult mr : result.column(column).values()){
                if (mr.getMatchType().compareTo(DiscoMatchType.Plugin)>=0){
                    hasValidMatch=true;
                    break;
                }
            }
            if (!hasValidMatch){
                return false;
            }
        }
        return true;
    }

    private Set<URI> getInputs(Operation op){
        Set<URI> models = new HashSet<URI>();
        for(MessageContent c : op.getInputs()){
            models.addAll(getModelReferences(c));
        }
        return models;
    }

    private Set<URI> getOutputs(Operation op){
        Set<URI> models = new HashSet<URI>();
        for(MessageContent c : op.getOutputs()){
            models.addAll(getModelReferences(c));
        }
        return models;
    }


    private Set<URI> getModelReferences(MessageContent msg){
        Set<URI> uris = new HashSet<URI>();
        for(MessagePart p : msg.getMandatoryParts()){
            for(Resource r : p.getModelReferences()){
                uris.add(r.getUri());
            }
        }
        return uris;
    }
}
