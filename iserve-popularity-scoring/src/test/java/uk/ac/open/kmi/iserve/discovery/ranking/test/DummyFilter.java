package uk.ac.open.kmi.iserve.discovery.ranking.test;

import uk.ac.open.kmi.iserve.discovery.api.ranking.Filter;

import java.net.URI;
import java.util.Set;

/**
 * Created by Luca Panziera on 10/11/14.
 */
public class DummyFilter implements Filter {
    @Override
    public Set<URI> apply(Set<URI> resource) {
        return resource;
    }

    @Override
    public Set<URI> apply(Set<URI> resource, String parameters) {
        return resource;
    }
}
