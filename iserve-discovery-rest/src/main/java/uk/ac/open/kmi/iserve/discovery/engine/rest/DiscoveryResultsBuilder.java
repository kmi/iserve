package uk.ac.open.kmi.iserve.discovery.engine.rest;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import uk.ac.open.kmi.iserve.core.ConfigurationProperty;
import uk.ac.open.kmi.iserve.core.iServeProperty;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.util.Pair;

import java.net.URI;
import java.util.Map;

/**
 * Created by Luca Panziera on 23/07/2014.
 */
public class DiscoveryResultsBuilder implements DiscoveryResultsBuilderPlugin {

    private String sparqlEndpoint;

    @Inject
    public DiscoveryResultsBuilder(@iServeProperty(ConfigurationProperty.SERVICES_SPARQL_QUERY) String sparqlEndpoint) {
        this.sparqlEndpoint = sparqlEndpoint;
    }

    public Map<URI, DiscoveryResult> build(Map<URI, Pair<Double, MatchResult>> result, String rankingType) {

        Map<URI, DiscoveryResult> r = Maps.newLinkedHashMap();

        if (rankingType != null && (rankingType.equals("standard") || rankingType.equals("inverse"))) {

            for (URI resource : result.keySet()) {
                DiscoveryResult discoveryResult = new DiscoveryResult();
                discoveryResult.setMatchResult(result.get(resource).getRight());
                discoveryResult.setRankingScore(result.get(resource).getLeft());
                r.put(resource, discoveryResult);
            }

        } else {
            for (URI resource : result.keySet()) {
                DiscoveryResult discoveryResult = new DiscoveryResult();
                discoveryResult.setMatchResult(result.get(resource).getRight());
                r.put(resource, discoveryResult);
            }
        }
        return r;
    }

}
