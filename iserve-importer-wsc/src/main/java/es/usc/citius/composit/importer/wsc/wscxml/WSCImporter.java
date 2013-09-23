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

package es.usc.citius.composit.importer.wsc.wscxml;


import es.usc.citius.composit.importer.wsc.wscxml.model.XMLInstance;
import es.usc.citius.composit.importer.wsc.wscxml.model.services.XMLService;
import es.usc.citius.composit.importer.wsc.wscxml.model.services.XMLServices;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import uk.ac.open.kmi.iserve.commons.io.ServiceTransformer;
import uk.ac.open.kmi.iserve.commons.io.TransformationException;
import uk.ac.open.kmi.iserve.commons.model.*;

import javax.xml.bind.JAXB;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Imports and transforms datasets from the Web Service Challenge 2008 (XML format)
 * Date: 7/18/13
 *
 * @author Pablo Rodríguez Mier
 */
public class WSCImporter implements ServiceTransformer {

    private static final Logger log = LoggerFactory.getLogger(WSCImporter.class);
    private WSCXMLSemanticReasoner reasoner;
    // Default name of the files for the taxonomy. These files must be in the same
    // directory as the service.xml file to auto-load them.
    private String taxonomyFile = "taxonomy.xml";
    private String ontologyFile = "taxonomy.owl";

    // This fake URL is replaced by iServe.
    private String fakeURL = "http://localhost/services/services.owl";

    // Mediatype of the WSC format
    public static final String mediaType = "text/xml";


    /**
     * The WSC specification does not define the ontology URI of the concepts.
     * Thus, the real URI of the ontology should be provided using the wscimporter.properties
     */
    public WSCImporter() throws Exception {
        // NOTE: The inputs and outputs of the services should be automatically translated from instances to
        // concepts using the XML Reasoner
        PropertiesConfiguration props = new PropertiesConfiguration("wscimporter.properties");
        this.taxonomyFile = (String) props.getProperty("taxonomy");
        // Name of the taxonomy.xml file (it will be localized automatically)
        log.info("Taxonomy file name {}", this.taxonomyFile);
        // Name of the ontology.owl file (it will be localized automatically)
        this.ontologyFile = (String) props.getProperty("ontology");
        log.info("Ontology file name {}", this.ontologyFile);
    }


    private MessageContent generateMessageContent(ArrayList<XMLInstance> instances, String fieldName, URL ontologyOwlUrl, WSCXMLSemanticReasoner reasoner) throws URISyntaxException {
        URI uri = URI.create(this.fakeURL + "#MessageContent_" + fieldName);
        MessageContent msg = new MessageContent(uri);
        for (XMLInstance instance : instances) {
            String concept = reasoner.getConceptInstance(instance.getName());
            URI partURI = URI.create(this.fakeURL + "#MessagePart_" + concept);
            MessagePart part = new MessagePart(partURI);
            part.addModelReference(new Resource(new URI(ontologyOwlUrl.toString() + "#" + concept)));
            msg.addMandatoryPart(part);
        }
        return msg;
    }

    /**
     * Transforms a datasets, defined by the XML description file of the services,
     * the taxonomy.xml file to translate instances to concepts, and the URL pointing
     * to the ontology that will be used when adding the model reference to the inputs/outputs
     * @param servicesXml
     * @param taxonomyXml
     * @param
     * @return
     */

    public List<Service> transform(InputStream servicesXml, InputStream taxonomyXml, InputStream ontologyOwlStream) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException, URISyntaxException {
        // Create the reasoner
        WSCXMLSemanticReasoner reasoner = new WSCXMLSemanticReasoner(taxonomyXml);
        // Obtain the base:xml uri associated to the ontology
        URL ontologyBaseUri = obtainXmlBaseUri(ontologyOwlStream);
        return transform(servicesXml, ontologyBaseUri, reasoner);
    }


    private List<Service> transform(InputStream originalDescription, URL modelReferenceOntoUrl, WSCXMLSemanticReasoner reasoner) throws URISyntaxException {
        // De-serialize from XML

        XMLServices services = JAXB.unmarshal(originalDescription, XMLServices.class);
        List<Service> listServices = new ArrayList<Service>(services.getServices().size());


        // Create the services following the iserve-commons-vocabulary model
        for (XMLService service : services.getServices()) {
            URI srvURI = URI.create(fakeURL + "#" + service.getName());
            URI opURI = URI.create(fakeURL + "/" + service.getName() + "#Operation");
            log.debug("Transforming service (Fake OWL URI: {})", srvURI);
            Service modelService = new Service(srvURI);

            // Create only one hasInput and hasOutput and mandatory parts for each input/output
            Operation operation = new Operation(opURI);

            // The modelReferenceOntoUrl must point to a valid url where the original ontology is
            operation.addInput(generateMessageContent(service.getInputs().getInstances(), "input", modelReferenceOntoUrl, reasoner));
            operation.addOutput(generateMessageContent(service.getOutputs().getInstances(), "output", modelReferenceOntoUrl, reasoner));
            operation.setLabel(service.getName() + "_op");


            modelService.addOperation(operation);
            modelService.setLabel(service.getName());
            listServices.add(modelService);
        }
        return listServices;
    }

