package uk.ac.open.kmi.iserve.discovert.hazelcast;

import com.google.common.collect.Table;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.api.MatchTypes;
import uk.ac.open.kmi.iserve.discovery.api.Matcher;
import uk.ac.open.kmi.iserve.discovery.api.impl.AtomicMatchResult;
import uk.ac.open.kmi.iserve.discovery.api.impl.EnumMatchTypes;
import uk.ac.open.kmi.iserve.discovery.disco.DiscoMatchType;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.discovery.disco.impl.SparqlLogicConceptMatcher;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.manager.IntegratedComponent;
import uk.ac.open.kmi.iserve.sal.manager.iServeManager;

import javax.inject.Named;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Pablo Rodríguez Mier
 */
public class HazelcastIndexedConceptMatcher extends IntegratedComponent implements Matcher {

    private IMap<URI, Map<URI, String>> map;
    private iServeManager manager;
    private SparqlLogicConceptMatcher matcher;

    @Inject
    protected HazelcastIndexedConceptMatcher(EventBus eventBus,
                                             ClientConfig hazelcastClientConfig,
                                             iServeManager iServeManager,
                                             SparqlLogicConceptMatcher sparqlMatcher,
                                             @Named("hazelcast.map.id") String hzMapId,
                                             @Named("iserve.url") String iServeUri) throws SalException {


        super(eventBus, iServeUri);
        // Load config from properties? injected through the constructor?
        //ClientConfig clientConfig = new ClientConfig();
        //clientConfig.addAddress(addr);  // config required
        HazelcastInstance client = HazelcastClient.newHazelcastClient(hazelcastClientConfig);
        // Request a new (default) map
        this.map = client.getMap(hzMapId);
        this.manager = iServeManager;
        this.matcher = sparqlMatcher;
        // Population strategy if the map is not populated correctly?
        // NOTE: Automatic mechanism for updating the indexes continuously when changes are
        // detected in the RDF store is required.
        populate(map);
    }

    // TODO: This should not be here. A separate crawler is required to update the index
    private void populate(IMap<URI,Map<URI, String>> map) {
        Set<URI> classes = new HashSet<URI>(this.manager.getKnowledgeBaseManager().listConcepts(null));
        Table<URI, URI, MatchResult> table = matcher.listMatchesAtLeastOfType(classes, LogicConceptMatchType.Plugin);
        for(URI origin : table.rowKeySet()){
            Map<URI, String> destMatch = new HashMap<URI, String>();
            Map<URI, MatchResult> dest = table.row(origin);
            for(URI destUri : dest.keySet()){
                destMatch.put(destUri, dest.get(destUri).getMatchType().name());
            }
            map.put(origin, destMatch);
        }
    }

    @Override
    public String getMatcherDescription() {
        return "HazelCast indexed matcher";
    }

    @Override
    public String getMatcherVersion() {
        return "HazelCast indexed matcher, v0.1";
    }

    @Override
    public MatchTypes<MatchType> getMatchTypesSupported() {
        return EnumMatchTypes.of(LogicConceptMatchType.class);
    }

    @Override
    public MatchResult match(URI origin, URI destination) {
        // Get match by origin
        String matchType = this.map.get(origin).get(destination);
        if (matchType!=null){
            // Disco match type
            DiscoMatchType type = DiscoMatchType.valueOf(matchType);
            return new AtomicMatchResult(origin, destination, type, this);

        }
        // Return fail
        return new AtomicMatchResult(origin, destination, DiscoMatchType.Fail, this);
    }
}
