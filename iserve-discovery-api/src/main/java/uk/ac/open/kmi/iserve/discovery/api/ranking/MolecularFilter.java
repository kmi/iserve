package uk.ac.open.kmi.iserve.discovery.api.ranking;

import com.google.common.collect.Sets;

import java.net.URI;
import java.util.Set;

/**
 * Created by Luca Panziera on 05/11/14.
 */
public class MolecularFilter implements Filter {
    private AtomicFilter atomicFilter;

    public MolecularFilter(AtomicFilter atomicFilter) {
        this.atomicFilter = atomicFilter;
    }

    @Override
    public Set<URI> apply(Set<URI> resources) {
        return Sets.filter(resources, atomicFilter);
    }
}
