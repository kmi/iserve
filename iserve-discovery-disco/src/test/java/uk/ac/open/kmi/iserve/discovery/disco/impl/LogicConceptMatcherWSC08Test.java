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
}
