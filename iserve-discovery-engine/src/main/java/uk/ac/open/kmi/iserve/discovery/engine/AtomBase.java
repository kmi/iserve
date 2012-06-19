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
package uk.ac.open.kmi.iserve.discovery.engine;

import java.io.StringWriter;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.ApacheHttpClient4Handler;

import uk.ac.open.kmi.iserve.discovery.api.DiscoveryException;
import uk.ac.open.kmi.iserve.discovery.util.DiscoveryUtil;

/**
 * provides base functionality for atom combinators
 * todo relative URIs in the atom feeds are currently not resolved against base
 * @author Jacek Kopecky
 *
 */
public abstract class AtomBase {

    /**
     * <p>HTTP header for sending authentication credentials.</p>
     */
    private static final String AUTHENTICATION_HEADER = "Authorization";

    /**
     * <p>HTTP authentication header value to include on each request.</p>
     */
    private String authentication = null;

	private Client httpClient;

    /**
     * <p>Default media type for content element.</p>
     */
    private static final MediaType CONTENT_MEDIA_TYPE = MediaType.APPLICATION_XML_TYPE;

    // -------------------------------------------------------- Static Variables
    /**
     * <p>Default media type for entry entities.</p>
     */
    private static final MediaType ENTRY_MEDIA_TYPE;
    static {
        Map<String,String> params = new HashMap<String,String>(1);
        params.put("type", "entry");
        ENTRY_MEDIA_TYPE = new MediaType("application", "xml", params);
    }

    /**
     * <p>Default media type for feed entities.</p>
     */
    private static final MediaType FEED_MEDIA_TYPE;

	private static final int MAX_CONNECTIONS_PER_HOST = 3;

	private static final int MAX_TOTAL_CONNECTIONS = 10;
	
    static {
        Map<String,String> params = new HashMap<String,String>(1);
        params.put("type", "feed");
        FEED_MEDIA_TYPE = new MediaType("application", "xml", params);
    }

    
    /**
     * <p>Construct a fully configured client instance.</p>
     */
    public AtomBase() {
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager(); 
        connectionManager.getParams().setDefaultMaxConnectionsPerHost(MAX_CONNECTIONS_PER_HOST); 
        connectionManager.getParams().setMaxTotalConnections(MAX_TOTAL_CONNECTIONS); 

        DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
//    	config.getProperties().put(ApacheHttpClient4Config.PROPERTY_PREEMPTIVE_BASIC_AUTHENTICATION, Boolean.TRUE);
        config.getProperties().put(ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER, connectionManager);

        httpClient = ApacheHttpClient4.create(config);
    }

    // ------------------------------------------------- Contacts Public Methods

	@GET
	@Produces ({MediaType.APPLICATION_ATOM_XML,
		MediaType.APPLICATION_JSON,
		MediaType.TEXT_XML})
	public Response operate(@Context UriInfo ui) {
		if ( ui.getAbsolutePath().toString().contains("data/atom/") ) {
			MultivaluedMap<String, String> params = ui.getQueryParameters();
			List<String> uris = params.get("f");
			if ( uris.size() < 2 ) {
				throw new WebApplicationException(new DiscoveryException("Atom feed combinators work with at least two f parameters (as feed URIs)"), 403);
			}

			// TODO: potential for misuse: with a single request to our server, a malicious client can make our server make multiple requests to other server(s)
			// maybe limit the applicability of this atom union only to URIs on the same server?
			Feed result;
			try {
				result = atomCombination(ui.getBaseUri().toString(), uris);
			} catch (Exception e) {
				throw new WebApplicationException(e, 403);
			}

			return Response.ok(result).build();
		}
		return Response.status(404).build();
	}

	/**
	 * FIXME: This ignores that Substraction is order-dependent!
	 * 
	 * @param requestURI
	 * @param uris
	 * @return
	 * @throws Exception
	 */
	private Feed atomCombination(String requestURI, List<String> uris) throws Exception {
		
		resolveAndCheckURIs(requestURI, uris);

		// download the feeds
		List<Feed> feeds = downloadFeeds(uris);
		int feedCount = feeds.size();

		if ( feedCount == 0 ) {
			throw new DiscoveryException("No supported feeds found");
		} else if ( feedCount == 1 ) {
			return feeds.get(0);
		}
		
		//feedUpdated = extractFeedData(feeds, feedTitles, feedRights, entriesByFeed, entriesByID);
		
		Feed combinedFeed = DiscoveryUtil.getAbderaInstance().getFactory().newFeed();
		combinedFeed.setId(requestURI);
		combinedFeed.addLink(requestURI,"self");
		combinedFeed.setGenerator("http://iserve.kmi.open.ac.uk/", null, 
				"iServe Atom " + combinatorName() + " combinator 2010/06/23"); 
		
		String rights = "The constituent feeds have the following rights statements (in no particular order): ";
		
		Iterator<Feed> iterator = feeds.iterator();
		Set<Entry> combination = new HashSet(iterator.next().getEntries());
		while ( iterator.hasNext() ) {
			Feed feed = (Feed) iterator.next();
			combination = combineResults(combination, feed.getEntries());
			// FIXME: merge title etc
			// feed.setTitle(plugin.getFeedTitle());
			rights += "\"" + feed.getRights() + "\"";
			if ( iterator.hasNext() ) {
				rights += ", ";
			} else {
				rights += ".";
			}
		}

		if ( combination != null ) {
			for ( Entry result : combination ) {
				combinedFeed.addEntry(result);
			}
		}

		combinedFeed.setUpdated(new Date());
		combinedFeed.setRights(rights);
		return combinedFeed;
	}

