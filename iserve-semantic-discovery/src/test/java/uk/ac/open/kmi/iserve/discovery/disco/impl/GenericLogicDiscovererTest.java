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
import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import junit.framework.Assert;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.OperationDiscoverer;
import uk.ac.open.kmi.iserve.discovery.api.ServiceDiscoverer;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.manager.RegistryManager;
import uk.ac.open.kmi.iserve.sal.manager.impl.RegistryManagementModule;
import uk.ac.open.kmi.msm4j.Service;
import uk.ac.open.kmi.msm4j.io.ServiceReader;
import uk.ac.open.kmi.msm4j.io.Syntax;
import uk.ac.open.kmi.msm4j.io.TransformationException;
import uk.ac.open.kmi.msm4j.io.impl.ServiceTransformationEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * GenericLogicDiscovererTest
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 04/10/2013
 */
@RunWith(JukitoRunner.class)
public class GenericLogicDiscovererTest {

    private static final Logger log = LoggerFactory.getLogger(GenericLogicDiscovererTest.class);

    private static final String MEDIATYPE = "text/xml";
    private static final String WSC08_01 = "/services/wsc08/01/";
    private static final String WSC08_01_SERVICES = WSC08_01 + "services.xml";
    private static final String WSC08_01_TAXONOMY_FILE = WSC08_01 + "taxonomy.owl";
    private static final String WSC_01_TAXONOMY_URL = "http://localhost/wsc/01/taxonomy.owl";
    private static final String WSC_01_TAXONOMY_NS = "http://localhost/wsc/01/taxonomy.owl#";

    /**
     * JukitoModule.
     */
    public static class InnerModule extends JukitoModule {
        @Override
        protected void configureTest() {
            // Add dependency
            install(new RegistryManagementModule());

            // bind
            bind(ConceptMatcher.class).to(SparqlLogicConceptMatcher.class);
//            bind(ConceptMatcher.class).to(SparqlIndexedLogicConceptMatcher.class);

            // bind
//            bind(GenericLogicDiscoverer.class).in(Singleton.class);
            bind(OperationDiscoverer.class).to(GenericLogicDiscoverer.class);
            bind(ServiceDiscoverer.class).to(GenericLogicDiscoverer.class);

            // Necessary to verify interaction with the real object
            bindSpy(GenericLogicDiscoverer.class);
        }
    }

    @BeforeClass
    public static void setUp() throws Exception {
        Injector injector = Guice.createInjector(new RegistryManagementModule());
        RegistryManager registryManager = injector.getInstance(RegistryManager.class);
        ServiceTransformationEngine transformationEngine = injector.getInstance(ServiceTransformationEngine
                .class);

        ServiceReader serviceReader = injector.getInstance(ServiceReader.class);

        registryManager.clearRegistry();

        uploadWscTaxonomy(registryManager);
        importWscServices(transformationEngine, registryManager);
        importPWapis(registryManager, serviceReader);
    }

