package uk.ac.open.kmi.iserve.rest.discovery;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.hp.hpl.jena.vocabulary.RDFS;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.util.Pair;
import uk.ac.open.kmi.iserve.sal.manager.NfpManager;
import uk.ac.open.kmi.msm4j.vocabulary.MSM_NFP;
import uk.ac.open.kmi.msm4j.vocabulary.SAWSDL;
import uk.ac.open.kmi.msm4j.vocabulary.SCHEMA;

import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * Created by Luca Panziera on 23/07/2014.
 */
public class DiscoveryResultsBuilder implements DiscoveryResultsBuilderPlugin {

    private NfpManager nfpManager;

    @Inject
    public DiscoveryResultsBuilder(NfpManager nfpManager) {
        this.nfpManager = nfpManager;
    }

    public Map<URI, DiscoveryResult> build(Map<URI, Pair<Double, MatchResult>> result, String rankingType) {

        Map<URI, DiscoveryResult> r = Maps.newLinkedHashMap();

        // Get labels
        Map<URI, Object> labelsMap = nfpManager.getPropertyValueOfResources(result.keySet(), URI.create(RDFS.label.getURI()), String.class);
        // Get comments
        Map<URI, Object> commentsMap = nfpManager.getPropertyValueOfResources(result.keySet(), URI.create(RDFS.comment.getURI()), String.class);

        // Get modelReferences
        Map<URI, Object> modelReferenceMap = nfpManager.getPropertyValueOfResources(result.keySet(), URI.create(SAWSDL.modelReference.getURI()), URI.class);


        if (rankingType != null && (rankingType.equals("standard") || rankingType.equals("inverse"))) {
            //Get recent mashups
            Map<URI, Object> mashupsMap = nfpManager.getPropertyValueOfResources(result.keySet(), URI.create(MSM_NFP.hasRecentMashups.getURI()), String.class);

            Map<URI, Object> providersMap = nfpManager.getPropertyValueOfResources(result.keySet(), URI.create(SCHEMA.provider.getURI()), URI.class);

            Set<URI> providers = Sets.newHashSet();
            for (Object provider : providersMap.values()) {
                if (provider instanceof Set) {
                    providers.add((URI) ((Set) provider).iterator().next());
                } else {
                    providers.add((URI) provider);
                }

            }

            Map<URI, Object> popularityMap = nfpManager.getPropertyValueOfResources(providers, URI.create(MSM_NFP.hasPopularity.getURI()), String.class);

            Map<URI, Object> forumMap = nfpManager.getPropertyValueOfResources(result.keySet(), URI.create(MSM_NFP.hasForum.getURI()), URI.class);

            Set<URI> forums = Sets.newHashSet();
            for (Object forum : forumMap.values()) {
                forums.add((URI) forum);
            }

            Map<URI, Object> vitalityMap = nfpManager.getPropertyValueOfResources(forums, URI.create(MSM_NFP.hasVitality.getURI()), String.class);

            for (URI resource : result.keySet()) {
                DiscoveryResult discoveryResult = new DiscoveryResult();
                discoveryResult.setLabel((String) labelsMap.get(resource));
                discoveryResult.setDescription((String) commentsMap.get(resource));
                if (modelReferenceMap.get(resource) instanceof URI) {
                    discoveryResult.addModelReference((URI) modelReferenceMap.get(resource));
                }
                if (modelReferenceMap.get(resource) instanceof Set) {
                    discoveryResult.setModelReferences((Set<URI>) modelReferenceMap.get(resource));
                }
                discoveryResult.setMatchResult(result.get(resource).getRight());
                discoveryResult.setRankingScore(result.get(resource).getLeft());
                discoveryResult.addRankPropertyValue(URI.create(MSM_NFP.hasRecentMashups.getURI()), mashupsMap.get(resource));
                discoveryResult.addRankPropertyValue(URI.create(MSM_NFP.hasPopularity.getURI()), popularityMap.get(providersMap.get(resource)));
                discoveryResult.addRankPropertyValue(URI.create(MSM_NFP.hasVitality.getURI()), vitalityMap.get(forumMap.get(resource)));
                r.put(resource, discoveryResult);
            }

        } else {
            for (URI resource : result.keySet()) {
                DiscoveryResult discoveryResult = new DiscoveryResult();
                discoveryResult.setLabel((String) labelsMap.get(resource));
                discoveryResult.setDescription((String) commentsMap.get(resource));
                discoveryResult.setMatchResult(result.get(resource).getRight());
                if (modelReferenceMap.get(resource) instanceof URI) {
                    discoveryResult.addModelReference((URI) modelReferenceMap.get(resource));
                }
                if (modelReferenceMap.get(resource) instanceof Set) {
                    discoveryResult.setModelReferences((Set<URI>) modelReferenceMap.get(resource));
                }
                r.put(resource, discoveryResult);
            }
        }
        return r;
    }

}
