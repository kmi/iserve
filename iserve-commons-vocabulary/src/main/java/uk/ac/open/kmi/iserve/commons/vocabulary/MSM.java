/*
   Copyright ${year}  Knowledge Media Institute - The Open University

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package uk.ac.open.kmi.iserve.commons.vocabulary;

import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.URIImpl;

/**
 * Minimal Service Model (MSM) Vocabulary
 * @author Dong LIU (OU)
 */
public class MSM {

	// Name spaces
	public static final String NS_URI = "http://cms-wg.sti2.org/ns/minimal-service-model#";
	public static final String NS_PREFIX = "msm";

	public static final String WL_NS_URI = "http://www.wsmo.org/ns/wsmo-lite#";
	public static final String WL_NS_PREFIX = "wl";

	public static final String SAWSDL_NS_URI = "http://www.w3.org/ns/sawsdl#";
	public static final String SAWSDL_PREFIX = "sawsdl";

	public static final String HRESTS_NS_URI = "http://www.wsmo.org/ns/hrests#";
	public static final String HRESTS_PREFIX = "hrest";

	// Classes
	public static final String SERVICE = NS_URI + "Service";
	public static final URI Service = new URIImpl(SERVICE);
	public static final String OPERATION = NS_URI + "Operation";
	public static final URI Operation = new URIImpl(OPERATION);
	public static final String MESSAGE = NS_URI + "MessageContent";
	public static final URI Message = new URIImpl(MESSAGE);
	public static final String MESSAGE_PART = NS_URI + "MessagePart";
	public static final URI MessagePart = new URIImpl(MESSAGE_PART);

	public static final String ONTOLOGY = WL_NS_URI + "Ontology";
	public static final URI Ontology = new URIImpl(ONTOLOGY);

	public static final String FUNCTIONAL_CLASSIFICATION_ROOT = WL_NS_URI + "FunctionalClassificationRoot";
	public static final URI FunctionalClassificationRoot = new URIImpl(FUNCTIONAL_CLASSIFICATION_ROOT);
	public static final String NONFUNCTIONAL_PARAMETER = WL_NS_URI + "NonfunctionalParameter";
	public static final URI NonfunctionalParameter = new URIImpl(NONFUNCTIONAL_PARAMETER);
	public static final String CONDITION = WL_NS_URI + "Condition";
	public static final URI Condition = new URIImpl(CONDITION);
	public static final String EFFECT = WL_NS_URI + "Effect";
	public static final URI Effect = new URIImpl(EFFECT);

	// Properties
	public static final String HAS_OPERATION = NS_URI + "hasOperation";
	public static final URI hasOperation = new URIImpl(HAS_OPERATION);

	public static final String HAS_INPUT_MESSAGE = NS_URI + "hasInput";
	public static final URI hasInput = new URIImpl(HAS_INPUT_MESSAGE);
	public static final String HAS_OUTPUT_MESSAGE = NS_URI + "hasOutput";
	public static final URI hasOutput = new URIImpl(HAS_OUTPUT_MESSAGE);

	public static final String HAS_INPUT_FAULT = NS_URI + "hasInputFault";
	public static final URI hasInputFault = new URIImpl(HAS_INPUT_FAULT);
	public static final String HAS_OUTPUT_FAULT = NS_URI + "hasOutputFault";
	public static final URI hasOutputFault = new URIImpl(HAS_OUTPUT_FAULT);

	public static final String USES_ONTOLOGY = WL_NS_URI + "usesOntology";
	public static final URI usesOntology = new URIImpl(USES_ONTOLOGY);

	public static final String HAS_PART = NS_URI + "hasPart";
	public static final URI hasPart = new URIImpl(HAS_PART);

	public static final String HAS_OPTIONAL_PART = NS_URI + "hasOptionalPart";
	public static final URI hasOptionalPart = new URIImpl(HAS_OPTIONAL_PART);

	public static final String HAS_MANDATORY_PART = NS_URI + "hasMandatoryPart";
	public static final URI hasMandatoryPart = new URIImpl(HAS_MANDATORY_PART);

	public static final String HAS_PART_TRANSITIVE = NS_URI + "hasPartTransitive";
	public static final URI hasPartTransitive = new URIImpl(HAS_PART_TRANSITIVE);

	public static final String HAS_ADDRESS = HRESTS_NS_URI + "hasAddress";
	public static final URI hasAddress = new URIImpl(HAS_ADDRESS);

	// SAWSDL properties
	public static final String MODEL_REFERENCE = SAWSDL_NS_URI + "modelReference";
	public static final URI modelReference = new URIImpl(MODEL_REFERENCE);
	public static final String LOWERING_SCHEMA_MAPPING = SAWSDL_NS_URI + "loweringSchemaMapping";
	public static final URI loweringSchemaMapping = new URIImpl(LOWERING_SCHEMA_MAPPING);
	public static final String LIFTING_SCHEMA_MAPPING = SAWSDL_NS_URI + "liftingSchemaMapping";
	public static final URI liftingSchemaMapping = new URIImpl(LIFTING_SCHEMA_MAPPING);

	public static final String DOCUMENT_INFIX = "resource/documents/";
	public static final String SERVICE_INFIX = "resource/services/";

}