	/**
	 * Given the results in the feeds to combine, generate the combination
	 * according to the concrete Atom operator implemented
	 * 
	 * @param combination
	 * @param entries
	 * @return result of the combination
	 */
	abstract Set<Entry> combineResults(Set<Entry> combination, List<Entry> entries);

	private void resolveAndCheckURIs(String requestURI, List<String> uris) {
		for ( int i = 0; i < uris.size(); i++ ) {
			UriBuilder uriBuilder = UriBuilder.fromUri(requestURI);
			UriBuilder properUriBuilder = uriBuilder.path(uris.get(i));
			java.net.URI properuri = properUriBuilder.build("");
			String scheme = properuri.getScheme();
			if ( !"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme) ) {
				System.err.println("only http and https schemes supported; bad URI: " + properuri.toString());
				uris.remove(i);
			} else {
				uris.set(i, URLDecoder.decode(properuri.toString()).replaceAll("#", "%23"));
			}
		}
	}

/*
	private Date extractFeedData(Feed[] feeds, String[] feedTitles, Set<String> feedRights,
			List<Element>[] entriesByFeed, Map<String, Element[]> entriesByID) throws ParseException {
		Date feedUpdated = null;
		for ( int i = 0; i < feeds.length; i++ ) {
			Document feed = feeds[i];
			List<Element> entries = new ArrayList<Element>();
			entriesByFeed[i] = entries;
            
			if ( feed == null ) {
				continue;
			}

			Element root = feed.getDocumentElement();
			if ( root == null ) {
				System.err.println("empty document number " + i);
				continue;
			}
			if ( !ATOM_NS.equals(root.getNamespaceURI()) || !"feed".equals(root.getLocalName()) ) {
				System.err.println("unknown root element: {" + root.getNamespaceURI() + "}" + root.getLocalName());
				continue;
			}
			for ( Node n = root.getFirstChild(); n != null; n = n.getNextSibling() ) {
				if ( !(n instanceof Element) ) {
					continue;
				}
				Element el = (Element) n;
				String elname = el.getLocalName();
				if ( ATOM_NS.equals(el.getNamespaceURI()) ) {
					// handle atom elements
					if ( "author".equals(elname) ) {
						// todo ignoring authors, should become contributors
						// feedAuthors.add(el);
					} else if ( "category".equals(elname) ) {
						// todo ignoring categories
						// feedCategories.add(el);
					} else if ( "contributor".equals(elname) ) {
						// todo ignoring contributors
						// feedContributors.add(el);
					} else if ( "rights".equals(elname) ) {
						feedRights.add(el.getTextContent());
					} else if ( "title".equals(elname) ) {
						feedTitles[i] = el.getTextContent();
					} else if ( "updated".equals(elname) ) {
						String updateString = el.getTextContent();
						Date update = SDF.parse(updateString); 
						if ( feedUpdated == null ) {
							feedUpdated = update;
						} else {
							if (feedUpdated.before(update)) {
								feedUpdated = update;
							}
						}
					} else if ("entry".equals(elname)) {
						entries.add(el);
						String entryID = getEntryID(el);
						if ( entryID == null ) {
							continue; // ignoring entries without ID
						}
						Element[] entryListByID = entriesByID.get(entryID);
						if ( entryListByID == null ) {
							entryListByID = new Element[feeds.length];
							entriesByID.put(entryID, entryListByID);
						}
						entryListByID[i] = el;
					} else if ("generator".equals(elname) || "icon".equals(elname) || "id".equals(elname) || "link".equals(elname) || "logo".equals(elname) || "subtitle".equals(elname)) {
						// ignoring generator, icon, ID, links, logo, subtitles
					}
				} else { 
					// TODO: ignoring extension element
//					feedExtensionEls.add(el);
				}
			}
		}
		return feedUpdated;
	}*/

