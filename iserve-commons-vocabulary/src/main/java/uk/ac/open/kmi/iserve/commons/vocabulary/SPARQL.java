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
import org.ontoware.rdf2go.vocabulary.RDF;
import org.ontoware.rdf2go.vocabulary.RDFS;

/**
 * SPARQL Help constants
 */
public class SPARQL {

	public static final String PREFIX_RDF = "PREFIX rdf:<" + RDF.RDF_NS + ">";

	public static final String PREFIX_RDFS = "PREFIX rdfs:<" + RDFS.RDFS_NS + ">";

	public static final String PREFIX_SAWSDL = "PREFIX " + MSM.SAWSDL_PREFIX + ":<" + MSM.SAWSDL_NS_URI +">";

	public static final String PREFIX_DC = "PREFIX " + DC.NS_PREFIX + ":<" + DC.NS_URI +">";

	public static final String PREFIX_MSM = "PREFIX " + MSM.NS_PREFIX + ":<" + MSM.NS_URI +">";

	public static final String PREFIX_WL = "PREFIX " + MSM.WL_NS_PREFIX + ":<" + MSM.WL_NS_URI +">";
	
	public static final String PREFIX_FOAF = "PREFIX " + FOAF.FOAF_NS_PREFIX + ":<" + FOAF.FOAF_NS_URI +">";

}
