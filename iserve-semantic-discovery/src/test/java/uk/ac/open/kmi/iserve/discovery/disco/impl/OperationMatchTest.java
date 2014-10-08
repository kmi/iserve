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

import com.google.common.collect.Table;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
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
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.manager.RegistryManager;
import uk.ac.open.kmi.iserve.sal.manager.impl.RegistryManagementModule;
import uk.ac.open.kmi.msm4j.*;
import uk.ac.open.kmi.msm4j.io.TransformationException;
import uk.ac.open.kmi.msm4j.io.impl.ServiceTransformationEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test for Operation Matcher based on Jukito to deal with Guice
 *
 * @author Pablo Rodríguez Mier
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 */
@RunWith(JukitoRunner.class)
public class OperationMatchTest {

    private static final Logger log = LoggerFactory.getLogger(OperationMatchTest.class);

    private static final String MEDIATYPE = "text/xml";

    private static final String WSC08_01 = "/services/wsc08/01/";
    private static final String WSC08_01_SERVICES = WSC08_01 + "services.xml";
    private static final String WSC08_01_TAXONOMY_FILE = WSC08_01 + "taxonomy.owl";
    private static final String WSC_01_TAXONOMY_URL = "http://localhost/wsc/01/taxonomy.owl";
    private static final String WSC_01_TAXONOMY_NS = "http://localhost/wsc/01/taxonomy.owl#";

    @Inject
    private ConceptMatcher conceptMatcher;

    /**
     * JukitoModule.
     */
    public static class InnerModule extends JukitoModule {
        @Override
        protected void configureTest() {
            // Get configuration
            install(new ConfigurationModule());

            // Add dependency
            install(new RegistryManagementModule());

            // bind
            bind(ConceptMatcher.class).to(SparqlLogicConceptMatcher.class);

            // Necessary to verify interaction with the real object
            bindSpy(SparqlLogicConceptMatcher.class);
        }
    }

    @BeforeClass
    public static void setUp() throws Exception {
        Injector injector = Guice.createInjector(new ConfigurationModule(), new RegistryManagementModule());
        RegistryManager registryManager = injector.getInstance(RegistryManager.class);
        ServiceTransformationEngine transformationEngine = injector.getInstance(ServiceTransformationEngine
                .class);

        registryManager.clearRegistry();

        uploadWscTaxonomy(registryManager);
        importWscServices(transformationEngine, registryManager);
    }

    private static void uploadWscTaxonomy(RegistryManager registryManager) throws URISyntaxException {
        // First load the ontology in the server to avoid issues
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        // Fetch the model
        String taxonomyFile = OperationMatchTest.class.getResource(WSC08_01_TAXONOMY_FILE).toURI().toASCIIString();
        model.read(taxonomyFile);

        // Upload the model first (it won't be automatically fetched as the URIs won't resolve so we do it manually)
        registryManager.getKnowledgeBaseManager().uploadModel(URI.create(WSC_01_TAXONOMY_URL), model, true);
    }

