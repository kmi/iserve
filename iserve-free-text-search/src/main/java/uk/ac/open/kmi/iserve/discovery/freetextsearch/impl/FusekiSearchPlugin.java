package uk.ac.open.kmi.iserve.discovery.freetextsearch.impl;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.ConfigurationProperty;
import uk.ac.open.kmi.iserve.core.iServeProperty;
import uk.ac.open.kmi.iserve.discovery.freetextsearch.FreeTextSearchPlugin;
import uk.ac.open.kmi.iserve.discovery.freetextsearch.FreeTextSearchResult;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * Created by Luca Panziera on 28/10/2014.
 */
public class FusekiSearchPlugin implements FreeTextSearchPlugin {

    private Logger logger = LoggerFactory.getLogger(OwlimSearchPlugin.class);
    private String queryEndpoint;
    private String updateEndpoint;

    @Inject
    public FusekiSearchPlugin(@iServeProperty(ConfigurationProperty.SERVICES_SPARQL_QUERY) String queryEndpoint, @iServeProperty(ConfigurationProperty.SERVICES_SPARQL_UPDATE) String updateEndpoint) {
        this.queryEndpoint = queryEndpoint;
        this.updateEndpoint = updateEndpoint;
    }

    @Override
    public Set<FreeTextSearchResult> search(String query) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX luc: <http://www.ontotext.com/owlim/lucene#> ")
                .append("PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> ")
                .append("PREFIX sawsdl:<http://www.w3.org/ns/sawsdl#> ")
                .append("PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> ")
                .append("PREFIX text: <http://jena.apache.org/text#> ")
                .append("SELECT * ")
                .append("WHERE { ?s text:query \"").append(query).append("\" . ")
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

        Set<FreeTextSearchResult> r = Sets.newLinkedHashSet();
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.next();
            RDFNode s = solution.get("s");
            if (s.isURIResource()) {
                try {
                    FreeTextSearchResult result = new FreeTextSearchResult(new URI(s.asResource().getURI()));
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
                        result.setModelReference(new URI(modelReference.getURI()));
                    }
                    r.add(result);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        return r;
    }

    @Override
    public Set<FreeTextSearchResult> search(String query, URI type) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX luc: <http://www.ontotext.com/owlim/lucene#> ")
                .append("PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> ")
                .append("PREFIX sawsdl:<http://www.w3.org/ns/sawsdl#> ")
                .append("PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> ")
                .append("PREFIX text: <http://jena.apache.org/text#> ")
                .append("SELECT * ")
                .append("WHERE { ?s text:query \"").append(query).append("\" . ")
                .append("OPTIONAL { ?s rdfs:label ?label . } ")
                .append("OPTIONAL { ?s rdfs:comment ?comment . } ")
                .append("OPTIONAL { ?s sawsdl:modelReference  ?modelReference . } ")
                .append("?s rdf:type <").append(type.toASCIIString()).append("> . } ");

        Query sparqlQuery = QueryFactory.create(queryBuilder.toString());
        return search(sparqlQuery);
    }
}
