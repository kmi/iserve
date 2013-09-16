package uk.ac.open.kmi.iserve.discovery.disco.hazelcast;

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
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.manager.IntegratedComponent;

import javax.inject.Named;
import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * @author Pablo Rodríguez Mier
 */
public class HazelcastIndexedConceptMatcher extends IntegratedComponent implements Matcher {

    private IMap<URI, Map<URI, String>> map;

    @Inject
    protected HazelcastIndexedConceptMatcher(EventBus eventBus,
                                             @Named("iserve.url") String iServeUri,
                                             @Named("iserve.hazelcast.address") String addr // ip:port
                                             ) throws SalException {


        super(eventBus, iServeUri);
        // Load config from properties? injected through the constructor?
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.addAddress(addr);  // config required
        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
        // Request a new (default) map
        this.map = client.getMap(this.getClass().getName());
    }

    @Override
    public String getMatcherDescription() {
        return "HazelCast indexed matcher";
    }

    @Override
    public String getMatcherVersion() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
