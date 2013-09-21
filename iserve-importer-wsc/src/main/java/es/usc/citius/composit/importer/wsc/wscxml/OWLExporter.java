package es.usc.citius.composit.importer.wsc.wscxml;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import es.usc.citius.composit.importer.wsc.wscxml.model.XMLInstance;
import es.usc.citius.composit.importer.wsc.wscxml.model.services.XMLService;
import es.usc.citius.composit.importer.wsc.wscxml.model.services.XMLServices;

import javax.xml.bind.JAXB;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * @author Pablo Rodríguez Mier
 */
public class OWLExporter {
    private String baseUri = "http://localhost";
    private String relativeServicePath = "services";
    private String relativeOntologyFile = "ontology/taxonomy.owl";

    // JAXB XML
    private XMLServices jaxb_services;
    // Concept taxonomy
    private WSCXMLSemanticReasoner taxonomy;
    // Index of services by name
    private Map<String, XMLService> index = new HashMap();
    // Template
    private String template;

    // End of line
    private String eol = System.getProperty("line.separator");

    public OWLExporter(String baseUri, String _pathXmlServices, WSCXMLSemanticReasoner taxonomy) throws IOException {
        this(_pathXmlServices, taxonomy);
        this.baseUri = baseUri;
    }

    public OWLExporter(String _pathXmlServices, WSCXMLSemanticReasoner taxonomy) throws IOException {
        this.jaxb_services = JAXB.unmarshal(new File(_pathXmlServices), XMLServices.class);
        this.taxonomy = taxonomy;
        //this.jaxb_taxonomy = JAXB.unmarshal(new File(_pathXmlTaxonomy), XMLTaxonomy.class);
        // Populate index
        for (XMLService service : jaxb_services.getServices()) {
            index.put(service.getName(), service);
        }
        // Load template
        this.template = loadTemplate();
    }

    public OWLExporter(String _pathXmlServices, String _pathXmlTaxonomy) throws IOException {
        this(_pathXmlServices, new WSCXMLSemanticReasoner(new File(_pathXmlTaxonomy)));
    }

    public OWLExporter(String baseUri, String _pathXmlServices, String _pathXmlTaxonomy) throws IOException {
        this(_pathXmlServices, new WSCXMLSemanticReasoner(new File(_pathXmlTaxonomy)));
        this.baseUri = baseUri;
    }

    private String loadTemplate() throws IOException {
        System.out.println("Loading template " + getClass().getResource("template.xml"));
        InputStream in = OWLExporter.class.getResourceAsStream("template.xml");
        byte[] buffer = new byte[in.available()];
        in.read(buffer);
        in.close();
        return new String(buffer);
    }

    public void exportOntologyTo(File ontologyFile, String ontologyBaseUri) throws IOException {
        BufferedWriter bout = new BufferedWriter(new FileWriter(ontologyFile));
        bout.write(getOWLOntology(ontologyBaseUri));
        bout.close();
    }

    /**
     * Dump converted owl-s services and owl ontology
     * under destinationPath/services and destinationPath/ontology
     *
     * @param destinationPath Destination path
     */
    public void exportTo(File destinationPath) throws Exception {
        if (destinationPath == null || !destinationPath.isDirectory() || !destinationPath.canWrite()) {
            throw new Exception("Invalid directory");
        }
        // Export ontology
        System.out.println(">Exporting ontology...");
        File pathOntology = new File(destinationPath.getPath() + "/ontology");
        pathOntology.mkdir();
        File ontologyFile = new File(pathOntology.getPath() + "/taxonomy.owl");
        System.out.println("Writing " + ontologyFile);
        BufferedWriter bout = new BufferedWriter(new FileWriter(ontologyFile));
        bout.write(getOWLOntology());
        bout.close();

        // Export services
        System.out.println(">Exporting services...");
        File pathServices = new File(destinationPath.getPath() + "/services");
        pathServices.mkdir();

        for (XMLService service : jaxb_services.getServices()) {
            // Get owl path
            File newOlwsFile = new File(pathServices.getPath() + "/" + service.getName() + ".owl");
            System.out.println("Writing " + newOlwsFile.getAbsoluteFile());
            String dump = getOWLSService(service.getName());
            BufferedWriter out = new BufferedWriter(new FileWriter(newOlwsFile));
            out.write(dump);
            out.close();
        }


    }

    public String getOWLSService(String serviceName) throws IOException {
        return getOWLSService(serviceName, baseUri, relativeOntologyFile, relativeServicePath);
    }

