package uk.ac.open.kmi.iserve.discovery.api;

import java.util.Map;
import java.util.Set;

/**
 * Created by Luca Panziera on 10/11/14.
 *
 * TODO: Structure this better so that we distinguish between scorers, rankers, filters, etc
 *
 */
public class DiscoveryRequest {
    private DiscoveryFunction discoveryFunction;
    private Set<Class> requestedModules;
    private boolean filter;
    private boolean rank;
    private Map<Class, String> moduleParametersMap;
    private String rankingType;

    public DiscoveryRequest(DiscoveryFunction discoveryFunction, Set<Class> requestedModules, boolean filter, boolean rank, Map<Class, String> moduleParametersMap, String rankingType) {
        this.discoveryFunction = discoveryFunction;
        this.requestedModules = requestedModules;
        this.filter = filter;
        this.rank = rank;
        this.moduleParametersMap = moduleParametersMap;
        this.rankingType = rankingType;
    }

    public DiscoveryFunction getDiscoveryFunction() {
        return discoveryFunction;
    }

    public boolean filter() {
        return filter;
    }

    public boolean rank() {
        return rank;
    }

    public Set<Class> getRequestedModules() {
        return requestedModules;
    }

    public Map<Class, String> getModuleParametersMap() {
        return moduleParametersMap;
    }

    public String getRankingType() {
        return rankingType;
    }
}
