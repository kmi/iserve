package uk.ac.open.kmi.iserve.discovery.api;

import com.google.common.eventbus.EventBus;
import com.google.gson.JsonElement;
import uk.ac.open.kmi.iserve.discovery.util.Pair;

import java.net.URI;
import java.net.URL;
import java.util.Map;

/**
 * Created by Luca Panziera on 25/02/15.
 */
public interface DiscoveryEngine {

    Map<URI, Pair<Double, MatchResult>> discover(String request, URL callback);

    Map<URI, Pair<Double, MatchResult>> discover(String request);

    Map<URI, Pair<Double, MatchResult>> discover(JsonElement request);

    Map<URI, Pair<Double, MatchResult>> discover(DiscoveryRequest discoveryRequest);

    EventBus getCallbackBus();

    // It returns discovery engine configuration description
    String toString();
}
