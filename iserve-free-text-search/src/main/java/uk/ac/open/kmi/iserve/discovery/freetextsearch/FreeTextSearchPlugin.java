package uk.ac.open.kmi.iserve.discovery.freetextsearch;

import java.net.URI;
import java.util.Set;

/**
 * Created by Luca Panziera on 26/08/2014.
 */
public interface FreeTextSearchPlugin {
    Set<FreeTextSearchResult> search(String query);

    Set<FreeTextSearchResult> search(String query, URI type);
}
