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
package uk.ac.open.kmi.iserve2.discovery.engine.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import uk.ac.open.kmi.iserve2.discovery.api.Result;

public class AtomUtil {

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	
	static {
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

    /**
     * generates an atom feed in a string
     * @param feedURI the URI at which this feed resides, it will be used both for the feed's ID and a link to itself
     * @param title feed title
     * @param generator a name of the component that generates this feed, e.g. iServe RDFS Functional Disco API
     * @param generatorURI URI for the website of the generator (e.g. iServe Disco API, or just simply iServe)
     * @param updated the date when the feed was last updated (usually right now - new Date())
     * @param serviceEntries a string that should contain all the atom entries for this feed - this is added just before the &lt;/feed&gt;
     * @return a string with the XML of the generated feed
     */
    public static String generateAtomFeed(String feedURI, String title, String generator, String generatorURI, Date updated,
            String serviceEntries) {
        String result = "<feed xmlns='http://www.w3.org/2005/Atom' xmlns:iserve='http://iserve.kmi.open.ac.uk/xmlns/'>\n"  // todo spec the iserve xml namespace
        	+ "  <id>" + xmlEncode(feedURI) + "</id>\n" 
        	+ "  <link rel='self' href='" + xmlAttEncode(feedURI) + "'/>\n" 
        	+ "  <generator uri='"+xmlAttEncode(generatorURI)+"'>" + xmlEncode(generator) + "</generator>\n"
        	+ "  <title>" + xmlEncode(title) + "</title>\n"
            + "  <updated>" + sdf.format(updated) + "</updated>\n"
            + serviceEntries
            + "</feed>\n";
        return result;
    }

	/**
	 * encode string for XML element value
	 * @param src string
	 * @return encoded string
	 */
	public static String xmlEncode(String src) {
	    if (src == null) {
	        return "";
	    }
		return src.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	/**
	 * encode string for an XML attribute value
	 * @param src string
	 * @return encoded string
	 */
	public static String xmlAttEncode(String src) {
		return xmlEncode(src).replace("'", "&apos;").replace("\"", "&quot;");
	}

    /**
     * generates an atom entry in a string
     * @param entryID a URI that identifies that entry (in iServe, it's the service identifier, the URI that identifies an instance of msm:Service)
     * @param title the title of the entry (e.g. the service label)
     * @param updated a date when the entry was last updated (can be now - new Date() but it means feed readers will always show this as updated)
     * @param matchDegreeNumber iServe-discovery-specific match degree - should be a number, the lower the better - 0 is perfect match; can be decimal, e.g. 3.5 - may be null then no iserve matchDegree element will be present
     * @param matchDegreeName the name of the match (e.g. exact, plugin etc.)
     * @param entryContent descriptive string to go in the entry content - can be null
     * @return a string with the XML of the generated entry
     */
    private static String generateAtomFeedEntry(String entryID, String title, Date updated, String matchDegreeNumber,
            String matchDegreeName, String entryContent) {
        String retval = "  <entry>\n"
            + "    <title>" + xmlEncode(title) + "</title>\n"
            + "    <link rel='alternate' href='" + xmlAttEncode(entryID) + "'/>\n"
            + "    <id>" + xmlEncode(entryID) + "</id>\n"
            + "    <author><name/></author>\n" // todo add submitter of service description, xml-encoded
            + "    <updated>" + sdf.format(updated) + "</updated>\n";
        if (matchDegreeNumber != null) {retval += "    <iserve:matchDegree num='" + xmlAttEncode(matchDegreeNumber) + "'>" + xmlEncode(matchDegreeName) + "</iserve:matchDegree>\n";}
        if (entryContent != null ) {retval += "    <content>" + xmlEncode(entryContent) + "</content>\n";}
        retval += "  </entry>\n";
        return retval;
    }

    public static String generateAtomFeedEntry(Result matchingResult) {
    	return generateAtomFeedEntry(matchingResult.getUri(), matchingResult.getLabel(), new Date(), "" + matchingResult.getMatchingDegree(),
    			matchingResult.getDegreeName(), matchingResult.getContent());
    }

}
