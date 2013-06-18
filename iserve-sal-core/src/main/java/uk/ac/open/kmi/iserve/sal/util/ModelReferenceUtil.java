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

package uk.ac.open.kmi.iserve.sal.util;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.IOUtil;
import uk.ac.open.kmi.iserve.commons.io.URIUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class ModelReferenceUtil {

    private static final Logger log = LoggerFactory.getLogger(ModelReferenceUtil.class);

    private static ModelReferenceUtil instance = new ModelReferenceUtil();

    private URI sparqlEndpoint;

    private Tika tika;

    private ModelReferenceUtil() {
        tika = new Tika();
    }

    public static ModelReferenceUtil getInstance() {
        return instance;
    }

    public void setSparqlEndpoint(URI endpoint) {
        this.sparqlEndpoint = endpoint;
    }

    public void inputModelReference(String modelrefUri) throws MalformedURLException, IOException {

        // If the SPARQL endpoint does not exist return immediately.
        if (this.getSparqlEndpoint() == null) {
            return;
        }

        String contextUri = URIUtil.getNameSpace(modelrefUri);

        String queryStr = "SELECT DISTINCT ?g where { graph ?g { ?s ?p ?o . } }";

        // Query the engine
        Query query = QueryFactory.create(queryStr);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(this.getSparqlEndpoint().toASCIIString(), query);

        try {
            // TODO: Remove profiling
            long startTime = System.currentTimeMillis();

            ResultSet qResults = qexec.execSelect();

            // TODO: Remove profiling
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            log.info("Time taken for querying the registry: " + duration);

            Resource resource;
            URI matchUri;
            // Iterate over the results obtained
            while (qResults.hasNext()) {
                QuerySolution soln = qResults.nextSolution();

                // Get the match URL
                resource = soln.getResource("g");
                if (resource != null && resource.isURIResource() && resource.getURI().equals(contextUri)) {
                    return;
                }
            }

            System.setProperty("sun.net.client.defaultConnectTimeout", "3000");
            System.setProperty("sun.net.client.defaultReadTimeout", "3000");
            String mimeType = tika.detect(new URL(modelrefUri));

            if (mimeType.contains("html") == true) {
                throw new IOException("It does not seem to be an ontology");
            }

            String ontologyString = IOUtil.readString(new URL(modelrefUri));

            // TODO: FIX this
//			model = connector.openRepositoryModel(URIUtil.getNameSpace(modelrefUri));
//			model.readFrom(new ByteArrayInputStream(ontologyString.getBytes()));

        } finally {
            qexec.close();
        }

    }

    public String getDatatype(String modelrefUri) {
        if (isProperty(modelrefUri)) {
            return getRange(modelrefUri);
        }
        if (isClass(modelrefUri)) {
            return getPropertyRange(modelrefUri);
        }
        return null;
    }

    private boolean isProperty(String modelrefUri) {

        // If the SPARQL endpoint does not exist return immediately.
        if (this.getSparqlEndpoint() == null) {
            // TODO: throw error
        }

        String queryStr = "SELECT ?t WHERE {\n" +
                "<" + modelrefUri + ">" + "<" + RDF.type.getURI() + ">" + " ?t . " +
                "}";

        // Query the engine
        Query query = QueryFactory.create(queryStr);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(this.getSparqlEndpoint().toASCIIString(), query);

        try {
            // TODO: Remove profiling
            long startTime = System.currentTimeMillis();

            ResultSet qResults = qexec.execSelect();

            // TODO: Remove profiling
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            log.info("Time taken for querying the registry: " + duration);

            Resource resource;

            // Iterate over the results obtained
            while (qResults.hasNext()) {
                QuerySolution soln = qResults.nextSolution();

                // Get the match URL
                resource = soln.getResource("t");
                if (resource != null && resource.isURIResource()) {
                    String typeUri = resource.getURI();

                    if (typeUri.equals(RDF.Property.getURI()) ||
                            typeUri.equals(OWL.ObjectProperty.getURI()) ||
                            typeUri.equals(OWL.DatatypeProperty.getURI()) ||
                            typeUri.equals(OWL.TransitiveProperty.getURI()) ||
                            typeUri.equals(OWL.SymmetricProperty.getURI()) ||
                            typeUri.equals(OWL.FunctionalProperty.getURI()) ||
                            typeUri.equals(OWL.InverseFunctionalProperty.getURI())) {
                        return true;
                    }

                }
            }
            return false;
        } finally {
            qexec.close();
        }
    }

    private boolean isClass(String modelrefUri) {

        // If the SPARQL endpoint does not exist return immediately.
        if (this.getSparqlEndpoint() == null) {
            // TODO: throw error
        }

        String queryStr = "SELECT ?t WHERE {\n" +
                "<" + modelrefUri + ">" + "<" + RDF.type.getURI() + ">" + " ?t . " +
                "}";

        // Query the engine
        Query query = QueryFactory.create(queryStr);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(this.getSparqlEndpoint().toASCIIString(), query);

        try {
            // TODO: Remove profiling
            long startTime = System.currentTimeMillis();

            ResultSet qResults = qexec.execSelect();

            // TODO: Remove profiling
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            log.info("Time taken for querying the registry: " + duration);

            Resource resource;

            // Iterate over the results obtained
            while (qResults.hasNext()) {
                QuerySolution soln = qResults.nextSolution();

                // Get the match URL
                resource = soln.getResource("t");
                if (resource != null && resource.isURIResource()) {
                    String typeUri = resource.getURI();

                    if (typeUri.equals(RDFS.Class.getURI()) ||
                            typeUri.equals(OWL.Class)) {
                        return true;
                    }

                }
            }
            return false;
        } finally {
            qexec.close();
        }
    }

    public String getLabel(String modelrefUri) {

        // If the SPARQL endpoint does not exist return immediately.
        if (this.getSparqlEndpoint() == null) {
            // TODO: throw error
        }

        String queryStr = "SELECT ?l WHERE {\n" +
                "<" + modelrefUri + ">" + "<" + RDFS.label.getURI() + ">" + " ?l . " +
                "}";

        // Query the engine
        Query query = QueryFactory.create(queryStr);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(this.getSparqlEndpoint().toASCIIString(), query);

        try {
            // TODO: Remove profiling
            long startTime = System.currentTimeMillis();

            ResultSet qResults = qexec.execSelect();

            // TODO: Remove profiling
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            log.info("Time taken for querying the registry: " + duration);

            // Iterate over the results obtained
            while (qResults.hasNext()) {
                QuerySolution soln = qResults.nextSolution();
                return soln.getLiteral("l").getString();
            }

            return null;
        } finally {
            qexec.close();
        }
    }

    public String getComment(String modelrefUri) {

        // If the SPARQL endpoint does not exist return immediately.
        if (this.getSparqlEndpoint() == null) {
            // TODO: throw error
        }

        String queryStr = "SELECT ?c WHERE {\n" +
                "<" + modelrefUri + ">" + "<" + RDFS.comment.getURI() + ">" + " ?c . " +
                "}";

        // Query the engine
        Query query = QueryFactory.create(queryStr);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(this.getSparqlEndpoint().toASCIIString(), query);

        try {
            // TODO: Remove profiling
            long startTime = System.currentTimeMillis();

            ResultSet qResults = qexec.execSelect();

            // TODO: Remove profiling
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            log.info("Time taken for querying the registry: " + duration);

            // Iterate over the results obtained
            while (qResults.hasNext()) {
                QuerySolution soln = qResults.nextSolution();
                return soln.getLiteral("c").getString();
            }

            return null;
        } finally {
            qexec.close();
        }
    }

    private String getRange(String modelrefUri) {

        // If the SPARQL endpoint does not exist return immediately.
        if (this.getSparqlEndpoint() == null) {
            // TODO: throw error
        }

        String queryStr = "SELECT ?r WHERE {\n" +
                "<" + modelrefUri + ">" + "<" + RDFS.range.getURI() + ">" + " ?r . " +
                "}";

        // Query the engine
        Query query = QueryFactory.create(queryStr);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(this.getSparqlEndpoint().toASCIIString(), query);

        try {
            // TODO: Remove profiling
            long startTime = System.currentTimeMillis();

            ResultSet qResults = qexec.execSelect();

            // TODO: Remove profiling
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            log.info("Time taken for querying the registry: " + duration);

            // Iterate over the results obtained
            while (qResults.hasNext()) {
                QuerySolution soln = qResults.nextSolution();
                return soln.getResource("r").getURI();
            }

            return null;
        } finally {
            qexec.close();
        }
    }

    private String getPropertyRange(String modelrefUri) {
        return null;

        // TODO: reimplement?

        //		String queryString = "SELECT DISTINCT ?r ?r2 WHERE {\n" +
//				" <" + modelrefUri + "> " + RDFS.subClassOf.toSPARQL() + " ?s . \n" +
//				" OPTIONAL { \n" +
//				"  ?p " + RDF.type.toSPARQL() + " " + RDF.Property.toSPARQL() + ". \n" +
//				"  ?s ?p ?r . \n" +
//				"  OPTIONAL { ?r " + RDF.type.toSPARQL() + " " + RDFS.Datatype.toSPARQL() + " . }\n" +
//				" } \n" +
//				" OPTIONAL { ?p2 " + RDFS.domain.toSPARQL() + " ?s . \n" +
//				"  ?p2 " + RDFS.range.toSPARQL() + " ?r2 . \n" +
//				" }\n" +
//				"}";
//		RepositoryModel model = connector.openRepositoryModel();
//		QueryResultTable qrt = model.sparqlSelect(queryString);
//		ClosableIterator<QueryRow> iter = qrt.iterator();
//		String result = null;
//		String result2 = null;
//		while ( iter.hasNext() ) {
//			QueryRow row = iter.next();
//			if ( row.getValue("r") != null ) {
//				String rangeType = row.getValue("r").toString();
//				if ( rangeType.startsWith(XSD.XSD_NS) ) {
//					result = rangeType;
//				}
//				if ( rangeType.endsWith(RDFS.Literal.toString()) ) {
//					result2 = XSD._string.toString();
//				}
//				if ( isClass(rangeType) ) {
//					result2 = XSD._anyURI.toString();
//				}
//			}
//			if ( row.getValue("r2") != null ) {
//				String rangeType = row.getValue("r2").toString();
//				if ( rangeType.startsWith(XSD.XSD_NS) ) {
//					return rangeType;
//				}
//				if ( rangeType.endsWith(RDFS.Literal.toString()) ) {
//					result2 = XSD._string.toString();
//				}
//			}
//		}
//		iter.close();
//		connector.closeRepositoryModel(model);
//		if ( result != null )
//			return result;
//		return result2;
    }

    /**
     * @return the sparqlEndpoint
     */
    public URI getSparqlEndpoint() {
        return this.sparqlEndpoint;
    }

    //	public static void main(String[] args) {
    //		String modelRefUri = "http://purl.org/iserve/ontology/owlstc/concept.owl#Price";
    ////		String modelRefUri = "http://localhost:8080/PoliceAPI.rdfs#hasID";
    ////		String modelRefUri = "http://localhost:8080/PoliceAPI.rdfs#PoliceFunctionalUnit";
    ////		String modelRefUri = "http://purl.org/iserve/ontology/owlstc/books.owl#Book";
    //		try {
    //			ModelReferenceUtil.getInstance().inputModelReference(modelRefUri);
    ////			ModelReferenceUtil.getInstance().getNameDescription(modelRefUri);
    //			ModelReferenceUtil.getInstance().getDatatype(modelRefUri);
    //		} catch (MalformedURLException e) {
    //			e.printStackTrace();
    //		} catch (IOException e) {
    //			e.printStackTrace();
    //		}
    //	}

}
