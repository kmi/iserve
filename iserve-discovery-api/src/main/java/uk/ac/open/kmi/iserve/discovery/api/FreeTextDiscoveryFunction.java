package uk.ac.open.kmi.iserve.discovery.api;

import com.google.gson.JsonObject;
import uk.ac.open.kmi.msm4j.vocabulary.MSM;

import java.net.URI;
import java.util.Map;

/**
 * Created by Luca Panziera on 10/11/14.
 */
public class FreeTextDiscoveryFunction extends DiscoveryFunction {
    private String query;
    private URI type;

    public FreeTextDiscoveryFunction(JsonObject discovery) {
        super(null);
        if (discovery.get("type").getAsString().equals("svc")) {
            type = URI.create(MSM.Service.getURI());
        } else if (discovery.get("type").getAsString().equals("op")) {
            type = URI.create(MSM.Operation.getURI());
        }
        query = discovery.get("query").getAsString();
    }

    @Override
    public Map<URI, MatchResult> compute() {
        return freeTextSearchPlugin.search(query, type);
    }

}
