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
package uk.ac.open.kmi.iserve.importer.sawsdl.schema;

import org.w3c.dom.Node;

import java.net.URISyntaxException;

public class SimpleType extends Type {

    public SimpleType(Node node) throws URISyntaxException {
        super(node);
    }

//	public void toRdf(Model tempModel, URI partUri) {
//		ModelReferenceExtractor.tokenizeModelReferenceString(getModelReference(), tempModel, partUri);
//		ModelReferenceExtractor.tokenizeLiLoString(getLoweringSchemaMapping(), tempModel, partUri, tempModel.createURI(SAWSDL.LOWERING_SCHEMA_MAPPING));
//		ModelReferenceExtractor.tokenizeLiLoString(getLiftingSchemaMapping(), tempModel, partUri, tempModel.createURI(SAWSDL.LIFTING_SCHEMA_MAPPING));
//	}

}
