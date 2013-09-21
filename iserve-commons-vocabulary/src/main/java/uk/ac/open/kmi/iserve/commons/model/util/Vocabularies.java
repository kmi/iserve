package uk.ac.open.kmi.iserve.commons.model.util;

import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import uk.ac.open.kmi.iserve.commons.vocabulary.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class holding the prefixes of the vocabularies.
 * Placed in here for now but will require rearranging
 * <p/>
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 31/05/2013
 * Time: 11:41
 */
public class Vocabularies {

    public static final Map<String, String> prefixes;

    static {
        HashMap<String, String> aMap = new HashMap<String, String>();
        aMap.put("rdf", RDF.getURI());
        aMap.put("rdfs", RDFS.getURI());
        aMap.put("msm", MSM.NS);
        aMap.put("sawsdl", SAWSDL.NS);
        aMap.put("wl", WSMO_LITE.NS);
        aMap.put("hr", HRESTS.NS);
        aMap.put("msm-wsdl", MSM_WSDL.NS);
        aMap.put("foaf", FOAF.NS);
        aMap.put("dcterms", DC.NS);
        prefixes = Collections.unmodifiableMap(aMap);
    }

}
