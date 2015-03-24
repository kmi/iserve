package uk.ac.open.kmi.iserve.discovery.api;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

/**
 * Created by Luca Panziera on 17/03/15.
 */
public class IODiscoveryFunction extends DiscoveryFunction {
    protected String operator;
    protected String type;
    protected String parameterType;
    protected Set<URI> parameters;

    public IODiscoveryFunction(JsonObject expression) {
        super(null);
        parse(expression);
    }

    public IODiscoveryFunction(JsonElement expression, String type, String parameterType) {
        super(null);
        this.type = type;
        this.parameterType = parameterType;
        parse(expression);
    }

    private void parse(JsonElement discovery) {
        if (discovery.isJsonObject()) {
            if (discovery.getAsJsonObject().has("io-rdfs")) {
                type = discovery.getAsJsonObject().getAsJsonObject("io-rdfs").get("type").getAsString();
            }
            if (discovery.getAsJsonObject().has("input") || discovery.getAsJsonObject().has("output")) {
                if (discovery.getAsJsonObject().has("input")) {
                    subFunctions.add(new IODiscoveryFunction(discovery.getAsJsonObject().get("input"), type, "input"));
                }
                if (discovery.getAsJsonObject().has("output")) {
                    subFunctions.add(new IODiscoveryFunction(discovery.getAsJsonObject().get("output"), type, "output"));
                }
            } else {
                JsonElement operatorEl = null;
                if (discovery.getAsJsonObject().has("and")) {
                    operator = "and";
                    operatorEl = discovery.getAsJsonObject().get("and");
                } else if (discovery.getAsJsonObject().has("or")) {
                    operator = "or";
                    operatorEl = discovery.getAsJsonObject().get("or");
                } else if (discovery.getAsJsonObject().has("diff")) {
                    operator = "diff";
                    operatorEl = discovery.getAsJsonObject().get("diff");
                }
                if (operatorEl.isJsonPrimitive()) {
                    try {
                        parameters.add(new URI(operatorEl.getAsString()));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                } else if (operatorEl.isJsonArray()) {
                    for (JsonElement clazz : operatorEl.getAsJsonArray()) {
                        if (clazz.isJsonPrimitive()) {
                            if (operator.equals("diff")) {
                                subFunctions.add(new IODiscoveryFunction(clazz, type, parameterType));
                            } else {
                                try {
                                    parameters.add(new URI(clazz.getAsString()));
                                } catch (URISyntaxException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (clazz.isJsonObject()) {
                            subFunctions.add(new IODiscoveryFunction(clazz, type, parameterType));
                        }
                    }
                }
            }
        } else if (discovery.isJsonArray()) {
            for (JsonElement el : discovery.getAsJsonArray()) {
                if (el.isJsonObject()) {
                    subFunctions.add(new IODiscoveryFunction(el, type, parameterType));
                } else if (el.isJsonPrimitive()) {
                    try {
                        parameters.add(new URI(el.getAsString()));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (discovery.isJsonPrimitive()) {
            try {
                parameters.add(new URI(discovery.getAsString()));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<URI, MatchResult> compute() {
        Map<URI, MatchResult> resultMap = Maps.newHashMap();
        if (!subFunctions.isEmpty()) {
            for (DiscoveryFunction subFunction : subFunctions) {
                subFunction.fork();
            }
        }
        if (!parameters.isEmpty()) {
            if (operator == null) {
                operator = "or";
            }
            if (operator.equals("or")) {
                if (type.equals("op") && parameterType.equals("input")) {
                    return operationDiscoverer.findOperationsConsumingSome(parameters);
                }
                if (type.equals("op") && parameterType.equals("output")) {
                    return operationDiscoverer.findOperationsProducingSome(parameters);
                }
                if (type.equals("svc") && parameterType.equals("input")) {
                    return serviceDiscoverer.findServicesConsumingSome(parameters);
                }
                if (type.equals("svc") && parameterType.equals("output")) {
                    return serviceDiscoverer.findServicesProducingSome(parameters);
                }
            }
            if (operator.equals("and")) {
                if (type.equals("op") && parameterType.equals("input")) {
                    return operationDiscoverer.findOperationsConsumingAll(parameters);
                }
                if (type.equals("op") && parameterType.equals("output")) {
                    return operationDiscoverer.findOperationsProducingAll(parameters);
                }
                if (type.equals("svc") && parameterType.equals("input")) {
                    return serviceDiscoverer.findServicesConsumingAll(parameters);
                }
                if (type.equals("svc") && parameterType.equals("output")) {
                    return serviceDiscoverer.findServicesProducingAll(parameters);
                }
            }
        }
        if (!subFunctions.isEmpty()) {
            for (DiscoveryFunction subFunction : subFunctions) {
                Map<URI, MatchResult> subResult = subFunction.join();
                if (operator.equals("or")) {
                    resultMap.putAll(subResult);
                }
                if (operator.equals("and")) {
                    if (resultMap.isEmpty()) {
                        resultMap.putAll(subResult);
                    } else {
                        resultMap.keySet().retainAll(subResult.keySet());
                    }
                }
                if (operator.equals("diff")) {
                    if (resultMap.isEmpty()) {
                        resultMap.putAll(subResult);
                    } else {
                        resultMap.keySet().removeAll(subResult.keySet());
                    }
                }
            }
        }


        return resultMap;
    }

}
