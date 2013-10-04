package uk.ac.open.kmi.iserve.discovery.disco.impl;

import com.google.common.collect.Table;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import es.usc.citius.composit.importer.wsc.wscxml.WSCDataset;
import junit.framework.Assert;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.Transformer;
import uk.ac.open.kmi.iserve.commons.model.*;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.manager.impl.iServeFacade;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.*;

/**
 * @author Pablo Rodríguez Mier
 */
public class OperationMatchTest {
    private static final Logger log = LoggerFactory.getLogger(SparqlLogicConceptMatcherWSC08Test.class);

    private static final String MEDIATYPE = "text/xml";

    private static final String SPARQL_ENDPOINT = "http://localhost:8080/openrdf-sesame/repositories/Test";

    private static final String WSC08_01 = "/WSC08/wsc08_datasets/01/";
    private static final String WSC08_01_SERVICES = WSC08_01 + "services.xml";
    private static final String WSC08_01_TAXONOMY_FILE = WSC08_01 + "taxonomy.owl";
    private static final String WSC_01_TAXONOMY_URL = "http://localhost/wsc/01/taxonomy.owl";
    private static final String WSC_01_TAXONOMY_NS = "http://localhost/wsc/01/taxonomy.owl#";

    private static ConceptMatcher conceptMatcher;
    private static iServeFacade manager;

    @BeforeClass
    public static void setUp() throws Exception {
        BasicConfigurator.configure();
        org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);

        manager = iServeFacade.getInstance();

        // Clean the whole thing before testing
        manager.clearRegistry();

        conceptMatcher = new SparqlLogicConceptMatcher(SPARQL_ENDPOINT);

        log.info("Importing WSC 2008 services");
        String file = SparqlLogicConceptMatcherWSC08Test.class.getResource(WSC08_01_SERVICES).getFile();
        log.debug("Using " + file);
        File services = new File(file);

        // Get base url
        URL base = SparqlLogicConceptMatcherWSC08Test.class.getResource(WSC08_01);

        // First load the ontology in the server to avoid issues
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        // Fetch the model
        String taxonomyFile = SparqlLogicConceptMatcherWSC08Test.class.getResource(WSC08_01_TAXONOMY_FILE).toURI().toASCIIString();
        model.read(taxonomyFile);

        // Upload the model first (it won't be automatically fetched as the URIs won't resolve so we do it manually)
        manager.getKnowledgeBaseManager().uploadModel(URI.create(WSC_01_TAXONOMY_URL), model, true);