    private static void importWscServices(ServiceTransformationEngine transformationEngine, RegistryManager registryManager) throws TransformationException, SalException, URISyntaxException, FileNotFoundException {
        log.info("Importing WSC Dataset");
        String file = OperationMatchTest.class.getResource(WSC08_01_SERVICES).getFile();
        log.info("Services XML file {}", file);
        File services = new File(file);
        URL base = OperationMatchTest.class.getResource(WSC08_01);
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

    private Service find(RegistryManager registryManager, String name) throws ServiceException {
        for (URI srvUri : registryManager.getServiceManager().listServices()) {
            if (srvUri.toASCIIString().contains(name)) {
                return registryManager.getServiceManager().getService(srvUri);
            }
        }
        return null;
    }

    private MatchType dataFlowMatch(Operation op1, Operation op2) {
        MessageContent outOp1 = op1.getOutputs().iterator().next();
        Set<URI> outputsOp1 = getModelReferences(outOp1);

        MessageContent inOp2 = op2.getInputs().iterator().next();
        Set<URI> inputsOp2 = getModelReferences(inOp2);

        // Match
        Table<URI, URI, MatchResult> result = conceptMatcher.match(outputsOp1, inputsOp2);
        // TODO: This should be independent of the match type used. MatchTypes.getHighest();
        MatchType best = LogicConceptMatchType.Exact;
        for (URI dest : inputsOp2) {
            // Get all matchers and find the best
            MatchType localBest = LogicConceptMatchType.Fail;
            Map<URI, MatchResult> matches = result.column(dest);
            // If there is no match, fail is assumed
            if (matches != null) {
                for (MatchResult matchResult : matches.values()) {
                    if (matchResult.getMatchType().compareTo(localBest) >= 0) {
                        localBest = matchResult.getMatchType();
                    }
                }
            }
            // Downgrade the best if the local Best for the match is worse than the global best
            if (localBest.compareTo(best) <= 0) {
                best = localBest;
            }
        }
        return best;
    }

    public Set<URI> getModelReferences(MessageContent msg) {
        Set<URI> uris = new HashSet<URI>();
        for (MessagePart p : msg.getMandatoryParts()) {
            for (uk.ac.open.kmi.msm4j.Resource r : p.getModelReferences()) {
                uris.add(r.getUri());
            }
        }
        return uris;
    }

    private Operation createOperationWithOutputs(Set<URI> outputs) {
        Operation op = new Operation(URI.create("http://localhost/op"));
        MessageContent content = new MessageContent(URI.create("http://localhost/msg"));
        for (URI output : outputs) {
            MessagePart part = new MessagePart(output);
            part.addModelReference(new Resource(output));
            content.addMandatoryPart(part);
        }
        op.addOutput(content);
        return op;
    }

    @Test
    public void testFail(RegistryManager registryManager) throws ServiceException {
        Set<URI> availableInputs = new HashSet<URI>();
        availableInputs.add(URI.create(WSC_01_TAXONOMY_NS + "con1233457844"));
        availableInputs.add(URI.create(WSC_01_TAXONOMY_NS + "con1849951292"));

        Operation fakeOp = createOperationWithOutputs(availableInputs);
        Service service = find(registryManager, "serv1529824753");
        Operation servOp = service.getOperations().iterator().next();
        MatchType type = dataFlowMatch(fakeOp, servOp);
        Assert.assertEquals(LogicConceptMatchType.Fail, type);
    }

    @Test
    public void testSubsumes(RegistryManager registryManager) throws ServiceException {
        Set<URI> availableInputs = new HashSet<URI>();
        availableInputs.add(URI.create(WSC_01_TAXONOMY_NS + "con1233457844"));
        availableInputs.add(URI.create(WSC_01_TAXONOMY_NS + "con1849951292"));
        availableInputs.add(URI.create(WSC_01_TAXONOMY_NS + "con1988815758"));

        Operation fakeOp = createOperationWithOutputs(availableInputs);
        Service service = find(registryManager, "serv1529824753");
        Operation servOp = service.getOperations().iterator().next();
        MatchType type = dataFlowMatch(fakeOp, servOp);
        Assert.assertEquals(LogicConceptMatchType.Subsume, type);
    }

    @Test
    public void testPlugin(RegistryManager registryManager) throws ServiceException {
        Set<URI> availableInputs = new HashSet<URI>();
        availableInputs.add(URI.create(WSC_01_TAXONOMY_NS + "con1233457844"));
        availableInputs.add(URI.create(WSC_01_TAXONOMY_NS + "con1849951292"));
        availableInputs.add(URI.create(WSC_01_TAXONOMY_NS + "con864995873"));

        Operation fakeOp = createOperationWithOutputs(availableInputs);
        Service service = find(registryManager, "serv1529824753");
        Operation servOp = service.getOperations().iterator().next();
        MatchType type = dataFlowMatch(fakeOp, servOp);
        Assert.assertEquals(LogicConceptMatchType.Plugin, type);
    }


    @Test
    public void testExact(RegistryManager registryManager) throws ServiceException {
        Set<URI> availableInputs = new HashSet<URI>();
        availableInputs.add(URI.create(WSC_01_TAXONOMY_NS + "con1794855625"));
        availableInputs.add(URI.create(WSC_01_TAXONOMY_NS + "con332477359"));
        Operation fakeOp = createOperationWithOutputs(availableInputs);
        Service service = find(registryManager, "serv904934656");
        Operation servOp = service.getOperations().iterator().next();
        MatchType type = dataFlowMatch(fakeOp, servOp);
        Assert.assertEquals(LogicConceptMatchType.Exact, type);
    }
}
