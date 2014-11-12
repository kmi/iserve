package uk.ac.open.kmi.iserve.discovery.api.freetextsearch.impl;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.ConfigurationProperty;
import uk.ac.open.kmi.iserve.core.iServeProperty;

/**
 * Created by Luca Panziera on 28/10/2014.
 */
public class FusekiSearchPlugin extends SparqlSearchPlugin {

    private Logger logger = LoggerFactory.getLogger(FusekiSearchPlugin.class);

    @Inject
    public FusekiSearchPlugin(@iServeProperty(ConfigurationProperty.SERVICES_SPARQL_QUERY) String queryEndpoint) {
        super("http://jena.apache.org/text#query");
        logger.debug("Creating instance of {}", this.getClass().getName());
        this.queryEndpoint = queryEndpoint;
    }

}
