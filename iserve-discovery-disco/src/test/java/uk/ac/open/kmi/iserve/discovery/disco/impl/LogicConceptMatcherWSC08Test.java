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
import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.Transformer;
import uk.ac.open.kmi.iserve.commons.model.MessageContent;
import uk.ac.open.kmi.iserve.commons.model.MessagePart;
import uk.ac.open.kmi.iserve.commons.model.Operation;
import uk.ac.open.kmi.iserve.commons.model.Service;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.disco.DiscoMatchType;
import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * LogicConceptMatcherWSC08Test
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @author <a href="mailto:pablo.rodriguez.mier@usc.es">Pablo Rodriguez Mier</a> (CiTIUS, University of Santiago de Compostela)
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

            protected void tearDown() throws Exception {
                // do your one-time tear down here!
            }
        };
        return setup;
    }

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

    public void testPluginMatchT1(){
        // Match http://localhost/ontology/taxonomy.owl#con1233457844->http://localhost/ontology/taxonomy.owl#con1653328292:Fail
        // con1233457844 is a subclass of con1653328292
        URI origin = URI.create("http://localhost/ontology/taxonomy.owl#con1233457844");
        URI destination = URI.create("http://localhost/ontology/taxonomy.owl#con1653328292");

        // Obtain matches
        Stopwatch stopwatch = new Stopwatch().start();
        MatchResult match = matcher.match(origin, destination);
        stopwatch.stop();

        log.info("Obtained match in {} \n {}", stopwatch, match);
        Assert.assertEquals(DiscoMatchType.Plugin, match.getMatchType());
    }


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
            for(Operation op : srv.getOperations()) {
                // Get inputs
                // System.out.println("> Checking " + op.getUri());
                for(MessageContent input : op.getInputs()){
                    for(MessagePart part : input.getMandatoryParts()){
                        URI to = part.getModelReferences().iterator().next().getUri();
                        // System.out.println("\tChecking input " + to);
                        for(URI from : available){
                            // Try to match
                            MatchResult result = matcher.match(from, to);
                            if (result.getMatchType().compareTo(DiscoMatchType.Plugin)>=0){
                                log.info("Service operation " + op.getUri() + " matched.");
                                log.info("\t> Match " + from + "->" + to + ":" + result.getMatchType());
                                assertTrue(expectedServices.contains(srv.getLabel()));
                            }
                        }
                    }
                }
            }
        }

    }
}
