/*
   Copyright ${year}  Knowledge Media Institute - The Open University

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package uk.ac.open.kmi.iserve.discovery.engine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.iserve.discovery.api.IServiceDiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.disco.AllServicesPlugin;
import uk.ac.open.kmi.iserve.discovery.disco.RDFSClassificationDiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.disco.RDFSInputOutputDiscoveryPlugin;

@Path("/disco")
public class DiscoveryResource {
	
	// Base URI for this resource
	@Context  UriInfo uriInfo;
	
	public static String NEW_LINE = System.getProperty("line.separator");
	private Map<String, IServiceDiscoveryPlugin> plugins;

	public DiscoveryResource() throws RepositoryException, 
		TransformerConfigurationException, IOException, 
		ParserConfigurationException {
		plugins = new HashMap<String, IServiceDiscoveryPlugin>();
		
		IServiceDiscoveryPlugin plugin;
		plugin = new AllServicesPlugin();
		plugins.put(plugin.getName(), plugin);

		plugin = new RDFSInputOutputDiscoveryPlugin(false);
		plugins.put(plugin.getName(), plugin);

		plugin = new RDFSClassificationDiscoveryPlugin(false);
		plugins.put(plugin.getName(), plugin);
	
//		plugin = new IMatcherDiscoveryPlugin(connector);
//		plugins.put(plugin.getName(), plugin);
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)	
	public Response listPluginsRegistered() throws WebApplicationException {
		return Response.ok(generateListOfPlugins()).build();
	}
	
	/**
	 * Generates an HTML page with the list of plugins currently loaded, their
	 * URLs, and further details
	 * 
	 * @return
	 */
	private String generateListOfPlugins() {
		
		StringBuffer result = new StringBuffer(insertHeader());
		
		if (plugins != null) {
			
			Set<java.util.Map.Entry<String, IServiceDiscoveryPlugin>> runningPlugins = 
				plugins.entrySet();
		
			result.append("<H2>iServe Discovery Plugins Registered</H2>");
			
			// Create table
			result.append("<table border=\"1\" width=\"90%\"><tr>" + NEW_LINE);
			result.append("<th>Plugin</th>"+ NEW_LINE);
			result.append("<th>Version</th>"+ NEW_LINE);
			result.append("<th>Description</th>"+ NEW_LINE);
			result.append("<th>Endpoint</th>"+ NEW_LINE);
			result.append("</tr>"+ NEW_LINE);
			
			for (java.util.Map.Entry<String, IServiceDiscoveryPlugin> entry : runningPlugins) {
				IServiceDiscoveryPlugin regPlugin = entry.getValue();
				result.append("<tr>"+ NEW_LINE);
				result.append("<td>" + regPlugin.getName() + "</td>" + NEW_LINE);
				result.append("<td>" + regPlugin.getVersion() + "</td>" + NEW_LINE);
				result.append("<td>" + regPlugin.getDescription() + "</td>" + NEW_LINE);
				UriBuilder ub = uriInfo.getAbsolutePathBuilder();
				String pluginUri = ub.path(regPlugin.getName()).build().toString();
				result.append("<td><a href=" + pluginUri + ">" + pluginUri + "</a></td>" + NEW_LINE);
				result.append("</tr>" + NEW_LINE);
			}
			
			// Close table
			result.append("</table>" + NEW_LINE);
			
		} else {
			result.append("No plugins registered." + NEW_LINE);
		}
		
		result.append(insertFooter());
		return result.toString();
	}

	/**
	 * TODO: Replace with a common HTML footer for all iServe related pages
	 * @return
	 */
	private String insertFooter() {
		StringBuffer result = new StringBuffer("</body>" + NEW_LINE);
		result.append("</html>" + NEW_LINE);
		return result.toString();
	}

	/**
	 * TODO: Replace with a common HTML header for all iServe related pages
	 * @return
	 */
	private String insertHeader() {
		StringBuffer result = new StringBuffer("<!DOCTYPE html>" + NEW_LINE);
		result.append("<html lang=\"en-UK\">" + NEW_LINE);
		result.append("<head>" + NEW_LINE);
		result.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />" + NEW_LINE);
		result.append("<title>iServe Service Discovery Plugins</title>" + NEW_LINE);
		result.append("</head>" + NEW_LINE);
		result.append("<body>" + NEW_LINE);
		
		return result.toString();
	}

}
