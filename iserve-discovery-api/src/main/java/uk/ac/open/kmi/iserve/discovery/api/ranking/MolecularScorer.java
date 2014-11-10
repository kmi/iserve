package uk.ac.open.kmi.iserve.discovery.api.ranking;

import com.google.common.collect.Maps;

import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * Created by Luca Panziera on 05/11/14.
 */
public class MolecularScorer implements Scorer {

    private AtomicScorer atomicScorer;

    public MolecularScorer(AtomicScorer atomicScorer) {
        this.atomicScorer = atomicScorer;
    }

    @Override
    public Map<URI, Double> apply(Set<URI> resources) {
        return Maps.toMap(resources, atomicScorer);
    }

    @Override
    public Map<URI, Double> apply(Set<URI> resource, String parameters) {
        return apply(resource);
    }
}
