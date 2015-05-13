package uk.ac.open.kmi.iserve.discovery.api;

import com.google.gson.JsonElement;
import uk.ac.open.kmi.iserve.discovery.util.Pair;

import java.net.URI;
import java.util.Map;

/**
 * Created by Luca Panziera on 25/02/15.
 */
public interface DiscoveryEngine {

    Map<URI, Pair<Double, MatchResult>> discover(String request, boolean frequent);

    Map<URI, Pair<Double, MatchResult>> discover(JsonElement request);

    Map<URI, Pair<Double, MatchResult>> discover(DiscoveryRequest discoveryRequest);

    // It returns discovery engine configuration description
    String toString();
}
