package uk.ac.open.kmi.iserve.discovery.api.freetextsearch;

import uk.ac.open.kmi.iserve.discovery.api.MatchResult;

import java.net.URI;
import java.util.Map;

/**
 * Created by Luca Panziera on 26/08/2014.
 */
public interface FreeTextSearchPlugin {
    Map<URI, MatchResult> search(String query);

    Map<URI, MatchResult> search(String query, URI type);
}
