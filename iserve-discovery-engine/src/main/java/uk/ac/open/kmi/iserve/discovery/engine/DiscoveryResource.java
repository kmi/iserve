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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.iserve.discovery.api.IServiceDiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.disco.AllServicesPlugin;
import uk.ac.open.kmi.iserve.discovery.disco.RDFSClassificationDiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.disco.RDFSInputOutputDiscoveryPlugin;

@Path("/disco")
public class DiscoveryResource {
	
	private Map<String, IServiceDiscoveryPlugin> plugins;
//	private RDFRepositoryConnector connector;
//	private ServiceManager serviceManager;

	public DiscoveryResource() throws RepositoryException, 
		TransformerConfigurationException, IOException, 
		ParserConfigurationException {
		plugins = new HashMap<String, IServiceDiscoveryPlugin>();

		IServiceDiscoveryPlugin plugin = new RDFSInputOutputDiscoveryPlugin(false);
		plugins.put(plugin.getName(), plugin);

//		plugin = new IMatcherDiscoveryPlugin(connector);
//		plugins.put(plugin.getName(), plugin);

		plugin = new RDFSClassificationDiscoveryPlugin(false);
		plugins.put(plugin.getName(), plugin);

		plugin = new AllServicesPlugin();
		plugins.put(plugin.getName(), plugin);
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
		
		String result = "";
		result += insertHeader();
		
		if (plugins != null) {
			
			Set<java.util.Map.Entry<String, IServiceDiscoveryPlugin>> runningPlugins = 
				plugins.entrySet();
		
			// Create table
			result += "<table border=\"1\" width=\"90%\">" +
					"<tr><th>Plugin</th>" +
					"<th>Description</th>" +
					"<th>Endpoint</th>" +
					"</tr>";
			
			for (java.util.Map.Entry<String, IServiceDiscoveryPlugin> entry : runningPlugins) {
				IServiceDiscoveryPlugin regPlugin = entry.getValue();
				result += "<tr>";
				result += "<td>" + regPlugin.getName() + "</td>";
				result += "<td>" + regPlugin.getDescription() + "</td>";
				result += "<td>" + regPlugin.getUri() + "</td>";
				result += "</tr>";
			}
			
			// Close table
			result += "</table>";
		} else {
			result += "No plugins registered.";
		}
		
		result += insertFooter();
		return result;
	}

	/**
	 * TODO: Replace with a common HTML footer for all iServe related pages
	 * @return
	 */
	private String insertFooter() {
		String result = "";
		result += "</body>" + "\n" +
				"</html>";
		return result;
	}

	/**
	 * TODO: Replace with a common HTML header for all iServe related pages
	 * @return
	 */
	private String insertHeader() {
		String result = "";
		result += "<!DOCTYPE html>" + "\n" + 
				"<html lang=\"en-UK\">" + "\n" +
				"<head>" + "\n" +
				"<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />" + "\n" +
				"<title>iServe Service Discovery Plugins</title>" + "\n" +
				"</head>" + "\n" +
				"<body>";
		
		return result;
	}

}
