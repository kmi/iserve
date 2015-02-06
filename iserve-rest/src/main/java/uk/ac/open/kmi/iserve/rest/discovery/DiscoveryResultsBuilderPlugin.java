package uk.ac.open.kmi.iserve.rest.discovery;

import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.util.Pair;

import java.net.URI;
import java.util.Map;

/**
 * Created by Luca Panziera on 08/08/2014.
 */
public interface DiscoveryResultsBuilderPlugin {
    public Map<URI, DiscoveryResult> build(Map<URI, Pair<Double, MatchResult>> result, String rankingType);
}
