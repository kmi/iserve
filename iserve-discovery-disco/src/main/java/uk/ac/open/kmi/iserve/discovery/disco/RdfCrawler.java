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
package uk.ac.open.kmi.iserve.discovery.disco;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.ontoware.rdf2go.ModelFactory;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.Syntax;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.commons.vocabulary.SAWSDL;
import uk.ac.open.kmi.iserve.commons.vocabulary.WSMO_LITE;

/**
 * This class is a parametric tool for crawling for RDF data.
 * 
 * todo parameters: timeout, max allowed file size, max allowed files, number of
 * threads, request throttling (global, per-host) todo add logging with
 * ScutterVocab?
 * 
 * @author Jacek Kopecky
 * 
 */
public class RdfCrawler {

	private List<String> knownNamespaces = new ArrayList<String>(10);

	private int maxDepth = 3;

	private static HttpClient httpClient = new HttpClient();

	private static ModelFactory modelFactory = RDF2Go.getModelFactory();

	/**
	 * default constructor
	 */
	public RdfCrawler() {
		this.addKnownNamespace(RDF.NAMESPACE);
		this.addKnownNamespace(RDFS.NAMESPACE);
		this.addKnownNamespace(OWL.NAMESPACE);
		this.addKnownNamespace(WSMO_LITE.NS);
		this.addKnownNamespace(SAWSDL.NS);
		this.addKnownNamespace(MSM.NS);
		this.addKnownNamespace("http://www.w3.org/ns/wsdl-extensions#");  // for WSDLX safety
	}

	static {
	    String proxyHost = System.getProperty("http.proxyHost");
	    if (proxyHost != null) {
	        int proxyPort = Integer.getInteger("http.proxyPort", 80);
            System.err.println("setting proxy to " + proxyHost + ":" + proxyPort);
	        httpClient.getHostConfiguration().setProxy(proxyHost, proxyPort);
	    }
	}

	/**
	 * set maximal crawl depth (the initial set of URIs is at depth 0); value 0 means no crawl. 
	 * @param d max crawl depth
	 */
	public void setMaxDepth(int d) {
		if ( d < 0 ) {
			throw new IllegalArgumentException("max depth cannot be negative");
		}
		this.maxDepth = d;
	}

	/**
	 * remove all known namespaces - initially these are RDF, RDFS, OWL, WSMO-Lite, MSM, SAWSDL and WSDLX
	 */
	public void clearKnownNamespaces() {
		this.knownNamespaces.clear();
	}

	/**
	 * add a known namespace - all URIs starting with this string will be ignored by the crawler
	 * @param ns the known namespace
	 */
	public void addKnownNamespace(String ns) {
		if ( !isInKnownNS(ns) ) {
			this.knownNamespaces.add(ns);
		}
	}

	private boolean isInKnownNS(String uri) {
		for (String known : this.knownNamespaces) {
			if (uri.startsWith(known)) {
				return true;
			}
		}
		return false;
	}
	
	private Set<URI> selectURIsToFollow(Set<Statement> data) {
	    // todo find out classes and properties about which we don't know enough; also use see also values
	    // this may not be necessary under the assumption that the already crawled ontologies give us enough information 
	    // (need to study cases where that's not true to implement this method usefully) 

		return Collections.emptySet();
	}

	/**
	 * crawls the given URIs and possibly further URIs found in the crawl to get useful information about the URIs
	 * @param uris a set of uris
	 * @return a set of statements gathered from the crawled documents
	 */
	public Set<Statement> crawl(Set<URI> uris) {
		Set<URI> crawledURIs = new HashSet<URI>();
		Set<Statement> result = new HashSet<Statement>();
		this.doCrawl(uris, crawledURIs, result, 1);
		return result;
	}
	
	/**
	 * crawls the URI and possibly further URIs found in the crawl to get useful information about the URI
	 * @param uri the initial URI of interest
	 * @return statements gathered from the crawled document(s)
	 */
	public Set<Statement> crawl(URI uri) {
	    return this.crawl(Collections.singleton(uri));
	}
	
	private URI removeFragment(URI uri) {
        int hash = uri.toString().indexOf('#');
        if (hash >= 0) {
            return new URIImpl(uri.toString().substring(0, hash));
        }
        return uri;
	}
	
    private void doCrawl(Set<URI> uris, Set<URI> crawledURIs, Set<Statement> result, int depth) {
        System.err.println("starting crawl at depth " + depth);
        if (depth > this.maxDepth) {
            System.err.println("stopped crawling after reaching maximum depth " + this.maxDepth + ", with " + uris.size() + " uncrawled URIs");
            return;
        }
        int before = result.size();
        for (URI uri : uris) {
            Set<Statement> data = get(uri, crawledURIs);
            result.addAll(data);
        }
        if (result.size() == before) {
            System.err.println("finishing because no new statements came at depth " + depth);
            return;
        }
        Set<URI> furtherURIs = selectURIsToFollow(result);
        furtherURIs.removeAll(crawledURIs);
        if (!furtherURIs.isEmpty()) {
            doCrawl(furtherURIs, crawledURIs, result, depth + 1);
        } else {
            System.err.println("stopped crawling at depth " + depth + " because there are no further interesting URIs");
        }
    }

