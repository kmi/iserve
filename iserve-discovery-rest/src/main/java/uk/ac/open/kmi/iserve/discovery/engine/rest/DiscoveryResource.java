///*
// * Copyright (c) 2013. Knowledge Media Institute - The Open University
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package uk.ac.open.kmi.iserve.discovery.engine;
//
//import com.google.common.base.Function;
//import com.google.common.collect.ImmutableMap;
//import com.google.common.collect.Maps;
//import com.google.common.collect.Ordering;
//import org.apache.abdera.model.Feed;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
//import uk.ac.open.kmi.iserve.discovery.disco.BasicScorer;
//import uk.ac.open.kmi.iserve.discovery.util.MatchResultComparators;
//
//import javax.ws.rs.*;
//import javax.ws.rs.core.*;
//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.transform.TransformerConfigurationException;
//import java.io.IOException;
//import java.net.URL;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Set;
//
///**
// * Resource providing access to the discovery infrastructure.
// * <p/>
// * TODO: Provide pagination support for discovery results
// * TODO: Enable the configuration of ordering and scoring functionality for
// * discovery results (composite or simple)
// * TODO: Provide a thread pool for more efficient discovery?
// *
// * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
// */
//@Path("/disco")
//public class DiscoveryResource {
//
//    // Base URI for this resource
//    @Context
//    UriInfo uriInfo;
//
//    public static String NEW_LINE = System.getProperty("line.separator");
//
//    private static final Logger log = LoggerFactory.getLogger(DiscoveryResource.class);
//
//    private Map<String, ServiceDiscoveryPlugin> servicePlugins = new HashMap<String, ServiceDiscoveryPlugin>();
//    private Map<String, OperationDiscoveryPlugin> operationPlugins = new HashMap<String, OperationDiscoveryPlugin>();
//
//    public DiscoveryResource() throws TransformerConfigurationException, IOException,
//            ParserConfigurationException {
//
//        // Register all plugins manually for now
//        DiscoveryPlugin plugin;
//        // TODO; Broken
//        // plugin = new AllServicesPlugin();
//        // registerPlugin(plugin);
//
//        // plugin = new RDFSInputOutputDiscoveryPlugin();
//        // registerPlugin(plugin);
//
//        // plugin = new RDFSClassificationDiscoveryPlugin();
//        // registerPlugin(plugin);
//
//        //		plugin = new IMatcherDiscoveryPlugin(connector);
//        //		plugins.put(plugin.getName(), plugin);
//
//
//    }
//
//    /**
//     * Registers a plugin within the discovery infrastructure
//     *
//     * @param plugin The plugin to be registered
//     */
//    public void registerPlugin(DiscoveryPlugin plugin) {
//        if (plugin != null) {
//            // Register the plugin for the appropriate kinds
//            if (plugin instanceof ServiceDiscoveryPlugin) {
//                servicePlugins.put(plugin.getName(), (ServiceDiscoveryPlugin) plugin);
//            }
//
//            if (plugin instanceof OperationDiscoveryPlugin) {
//                operationPlugins.put(plugin.getName(), (OperationDiscoveryPlugin) plugin);
//            }
//        }
//    }
//
//    @GET
//    @Produces(MediaType.TEXT_HTML)
//    public Response listPluginsRegistered() throws WebApplicationException {
//        return Response.ok(generateListOfPlugins()).build();
//    }
//
//    /**
//     * Generates an HTML page with the list of plugins currently loaded, their
//     * URLs, and further details
//     *
//     * @return
//     */
//    private String generateListOfPlugins() {
//        StringBuffer result = new StringBuffer(insertHeader());
//        result.append("<H2>iServe Discovery Plugins Registered</H2>");
//        result.append("<H3>Service Discovery Plugins</H3>");
//        result.append(generatePluginsTable(ServiceDiscoveryPlugin.class, servicePlugins));
//        result.append("<H3>Operation Discovery Plugins</H3>");
//        result.append(generatePluginsTable(OperationDiscoveryPlugin.class, operationPlugins));
//        result.append(insertFooter());
//        return result.toString();
//    }
//
//    /**
//     * Generate an HTML table with the details of the plugins in the Map handed
//     *
//     * @param <T>          the type of plugins which extends Discovery Plugin
//     * @param typeOfPlugin the type of plugin being listed
//     * @param pluginsMap   The map of plugins to use
//     * @return An HTML table with the details of the plugins in the map
//     */
//    private <T extends DiscoveryPlugin> String generatePluginsTable(Class typeOfPlugin, Map<String, T> pluginsMap) {
//
//        StringBuffer result = new StringBuffer();
//        if (pluginsMap != null && !pluginsMap.isEmpty()) {
//            Set<Entry<String, T>> plugins = pluginsMap.entrySet();
//
//            // Create table
//            result.append("<table border=\"1\" width=\"90%\"><tr>" + NEW_LINE);
//            result.append("<th>Plugin</th>" + NEW_LINE);
//            result.append("<th>Version</th>" + NEW_LINE);
//            result.append("<th>Description</th>" + NEW_LINE);
//            result.append("<th>Parameters</th>" + NEW_LINE);
//            result.append("<th>Endpoint</th>" + NEW_LINE);
//            result.append("</tr>" + NEW_LINE);
//
//            String interPath = "";
//            for (java.util.Map.Entry<String, T> entry : plugins) {
//                DiscoveryPlugin regPlugin = entry.getValue();
//                result.append("<tr>" + NEW_LINE);
//                result.append("<td>" + regPlugin.getName() + "</td>" + NEW_LINE);
//                result.append("<td>" + regPlugin.getVersion() + "</td>" + NEW_LINE);
//                result.append("<td>" + regPlugin.getDescription() + "</td>" + NEW_LINE);
//
//                // List parameter details
//                result.append("<td><ul>");
//                Set<Entry<String, String>> parameters = regPlugin.getParametersDetails().entrySet();
//                for (Entry<String, String> param : parameters) {
//                    result.append("<li><b>" + param.getKey() + "</b>: " + param.getValue() + "</li>" + NEW_LINE);
//                }
//                result.append("</ul></td>" + NEW_LINE);
//
//                // Include the endpoint
//                UriBuilder ub = uriInfo.getAbsolutePathBuilder();
//                // Fix the intermediate path
//                if (typeOfPlugin.equals(OperationDiscoveryPlugin.class)) {
//                    interPath = "op";
//                }
//                if (typeOfPlugin.equals(ServiceDiscoveryPlugin.class)) {
//                    interPath = "svc";
//                }
//                String pluginUri = ub.path(interPath + "/" + regPlugin.getName()).build().toString();
//                result.append("<td><a href=" + pluginUri + ">" + pluginUri + "</a></td>" + NEW_LINE);
//                result.append("</tr>" + NEW_LINE);
//            }
//
//            // Close table
//            result.append("</table>" + NEW_LINE);
//
//        } else {
//            result.append("No plugins registered." + NEW_LINE);
//        }
//
//        return result.toString();
//    }
//
//    /**
//     * TODO: Replace with a common HTML footer for all iServe related pages
//     *
//     * @return
//     */
//    private String insertFooter() {
//        StringBuffer result = new StringBuffer("</body>" + NEW_LINE);
//        result.append("</html>" + NEW_LINE);
//        return result.toString();
//    }
//
//    /**
//     * TODO: Replace with a common HTML header for all iServe related pages
//     *
//     * @return
//     */
//    private String insertHeader() {
//        StringBuffer result = new StringBuffer("<!DOCTYPE html>" + NEW_LINE);
//        result.append("<html lang=\"en-UK\">" + NEW_LINE);
//        result.append("<head>" + NEW_LINE);
//        result.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />" + NEW_LINE);
//        result.append("<title>iServe Service Discovery Plugins</title>" + NEW_LINE);
//        result.append("</head>" + NEW_LINE);
//        result.append("<body>" + NEW_LINE);
//
//        return result.toString();
//    }
//
//    @GET
//    @Produces({MediaType.APPLICATION_ATOM_XML,
//            MediaType.APPLICATION_JSON,
//            MediaType.TEXT_XML})
//    @Path("/op/{name}")
//    public Response discoverOperations(@PathParam("name") String name, @Context UriInfo ui) throws WebApplicationException {
//        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
//
//        // find operation discovery plug-in
//        OperationDiscoveryPlugin plugin = operationPlugins.get(name);
//        if (plugin == null) {
//            throw new NotFoundException("Plugin named '" + name + "' is not available for operation discovery.");
//        }
//
//        Map<URL, MatchResult> matchingResults;
//        try {
//            // apply it
//            matchingResults = plugin.discoverOperations(queryParams);
//        } catch (DiscoveryException e) {
//            throw new WebApplicationException(e, e.getStatus());
//        }
//
//        Feed feed = generateFeed(rank(matchingResults), plugin);
//        return Response.ok(feed).build();
//    }
//
//    @GET
//    @Produces({MediaType.APPLICATION_ATOM_XML,
//            MediaType.APPLICATION_JSON,
//            MediaType.TEXT_XML})
//    @Path("/svc/{name}")
//    public Response discoverServices(@PathParam("name") String name,
//                                     @Context UriInfo ui) throws WebApplicationException {
//
//        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
//
//        // find service discovery plug-in
//        ServiceDiscoveryPlugin plugin = servicePlugins.get(name);
//        if (plugin == null) {
//            throw new NotFoundException("Plugin named '" + name +
//                    "' is not available for service discovery.");
//        }
//
//        long startTime = System.currentTimeMillis();
//        long endTime;
//        long duration;
//
//        Map<URL, MatchResult> matchingResults;
//        try {
//            // apply it
//            matchingResults = plugin.discoverServices(queryParams);
//        } catch (DiscoveryException e) {
//            throw new WebApplicationException(e, e.getStatus());
//        }
//
//        endTime = System.currentTimeMillis();
//        duration = endTime - startTime;
//        log.info("Time elapsed during discovery: " + duration);
//
//        startTime = System.currentTimeMillis();
//
//        Map<URL, MatchResult> rankedResults = rank(matchingResults);
//
//        endTime = System.currentTimeMillis();
//        duration = endTime - startTime;
//        log.info("Time elapsed during ranking: " + duration);
//
//        // Generate feed out of the ranked list of matches
//        startTime = System.currentTimeMillis();
//
//        Feed feed = generateFeed(rankedResults, plugin);
//
//        endTime = System.currentTimeMillis();
//        duration = endTime - startTime;
//        log.info("Time elapsed generating the feed: " + duration);
//
//        return Response.ok(feed).build();
//    }
//
//    /**
//     * Primitive Ranker
//     * TODO: Move out to its place as a configurable ranking engine
//     * in terms of both scoring and ordering functions
//     *
//     * @param matchingResults
//     * @return
//     */
//    private Map<URL, MatchResult> rank(Map<URL, MatchResult> matchingResults) {
//
//        if (matchingResults == null) {
//            return null;
//        }
//
//        Function<Map.Entry<URL, MatchResult>, MatchResult> getMatchResult =
//                new Function<Map.Entry<URL, MatchResult>, MatchResult>() {
//                    public MatchResult apply(Map.Entry<URL, MatchResult> entry) {
//                        return entry.getValue();
//                    }
//                };
//
//        // Order the results by score and then by url
//        Ordering<Map.Entry<URL, MatchResult>> entryOrdering =
//                Ordering.from(MatchResultComparators.BY_TYPE).onResultOf(getMatchResult).reverse().
//                        compound(Ordering.from(MatchResultComparators.BY_URI).onResultOf(getMatchResult));
//
//        // First obtain the map with the values scored, then order it
//        Map<URL, MatchResult> scoredMap = Maps.transformValues(matchingResults, new BasicScorer());
//
//        // Desired entries in desired order.  Put them in an ImmutableMap in this order.
//        ImmutableMap.Builder<URL, MatchResult> builder = ImmutableMap.builder();
//        for (Entry<URL, MatchResult> entry : entryOrdering.sortedCopy(scoredMap.entrySet())) {
//            builder.put(entry.getKey(), entry.getValue());
//        }
//
//        return builder.build();
//    }
//
//}
