package uk.ac.open.kmi.iserve.composit;

import com.google.caliper.memory.ObjectGraphMeasurer;
import com.google.common.base.Stopwatch;
import es.usc.citius.composit.importer.wsc.wscxml.WSCImporter;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;


public class RelevantServicesFinderTest {
    private static final Logger log = LoggerFactory.getLogger(RelevantServicesFinderTest.class);
    private static final String MEDIATYPE = "text/xml";
    private static Dataset dataset;

    private static Set<URI> WSC08_01_INPUTS, WSC08_02_INPUTS, WSC08_03_INPUTS, WSC08_04_INPUTS,
                           WSC08_05_INPUTS, WSC08_06_INPUTS, WSC08_07_INPUTS, WSC08_08_INPUTS;

    static {

        WSC08_01_INPUTS = new HashSet<URI>();
        WSC08_01_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_01.owl#con1233457844"));
        WSC08_01_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_01.owl#con1849951292"));
        WSC08_01_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_01.owl#con864995873"));

        WSC08_02_INPUTS = new HashSet<URI>();
        WSC08_02_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_02.owl#con1498435960"));
        WSC08_02_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_02.owl#con189054683"));
        WSC08_02_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_02.owl#con608925131"));
        WSC08_02_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_02.owl#con1518098260"));

        WSC08_03_INPUTS = new HashSet<URI>();
        WSC08_03_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_03.owl#con1765781068"));
        WSC08_03_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_03.owl#con1958306700"));
        WSC08_03_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_03.owl#con1881706184"));

        WSC08_04_INPUTS = new HashSet<URI>();
        WSC08_04_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_04.owl#con1174477567"));
        WSC08_04_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_04.owl#con1559120783"));
        WSC08_04_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_04.owl#con34478529"));
        WSC08_04_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_04.owl#con1492759465"));
        WSC08_04_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_04.owl#con1735261165"));
        WSC08_04_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_04.owl#con1801416348"));

        WSC08_05_INPUTS = new HashSet<URI>();
        WSC08_05_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_05.owl#con428391640"));
        WSC08_05_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_05.owl#con2100909192"));

        WSC08_06_INPUTS = new HashSet<URI>();
        WSC08_06_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_06.owl#con1927582736"));
        WSC08_06_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_06.owl#con2066855250"));
        WSC08_06_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_06.owl#con1482928259"));
        WSC08_06_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_06.owl#con1174685776"));
        WSC08_06_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_06.owl#con1929429507"));
        WSC08_06_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_06.owl#con1036639498"));
        WSC08_06_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_06.owl#con683948594"));
        WSC08_06_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_06.owl#con1055275345"));
        WSC08_06_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_06.owl#con74623429"));

        WSC08_07_INPUTS = new HashSet<URI>();
        WSC08_07_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_07.owl#con484707919"));
        WSC08_07_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_07.owl#con797253905"));
        WSC08_07_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_07.owl#con891470167"));
        WSC08_07_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_07.owl#con1591526279"));
        WSC08_07_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_07.owl#con1250100988"));
        WSC08_07_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_07.owl#con2073456634"));
        WSC08_07_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_07.owl#con222954059"));
        WSC08_07_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_07.owl#con409949952"));

        WSC08_08_INPUTS = new HashSet<URI>();
        WSC08_08_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_08.owl#con1269728670"));
        WSC08_08_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_08.owl#con1666449411"));
        WSC08_08_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_08.owl#con917858046"));
        WSC08_08_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_08.owl#con625786234"));
        WSC08_08_INPUTS.add(URI.create("http://localhost/ontology/taxonomy_wsc08_08.owl#con1966097554"));
    }

