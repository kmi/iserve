package uk.ac.open.kmi.iserve.discovery.ranking.impl;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import uk.ac.open.kmi.iserve.discovery.api.ranking.Filter;
import uk.ac.open.kmi.iserve.sal.manager.NfpManager;
import uk.ac.open.kmi.msm4j.vocabulary.MSM_SWAGGER;

import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * Created by Luca Panziera on 28/11/14.
 */
public class SwaggerServiceFilter implements Filter {

    private NfpManager nfpManager;

    @Inject
    SwaggerServiceFilter(NfpManager nfpManager) {
        this.nfpManager = nfpManager;
    }

    @Override
    public Set<URI> apply(Set<URI> resources) {
        Map<URI, Object> propertyMap = nfpManager.getPropertyValueOfResources(resources, URI.create(MSM_SWAGGER.isGroundedIn.getURI()), URI.class);
        return Sets.filter(resources, new SwaggerServicePredicate(propertyMap));
    }

    @Override
    public Set<URI> apply(Set<URI> resources, String parameters) {
        return apply(resources);
    }


    private class SwaggerServicePredicate implements Predicate<URI> {

        private final Map<URI, Object> propertyMap;

        public SwaggerServicePredicate(Map<URI, Object> propertyMap) {
            this.propertyMap = propertyMap;
        }

        @Override
        public boolean apply(URI resource) {
            if (propertyMap.containsKey(resource) && propertyMap.get(resource) != null) {
                return true;
            }
            return false;
        }
    }
}