    /**
     * Converts the OWL-S service associate to the XML Service defined
     * in WSC-08
     *
     * @param serviceName          Service name (serv...)
     * @param baseUri              root base uri, for example http://localhost
     * @param relativeOntologyFile for example ontology/onto.owl. This is appended to baseUri.
     * @return RDF (OWL-S) service
     */
    public String getOWLSService(String serviceName, String baseUri, String relativeOntologyFile, String relativeServicePath) throws IOException {
        String owlTemplate = new String(template);
        XMLService service = index.get(serviceName);
        String ontologyUri = baseUri + "/" + relativeOntologyFile;
        // Build input section
        String input_section = "";
        for (XMLInstance input : service.getInputs().getInstances()) {
            String concept = taxonomy.getConceptInstance(input.getName());
            String block = "<process:hasInput>" + eol +
                    "  <process:Input rdf:ID=\"" + input.getName() + "\">" + eol +
                    "  <process:parameterType rdf:datatype=\"http://www.w3.org/2001/XMLSchema#anyURI\">" + eol +
                    ontologyUri + "#" + concept + eol +
                    "  </process:parameterType>" + eol +
                    "  </process:Input>" + eol +
                    "</process:hasInput>" + eol;

            input_section += block;
        }

        // Build output section
        String output_section = "";
        for (XMLInstance output : service.getOutputs().getInstances()) {
            String concept = taxonomy.getConceptInstance(output.getName());
            String block = "<process:hasOutput>" + eol +
                    "<process:Output rdf:ID=\"" + output.getName() + "\">" + eol +
                    "<process:parameterType rdf:datatype=\"http://www.w3.org/2001/XMLSchema#anyURI\">" + eol +
                    ontologyUri + "#" + concept + eol +
                    "</process:parameterType>" + eol +
                    "</process:Output>" + eol +
                    "</process:hasOutput>" + eol;

            output_section += block;
        }

        // Build profile input section
        String profile_input_section = "";
        for (XMLInstance input : service.getInputs().getInstances()) {
            String concept = taxonomy.getConceptInstance(input.getName());
            profile_input_section += "<profile:hasInput rdf:resource=\"#" + input.getName() + "\"/>" + eol;
        }

        // Build profile output section
        String profile_output_section = "";
        for (XMLInstance output : service.getOutputs().getInstances()) {
            String concept = taxonomy.getConceptInstance(output.getName());
            profile_output_section += "<profile:hasOutput rdf:resource=\"#" + output.getName() + "\"/>" + eol;
        }

        // Replace sections
        owlTemplate = owlTemplate.replace("%input_section%", input_section);
        owlTemplate = owlTemplate.replace("%output_section%", output_section);
        owlTemplate = owlTemplate.replace("%profile_input_section%", profile_input_section);
        owlTemplate = owlTemplate.replace("%profile_output_section%", profile_output_section);
        // Replace service & baseUri
        owlTemplate = owlTemplate.replace("%service%", service.getName());
        owlTemplate = owlTemplate.replace("%ontology%", ontologyUri);
        owlTemplate = owlTemplate.replace("%baseUri%", baseUri);
        owlTemplate = owlTemplate.replace("%servicePath%", relativeServicePath);
        return owlTemplate;
    }

    public String getOWLOntology() {
        return getOWLOntology(baseUri + "/" + relativeOntologyFile);
    }

    /**
     * @return Retrieves the OWL ontology
     */
    public String getOWLOntology(String baseUri) {
        String onto = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + eol +
                "<rdf:RDF" + eol +
                "  xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"" + eol +
                "  xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"" + eol +
                "  xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"" + eol +
                "  xmlns:owl=\"http://www.w3.org/2002/07/owl#\"" + eol +
                "  xml:base=\"" + baseUri + "\">" + eol +
                "  <owl:Ontology rdf:about=\"\" />" + eol;

        String strData = "";
        int totalConcepts = taxonomy.getConcepts().size();
        int counter = 0;
        for (String concept : taxonomy.getConcepts()) {
            // Check if this class has superclasses
            Set<String> superclasses = taxonomy.getSuperclasses(concept);//taxonomy.getClassSuperclassTaxonomy().get(concept);
            if (superclasses != null && !superclasses.isEmpty()) {
                strData += "<owl:Class rdf:ID=\"" + concept + "\">" + eol;
                for (String superclass : superclasses) {
                    strData += "   <rdfs:subClassOf rdf:resource=\"#" + superclass + "\" />" + eol;
                }
                strData += "</owl:Class>" + eol;
            } else {
                strData += "<owl:Class rdf:ID=\"" + concept + "\" />" + eol;
            }
            counter++;
            System.out.println("Concept " + concept + " processed (" + counter + "/" + totalConcepts + ")");
        }
        /*
        counter = 0;
        for(XMLConcept concept : taxonomy.listConcepts()){
            // Write instances
            if (concept.getInstances() != null && !concept.getInstances().isEmpty()){
                for(XMLInstance instance : concept.getInstances()){
                strData += "<owl:Thing rdf:ID=\"" + instance.getName() + "\">" + eol +
                            "    <rdf:type rdf:resource=\"#" + concept.getName() + "\" />"+ eol +
                            "</owl:Thing>" + eol;
                }
            }
            counter++;
            System.out.println("Instance for concept " + concept.getName() + " processed (" + counter + "/" + totalConcepts +")");
        }*/
        return onto + strData + "</rdf:RDF>";
    }

}