    private enum Dataset {
        SMALL_01("/simple-datasets/03/",        WSC08_01_INPUTS, "http://localhost/small",    new int[]{5,3,2}),
        WSC08_01("/WSC08/wsc08_datasets/01/",   WSC08_01_INPUTS, "http://localhost/ontology/taxonomy_wsc08_01.owl", new int[]{16,12,7,10,3,4,1,1,1,5}),
        WSC08_02("/WSC08/wsc08_datasets/02/",   WSC08_02_INPUTS, "http://localhost/ontology/taxonomy_wsc08_02.owl", new int[]{9,15,11,16,5,4,1,1}),
        WSC08_03("/WSC08/wsc08_datasets/03/",   WSC08_03_INPUTS, "http://localhost/ontology/taxonomy_wsc08_03.owl", new int[]{4,2,1,3,6,5,2,4,4,4,5,9,10,2,2,15,5,1,2,2,8,6,3}),
        WSC08_04("/WSC08/wsc08_datasets/04/",   WSC08_04_INPUTS, "http://localhost/ontology/taxonomy_wsc08_04.owl", new int[]{15,9,10,7,3}),
        WSC08_05("/WSC08/wsc08_datasets/05/",   WSC08_05_INPUTS, "http://localhost/ontology/taxonomy_wsc08_05.owl", new int[]{11,14,12,17,9,12,13,9,4,1}),
        WSC08_06("/WSC08/wsc08_datasets/06/",   WSC08_06_INPUTS, "http://localhost/ontology/taxonomy_wsc08_06.owl", new int[]{43,15,47,20,31,24,9,2,6,2,1,2,1,1}),
        WSC08_07("/WSC08/wsc08_datasets/07/",   WSC08_07_INPUTS, "http://localhost/ontology/taxonomy_wsc08_07.owl", new int[]{6,18,15,12,7,6,4,9,13,15,8,11,10,17,9,3,1}),
        WSC08_08("/WSC08/wsc08_datasets/08/",   WSC08_08_INPUTS, "http://localhost/ontology/taxonomy_wsc08_08.owl", new int[]{11,5,4,6,5,9,3,2,10,7,7,3,5,4,2,4,12,3,4,15,5,1,3,1});

        String path;
        Set<URI> initialInputs;
        int[] expectedSizes;
        String ontologyUrl;

        Dataset(String path, Set<URI> inputs, String ontologyUrl, int[] expectedSizes){
            this.path = path;
            this.initialInputs = inputs;
            this.expectedSizes = expectedSizes;
            this.ontologyUrl = ontologyUrl;

        }

        private String getServiceDescriptionFile() {
            return path + "services.xml";
        }

        private String getTaxonomyXmlFile(){
            return path + "taxonomy.xml";
        }




    }

    @BeforeClass
    public static void setUp() throws Exception {
        BasicConfigurator.configure();
        org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);
        // do your one-time setup here

        // Clean the whole thing before testing
        ManagerSingleton.getInstance().clearRegistry();

        // Select Dataset
        dataset = Dataset.WSC08_02;
        log.info("Importing services");
        String servicesDescriptionXmlFile =  RelevantServicesFinderTest.class.getResource(dataset.getServiceDescriptionFile()).getFile();
        String taxonomyXmlFile =  RelevantServicesFinderTest.class.getResource(dataset.getTaxonomyXmlFile()).getFile();
        log.info("Service description file {}", servicesDescriptionXmlFile);
        log.info("XML Taxonomy file {}", taxonomyXmlFile);
        log.info("Ontology Owl URL {}", dataset.ontologyUrl);
        WSCImporter imp = new WSCImporter(taxonomyXmlFile, dataset.ontologyUrl);
        List<Service> result = imp.transform(new FileInputStream(servicesDescriptionXmlFile), dataset.ontologyUrl);

        /*
        log.info("Importing services");
        String file =  RelevantServicesFinderTest.class.getResource(dataset.getPath()).getFile();
        log.debug("Using " + file);
        File services = new File(file);

        List<Service> result = Transformer.getInstance().transform(services, null, MEDIATYPE);
        */

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

        Stopwatch w = new Stopwatch().start();
        FullIndexedLogicConceptMatcher matcher = new FullIndexedLogicConceptMatcher(manager, kb, new LogicConceptMatcher());
        log.info(ObjectGraphMeasurer.measure(matcher).toString());
        log.info("Matcher populated in {}", w.toString());
        RelevantServicesFinder finder = new RelevantServicesFinder(matcher, manager);

        w.reset().start();
        List<Set<URI>> operations = finder.searchImproved2(dataset.initialInputs, DiscoMatchType.Plugin);
        log.info("Total time: {}", w.stop().toString());

        for(int i=0; i<operations.size();i++){
            assertTrue(operations.get(i).size()==dataset.expectedSizes[i]);
        }

        //MatchGraph graph = finder.graph(operations, DiscoMatchType.Plugin);
        //graph.display();
        //Thread.currentThread().join();
    }


}
