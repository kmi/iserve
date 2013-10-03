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

package uk.ac.open.kmi.iserve.importer.owls;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import org.apache.commons.cli.*;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.ServiceTransformer;
import uk.ac.open.kmi.iserve.commons.io.ServiceWriter;
import uk.ac.open.kmi.iserve.commons.io.ServiceWriterImpl;
import uk.ac.open.kmi.iserve.commons.io.TransformationException;
import uk.ac.open.kmi.iserve.commons.io.extensionTypes.PddlType;
import uk.ac.open.kmi.iserve.commons.model.*;
import uk.ac.open.kmi.iserve.commons.model.util.Vocabularies;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OwlsTransformer implements ServiceTransformer {

    private static final Logger log = LoggerFactory.getLogger(OwlsTransformer.class);

    private static final String SERVICE_VAR = "service";
    private static final String PROFILE_VAR = "profile";
    private static final String PROCESS_VAR = "process";
    private static final String PROCESS_MODEL_VAR = PROCESS_VAR + "Model";
    private static final String CONDITION_VAR = "condition";
    private static final String CONDITION_LABEL_VAR = "conditionlabel";
    private static final String CONDITION_EXPR_LANG_VAR = "conditionExprLang";
    private static final String CONDITION_EXPR_BODY_VAR = "conditionExprBody";
    private static final String CLASSIFICATION_VAR = "sclass";
    private static final String SVC_PRODUCT_VAR = "sproduct";
    private static final String RESULT_VAR = "result";
    private static final String RESULT_VAR_VAR = "resultVar";
    private static final String RESULT_VAR_TYPE_VAR = RESULT_VAR_VAR + "Type";
    private static final String IN_CONDITION_VAR = "inCondition";
    private static final String IN_CONDITION_EXPR_BODY_VAR = IN_CONDITION_VAR + "ExprBody";
    private static final String IN_CONDITION_EXPR_LANG_VAR = IN_CONDITION_VAR + "ExprLang";
    private static final String EFFECT_VAR = "effect";
    private static final String EFFECT_EXPR_BODY_VAR = EFFECT_VAR + "ExprBody";
    private static final String EFFECT_EXPR_LANG_VAR = EFFECT_VAR + "ExprLang";
    private static final String PROCESS_GROUNDING_VAR = "processGrounding";
    private static final String WSDL_DOC_VAR = "wsdlDocument";
    private static final String WSDL_GROUNDING_VAR = "wsdlGrounding"; // direct operation grounding for WSDL 2
    private static final String WSDL_11_OPERATION_VAR = "wsdlOperation"; // indirect operation grounding for WSDL 11
    private static final String WSDL_11_PORT_VAR = "wsdlPortType"; // indirect operation grounding for WSDL 11

    private static final String MESSAGE_PART_VAR = "messagePart";
    private static final String MESSAGE_PART_TYPE_VAR = MESSAGE_PART_VAR + "Type";
    private static final String WSDL_MESSAGE_VAR = "wsdlMessage";
    private static final String MAP_VAR = "map";
    private static final String WSDL_PART_VAR = "wsdlPart";
    private static final String TRANSFORM_VAR = "transform";

    private static final String TYPE_OF_MESSAGE_VAR = "messageType";
    private static final String INPUT_PARAMETER_VAR = "inputParameter";
    private static final String INPUT_PARAMETER_TYPE_VAR = INPUT_PARAMETER_VAR + "Type";
    private static final String INPUT_MESSAGE_VAR = "inputMessage";
    private static final String INPUT_MAP_VAR = "inputMap";
    private static final String INPUT_TRANSFORM_VAR = "inputTransform";
    private static final String OUTPUT_PARAMETER_VAR = "outputParameter";
    private static final String OUTPUT_PARAMETER_TYPE_VAR = OUTPUT_PARAMETER_VAR + "Type";
    private static final String OUTPUT_MESSAGE_VAR = "outputMessage";
    private static final String OUTPUT_MAP_VAR = "outputMap";
    private static final String OUTPUT_PART_VAR = "outputPart";
    private static final String OUTPUT_TRANSFORM_VAR = "outputTransform";
    private static final String INPUT_PART_VAR = "inputPart";
    private static final String INPUT_PARAMETER2_VAR = INPUT_PARAMETER_VAR + "2";
    private static final String INPUT_PARAMETER2_TYPE_VAR = INPUT_PARAMETER_TYPE_VAR + "2";
    private static final String OUTPUT_PARAMETER2_VAR = OUTPUT_PARAMETER_VAR + "2";
    private static final String OUTPUT_PARAMETER2_TYPE_VAR = OUTPUT_PARAMETER_TYPE_VAR + "2";


    // TODO: Use the constants automatically generated for resources
    private static final String QUERY_GLOBAL =
            "SELECT DISTINCT * WHERE {\n"
                    + "  ?" + SERVICE_VAR + " rdf:type service:Service .\n"
                    + "  ?" + SERVICE_VAR + " service:presents ?" + PROFILE_VAR + " .\n"
                    + "  OPTIONAL { \n"
                    + "    ?" + SERVICE_VAR + " service:describedBy ?" + PROCESS_MODEL_VAR + " .\n"
                    + "    ?" + PROCESS_MODEL_VAR + " process:hasProcess ?" + PROCESS_VAR + " .\n"
                    + "  }\n"
                    + "  OPTIONAL { ?" + SERVICE_VAR + " service:describedBy ?" + PROCESS_VAR + " . }\n"
                    + "  OPTIONAL { \n"
                    + "    ?" + PROCESS_VAR + " process:hasPrecondition ?" + CONDITION_VAR + " . \n"
                    + "    OPTIONAL { ?" + CONDITION_VAR + " rdfs:label ?" + CONDITION_LABEL_VAR + " . }\n"
                    + "    ?" + CONDITION_VAR + " expr:expressionBody ?" + CONDITION_EXPR_BODY_VAR + " .\n"
                    + "    OPTIONAL {\n"
                    + "       ?" + CONDITION_VAR + " expr:expressionLanguage ?" + CONDITION_EXPR_LANG_VAR + " .\n"
                    + "    }\n"
                    + "    OPTIONAL {\n"
                    + "       ?" + CONDITION_VAR + " <http://127.0.0.1/ontology/Expression.owl#expressionLanguage> ?" + CONDITION_EXPR_LANG_VAR + " .\n"            // Hack for OWL-S TC4
                    + "    }\n"
                    + "  }\n"
                    + "  OPTIONAL {?" + PROFILE_VAR + " profile:serviceClassification ?" + CLASSIFICATION_VAR + " .}\n"
                    + "  OPTIONAL {?" + PROFILE_VAR + " profile:serviceProduct ?" + SVC_PRODUCT_VAR + " .}\n"
                    + "  OPTIONAL {\n"
                    + "    ?" + PROCESS_VAR + " process:hasResult ?" + RESULT_VAR + " .\n"
                    + "    OPTIONAL {\n"
                    + "      ?" + RESULT_VAR + " process:hasResultVar ?" + RESULT_VAR_VAR + " . \n"
                    + "      ?" + RESULT_VAR_VAR + " process:parameterType ?" + RESULT_VAR_TYPE_VAR + " . "
                    + "    }\n"
                    + "    OPTIONAL {\n"
                    + "      ?" + RESULT_VAR + " process:inCondition ?" + IN_CONDITION_VAR + ".\n"
                    + "      ?" + IN_CONDITION_VAR + " expr:expressionBody ?" + IN_CONDITION_EXPR_BODY_VAR + " .\n"
                    + "      OPTIONAL {\n"
                    + "         ?" + IN_CONDITION_VAR + " expr:expressionLanguage ?" + IN_CONDITION_EXPR_LANG_VAR + " .\n"
                    + "      }\n"
                    + "      OPTIONAL {\n"
                    + "         ?" + IN_CONDITION_VAR + " <http://127.0.0.1/ontology/Expression.owl#expressionLanguage> ?" + IN_CONDITION_EXPR_LANG_VAR + " .\n"    // Hack for OWL-S TC4
                    + "      }\n"
                    + "    }\n"
                    + "    ?" + RESULT_VAR + " process:hasEffect ?" + EFFECT_VAR + " .\n"
                    + "    ?" + EFFECT_VAR + " expr:expressionBody ?" + EFFECT_EXPR_BODY_VAR + " .\n"
                    + "    OPTIONAL {\n"
                    + "         ?" + EFFECT_VAR + " expr:expressionLanguage ?" + EFFECT_EXPR_LANG_VAR + " .\n"
                    + "    }\n"
                    + "    OPTIONAL {\n"
                    + "         ?" + EFFECT_VAR + " <http://127.0.0.1/ontology/Expression.owl#expressionLanguage> ?" + EFFECT_EXPR_LANG_VAR + " .\n"                // Hack for OWL-S TC4
                    + "    }\n"
                    + "  }\n"
                    + "  OPTIONAL {\n"
                    + "   ?" + PROCESS_GROUNDING_VAR + " grounding:owlsProcess ?" + PROCESS_VAR + " .\n"
                    + "   ?" + PROCESS_GROUNDING_VAR + " grounding:wsdlDocument ?" + WSDL_DOC_VAR + " .\n"
                    + "   ?" + PROCESS_GROUNDING_VAR + " grounding:wsdlOperation ?" + WSDL_GROUNDING_VAR + " .\n"
                    + "   ?" + WSDL_GROUNDING_VAR + " grounding:operation ?" + WSDL_11_OPERATION_VAR + " .\n"
                    + "   ?" + WSDL_GROUNDING_VAR + " grounding:portType ?" + WSDL_11_PORT_VAR + " .\n"
                    + "  }\n"
                    + "  OPTIONAL {\n"
                    + "   ?" + PROCESS_GROUNDING_VAR + " grounding:wsdlInputMessage ?" + INPUT_MESSAGE_VAR + " .\n"
                    + "  }\n"
                    + "  OPTIONAL {\n"
                    + "   ?" + PROCESS_GROUNDING_VAR + " grounding:wsdlOutputMessage ?" + OUTPUT_MESSAGE_VAR + " .\n"
                    + "  }\n"
                    + "}";

    private static final String QUERY_IO =
            "SELECT DISTINCT * WHERE {\n"
                    + "  {\n"
                    + "   ?" + PROCESS_GROUNDING_VAR + " grounding:wsdlInputMessage ?" + WSDL_MESSAGE_VAR + " .\n"
                    + "   ?" + PROCESS_GROUNDING_VAR + " grounding:wsdlInput ?" + MAP_VAR + " .\n"
                    + "   BIND (\"input\" AS ?" + TYPE_OF_MESSAGE_VAR + ") .\n"
                    + "  }\n"
                    + " UNION \n"
                    + "  {\n"
                    + "   ?" + PROCESS_GROUNDING_VAR + " grounding:wsdlOutputMessage ?" + WSDL_MESSAGE_VAR + " .\n"
                    + "   ?" + PROCESS_GROUNDING_VAR + " grounding:wsdlOutput ?" + MAP_VAR + " .\n"
                    + "   BIND (\"output\" AS ?" + TYPE_OF_MESSAGE_VAR + ") .\n"
                    + "  }\n"
                    + "   ?" + MAP_VAR + " grounding:wsdlMessagePart ?" + WSDL_PART_VAR + " .\n"
                    + "   ?" + MAP_VAR + " grounding:owlsParameter ?" + MESSAGE_PART_VAR + " .\n"
                    + "   ?" + MESSAGE_PART_VAR + " process:parameterType ?" + MESSAGE_PART_TYPE_VAR + " .\n"
                    + "   OPTIONAL {?" + MAP_VAR + " grounding:xsltTransformationString ?" + TRANSFORM_VAR + " . }\n"
                    + "}";

    // Include information about the software version
    private static final String VERSION_PROP_FILE = "plugin.properties";
    private static final String VERSION_PROP = "version";
    private static final String VERSION_UNKNOWN = "Unknown";
    private static final String OWL_S_11_SERVICE = "http://www.daml.org/services/owl-s/1.1/Service.owl#";
    private static final String OWL_S_11_PROFILE = "http://www.daml.org/services/owl-s/1.1/Profile.owl#";
    private static final String OWL_S_11_PROCESS = "http://www.daml.org/services/owl-s/1.1/Process.owl#";
    private static final String OWL_S_11_GROUNDING = "http://www.daml.org/services/owl-s/1.1/Grounding.owl#";
    private static final String OWL_S_11_EXPRESSION = "http://www.daml.org/services/owl-s/1.1/generic/Expression.owl#";

    // Injection of the version
    private
    @Inject(optional = true)
    @Named("version")
    String version = VERSION_UNKNOWN;

    // Supported Media Type
    public static String mediaType = "application/owl+xml";

    // Supported File Extensions
    private static List<String> fileExtensions = new ArrayList<String>();

    static {
        fileExtensions.add("owls");
        fileExtensions.add("owl");
    }

    private Map<String, String> prefixes;

    // Default Ont Model Specification to use when loading and transforming services
    private OntModelSpec modelSpec;


    public OwlsTransformer() {
        // Initialise prefixes
        prefixes = new HashMap<String, String>(Vocabularies.prefixes);
        // Add those specific for handling OWLS
        prefixes.put("service", OWL_S_11_SERVICE);
        prefixes.put("profile", OWL_S_11_PROFILE);
        prefixes.put("process", OWL_S_11_PROCESS);
        prefixes.put("grounding", OWL_S_11_GROUNDING);
        prefixes.put("expr", OWL_S_11_EXPRESSION);

        setupModelSpecification();

        if (version == null) {
            obtainVersionInformation();
        }
    }


    /**
     * Setup a the OntModelSpec to be used when parsing services.
     * In particular, setup import redirections, etc.
     *
     * @return
     */
    private void setupModelSpecification() {

        OntDocumentManager documentManager = new OntDocumentManager();

        // Add mapping specific to OWLS TC 4 tests.
        // Add mappings to local files to save time and avoid connection issues
        documentManager.addAltEntry("http://127.0.0.1/ontology/PDDLExpression.owl", this.getClass().getResource("/PDDLExpression.owl").toString());

        // Ignore all imports altogether for speed
        documentManager.setProcessImports(false);

        this.modelSpec = new OntModelSpec(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
        this.modelSpec.setDocumentManager(documentManager);
    }

    private void obtainVersionInformation() {
        log.info("Loading version information from {}", VERSION_PROP_FILE);
        PropertiesConfiguration config = null;
        try {
            config = new PropertiesConfiguration(VERSION_PROP_FILE);
            this.version = config.getString(VERSION_PROP, VERSION_UNKNOWN);
        } catch (ConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * Obtains the Media Type this plugin supports for transformation.
     * Although registered Media Types do not necessarily identify uniquely
     * a given semantic service description format it is used for identification
     * for now.
     *
     * @return the media type covered
     */
    @Override
    public String getSupportedMediaType() {
        return mediaType;
    }

    /**
     * Obtains the different file extensions that this plugin can be applied to.
     * Again this does not necessarily uniquely identify a format but helps filter the filesystem
     * when doing batch transformation and may also be used as final solution in cases where we fail
     * to identify a format.
     *
     * @return a List of file extensions supported
     */
    @Override
    public List<String> getSupportedFileExtensions() {
        return fileExtensions;
    }

    /**
     * Obtains the version of the plugin used. Relevant as this is based on plugins and 3rd party may
     * provide their own implementations. Having the version is useful for provenance information and debuggin
     * purposes.
     *
     * @return a String with the version of the plugin.
     */
    @Override
    public String getVersion() {
        return this.version;
    }

    /**
     * Parses and transforms a stream with service description(s), e.g. SAWSDL, OWL-S, hRESTS, etc., and
     * returns a list of Service objects defined in the stream.
     *
     * @param originalDescription The semantic Web service description(s)
     * @param baseUri             The base URI to use while transforming the service description
     * @return A List with the services transformed conforming to MSM model
     */
    @Override
    public List<Service> transform(InputStream originalDescription, String baseUri) throws TransformationException {
        // read the file
        // TODO: figure out the syntax?

        // Register the expression types for conditions
        RDFDatatype rtype = PddlType.TYPE;
        TypeMapper.getInstance().registerDatatype(rtype);

        OntModel model = ModelFactory.createOntologyModel(this.modelSpec);

        // Read also the PDDL file to obtain the right Literal Type with OWL-S TC4
        model.read("http://127.0.0.1/ontology/PDDLExpression.owl");
        model.read(originalDescription, baseUri);

        // Transform the original model (may have several service definitions)
        List<Service> services = transform(model, baseUri);

        return services;
    }

    private List<Service> transform(Model model, String baseUri) {

        List<Service> result = new ArrayList<Service>();
        // Exit early if empty
        if (model == null) {
            return result;
        }

        // Query the model to obtain only services
        Query query = QueryFactory.create();
        query.setPrefixMapping(query.getPrefixMapping().setNsPrefixes(prefixes));
        QueryFactory.parse(query, QUERY_GLOBAL, baseUri, Syntax.syntaxSPARQL_11);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);

        log.debug("Querying model:");
        log.debug(query.serialize());

        try {
            ResultSet qResults = qexec.execSelect();

            // Process the services found and generate the instances
            QuerySolution soln;
            Service svc;
            while (qResults.hasNext()) {
                soln = qResults.nextSolution();
                try {
                    svc = obtainService(model, soln);
                    if (svc != null) {
                        result.add(svc);
                    }
                } catch (URISyntaxException e) {
                    log.error("Incorrect URL while transforming OWL-S service", e);
                }
            }

            // Return the result
            return result;
        } finally {
            if (qexec != null) {
                qexec.close();
            }
        }
    }

    /**
     * Given a query solution obtained by querying the model for OWL-S services
     * generates a Service instance capturing the information.
     * See constants for the variable names used.
     * <p/>
     * TODO: This needs urgent restructuring
     *
     * @param model
     * @param querySolution query solution holding the service details
     * @return an instance of Service duly filled or null if none could be found
     */
    private Service obtainService(Model model, QuerySolution querySolution) throws URISyntaxException {

        Service result = null;
        // Exit early if null
        if (model == null || querySolution == null) {
            return result;
        }

        Resource res;

        // Create Svc
        URI svcUri = new URI(querySolution.getResource(SERVICE_VAR).getURI());
        // TODO: decide how to handle the URIs to be replaced when uploaded
        result = new Service(svcUri);
        result.setSource(svcUri); // Redundant here but not after the URL changes
        result.setComment("Automatically transformed by OWL-S Importer v" + this.getVersion());
        // TODO: set label
        //result.setLabel();
        //TODO: set creator
        //result.setCreator();

        // Add the grounding doc
        Literal lit = querySolution.getLiteral(WSDL_DOC_VAR);
        if (lit != null) {
            result.setWsdlGrounding(new URI(lit.getString()));
        }

        // Add model references
        res = querySolution.getResource(CLASSIFICATION_VAR);
        if (res != null) {
            URI fcUri = new URI(res.getURI());
            result.addModelReference(new uk.ac.open.kmi.iserve.commons.model.Resource(fcUri));
        }

        res = querySolution.getResource(SVC_PRODUCT_VAR);
        if (res != null) {
            URI fcUri = new URI(res.getURI());
            result.addModelReference(new uk.ac.open.kmi.iserve.commons.model.Resource(fcUri));
        }

        // Process Operations
        Operation op = obtainOperation(model, querySolution);
        if (op != null)
            result.addOperation(op);

        return result;
    }

    // TODO: This assumes there is just one operation
    private Operation obtainOperation(Model model, QuerySolution querySolution) throws URISyntaxException {

        Resource res;
        Literal lit;
        Operation result = null;

        // Obtain the operation and exit early if none is available
        res = querySolution.getResource(PROCESS_VAR);
        if (res == null) {
            return result;
        }

        result = new Operation(new URI(res.getURI()));
        result.setSource(result.getUri()); // Redundant here but not after the URL changes

        // Process conditions
        Condition cond = obtainCondition(model, querySolution);
        if (cond != null)
            result.addCondition(cond);

        // Process effects
        Effect effect = obtainEffect(model, querySolution);
        if (effect != null)
            result.addEffect(effect);

        // Define a default node for the top level Input Message Content
        MessageContent inputMc = new MessageContent(new URI(result.getUri().toASCIIString() + "_Input"));
        result.addInput(inputMc);
        // Add the grounding
        lit = querySolution.getLiteral(INPUT_MESSAGE_VAR);
        if (lit != null) {
            inputMc.setWsdlGrounding(new URI(lit.getString()));
        }

        // Define a default node for the top level Output Message Content
        MessageContent outputMc = new MessageContent(new URI(result.getUri().toASCIIString() + "_Output"));
        result.addOutput(outputMc);
        // Add the grounding
        lit = querySolution.getLiteral(OUTPUT_MESSAGE_VAR);
        if (lit != null) {
            outputMc.setWsdlGrounding(new URI(lit.getString()));
        }

        populateMessageParts(model, inputMc, outputMc);
        return result;
    }

    private void populateMessageParts(Model model, MessageContent inputMc, MessageContent outputMc) {

        // Query the model
        Query query = QueryFactory.create();
        query.setPrefixMapping(query.getPrefixMapping().setNsPrefixes(prefixes));
        QueryFactory.parse(query, QUERY_IO, null, Syntax.syntaxSPARQL_11);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);

        log.debug("Querying model:");
        log.debug(query.serialize());

        try {
            MessagePart mp = null;
            QuerySolution solution;
            ResultSet qResults = qexec.execSelect();
            while (qResults.hasNext()) {
                solution = qResults.nextSolution();
                mp = obtainMessagePart(solution);
                if (mp != null) {
                    Literal lit = solution.getLiteral(TYPE_OF_MESSAGE_VAR);
                    if (lit != null) {
                        if (lit.getString().equals("input")) {
                            inputMc.addMandatoryPart(mp);
                        } else {
                            outputMc.addMandatoryPart(mp);
                        }
                    }
                }
            }

        } finally {
            if (qexec != null) {
                qexec.close();
            }
        }
    }

    private MessagePart obtainMessagePart(QuerySolution querySolution) {

        MessagePart result = null;

        Resource res = querySolution.getResource(MESSAGE_PART_VAR);
        if (res != null) {
            URI mpUri;
            try {
                mpUri = new URI(res.getURI());
                result = new MessagePart(mpUri);

                Literal lit = querySolution.getLiteral(WSDL_PART_VAR);
                if (lit != null) {
                    URI wsdlGrounding = new URI(lit.getString());
                    result.setWsdlGrounding(wsdlGrounding);
                    // Get the type
                    lit = querySolution.getLiteral(MESSAGE_PART_TYPE_VAR);
                    if (lit != null) {
                        URI typeUri = new URI(lit.getString());
                        result.addModelReference(new uk.ac.open.kmi.iserve.commons.model.Resource(typeUri));
                    }
                }
            } catch (URISyntaxException e) {
                log.error("Incorrect URI specified while parsing Message Content", e);
            }
        }
        return result;
    }


    private LogicalAxiom obtainAxiom(Model model, QuerySolution querySolution, LogicalAxiom.Type type) {

        LogicalAxiom result = null;
        Resource res;
        // Get the appropriate resource
        if (type == LogicalAxiom.Type.CONDITION) {
            res = querySolution.getResource(CONDITION_VAR);
        } else {
            res = querySolution.getResource(EFFECT_VAR);
        }

        if (res != null) {
            URI axiomUri = null;
            try {
                if (!res.isAnon()) {
                    axiomUri = new URI(res.getURI());
                }

                // TODO: earlier version used to deal with labels on conditions. Keep this?

                // Set the axiom
                Literal body;
                Resource lang;
                if (type == LogicalAxiom.Type.CONDITION) {
                    result = new Condition(axiomUri);
                    body = querySolution.getLiteral(CONDITION_EXPR_BODY_VAR);
                    lang = querySolution.getResource(CONDITION_EXPR_LANG_VAR);
                } else {
                    result = new Effect(axiomUri);
                    body = querySolution.getLiteral(EFFECT_EXPR_BODY_VAR);
                    lang = querySolution.getResource(EFFECT_EXPR_LANG_VAR);
                }

                // Clean the body
                String expression = body.getString().trim();
                expression = expression.replaceAll("[\n\t]", "");

                Literal exprLiteral;
                if (lang != null) {
                    exprLiteral = model.createTypedLiteral(expression, TypeMapper.getInstance().getTypeByName(lang.getURI()));
                } else {
                    exprLiteral = model.createLiteral(expression, null);
                }

                result.setTypedValue(exprLiteral.getValue());

            } catch (URISyntaxException e) {
                log.error("Incorrect URI specified for Axiom", e);
            }
        }
        return result;

    }

    private Effect obtainEffect(Model model, QuerySolution querySolution) {
        return (Effect) obtainAxiom(model, querySolution, LogicalAxiom.Type.EFFECT);
    }

    private Condition obtainCondition(Model model, QuerySolution querySolution) {
        return (Condition) obtainAxiom(model, querySolution, LogicalAxiom.Type.CONDITION);
    }

    public static void main(String[] args) {

        Options options = new Options();
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        // create the parser
        CommandLineParser parser = new GnuParser();

        options.addOption("h", "help", false, "print this message");
        options.addOption("i", "input", true, "input directory or file to transform");
        options.addOption("s", "save", false, "save result? (default: false)");
        options.addOption("f", "format", true, "format to use when serialising. Default: TTL.");

        // parse the command line arguments
        CommandLine line = null;
        String input = null;
        File inputFile;
        try {
            line = parser.parse(options, args);
            input = line.getOptionValue("i");
            if (line.hasOption("help")) {
                formatter.printHelp("OwlsImporter", options);
                return;
            }
            if (input == null) {
                // The input should not be null
                formatter.printHelp(80, "java " + OwlsTransformer.class.getCanonicalName(), "Options:", options, "You must provide an input", true);
                return;
            }
        } catch (ParseException e) {
            formatter.printHelp(80, "java " + OwlsTransformer.class.getCanonicalName(), "Options:", options, "Error parsing input", true);
        }

        inputFile = new File(input);
        if (inputFile == null || !inputFile.exists()) {
            formatter.printHelp(80, "java " + OwlsTransformer.class.getCanonicalName(), "Options:", options, "Input not found", true);
            System.out.println(inputFile.getAbsolutePath());
            return;
        }

        // Obtain input for transformation
        File[] toTransform;
        if (inputFile.isDirectory()) {
            // Get potential files to transform based on extension
            FilenameFilter owlsFilter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.endsWith(".owl") || name.endsWith(".owls"));
                }
            };

            toTransform = inputFile.listFiles(owlsFilter);
        } else {
            toTransform = new File[1];
            toTransform[0] = inputFile;
        }

        // Obtain details for saving results
        File rdfDir = null;
        boolean save = line.hasOption("s");
        if (save) {
            rdfDir = new File(inputFile.getAbsolutePath() + "/RDF");
            rdfDir.mkdir();
        }

        // Obtain details for serialisation format
        String format = line.getOptionValue("f", uk.ac.open.kmi.iserve.commons.io.Syntax.TTL.getName());
        uk.ac.open.kmi.iserve.commons.io.Syntax syntax = uk.ac.open.kmi.iserve.commons.io.Syntax.valueOf(format);

        OwlsTransformer importer;
        ServiceWriter writer;

        importer = new OwlsTransformer();
        writer = new ServiceWriterImpl();

        List<Service> services;
        log.info("Launching a batch transformation of OWL-S services, parameters {}", line);
        for (File file : toTransform) {
            log.info("Transforming file {}", file.getAbsolutePath());
            try {
                services = importer.transform(new FileInputStream(file), null);

                if (services != null) {
                    log.info("Services obtained {}", services);

                    File resultFile = null;
                    if (rdfDir != null) {
                        String fileName = file.getName();
                        String newFileName = fileName.substring(0, fileName.length() - 4) + syntax.getExtension();
                        resultFile = new File(rdfDir.getAbsolutePath() + "/" + newFileName);
                    }

                    if (rdfDir != null) {
                        OutputStream out = null;
                        try {
                            out = new FileOutputStream(resultFile);
                            for (Service service : services) {
                                if (out != null) {
                                    writer.serialise(service, out, syntax);
                                    log.info("Service saved at {}", resultFile.getAbsolutePath());
                                } else {
                                    writer.serialise(service, System.out, syntax);
                                }
                            }
                        } finally {
                            if (out != null)
                                out.close();
                        }
                    }

                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TransformationException e) {
                e.printStackTrace();
            }
        }
    }
}
