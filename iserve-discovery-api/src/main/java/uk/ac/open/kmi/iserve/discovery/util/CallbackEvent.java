package uk.ac.open.kmi.iserve.discovery.util;

import uk.ac.open.kmi.iserve.discovery.api.MatchResult;

import java.net.URI;
import java.net.URL;
import java.util.Map;

/**
 * Created by Luca Panziera on 14/05/15.
 */
public class CallbackEvent {
    private URL callback;
    private Map<URI, Pair<Double, MatchResult>> result;

    public CallbackEvent(URL callback, Map<URI, Pair<Double, MatchResult>> result) {
        this.callback = callback;
        this.result = result;
    }

    public URL getCallback() {
        return callback;
    }

    public Map<URI, Pair<Double, MatchResult>> getResult() {
        return result;
    }
}
