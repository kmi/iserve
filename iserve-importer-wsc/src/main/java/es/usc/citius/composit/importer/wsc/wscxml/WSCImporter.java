package es.usc.citius.composit.importer.wsc.wscxml;


import es.usc.citius.composit.importer.wsc.wscxml.model.XMLInstance;
import es.usc.citius.composit.importer.wsc.wscxml.model.services.XMLService;
import es.usc.citius.composit.importer.wsc.wscxml.model.services.XMLServices;
import uk.ac.open.kmi.iserve.commons.model.MessageContent;
import uk.ac.open.kmi.iserve.commons.model.Operation;
import uk.ac.open.kmi.iserve.commons.model.Resource;
import uk.ac.open.kmi.iserve.commons.model.Service;
import uk.ac.open.kmi.iserve.sal.ServiceImporter;
import uk.ac.open.kmi.iserve.sal.exception.ImporterException;

import javax.xml.bind.JAXB;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Imports and transforms datasets from the Web Service Challenge 2008 (XML format)
 * Date: 7/18/13
 * @author Pablo Rodríguez Mier
 */
public class WSCImporter implements ServiceImporter {

    public static final String DEFAULT_NAMESPACE_URI = "http://www.ws-challenge.org/WSC08Services";
    private WSCXMLSemanticReasoner reasoner;
    private String xmlTaxonomyURL;
    private String ontologyName = "taxonomy.owl";
    private String owlTaxonomyURL;

    /**
     * By default, the WSC specification does not define the ontology URI of the concepts.
     * Thus, the real URI of the ontology should be provided. Example: http://localhost/ontology/taxonomy.xml
     * @param xmlTaxonomyURL URL of the ontology where the semantic concepts (inputs/outputs) are defined (XML format)
     */
    public WSCImporter(String xmlTaxonomyURL) throws IOException {
        // NOTE: The inputs and outputs of the services should be automatically translated from instances to
        // concepts using the XML Reasoner
        URL ontology = new URL(xmlTaxonomyURL);
        this.reasoner = new WSCXMLSemanticReasoner(ontology.openStream());
        // TODO Convert the ontology to OWL ??

        this.xmlTaxonomyURL = xmlTaxonomyURL;
    }

    @Override
    public List<Service> transform(InputStream originalDescription) throws ImporterException {
        return transform(originalDescription, null);
    }

    private MessageContent transform(XMLInstance instance, String baseURI){
        String concept = this.reasoner.getConceptInstance(instance.getName());
        URI uri = URI.create(baseURI + ontologyName + "#" + concept);
        MessageContent content = new MessageContent(uri);
        content.setSource(uri);
        content.setWsdlGrounding(uri);
        content.setLabel(instance.getName());
        content.addModelReference(new Resource(uri));
        return content;
    }

    @Override
    public List<Service> transform(InputStream originalDescription, String baseUri) throws ImporterException {
        // De-serialize from XML
        XMLServices services = JAXB.unmarshal(originalDescription, XMLServices.class);
        List<Service> listServices = new ArrayList<Service>(services.getServices().size());
        String uri = baseUri.endsWith("/") ? baseUri : baseUri + "/";
        // Create the services following the iserve-commons-vocabulary model
        for(XMLService service : services.getServices()){
            URI srvURI = URI.create(uri+service.getName());
            Service modelService = new Service(srvURI);
            modelService.setSource(srvURI);
            modelService.setWsdlGrounding(srvURI);

            Operation operation = new Operation(URI.create(baseUri + service.getName()));
            for(XMLInstance input : service.getInputs().getInstances()){
                operation.addInput(transform(input, baseUri));
            }
            for(XMLInstance output : service.getOutputs().getInstances()){
                operation.addOutput(transform(output, baseUri));
            }
            modelService.addOperation(operation);
            modelService.setLabel(service.getName());

            listServices.add(modelService);
        }
        return listServices;
    }

    @Override
    public List<Service> transform(File originalDescription) throws ImporterException {
        if (originalDescription != null && originalDescription.exists()) {
            // Open the file and transform it
            InputStream in = null;
            try {
                in = new FileInputStream(originalDescription);
                return transform(in);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new ImporterException("Unable to open input file", e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new ImporterException("Error while closing input file", e);
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<Service> transform(File originalDescription, String baseUri) throws ImporterException {
        return transform(originalDescription);
    }

    /*
    public static void main(String[] args) throws IOException, ImporterException {
        WSCImporter imp = new WSCImporter(new File("/home/citius/KMi/iserve-project/http-server/htdocs/ontology/taxonomy.xml").toURI().toURL().toString());
        imp.transform(new FileInputStream(new File("/home/citius/KMi/iserve-project/http-server/htdocs/services/services.xml")),"");
    }*/
}
