package uk.ac.open.kmi.iserve.discovery.freetextsearch.impl;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.discovery.freetextsearch.FreeTextSearchPlugin;
import uk.ac.open.kmi.iserve.discovery.freetextsearch.FreeTextSearchResult;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

/**
 * Created by Luca Panziera on 03/11/14.
 */
public abstract class SparqlSearchPlugin implements FreeTextSearchPlugin {

    protected String queryEndpoint;
    protected String updateEndpoint;
    private Logger logger = LoggerFactory.getLogger(SparqlSearchPlugin.class);
    private String searchProperty;

    public SparqlSearchPlugin(String searchProperty) {
        this.searchProperty = searchProperty;
    }

    @Override
    public Set<FreeTextSearchResult> search(String query) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder
                .append("PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> ")
                .append("PREFIX sawsdl:<http://www.w3.org/ns/sawsdl#> ")
                .append("PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> ")
                .append("SELECT * ")
                .append("WHERE { ?s <").append(searchProperty).append("> \"").append(query).append("\" . ")
                .append("OPTIONAL { ?s rdfs:label ?label . } ")
                .append("OPTIONAL { ?s rdfs:comment ?comment . } ")
                .append("OPTIONAL { ?s sawsdl:modelReference  ?modelReference . } } ");

        Query sparqlQuery = QueryFactory.create(queryBuilder.toString());
        return search(sparqlQuery);
    }

    private Set<FreeTextSearchResult> search(Query sparqlQuery) {
        logger.debug("Executing SPARQL query: {}", sparqlQuery);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(queryEndpoint.toString(), sparqlQuery);
        ResultSet resultSet = qexec.execSelect();

        Map<String, FreeTextSearchResult> r = Maps.newLinkedHashMap();
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.next();
            RDFNode s = solution.get("s");
            if (s.isURIResource()) {
                String resource = s.asResource().getURI();
                if (!r.containsKey(resource)) {
                    try {
                        FreeTextSearchResult result = new FreeTextSearchResult(new URI(resource));
                        Literal label = solution.getLiteral("label");
                        Literal comment = solution.getLiteral("comment");
                        Resource modelReference = solution.getResource("modelReference");
                        if (label != null) {
                            result.setLabel(label.getString());
                        }
                        if (comment != null) {
                            result.setComment(comment.getString());
                        }
                        if (modelReference != null) {
                            result.addModelReference(new URI(modelReference.getURI()));
                        }
                        r.put(resource, result);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Resource modelReference = solution.getResource("modelReference");
                        r.get(resource).addModelReference(new URI(modelReference.getURI()));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return Sets.newLinkedHashSet(r.values());
    }

    @Override
    public Set<FreeTextSearchResult> search(String query, URI type) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder
                .append("PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> ")
                .append("PREFIX sawsdl:<http://www.w3.org/ns/sawsdl#> ")
                .append("PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> ")
                .append("SELECT * ")
                .append("WHERE { ?s <").append(searchProperty).append("> \"").append(query).append("\" . ")
                .append("OPTIONAL { ?s rdfs:label ?label . } ")
                .append("OPTIONAL { ?s rdfs:comment ?comment . } ")
                .append("OPTIONAL { ?s sawsdl:modelReference  ?modelReference . } ")
                .append("?s rdf:type <").append(type.toASCIIString()).append("> . } ");

        Query sparqlQuery = QueryFactory.create(queryBuilder.toString());
        return search(sparqlQuery);
    }

}
