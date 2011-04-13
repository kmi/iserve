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
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.URI;
import org.openrdf.rdf2go.RepositoryModel;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.iserve.commons.io.RDFRepositoryConnector;
import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;

@Path("/update-ontologies")
public class OntologyUpdateResource {

    private static RDFRepositoryConnector rdfRepositoryConnector = null;

    private static final List<URI> builtinExtras;

    private static RdfCrawler rdfCrawler = new RdfCrawler();

    // these are only informational fields so I don't care abou any potential race conditions in reading and updating them
    private static Date lastUpdate = null;

    private static long lastTripleCount = 0;

    private static Thread updaterThread;

    private static final String[] NONE = new String[0];

    static {
        List<URI> extras = new ArrayList<URI>(1);
        extras.add(new org.ontoware.rdf2go.model.node.impl.URIImpl("http://www.w3.org/2009/08/skos-reference/skos.rdf"));
        builtinExtras = Collections.unmodifiableList(extras);
    }

    public OntologyUpdateResource() throws RepositoryException, IOException {
        rdfRepositoryConnector = Factory.getInstance().createRDFRepositoryConnector();
        if (updaterThread == null) {
            initUpdaterThread();
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getStatistics() {
        RepositoryModel repoModel = rdfRepositoryConnector.openRepositoryModel();
        Set<URI> modelRefs = getExternalReferences(repoModel, NONE, NONE);
        rdfRepositoryConnector.closeRepositoryModel(repoModel);

        String result = "<html><head><title>iServe Ontology Autoupdater</title></head><body><h1>iServe Ontology Autoupdater</h1>\n" +
                "<p>Last update was on " + String.valueOf(lastUpdate) + ", resulting in " + lastTripleCount + " triples.</p>\n" +
                "<form action='' method='post'>Extra: (optional) <input type='text' name='extra'/><br/><input type='submit' value='Update now!'></form>\n" +
                "<p>Current references (count " + modelRefs.size() + "):</p>\n<pre>";
        for (URI ref : modelRefs) {
            result += ref.toString() + "\n";
        }
        result += "</pre></body></html>";

        return Response.ok(result).build();
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    public Response update(@FormParam("extra") List<String> extras, @FormParam("svc") List<String> svcs) {
        Set<Statement> crawledData = updateOntologies(listToArray(svcs), listToArray(extras));
        lastUpdate = new Date();
        lastTripleCount = crawledData.size();

        String result = "<html><head><title>iServe Ontology Autoupdater - Updated</title></head><body><h1>iServe Ontology Autoupdater - Successfully updated</h1>\n" +
                "<p>Updated on " + String.valueOf(lastUpdate) + ", got " + crawledData.size() + " statements.</p>\n" +
                "<form action='' method='post'>Extra: (optional) <input type='text' name='extra'/><br/><input type='submit' value='Update again!'></form></body></html>";
        return Response.ok(result).build();
    }

    private String[] listToArray(List<String> stringList) {
        if (stringList == null) {
            return NONE;
        }
        String[] result = new String[stringList.size()];
        for ( int i = 0; i < stringList.size(); i++ ) {
            result[i] = stringList.get(i);
        }
        return result;
    }

    private static synchronized void initUpdaterThread() {
        if (updaterThread != null) {
            return; // thread already initialized
        }

        updaterThread = new Thread(new Runnable(){
            public void run() {
                System.err.println(new Date().toString() + ": started updater thread ");
                while (true) {
                    try {
                        Thread.sleep(1000*60*60*24);  // wait a day
                        updateOntologies();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        updaterThread.start();
    }

    private static void updateOntologies() {
        try {
            updateOntologies(NONE, NONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static Set<Statement> updateOntologies(String[] services, String[] extras) {
        // the incoming representation is ignored and can be empty
        RepositoryModel repoModel = rdfRepositoryConnector.openRepositoryModel();
        
        Set<URI> modelRefs = getExternalReferences(repoModel, services, extras);
        
        rdfRepositoryConnector.closeRepositoryModel(repoModel);
        
        Set<Statement> crawledData = rdfCrawler.crawl(modelRefs);
        if (crawledData.size() != 0) {
            // put the statements in the repository
            repoModel = rdfRepositoryConnector.openRepositoryModel("internal:iserve-modelref-defn-cache");
//                repoModel.removeAll(); // todo clear the cache context if we want to perform an update here, but owlim seems to interpret this as clearing the whole repository
            repoModel.addAll(crawledData.iterator());
            rdfRepositoryConnector.closeRepositoryModel(repoModel);
        }
        return crawledData;
    }

    /**
     * returns model references and usesOntology links from all the service descriptions; i.e., the references that we want to learn about
     * @param repoModel the repository
     * @param services if this array has any non-empty value, this function will return only the model references and usesOntology links from the given service(s)
     * @param extras extra URIs that will be included in the returned set of URIs (those that are non-empty)
     * @return a set of URIs that contains all the model references and usesOntology links
     * @throws ResourceException
     */
    private static Set<URI> getExternalReferences(RepositoryModel repoModel, String[] services, String[] extras) throws WebApplicationException {
        if (services.length != 0) {
            throw new WebApplicationException(Response.status(501).entity("limiting the ontology update by service is not implemented yet").build());
            // TODO: implement ontology update limited by services
        }

        Set<URI> modelRefs = new HashSet<URI>();

        String query = "prefix sawsdl: <" + MSM.SAWSDL_NS_URI + ">\n" + "select distinct ?ref \n" +
                "where { ?x sawsdl:modelReference ?ref . }";

        QueryResultTable qresult = repoModel.querySelect(query, "sparql");
        for (Iterator<QueryRow> it = qresult.iterator(); it.hasNext();) {
            QueryRow row = it.next();
            try {
                URI ref = row.getValue("ref").asURI();
                modelRefs.add(ref);
            } catch (IllegalArgumentException e) {
                System.err.println("ignoring mref '" + row.getValue("ref") + "' because it caused exception:");
                e.printStackTrace();
            }
        }

        query = "prefix wl: <" + MSM.WL_NS_URI + ">\n" + "select distinct ?ref \n" +
                "where { ?x wl:usesOntology ?ref . }";

        qresult = repoModel.querySelect(query, "sparql");
        for (Iterator<QueryRow> it = qresult.iterator(); it.hasNext();) {
            QueryRow row = it.next();
            URI ref = row.getValue("ref").asURI();
            modelRefs.add(ref);
        }
        
        for (String extra : extras) {
            if (extra != null && !"".equals(extra)) {
                try {
                    modelRefs.add(new org.ontoware.rdf2go.model.node.impl.URIImpl(extra));
                    System.err.println("added extra " + extra);
                } catch (IllegalArgumentException e) {
                    System.err.println("ignoring extra '" + extra + "' because it caused exception:");
                    e.printStackTrace();
                }
            }
        }
        
        for (URI extra : builtinExtras) {
            modelRefs.add(extra);
        }
        return modelRefs;
    }

}
