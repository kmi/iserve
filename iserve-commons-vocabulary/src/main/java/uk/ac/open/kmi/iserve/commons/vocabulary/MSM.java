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


/**
 * Minimal Service Model (MSM) Vocabulary
 * @author Dong LIU (OU)
 */
public class MSM {

	// Namespaces
	public static final String NS = "http://cms-wg.sti2.org/ns/minimal-service-model#";
	public static final String NS_PREFIX = "msm";

	// Classes
	public static final String SERVICE = NS + "Service";
	public static final String OPERATION = NS + "Operation";
	public static final String MESSAGE = NS + "MessageContent";
	public static final String MESSAGE_PART = NS + "MessagePart";

	// Properties
	public static final String HAS_OPERATION = NS + "hasOperation";
	public static final String HAS_INPUT_MESSAGE = NS + "hasInput";
	public static final String HAS_OUTPUT_MESSAGE = NS + "hasOutput";
	public static final String HAS_INPUT_FAULT = NS + "hasInputFault";
	public static final String HAS_OUTPUT_FAULT = NS + "hasOutputFault";
	public static final String HAS_PART = NS + "hasPart";
	public static final String HAS_OPTIONAL_PART = NS + "hasOptionalPart";
	public static final String HAS_MANDATORY_PART = NS + "hasMandatoryPart";
	public static final String HAS_PART_TRANSITIVE = NS + "hasPartTransitive";
}
