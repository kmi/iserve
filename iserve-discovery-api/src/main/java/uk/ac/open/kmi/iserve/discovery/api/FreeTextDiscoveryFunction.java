package uk.ac.open.kmi.iserve.discovery.api;

import uk.ac.open.kmi.iserve.discovery.api.freetextsearch.FreeTextSearchPlugin;

import java.net.URI;
import java.util.Map;

/**
 * Created by Luca Panziera on 10/11/14.
 */
public class FreeTextDiscoveryFunction extends DiscoveryFunction {
    private FreeTextSearchPlugin freeTextSearchPlugin;
    private String query;
    private URI type;

    public FreeTextDiscoveryFunction(FreeTextSearchPlugin freeTextSearchPlugin, String query, URI type) {
        this.freeTextSearchPlugin = freeTextSearchPlugin;
        this.query = query;
        this.type = type;
    }

    @Override
    public Map<URI, MatchResult> compute() {
        return freeTextSearchPlugin.search(query, type);
    }
}
