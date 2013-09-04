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
    private String xmlTaxonomyURL;
    private String owlOntologyURL;
    private String exportOWLTo;
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
        this.xmlTaxonomyURL = (String) props.getProperty("taxonomy.url");
        log.info("Using taxonomy {}", this.xmlTaxonomyURL);
        this.owlOntologyURL = (String) props.getProperty("ontology.url");
        URL xmlTaxonomy = new URL(xmlTaxonomyURL);
        log.info("Using ontology {}", this.owlOntologyURL);
        this.exportOWLTo = (String) props.getProperty("taxonomy.export");
        log.info("Exporting xml taxonomy to owl in {}", this.exportOWLTo);
        this.reasoner = new WSCXMLSemanticReasoner(xmlTaxonomy.openStream());
        // Convert the ontology to OWL
        if (this.exportOWLTo != null) {
            File file = new File(this.exportOWLTo);
            // TODO; Export only the ontology
            new OWLExporter(this.xmlTaxonomyURL, this.reasoner).exportTo(file);
        }
    }

    public WSCImporter(String xmlTaxonomyFile, String owlOntologyURL) throws IOException {
        this.reasoner = new WSCXMLSemanticReasoner(new FileInputStream(new File(xmlTaxonomyFile)));
        this.owlOntologyURL = owlOntologyURL;
    }


    private MessageContent transform(XMLInstance instance, String baseURI) {
        // TODO Handle baseURI in some way!
        String concept = this.reasoner.getConceptInstance(instance.getName());
        URI uri = URI.create(this.fakeURL + "#MessageContent_" + concept);
        MessageContent content = new MessageContent(uri);
        //content.setLabel("MessageContext");
        //content.setSource(uri);
        //content.setWsdlGrounding(uri);
        //content.setLabel(instance.getName());
        content.addModelReference(new Resource(URI.create(this.owlOntologyURL + "#" + concept)));
        return content;
    }

    private MessageContent transform(ArrayList<XMLInstance> instances, String fieldName, String owlOntologyURL) {
        URI uri = URI.create(this.fakeURL + "#MessageContent_" + fieldName);
        MessageContent msg = new MessageContent(uri);
        for (XMLInstance instance : instances) {
            String concept = this.reasoner.getConceptInstance(instance.getName());
            URI partURI = URI.create(this.fakeURL + "#MessagePart_" + concept);
            MessagePart part = new MessagePart(partURI);
            part.addModelReference(new Resource(URI.create(this.owlOntologyURL + "#" + concept)));
            msg.addMandatoryPart(part);
        }
        return msg;
    }


    public List<Service> transform(InputStream originalDescription, String baseUri, String owlOntologyURL) {
        // De-serialize from XML

        XMLServices services = JAXB.unmarshal(originalDescription, XMLServices.class);
        List<Service> listServices = new ArrayList<Service>(services.getServices().size());

        // Create the services following the iserve-commons-vocabulary model
        for (XMLService service : services.getServices()) {
            URI srvURI = URI.create(fakeURL + "#" + service.getName());
            URI opURI = URI.create(fakeURL + "/" + service.getName() + "#Operation");
            log.debug("Transforming service (URI: {})", srvURI);
            Service modelService = new Service(srvURI);
            //modelService.setSource(srvURI);
            //modelService.setWsdlGrounding(srvURI);

            // Create only one hasInput and hasOutput and mandatory parts for each input/output
            Operation operation = new Operation(opURI);
            operation.addInput(transform(service.getInputs().getInstances(), "input", owlOntologyURL));
            operation.addOutput(transform(service.getOutputs().getInstances(), "output", owlOntologyURL));
            operation.setLabel(service.getName() + "_op");

            // The commented method is not supported by iServe (some methods cannot read more than
            // one hasInput/hasOutput
            // TODO; Revise hasInput/hasOutput problem
            /*
            for(XMLInstance input : service.getInputs().getInstances()){
                operation.addInput(transform(input, baseUri));
            }
            for(XMLInstance output : service.getOutputs().getInstances()){
                operation.addOutput(transform(output, baseUri));
            }*/

            modelService.addOperation(operation);
            modelService.setLabel(service.getName());
            listServices.add(modelService);
        }
        return listServices;
    }

    @Override
    public List<Service> transform(InputStream originalDescription, String baseUri) {
        // De-serialize from XML
        return transform(originalDescription, baseUri, this.owlOntologyURL);
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

    public void setXmlTaxonomyURL(String xmlTaxonomyURL) {
        this.xmlTaxonomyURL = xmlTaxonomyURL;
    }

    public void setOwlOntologyURL(String owlOntologyURL) {
        this.owlOntologyURL = owlOntologyURL;
    }
}
