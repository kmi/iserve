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
import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import junit.framework.Assert;
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
import uk.ac.open.kmi.msm4j.io.Syntax;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.*;

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

        registryManager.clearRegistry();

        importPWapis(registryManager, serviceReader);

    }

    private static void importPWapis(RegistryManager registryManager, ServiceReader serviceReader) {


        Set<File> descriptions = new TreeSet<File>();
        File ontoDir = new File(PopularityBasedRankingTest.class.getResource("/pw-example").getFile());
        if (ontoDir.exists() && ontoDir.isDirectory() && ontoDir.listFiles().length > 0) {
            descriptions.addAll(Arrays.asList(ontoDir.listFiles(new Notation3ExtFilter())));
        }

        OntModel schemaModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        schemaModel.read(PopularityBasedRankingTest.class.getResource("/pw-example/schema.rdf").getFile());
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

        String query = "service-class http://schema.org/Action rank";

        Stopwatch stopwatch = new Stopwatch().start();
        Map<URI, Pair<Double, MatchResult>> result = discoveryEngine.discover(query);
        stopwatch.stop();

        logger.info("Discovery complete in {}", stopwatch);
        logger.info("Result contains {} resources", result.size());
        ImmutableList<URI> services = ImmutableList.copyOf(result.keySet());
        Assert.assertTrue(services.get(0).toString().contains("google-visualization"));

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

    public static class Notation3ExtFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            return (name.endsWith(".n3"));
        }
    }

}
