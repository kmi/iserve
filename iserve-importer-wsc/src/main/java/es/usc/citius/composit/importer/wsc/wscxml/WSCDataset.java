package es.usc.citius.composit.importer.wsc.wscxml;


import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;


/**
 * @author Pablo Rodríguez Mier
 */
public class WSCDataset {

    public enum Dataset {
        WSC08_01("/WSC08/wsc08_datasets/01/", StaticRef.WSC08_01_INPUTS, new int[]{16, 12, 7, 10, 3, 4, 1, 1, 1, 5}),
        WSC08_02("/WSC08/wsc08_datasets/02/", StaticRef.WSC08_02_INPUTS, new int[]{9, 15, 11, 16, 5, 4, 1, 1}),
        WSC08_03("/WSC08/wsc08_datasets/03/", StaticRef.WSC08_03_INPUTS, new int[]{4, 2, 1, 3, 6, 5, 2, 4, 4, 4, 5, 9, 10, 2, 2, 15, 5, 1, 2, 2, 8, 6, 3}),
        WSC08_04("/WSC08/wsc08_datasets/04/", StaticRef.WSC08_04_INPUTS, new int[]{15, 9, 10, 7, 3}),
        WSC08_05("/WSC08/wsc08_datasets/05/", StaticRef.WSC08_05_INPUTS, new int[]{11, 14, 12, 17, 9, 12, 13, 9, 4, 1}),
        WSC08_06("/WSC08/wsc08_datasets/06/", StaticRef.WSC08_06_INPUTS, new int[]{43, 15, 47, 20, 31, 24, 9, 2, 6, 2, 1, 2, 1, 1}),
        WSC08_07("/WSC08/wsc08_datasets/07/", StaticRef.WSC08_07_INPUTS, new int[]{6, 18, 15, 12, 7, 6, 4, 9, 13, 15, 8, 11, 10, 17, 9, 3, 1}),
        WSC08_08("/WSC08/wsc08_datasets/08/", StaticRef.WSC08_08_INPUTS, new int[]{11, 5, 4, 6, 5, 9, 3, 2, 10, 7, 7, 3, 5, 4, 2, 4, 12, 3, 4, 15, 5, 1, 3, 1});
        String datasetPath;
        Set<String> initialInputs;
        int[] expectedSizes;
        URL baseUrl;
        String relativeOntologyPath;

        Dataset(String datasetPath, Set<String> inputs, int[] expectedSizes) {
            this.datasetPath = datasetPath;
            this.initialInputs = inputs;
            this.expectedSizes = expectedSizes;
        }

        public InputStream openServiceDescriptionXmlFile() {
            return WSCDataset.class.getResourceAsStream(this.getServiceDescriptionFile());
        }

        public InputStream openTaxonomyXmlFile() {
            return WSCDataset.class.getResourceAsStream(this.getTaxonomyXmlFile());
        }

        public InputStream openOwlOntologyFile() {
            return WSCDataset.class.getResourceAsStream(this.getOntologyOwlFile());
        }

        public URL getOwlXmlBaseUrl() throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
            if (baseUrl == null) {
                baseUrl = WSCImporter.obtainXmlBaseUri(openOwlOntologyFile());
            }
            return baseUrl;
        }

