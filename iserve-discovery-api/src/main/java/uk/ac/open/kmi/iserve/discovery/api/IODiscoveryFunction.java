package uk.ac.open.kmi.iserve.discovery.api;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Luca Panziera on 17/03/15.
 */
public class IODiscoveryFunction extends DiscoveryFunction {
    protected String operator;
    protected String type;
    protected String parameterType;
    protected Set<URI> parameters = new LinkedHashSet<URI>();

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
                if (discovery.getAsJsonObject().getAsJsonObject("io-rdfs").has("input") || discovery.getAsJsonObject().getAsJsonObject("io-rdfs").has("output")) {
                    if (discovery.getAsJsonObject().getAsJsonObject("io-rdfs").has("input")) {
                        subFunctions.add(new IODiscoveryFunction(discovery.getAsJsonObject().getAsJsonObject("io-rdfs").get("input"), type, "input"));
                    }
                    if (discovery.getAsJsonObject().getAsJsonObject("io-rdfs").has("output")) {
                        subFunctions.add(new IODiscoveryFunction(discovery.getAsJsonObject().getAsJsonObject("io-rdfs").get("output"), type, "output"));
                    }
                } else {
                    subFunctions.add(new IODiscoveryFunction(discovery.getAsJsonObject().getAsJsonObject("io-rdfs"), type, null));
                }
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
                    //TODO INSERT PARSING
                } else if (discovery.getAsJsonObject().has("or")) {
                    operator = "or";
                    operatorEl = discovery.getAsJsonObject().get("or");
                } else if (discovery.getAsJsonObject().has("diff")) {
                    operator = "diff";
                    operatorEl = discovery.getAsJsonObject().get("diff");
                }
                if (operatorEl != null && operatorEl.isJsonPrimitive()) {
                    try {
                        parameters.add(new URI(operatorEl.getAsString()));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                } else if (operatorEl != null && operatorEl.isJsonArray()) {
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
                } else if (operatorEl != null && operatorEl.isJsonObject()) {
                    if (operatorEl.getAsJsonObject().has("input")) {
                        subFunctions.add(new IODiscoveryFunction(operatorEl.getAsJsonObject().get("input"), type, "input"));
                    }
                    if (operatorEl.getAsJsonObject().has("output")) {
                        subFunctions.add(new IODiscoveryFunction(operatorEl.getAsJsonObject().get("output"), type, "output"));
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
                String value = discovery.getAsJsonPrimitive().getAsString();
                parameters.add(new URI(value));
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
                if (operator == null || operator.equals("or")) {
                    resultMap.putAll(subResult);
                } else if (operator.equals("and")) {
                    if (resultMap.isEmpty()) {
                        resultMap.putAll(subResult);
                    } else {
                        resultMap.keySet().retainAll(subResult.keySet());
                    }
                } else if (operator.equals("diff")) {
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
