package uk.ac.open.kmi.iserve.discovery.ranking.test;

import uk.ac.open.kmi.iserve.discovery.api.ranking.AtomicFilter;

import java.net.URI;

/**
 * Created by Luca Panziera on 10/11/14.
 */
public class DummyAtomicFilter implements AtomicFilter {
    @Override
    public boolean apply(URI resource) {
        return true;
    }

    @Override
    public boolean apply(URI resource, String parameter) {
        return true;
    }
}