        //List<Service> result = new WSCImporter().transform(new FileInputStream(services), null);
        // Automatic plugin discovery
        List<Service> result = Transformer.getInstance().transform(services, base.toURI().toASCIIString(), MEDIATYPE);
        // Import all services
        for (Service s : result) {
            URI uri = manager.getServiceManager().addService(s);
            Assert.assertNotNull(uri);
            log.info("Service added: " + uri.toASCIIString());
        }
    }

    private Service find(String name) throws ServiceException {
        for(URI srvUri : manager.getServiceManager().listServices()){
            if (srvUri.toASCIIString().contains(name)){
                return manager.getServiceManager().getService(srvUri);
            }
        }
        return null;
    }

    private MatchType dataFlowMatch(Operation op1, Operation op2){
        MessageContent outOp1 = op1.getOutputs().iterator().next();
        Set<URI> outputsOp1 = getModelReferences(outOp1);

        MessageContent inOp2 = op2.getInputs().iterator().next();
        Set<URI> inputsOp2 = getModelReferences(inOp2);

        // Match
        Table<URI,URI, MatchResult> result = conceptMatcher.match(outputsOp1, inputsOp2);
        // TODO: This should be independent of the match type used. MatchTypes.getHighest();
        MatchType best = LogicConceptMatchType.Exact;
        for(URI dest :inputsOp2){
            // Get all matchers and find the best
            MatchType localBest = LogicConceptMatchType.Fail;
            Map<URI, MatchResult> matches = result.column(dest);
            // If there is no match, fail is assumed
            if (matches != null){
                for(MatchResult matchResult : matches.values()){
                    if (matchResult.getMatchType().compareTo(localBest)>=0){
                        localBest = matchResult.getMatchType();
                    }
                }
            }
            // Downgrade the best if the local Best for the match is worse than the global best
            if (localBest.compareTo(best)<=0){
                best = localBest;
            }
        }
        return best;
    }

    public Set<URI> getModelReferences(MessageContent msg){
        Set<URI> uris = new HashSet<URI>();
        for(MessagePart p : msg.getMandatoryParts()){
            for(uk.ac.open.kmi.iserve.commons.model.Resource r : p.getModelReferences()){
                uris.add(r.getUri());
            }
        }
        return uris;
    }

    private Operation createOperationWithOutputs(Set<URI> outputs){
        Operation op = new Operation(URI.create("http://localhost/op"));
        MessageContent content = new MessageContent(URI.create("http://localhost/msg"));
        for(URI output : outputs){
            MessagePart part = new MessagePart(output);
            part.addModelReference(new Resource(output));
            content.addMandatoryPart(part);
        }
        op.addOutput(content);
        return op;
    }

    @Test
    public void testFail() throws ServiceException {
        Set<URI> availableInputs = new HashSet<URI>();
        availableInputs.add(URI.create(WSC_01_TAXONOMY_NS + "con1233457844"));
        availableInputs.add(URI.create(WSC_01_TAXONOMY_NS + "con1849951292"));

        Operation fakeOp = createOperationWithOutputs(availableInputs);
        Service service = find("serv1529824753");
        Operation servOp = service.getOperations().iterator().next();
        MatchType type = dataFlowMatch(fakeOp, servOp);
        Assert.assertEquals(LogicConceptMatchType.Fail, type);
    }

    @Test
    public void testSubsumes() throws ServiceException {
        Set<URI> availableInputs = new HashSet<URI>();
        availableInputs.add(URI.create(WSC_01_TAXONOMY_NS + "con1233457844"));
        availableInputs.add(URI.create(WSC_01_TAXONOMY_NS + "con1849951292"));
        availableInputs.add(URI.create(WSC_01_TAXONOMY_NS + "con1988815758"));

        Operation fakeOp = createOperationWithOutputs(availableInputs);
        Service service = find("serv1529824753");
        Operation servOp = service.getOperations().iterator().next();
        MatchType type = dataFlowMatch(fakeOp, servOp);
        Assert.assertEquals(LogicConceptMatchType.Subsume, type);
    }

    @Test
    public void testPlugin() throws ServiceException {
        Set<URI> availableInputs = new HashSet<URI>();
        availableInputs.add(URI.create(WSC_01_TAXONOMY_NS + "con1233457844"));
        availableInputs.add(URI.create(WSC_01_TAXONOMY_NS + "con1849951292"));
        availableInputs.add(URI.create(WSC_01_TAXONOMY_NS + "con864995873"));

        Operation fakeOp = createOperationWithOutputs(availableInputs);
        Service service = find("serv1529824753");
        Operation servOp = service.getOperations().iterator().next();
        MatchType type = dataFlowMatch(fakeOp, servOp);
        Assert.assertEquals(LogicConceptMatchType.Plugin, type);
    }


    @Test
    public void testExact() throws ServiceException {
        Set<URI> availableInputs = new HashSet<URI>();
        availableInputs.add(URI.create(WSC_01_TAXONOMY_NS + "con1794855625"));
        availableInputs.add(URI.create(WSC_01_TAXONOMY_NS + "con332477359"));
        Operation fakeOp = createOperationWithOutputs(availableInputs);
        Service service = find("serv904934656");
        Operation servOp = service.getOperations().iterator().next();
        MatchType type = dataFlowMatch(fakeOp, servOp);
        Assert.assertEquals(LogicConceptMatchType.Exact, type);
    }
}