        public URL getReplacedHostPortBaseUrl(String host, int port) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
            URL original = this.getOwlXmlBaseUrl();
            String newUrl = "http://" + host + ":" + port + original.getPath();
            return new URL(newUrl);
        }

        public InputStream openOwlOntologyFileWithReplacedBaseUrl(URL replacedBaseUrl) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
            URL originalBaseUrl = getOwlXmlBaseUrl();
            // TODO: Find a input stream which replaces the strings on the fly instead of loading the entire content in memory
            String ontologyContent = IOUtils.toString(this.openOwlOntologyFile());
            ontologyContent = ontologyContent.replace(originalBaseUrl.toString(), replacedBaseUrl.toString());
            return IOUtils.toInputStream(ontologyContent);
        }

        public String getServiceDescriptionFile() {
            return datasetPath + "services.xml";
        }

        public String getTaxonomyXmlFile() {
            return datasetPath + "taxonomy.xml";
        }

        public String getOntologyOwlFile() {
            return datasetPath + "taxonomy.owl";
        }

        public String getRelativeOntologyUrl() {
            return this.relativeOntologyPath + "taxonomy.owl";
        }

        public Set<URI> getInputs(URL baseOntoUrl) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
            Set<URI> uris = new HashSet<URI>();
            for (String input : initialInputs) {
                uris.add(URI.create(baseOntoUrl.toString() + "#" + input));
            }
            return uris;
        }


    }

    private static class StaticRef {

        static Set<String> WSC08_01_INPUTS, WSC08_02_INPUTS, WSC08_03_INPUTS, WSC08_04_INPUTS,
                WSC08_05_INPUTS, WSC08_06_INPUTS, WSC08_07_INPUTS, WSC08_08_INPUTS;

        static {
            WSC08_01_INPUTS = new HashSet<String>();
            WSC08_01_INPUTS.add("con1233457844");
            WSC08_01_INPUTS.add("con1849951292");
            WSC08_01_INPUTS.add("con864995873");

            WSC08_02_INPUTS = new HashSet<String>();
            WSC08_02_INPUTS.add("con1498435960");
            WSC08_02_INPUTS.add("con189054683");
            WSC08_02_INPUTS.add("con608925131");
            WSC08_02_INPUTS.add("con1518098260");

            WSC08_03_INPUTS = new HashSet<String>();
            WSC08_03_INPUTS.add("con1765781068");
            WSC08_03_INPUTS.add("con1958306700");
            WSC08_03_INPUTS.add("con1881706184");

            WSC08_04_INPUTS = new HashSet<String>();
            WSC08_04_INPUTS.add("con1174477567");
            WSC08_04_INPUTS.add("con1559120783");
            WSC08_04_INPUTS.add("con34478529");
            WSC08_04_INPUTS.add("con1492759465");
            WSC08_04_INPUTS.add("con1735261165");
            WSC08_04_INPUTS.add("con1801416348");

            WSC08_05_INPUTS = new HashSet<String>();
            WSC08_05_INPUTS.add("con428391640");
            WSC08_05_INPUTS.add("con2100909192");

            WSC08_06_INPUTS = new HashSet<String>();
            WSC08_06_INPUTS.add("con1927582736");
            WSC08_06_INPUTS.add("con2066855250");
            WSC08_06_INPUTS.add("con1482928259");
            WSC08_06_INPUTS.add("con1174685776");
            WSC08_06_INPUTS.add("con1929429507");
            WSC08_06_INPUTS.add("con1036639498");
            WSC08_06_INPUTS.add("con683948594");
            WSC08_06_INPUTS.add("con1055275345");
            WSC08_06_INPUTS.add("con74623429");

            WSC08_07_INPUTS = new HashSet<String>();
            WSC08_07_INPUTS.add("con484707919");
            WSC08_07_INPUTS.add("con797253905");
            WSC08_07_INPUTS.add("con891470167");
            WSC08_07_INPUTS.add("con1591526279");
            WSC08_07_INPUTS.add("con1250100988");
            WSC08_07_INPUTS.add("con2073456634");
            WSC08_07_INPUTS.add("con222954059");
            WSC08_07_INPUTS.add("con409949952");

            WSC08_08_INPUTS = new HashSet<String>();
            WSC08_08_INPUTS.add("con1269728670");
            WSC08_08_INPUTS.add("con1666449411");
            WSC08_08_INPUTS.add("con917858046");
            WSC08_08_INPUTS.add("con625786234");
            WSC08_08_INPUTS.add("con1966097554");
        }

    }


}




