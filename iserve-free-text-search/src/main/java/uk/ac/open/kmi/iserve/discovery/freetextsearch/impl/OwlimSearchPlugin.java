package uk.ac.open.kmi.iserve.discovery.freetextsearch.impl;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.SystemConfiguration;
import uk.ac.open.kmi.iserve.discovery.freetextsearch.FreeTextSearchPlugin;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * Created by Luca Panziera on 26/08/2014.
 */
@Singleton
public class OwlimSearchPlugin implements FreeTextSearchPlugin {

    private Logger logger = LoggerFactory.getLogger(OwlimSearchPlugin.class);
    private String queryEndpoint;
    private String updateEndpoint;

    @Inject
    public OwlimSearchPlugin(@Named(SystemConfiguration.SERVICES_REPOSITORY_SPARQL_PROP) String queryEndpoint, @Named(SystemConfiguration.SERVICES_REPOSITORY_SPARQL_UPDATE_PROP) String updateEndpoint) {
        this.queryEndpoint = queryEndpoint;
        this.updateEndpoint = updateEndpoint;
        runIndexing();
    }

    private void runIndexing() {
        logger.info("Free text search indexing...");
        StringBuilder updateBuilder = new StringBuilder();
        updateBuilder.append("PREFIX luc: <http://www.ontotext.com/owlim/lucene#> ")
                .append("INSERT DATA {")
                .append("luc:include luc:setParam \"literal uri\" . ")
                .append("luc:index luc:setParam \"literals, uri\" . ")
                .append("luc:moleculeSize luc:setParam \"1\" . ")
                .append("luc:entityIndex luc:createIndex \"true\" . }");
        UpdateRequest request = UpdateFactory.create();
        request.add(updateBuilder.toString());
        UpdateProcessor processor = UpdateExecutionFactory.createRemoteForm(request, updateEndpoint);
        processor.execute();
    }

    @Override
    public Set<URI> search(String query) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX luc: <http://www.ontotext.com/owlim/lucene#> ")
                .append("SELECT ?s ")
                .append("WHERE { ?s luc:entityIndex \"").append(query).append("\" . }");

        Query sparqlQuery = QueryFactory.create(queryBuilder.toString());
        return search(sparqlQuery);
    }

    private Set<URI> search(Query sparqlQuery) {
        logger.debug("Executing SPARQL query: {}", sparqlQuery);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(queryEndpoint.toString(), sparqlQuery);
        ResultSet resultSet = qexec.execSelect();

        Set<URI> r = Sets.newLinkedHashSet();
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.next();
            RDFNode s = solution.get("s");
            if (s.isURIResource()) {
                try {
                    r.add(new URI(s.asResource().getURI()));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        return r;
    }

    @Override
    public Set<URI> search(String query, URI type) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX luc: <http://www.ontotext.com/owlim/lucene#> ")
                .append("PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> ")
                .append("SELECT ?s ")
                .append("WHERE { ?s luc:entityIndex \"").append(query).append("\" . ")
                .append("?s rdf:type <").append(type.toASCIIString()).append("> . }");

        Query sparqlQuery = QueryFactory.create(queryBuilder.toString());
        return search(sparqlQuery);
    }
}