    /**
     * Interface method for transforming services defined in the WSC XML format. Note that, given the service
     * definition of the WSC does not have valid urls pointing to the ontology, and since they use instances
     * instead of concepts for the inputs and outputs, a single stream with the service definition is not enough.
     * Two more files are required: a taxonomy.xml to easily translate instances to concepts, and a valid taxonomy.owl
     * ontology, which is a translation of the taxonomy.xml into owl to import into iServe. The baseUri is used to
     * automatically locate these files without specifying anything more to the transformer. By default, if the
     * baseUri is http://localhost/, this method will try to open http://localhost/taxonomy.xml and will use
     * http://localhost/taxonomy.owl as the model reference URL.
     *
     * @param originalDescription The semantic Web service description(s) stream
     * @param baseUri             The base URI to use while transforming the service description. The baseUri should point to
     *                            a valid location where the baseUri/taxonomy.xml and baseUri/taxonomy.owl can be located.
     * @return
     */
    @Override
    public List<Service> transform(InputStream originalDescription, String baseUri) throws TransformationException {
        // De-serialize from XML
        // Use the baseUri to locate automatically the taxonomy.xml and ontology.owl
        if (baseUri == null) {
            throw new NullPointerException("BaseUri cannot be null. Please specify a baseUri to locate" +
                    " the WSC files");
        }
        baseUri = baseUri.endsWith("/") ? baseUri : baseUri + "/";
        // Define the URL for the taxonomy xml file
        URL taxonomyXmlUrl;
        String taxonomyUrlStr = baseUri + this.taxonomyFile;
        try {
            taxonomyXmlUrl = new URL(taxonomyUrlStr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new TransformationException("There was a problem using the provided base uri " + baseUri + ". " +
                    " The taxonomy.xml URL " + taxonomyUrlStr + " is malformed.");
        }
        String ontologyUrlStr = baseUri + this.ontologyFile;
        URL ontologyOwlUrl;
        try {
            ontologyOwlUrl = new URL(ontologyUrlStr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new TransformationException("There was a problem using the provided base uri " + baseUri + ". " +
                    " The taxonomy.owl URL " + taxonomyUrlStr + " is malformed.");
        }
        // Create a reasoner to process the taxonomy.xml
        try {
            this.reasoner = new WSCXMLSemanticReasoner(taxonomyXmlUrl.openStream());
        } catch (IOException e) {
            e.printStackTrace();
            throw new TransformationException("Taxonomy XML file could not be located at " + taxonomyXmlUrl);
        }
        // Obtain the original xml:base uri from the owl file.
        URL ontologyBaseUri = null;
        try {
            ontologyBaseUri = obtainXmlBaseUri(ontologyOwlUrl);
            if (ontologyBaseUri == null){
                throw new NullPointerException("The ontology xml:base uri cannot be derived automatically " +
                        "from " + ontologyOwlUrl + ". Please check that the ontology has an attribute " +
                        "xml:base within the <rdf:RDF> tag");
            }
        } catch (Exception e) {
            TransformationException ex = new TransformationException("Cannot obtain the base uri for the ontology. " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }

        // Transform and return the result
        try {
            return transform(originalDescription, ontologyBaseUri, reasoner);
        } catch (URISyntaxException e) {
            TransformationException ex = new TransformationException("There were some problems trying to generate modelReferences. " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }

    }



    public static URL obtainXmlBaseUri(InputStream owlStream) throws IOException, XPathExpressionException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        // Detect namespaces
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(owlStream);
        // Obtain the document element where the xml:base is
        Element rootElement = document.getDocumentElement();
        String baseUri = rootElement.getAttribute("xml:base");
        return new URL(baseUri);
    }

    public static URL obtainXmlBaseUri(URL taxonomyOwlUrl) throws IOException, XPathExpressionException, ParserConfigurationException, SAXException {
        return obtainXmlBaseUri(taxonomyOwlUrl.openStream());
    }

    @Override
    public String getSupportedMediaType() {
        return mediaType;
    }

    @Override
    public List<String> getSupportedFileExtensions() {
        return Arrays.asList("xml");
    }

    @Override
    public String getVersion() {
        return "WSC Importer 0.1";
    }

    public void setReasoner(WSCXMLSemanticReasoner reasoner) {
        this.reasoner = reasoner;
    }

    public void setTaxonomyFile(String taxonomyFile) {
        this.taxonomyFile = taxonomyFile;
    }

    public void setOntologyFile(String ontologyFile) {
        this.ontologyFile = ontologyFile;
    }
}