    private static void importPWapis(RegistryManager registryManager, ServiceReader serviceReader) {


        Set<File> descriptions = new TreeSet<File>();
        File ontoDir = new File(GenericLogicDiscovererTest.class.getResource("/pw-example").getFile());
        if (ontoDir.exists() && ontoDir.isDirectory() && ontoDir.listFiles().length > 0) {
            descriptions.addAll(Arrays.asList(ontoDir.listFiles(new Notation3ExtFilter())));
        }

        for (File desc : descriptions) {
            try {
                List<Service> services = serviceReader.parse(new FileInputStream(desc), "http://" + desc.getName(), Syntax.N3);
                for (Service service : services) {
                    log.info("Loading {}", service.getUri());
                    registryManager.getServiceManager().addService(service);
                }
            } catch (ServiceException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    private static void uploadWscTaxonomy(RegistryManager registryManager) throws URISyntaxException {
        // First load the ontology in the server to avoid issues
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        // Fetch the model
        String taxonomyFile = GenericLogicDiscovererTest.class.getResource(WSC08_01_TAXONOMY_FILE).toURI().toASCIIString();
        model.read(taxonomyFile);

        // Upload the model first (it won't be automatically fetched as the URIs won't resolve so we do it manually)
        registryManager.getKnowledgeBaseManager().uploadModel(URI.create(WSC_01_TAXONOMY_URL), model, true);
    }

    private static void importWscServices(ServiceTransformationEngine transformationEngine, RegistryManager registryManager) throws TransformationException, SalException, URISyntaxException, FileNotFoundException {
        log.info("Importing WSC Dataset");
        String file = GenericLogicDiscovererTest.class.getResource(WSC08_01_SERVICES).getFile();
        log.info("Services XML file {}", file);
        File services = new File(file);
        URL base = GenericLogicDiscovererTest.class.getResource(WSC08_01);
        log.info("Dataset Base URI {}", base.toURI().toASCIIString());

        List<Service> result = transformationEngine.transform(services, base.toURI().toASCIIString(), MEDIATYPE);
        //List<Service> result = Transformer.getInstance().transform(services, null, MEDIATYPE);
        if (result.size() == 0) {
            Assert.fail("No services transformed!");
        }
        // Import all services
        int counter = 0;
        for (Service s : result) {
            URI uri = registryManager.getServiceManager().addService(s);
            Assert.assertNotNull(uri);
            log.info("Service added: " + uri.toASCIIString());
            counter++;
        }
        log.debug("Total services added {}", counter);
    }

    @Test
    public void testFindOperationsConsumingAll(OperationDiscoverer opDiscoverer) throws Exception {

        URI input1Uri = URI.create(WSC_01_TAXONOMY_NS + "con332477359");
        URI input2Uri = URI.create(WSC_01_TAXONOMY_NS + "con1794855625");

        // Obtain matches
        Stopwatch stopwatch = new Stopwatch().start();
        Map<URI, MatchResult> matches = opDiscoverer.findOperationsConsumingAll(ImmutableSet.of(input1Uri, input2Uri));
        stopwatch.stop();

        log.info("Obtained ({}) matches in {} \n {}", matches.size(), stopwatch, matches);

    }


//    <http://localhost/wsc/01/taxonomy.owl#con332477359>
//
//    Super
//
//    <http://localhost/wsc/01/taxonomy.owl#con1226699739>
//    <http://localhost/wsc/01/taxonomy.owl#con1233457844>
//    <http://localhost/wsc/01/taxonomy.owl#con1653328292>
//    <http://localhost/wsc/01/taxonomy.owl#con1988815758>
//
//    Input exact
//    <http://localhost:9090/iserve/id/services/db05b706-5457-448b-9d3b-9161dbe9518e/serv904934656/Operation>
//    <http://localhost:9090/iserve/id/services/db05b706-5457-448b-9d3b-9161dbe9518e/serv904934656>
//
//    Further Inputs
//    <http://localhost/wsc/01/taxonomy.owl#con1794855625>
//
//    Outputs
//    <http://localhost/wsc/01/taxonomy.owl#con512919114>
//    <http://localhost/wsc/01/taxonomy.owl#con633555781>

    @Test
    public void testFindOperationsConsumingSome(OperationDiscoverer opDiscoverer) throws Exception {

        URI inputUri = URI.create(WSC_01_TAXONOMY_NS + "con332477359");

        // Obtain matches
        Stopwatch stopwatch = new Stopwatch().start();
        Map<URI, MatchResult> matches = opDiscoverer.findOperationsConsumingSome(ImmutableSet.of(inputUri));
        stopwatch.stop();

        log.info("Obtained ({}) matches in {} \n {}", matches.size(), stopwatch, matches);
//        Assert.assertEquals(LogicConceptMatchType.Plugin, match.getMatchType());

    }

    @Test
    @Ignore
    public void testFindOperationsProducingAll() throws Exception {

    }

    @Test
    @Ignore
    public void testFindOperationsProducingSome() throws Exception {

    }

    @Test
    @Ignore
    public void testFindOperationsClassifiedByAll() throws Exception {

    }

    @Test
    @Ignore
    public void testFindOperationsClassifiedBySome() throws Exception {

    }

    @Test
    @Ignore
    public void testFindOperationsInvocableWith() throws Exception {

    }

    @Test
    @Ignore
    public void testFindServicesConsumingAll() throws Exception {

    }

    @Test
    @Ignore
    public void testFindServicesConsumingSome() throws Exception {

    }

    @Test
    @Ignore
    public void testFindServicesProducingAll() throws Exception {

    }

    @Test
    @Ignore
    public void testFindServicesProducingSome() throws Exception {

    }

    @Test
    @Ignore
    public void testFindServicesClassifiedByAll(ServiceDiscoverer serviceDiscoverer) throws Exception {
        URI modelA = URI.create("http://schema.org/CreateAction");
        URI modelB = URI.create("http://schema.org/SearchAction");

        Stopwatch stopwatch = new Stopwatch().start();
        Map<URI, MatchResult> matches = serviceDiscoverer.findServicesClassifiedByAll(ImmutableSet.of(modelA, modelB));
        stopwatch.stop();

        log.info("Obtained ({}) matches in {} \n {}", matches.size(), stopwatch, matches);
        Assert.assertTrue(matches.size() == 1);
    }

    @Test
    @Ignore
    public void testFindServicesClassifiedBySome(ServiceDiscoverer serviceDiscoverer) throws Exception {
        URI modelA = URI.create("http://schema.org/CreateAction");
        URI modelB = URI.create("http://schema.org/SearchAction");

        Stopwatch stopwatch = new Stopwatch().start();
        Map<URI, MatchResult> matches = serviceDiscoverer.findServicesClassifiedBySome(ImmutableSet.of(modelA, modelB));
        stopwatch.stop();

        log.info("Obtained ({}) matches in {} \n {}", matches.size(), stopwatch, matches);
        Assert.assertTrue(matches.size() == 13);
    }

    @Test
    @Ignore
    public void testFindServicesInvocableWith() throws Exception {

    }

    public static class Notation3ExtFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            return (name.endsWith(".n3"));
        }
    }
}