	/**
	 * gets the RDF statements at the given URI; if any error occurs the result is an empty set
	 * @param uri
	 * @param crawledURIs can be updated in case of redirections and stuff; get will also add uri to crawledURIs
	 * @return a set of URIs, never null
	 */
	private Set<Statement> get(URI uri, Set<URI> crawledURIs) {
	    uri = removeFragment(uri);
        if (crawledURIs.contains(uri)) {
            return Collections.emptySet();
        }        
	    crawledURIs.add(uri);
        System.err.println("getting " + uri);
	    
	    String scheme = java.net.URI.create(uri.toString()).getScheme();
	    if (!"http".equals(scheme) && !"https".equals(scheme)) {
	        System.err.println("only http and https schemes are supported");
	        return Collections.emptySet();
	    }
	    
	    if (isInKnownNS(uri.toString())) {
	        // ignore URIs from known namespaces
	        System.err.println("ignored as known");
	        return Collections.emptySet();
	    }
	    
	    HttpMethod get = new GetMethod(uri.toString());
	    get.setFollowRedirects(false);
	    get.addRequestHeader("Accept", "application/rdf+xml, text/n3, text/turtle, text/rdf+n3, application/x-turtle, application/owl+xml, application/xml; q=0.5, text/plain; q=0.01"); // todo check that these are all the useful rdf media types
	    int responseCode;
	    try {
            responseCode = httpClient.executeMethod(get);
            System.err.println("response code: " + responseCode);
        } catch (IOException e) {
            e.printStackTrace();
            get.releaseConnection();
            return Collections.emptySet();
        }
        
        if (responseCode >= 300 && responseCode <= 399) {
            // if there is a redirect, follow it (unless it's in crawled URIs) and add the redirect URI also to crawled URIs
             Header[] locations = get.getResponseHeaders("Location");
             get.releaseConnection();
             if (locations.length > 1) {
                 System.err.println("oooh, someone tell jacek a " + responseCode + " redirect at " + uri + " returns more than one location");
             }
             if (locations.length == 0) {
                 System.err.println("no location in " + responseCode + " redirect at uri " + uri);
                 return Collections.emptySet();
             }
             URI location = new URIImpl(locations[0].getValue());
             System.err.print("redirect, "); // this prefixes the normal "getting ..." output at the start of get()
             return get(location, crawledURIs); 
        } else if (responseCode != 200) {
            // various errors, already logged above, so ignore
            get.releaseConnection();
            return Collections.emptySet();
        }
        // response 200 OK
        
        String mediaType = get.getResponseHeader("Content-type").getValue();
        int semi = mediaType.indexOf(';');
        if (semi > -1) {
            mediaType = mediaType.substring(0, semi);
        }
        System.err.println("media type " + mediaType);
        Model model = modelFactory.createModel();  // add a context parameter here? would it be useful?
        model.open();
        try {
            // text/plain is being returned by proton
            if ("text/plain".equals(mediaType) || "application/rdf+xml".equals(mediaType) || "application/xml".equals(mediaType) || "application/owl+xml".equals(mediaType)) {
                model.readFrom(get.getResponseBodyAsStream(), Syntax.RdfXml);
            } else if ("text/n3".equals(mediaType) || "text/rdf+n3".equals(mediaType)) {
                model.readFrom(get.getResponseBodyAsStream(), Syntax.Turtle);  // todo turtle is just a subset of n3, so here's hoping that it works; RDF2go doesn't have n3
            } else if ("text/turtle".equals(mediaType) || "application/x-turtle".equals(mediaType)) {
                model.readFrom(get.getResponseBodyAsStream(), Syntax.Turtle);
            } else {
                // unknown media type, log and ignore
                System.err.println("unknown media type");
                model.close();
                return Collections.emptySet();
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.close();
            get.releaseConnection();
            return Collections.emptySet();
        }
        get.releaseConnection();

        System.err.println("number of triples: " + model.size());
        Set<Statement> retval = new HashSet<Statement>();
        for (Statement statement : model) {
            retval.add(statement);
        }
        model.close();
        return retval;
    }

	/**
	 * main
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
	    if (args.length == 0) {
	        System.err.println("Expecting a set of URIs as arguments");
	        System.exit(-1);
	    }
	    Set<URI> uris = new HashSet<URI>();
	    try { 
    	    for (String arg : args) {
    	        URI uri = new URIImpl(arg);
    	        uris.add(uri);
    	    }
	    } catch (IllegalArgumentException e) {
	        System.err.println("Invalid URI as argument: " + e.getMessage());
            System.exit(-1);
	    }
	    
	    Set<Statement> result = new RdfCrawler().crawl(uris);
	    System.out.println("crawl finished, here are the results (" + result.size() + " triples):");
	    Model model = modelFactory.createModel();
	    model.open();
	    List<Statement> triples = new ArrayList<Statement>(result);
	    Collections.sort(triples);
	    model.addAll(triples.iterator());
	    System.out.println(model.serialize(Syntax.Turtle));
	}

}
