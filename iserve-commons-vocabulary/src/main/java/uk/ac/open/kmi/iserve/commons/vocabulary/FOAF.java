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

public class FOAF {

	public static final String FOAF_NS_URI = "http://xmlns.com/foaf/0.1/";
	public static final String FOAF_NS_PREFIX = "foaf";

	public static final String FOAF_OPENID = FOAF_NS_URI + "openid";
	public static final URI foafOpenId = new URIImpl(FOAF_OPENID);

	public static final String FOAF_DOCUMENT = FOAF_NS_URI + "Document";
	public static final String FOAF_SHA1 = FOAF_NS_URI + "sha1";

}
