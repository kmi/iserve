/*
 * Copyright (c) 2014. Knowledge Media Institute - The Open University
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

package uk.ac.open.kmi.iserve.discovery.ranking.test;

import com.google.common.base.Stopwatch;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.ConfigurationModule;
import uk.ac.open.kmi.iserve.discovery.api.*;
import uk.ac.open.kmi.iserve.discovery.api.ranking.Filter;
import uk.ac.open.kmi.iserve.discovery.api.ranking.Ranker;
import uk.ac.open.kmi.iserve.discovery.api.ranking.ScoreComposer;
import uk.ac.open.kmi.iserve.discovery.api.ranking.Scorer;
import uk.ac.open.kmi.iserve.discovery.api.ranking.impl.BasicScoreComposer;
import uk.ac.open.kmi.iserve.discovery.api.ranking.impl.StandardRanker;
import uk.ac.open.kmi.iserve.discovery.disco.impl.GenericLogicDiscoverer;
import uk.ac.open.kmi.iserve.discovery.disco.impl.SparqlLogicConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.ranking.impl.CommunityVitalityScorer;
import uk.ac.open.kmi.iserve.discovery.ranking.impl.ProviderPopularityScorer;
import uk.ac.open.kmi.iserve.discovery.util.Pair;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.manager.RegistryManager;
import uk.ac.open.kmi.iserve.sal.manager.impl.RegistryManagementModule;
import uk.ac.open.kmi.msm4j.Service;
import uk.ac.open.kmi.msm4j.io.ServiceReader;
import uk.ac.open.kmi.msm4j.io.ServiceWriter;
import uk.ac.open.kmi.msm4j.io.Syntax;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Luca Panziera on 01/05/2014.
 */
@RunWith(JukitoRunner.class)
public class PopularityBasedRankingTest {
    private static Logger logger = LoggerFactory.getLogger(PopularityBasedRankingTest.class);

    @BeforeClass
    public static void setUp() throws Exception {


        Injector injector = Guice.createInjector(new ConfigurationModule(), new RegistryManagementModule());

        RegistryManager registryManager = injector.getInstance(RegistryManager.class);
        ServiceReader serviceReader = injector.getInstance(ServiceReader.class);
        ServiceWriter serviceWriter = injector.getInstance(ServiceWriter.class);

        // descriptions creation

//        logger.info("Description building...");
//
//        Set<File> descriptions = new DescriptionBuilder(serviceWriter).createPwDescriptions();
//
//        Set<URI> services = registryManager.getServiceManager().listServices();
//
//        for (URI service : services) {
//            String pwid = service.toString().substring(service.toString().lastIndexOf("/") + 1);
//            if (descriptions.contains(new File("onto/" + pwid + ".n3"))) {
//                descriptions.remove(new File("onto/" + pwid + ".n3"));
//            }
//        }
//
//        registryManager.getServiceManager().clearServices();
//
//        // descriptions storage
//
//        logger.info("Description storage...");
//
//        storeDescriptions(new File("schema.rdf"), descriptions, registryManager, serviceReader);


    }

    private static void storeDescriptions(File schemaFile, Set<File> descriptions, RegistryManager registryManager, ServiceReader serviceReader) {
        OntModel schemaModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        schemaModel.read(schemaFile.getAbsolutePath());
        registryManager.getKnowledgeBaseManager().uploadModel(URI.create("http://schema.org/"), schemaModel, true);

        for (File desc : descriptions) {
            try {
                List<Service> services = serviceReader.parse(new FileInputStream(desc), "http://" + desc.getName(), Syntax.N3);
                for (Service service : services) {
                    logger.info("Loading {}", service.getUri());
                    registryManager.getServiceManager().addService(service);
                }
            } catch (ServiceException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    @Test
    public void discoveryTest(DiscoveryEngine discoveryEngine) {

        URI modelA = URI.create("http://schema.org/InformAction");
        URI modelB = URI.create("http://schema.org/TravelAction");

        String query = "service-class " + modelA + " intersection service-class " + modelB + " rank";

        Stopwatch stopwatch = new Stopwatch().start();
        Map<URI, Pair<Double, MatchResult>> result = discoveryEngine.discover(query);
        stopwatch.stop();

        logger.info("Discovery complete in {}", stopwatch);

        stopwatch = new Stopwatch().start();
        result = discoveryEngine.discover(query);
        stopwatch.stop();

        logger.info("Discovery complete in {}", stopwatch);
        logger.info("Result contains {} resources", result.size());

        int i = 0;
        for (URI resource : result.keySet()) {
            i++;
            logger.info("{} - {} -> {}", i, resource.toString(), result.get(resource).getLeft());
        }

    }

    /**
     * JukitoModule.
     */
    public static class InnerModule extends JukitoModule {
        @Override
        protected void configureTest() {

            install(new ConfigurationModule());
            install(new RegistryManagementModule());

            bind(ConceptMatcher.class).to(SparqlLogicConceptMatcher.class);

            bind(OperationDiscoverer.class).to(GenericLogicDiscoverer.class);
            bind(ServiceDiscoverer.class).to(GenericLogicDiscoverer.class);

            //Scorers configuration
            Multibinder<Filter> filterBinder = Multibinder.newSetBinder(binder(), Filter.class);

            //Scorers configuration
            Multibinder<Scorer> scorerBinder = Multibinder.newSetBinder(binder(), Scorer.class);
            scorerBinder.addBinding().to(CommunityVitalityScorer.class);
            scorerBinder.addBinding().to(ProviderPopularityScorer.class);

            //Score composer configuration
            bind(ScoreComposer.class).to(BasicScoreComposer.class);

            //Ranker configuration
            bind(Ranker.class).to(StandardRanker.class);

        }

    }

}
