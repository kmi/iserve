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
package uk.ac.open.kmi.iserve.importer.sawsdl.schema;

import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.node.URI;
import org.w3c.dom.Node;

import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.importer.sawsdl.util.ModelReferenceExtractor;

public class SimpleType extends Type {

	public SimpleType(Node node, String baseUriString) {
		super(node, baseUriString);
	}

	public void toRdf(Model tempModel, URI partUri) {
		ModelReferenceExtractor.processModelRefString(getModelReference(), tempModel, partUri);
		ModelReferenceExtractor.processLiLoString(getLoweringSchemaMapping(), tempModel, partUri, MSM.loweringSchemaMapping);
		ModelReferenceExtractor.processLiLoString(getLiftingSchemaMapping(), tempModel, partUri, MSM.liftingSchemaMapping);
	}

}