	/**
	 * downloads feeds
	 * @param uris array of uris of feeds to download
	 * @return count of feeds actually successfully downloaded
	 */
	private List<Feed> downloadFeeds(List<String> uris) {
		List<Feed> result = new ArrayList<Feed>();
		
		for ( int i = 0; i < uris.size(); i++ ) {
			String uri = uris.get(i);
			if ( uri == null ) {
				continue;
			}

			result.add(httpClient.resource(uri).accept(
					MediaType.APPLICATION_ATOM_XML).get(Feed.class));
		}
		
		return result;
	}
/*
	protected Element mergeEntries(Element[] entries, Document doc) throws ParseException {
		Date entryUpdated = null; // newest
		Set<String> entryTitles = new HashSet<String>(); // from all entries if different
		Set<String> entryRights = new HashSet<String>(); // from all entries if different
		List<Element> entryExtensions = new ArrayList<Element>(); // from all entries
		Element entryAuthor = null; // from the first entry
		String entryID = null; // from the first entry
		List<Element> entryLinks = new ArrayList<Element>(); // only from the first entry
		Element entryLinksEntry = null; // this is the entry from which the links are taken
		Element entryContent = null; // from the first entry
		Element entrySummary = null; // from the first entry
		// ignoring: extra authors, contributors, categories, extra content, source, extra summaries 

		for ( Element entry : entries ) {
			if ( entry == null ) {
				continue;
			}
			for ( Node n = entry.getFirstChild(); n != null; n = n.getNextSibling() ) {
				if ( !(n instanceof Element) ) {
					continue;
				}
				Element el = (Element) n;
				String elname = el.getLocalName();
				if ( ATOM_NS.equals(el.getNamespaceURI()) ) {
					if ( "author".equals(elname) ) {
						if ( entryAuthor == null ) {
							entryAuthor = el;
						}
					} else if ( "rights".equals(elname) ) {
						entryRights.add(el.getTextContent());
					} else if ( "id".equals(elname) ) {
						if ( entryID == null ) {
							entryID = el.getTextContent();
						}
					} else if ( "content".equals(elname) ) {
						if ( entryContent == null ) {
							entryContent = el;
						}
					} else if ( "summary".equals(elname) ) {
						if ( entrySummary == null ) {
							entrySummary = el;
						}
					} else if ( "title".equals(elname) ) {
						entryTitles.add(el.getTextContent());
					} else if ( "link".equals(elname) ) {
						if (entryLinksEntry == null || entry == entryLinksEntry) {
							entryLinks.add(el);
							entryLinksEntry = entry;
						}
					} else if ( "updated".equals(elname) ) {
						String updateString = el.getTextContent();
						Date update = SDF.parse(updateString); 
						if ( entryUpdated == null ) {
							entryUpdated = update;
						} else {
							if ( entryUpdated.before(update) ) {
								entryUpdated = update;
							}
						}
					}
				} else {
					entryExtensions.add(el);
				}
			}
		}

		Element retval = doc.createElementNS(ATOM_NS, "entry");

		Element tmp = doc.createElementNS(ATOM_NS, "id");
		retval.appendChild(doc.createTextNode("\n    ")); retval.appendChild(tmp);
		tmp.setTextContent(entryID);

		tmp = doc.createElementNS(ATOM_NS, "title");
		retval.appendChild(doc.createTextNode("\n    ")); retval.appendChild(tmp);
		String title = combinatorEntryTitle(entryTitles);
		tmp.setTextContent(title);

		tmp = doc.createElementNS(ATOM_NS, "updated");
		retval.appendChild(doc.createTextNode("\n    ")); retval.appendChild(tmp);
		tmp.setTextContent(SDF.format(entryUpdated));

		if ( entryAuthor != null ) {
			tmp = (Element)doc.importNode(entryAuthor, true);
			retval.appendChild(doc.createTextNode("\n    ")); retval.appendChild(tmp);
		}

		if ( entryContent != null ) {
			tmp = (Element)doc.importNode(entryContent, true);
			retval.appendChild(doc.createTextNode("\n    ")); retval.appendChild(tmp);
		}

		if ( entrySummary != null ) {
			tmp = (Element)doc.importNode(entrySummary, true);
			retval.appendChild(doc.createTextNode("\n    ")); retval.appendChild(tmp);
		}

		for ( Element link : entryLinks ) {
			tmp = (Element)doc.importNode(link, true);
			retval.appendChild(doc.createTextNode("\n    ")); retval.appendChild(tmp);            
		}

		for ( Element extension : entryExtensions ) {
			tmp = (Element)doc.importNode(extension, true);
			retval.appendChild(doc.createTextNode("\n    ")); retval.appendChild(tmp);            
		}

		if ( entryRights.size() != 0 ) {
			tmp = doc.createElementNS(ATOM_NS, "rights");
			retval.appendChild(doc.createTextNode("\n    ")); retval.appendChild(tmp);
			String rights = "";
			for ( Iterator<String> it = entryRights.iterator(); it.hasNext(); ) {
				rights += it.next();
				if ( it.hasNext() ) {
					rights += " / ";
				}
			}
			tmp.setTextContent(rights);
		}
		retval.appendChild(doc.createTextNode("\n  ")); 

		return retval;
	}*/

	private String combinatorEntryTitle(Set<String> entryTitles) {
		String title = "";
		for ( Iterator<String> it = entryTitles.iterator(); it.hasNext(); ) {
			title += it.next();
			if ( it.hasNext() ) {
				title += " / ";
			}
		}
		return title;
	}

	abstract String combinatorFeedTitle(String[] feedTitles);

	abstract String combinatorName();

}
