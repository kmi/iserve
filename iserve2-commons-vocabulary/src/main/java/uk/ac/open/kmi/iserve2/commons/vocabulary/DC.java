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
package uk.ac.open.kmi.iserve2.commons.vocabulary;

import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.URIImpl;

public class DC {

	public static final String NS_URI = "http://purl.org/dc/elements/1.1/";
	public static final String NS_PREFIX = "dc";

	public static final String CREATOR = NS_URI + "creator";
	public static final URI creator = new URIImpl(CREATOR);

	public static final String SOURCE = NS_URI + "source";
	public static final URI source = new URIImpl(SOURCE);

}
