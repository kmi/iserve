package uk.ac.open.kmi.iserve.discovery.api.freetextsearch.sparql.impl;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.ConfigurationProperty;
import uk.ac.open.kmi.iserve.core.iServeProperty;

/**
 * Created by Luca Panziera on 26/08/2014.
 */
@Singleton
public class OwlimSearchPlugin extends SparqlSearchPlugin {

    //TODO implement it as proper singleton
    static boolean indexed = false;
    private Logger logger = LoggerFactory.getLogger(OwlimSearchPlugin.class);

    @Inject
    public OwlimSearchPlugin(@iServeProperty(ConfigurationProperty.SERVICES_SPARQL_QUERY) String queryEndpoint, @iServeProperty(ConfigurationProperty.SERVICES_SPARQL_UPDATE) String updateEndpoint) {
        super("http://www.ontotext.com/owlim/lucene#entityIndex");
        logger.debug("Creating instance of {}", this.getClass().getName());
        this.queryEndpoint = queryEndpoint;
        this.updateEndpoint = updateEndpoint;
        runIndexing();
    }

    private void runIndexing() {
        if (!indexed) {
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
            indexed = true;
        }

    }


}
