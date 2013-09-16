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
import uk.ac.open.kmi.iserve.commons.io.ServiceTransformer;
import uk.ac.open.kmi.iserve.commons.model.*;

import javax.xml.bind.JAXB;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private String taxonomyFile;
    private String ontologyFile;
    //private String exportOWLTo;
    private String fakeURL = "http://localhost/services/services.owl";

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
        log.info("Taxonomy file name {}", this.taxonomyFile);
        this.ontologyFile = (String) props.getProperty("ontology");
        log.info("Ontology file name {}", this.ontologyFile);
        //this.exportOWLTo = (String) props.getProperty("taxonomy.export");
        //log.info("Exporting xml taxonomy to owl in {}", this.exportOWLTo);
        //this.reasoner = new WSCXMLSemanticReasoner(xmlTaxonomy.openStream());
    }

    public WSCImporter(String xmlTaxonomyFile, String ontologyFile) throws IOException {
        this.reasoner = new WSCXMLSemanticReasoner(new FileInputStream(new File(xmlTaxonomyFile)));
        this.ontologyFile = ontologyFile;
    }

    public WSCImporter(InputStream xmlTaxonomyFileStream, String ontologyFile) throws IOException {
        this.reasoner = new WSCXMLSemanticReasoner(xmlTaxonomyFileStream);
        this.ontologyFile = ontologyFile;
    }


    private MessageContent transform(XMLInstance instance, String ontologyOwlUrl, WSCXMLSemanticReasoner reasoner) throws URISyntaxException {
        String concept = reasoner.getConceptInstance(instance.getName());
        URI uri = URI.create(this.fakeURL + "#MessageContent_" + concept);
        MessageContent content = new MessageContent(uri);

        String globalOntologyUri = deriveGlobalOntologyUri(ontologyOwlUrl);
        content.addModelReference(new Resource(new URI(globalOntologyUri + "#" + concept)));
        return content;
    }

    private String deriveGlobalOntologyUri(String ontologyOwlUrl) {
        String endPart = ontologyOwlUrl.substring(ontologyOwlUrl.indexOf("wsc08_datasets"));
        return "http://localhost/wsc/" + endPart;
    }

    private MessageContent transform(ArrayList<XMLInstance> instances, String fieldName, String ontologyOwlUrl, WSCXMLSemanticReasoner reasoner) throws URISyntaxException {
        URI uri = URI.create(this.fakeURL + "#MessageContent_" + fieldName);
        MessageContent msg = new MessageContent(uri);
        for (XMLInstance instance : instances) {
            String concept = reasoner.getConceptInstance(instance.getName());
            URI partURI = URI.create(this.fakeURL + "#MessagePart_" + concept);
            MessagePart part = new MessagePart(partURI);
            String globalOntologyUri = deriveGlobalOntologyUri(ontologyOwlUrl);
            part.addModelReference(new Resource(new URI(globalOntologyUri + "#" + concept)));
            msg.addMandatoryPart(part);
        }
        return msg;
    }


    public List<Service> transform(InputStream originalDescription, String ontologyOwlUrl, WSCXMLSemanticReasoner reasoner) throws URISyntaxException {
        // De-serialize from XML

        XMLServices services = JAXB.unmarshal(originalDescription, XMLServices.class);
        List<Service> listServices = new ArrayList<Service>(services.getServices().size());


        // Create the services following the iserve-commons-vocabulary model
        for (XMLService service : services.getServices()) {
            URI srvURI = URI.create(fakeURL + "#" + service.getName());
            URI opURI = URI.create(fakeURL + "/" + service.getName() + "#Operation");
            log.debug("Transforming service (Fake OWL URI: {})", srvURI);
            Service modelService = new Service(srvURI);
            //modelService.setSource(srvURI);
            //modelService.setWsdlGrounding(srvURI);

            // Create only one hasInput and hasOutput and mandatory parts for each input/output
            Operation operation = new Operation(opURI);
            //TODO: Remove the line below!
            //ontologyOwlUrl="http://localhost/invalid.owl";
            operation.addInput(transform(service.getInputs().getInstances(), "input", ontologyOwlUrl, reasoner));
            operation.addOutput(transform(service.getOutputs().getInstances(), "output", ontologyOwlUrl, reasoner));
            operation.setLabel(service.getName() + "_op");


            modelService.addOperation(operation);
            modelService.setLabel(service.getName());
            listServices.add(modelService);
        }
        return listServices;
    }

    @Override
    public List<Service> transform(InputStream originalDescription, String baseUri) {
        // De-serialize from XML
        // Use the baseUri to locate automatically the taxonomy.xml and ontology.owl
        String ontologyOwlUrl = null;
        if (baseUri == null) {
            throw new NullPointerException("BaseUri cannot be null. Please specify a baseUri to locate" +
                    " the WSC files");
        }
        try {
            ontologyOwlUrl = new URL(baseUri + this.ontologyFile).toURI().toASCIIString();
            if (reasoner == null) {
                // Try to load automatically the required taxonomy.xml (only required if
                // there is no reasoner instantiated)
                URL taxonomyXmlUrl = new URL(baseUri + this.taxonomyFile);
                // Create a new reasoner
                reasoner = new WSCXMLSemanticReasoner(taxonomyXmlUrl.openStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            return transform(originalDescription, ontologyOwlUrl, reasoner);
        } catch (URISyntaxException e) {
            log.error("Problems generating URIs while transforming service.", e);
            return new ArrayList<Service>();
        }
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
