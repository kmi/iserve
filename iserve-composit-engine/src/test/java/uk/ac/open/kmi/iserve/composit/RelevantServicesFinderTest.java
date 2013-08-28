package uk.ac.open.kmi.iserve.composit;

import junit.framework.Assert;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.Transformer;
import uk.ac.open.kmi.iserve.commons.model.Service;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.disco.DiscoMatchType;
import uk.ac.open.kmi.iserve.discovery.disco.impl.FullIndexedLogicConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.disco.impl.LogicConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.disco.impl.PartialIndexedLogicConceptMatcher;
import uk.ac.open.kmi.iserve.sal.manager.KnowledgeBaseManager;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;
import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;


public class RelevantServicesFinderTest {
    private static final Logger log = LoggerFactory.getLogger(RelevantServicesFinderTest.class);
    //private static final String DATASET = "/wsc08-dataset01/services/services.xml";
    private static final String DATASET = "/simple-datasets/03/services.xml";
    private static final String MEDIATYPE = "text/xml";

    @BeforeClass
    public static void setUp() throws Exception {
        BasicConfigurator.configure();
        org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);
        // do your one-time setup here

        // Clean the whole thing before testing
        ManagerSingleton.getInstance().clearRegistry();

        log.info("Importing services");
        String file =  RelevantServicesFinderTest.class.getResource(DATASET).getFile();
        log.debug("Using " + file);
        File services = new File(file);

        List<Service> result = Transformer.getInstance().transform(services, null, MEDIATYPE);
        // Import all services
        for(Service s : result){
            URI uri = ManagerSingleton.getInstance().importService(s);
            Assert.assertNotNull(uri);
            log.info("Service added: " + uri.toASCIIString());
        }
    }

    @Test
    public void test() throws Exception {
        ServiceManager manager = ManagerSingleton.getInstance().getServiceManager();
        KnowledgeBaseManager kb = ManagerSingleton.getInstance().getKbManager();
        //ConceptMatcher matcher = new LogicConceptMatcher();
        //ConceptMatcher matcher = new PartialIndexedLogicConceptMatcher(manager, new LogicConceptMatcher());
        ConceptMatcher matcher = new FullIndexedLogicConceptMatcher(manager, kb, new LogicConceptMatcher());
        RelevantServicesFinder finder = new RelevantServicesFinder(matcher, manager);

        Set<URI> available = new HashSet<URI>();
        available.add(URI.create("http://localhost/ontology/taxonomy.owl#con1233457844"));
        available.add(URI.create("http://localhost/ontology/taxonomy.owl#con1849951292"));
        available.add(URI.create("http://localhost/ontology/taxonomy.owl#con864995873"));

        int[] expectedSize={5,3,2};
        List<Set<URI>> operations = finder.searchImproved(available, DiscoMatchType.Plugin);
        for(int i=0; i<operations.size();i++){
            assertTrue(operations.get(i).size()==expectedSize[i]);
        }
        //MatchGraph graph = finder.graph(operations, DiscoMatchType.Plugin);
        //graph.display();
        //Thread.currentThread().join();
    }


}
