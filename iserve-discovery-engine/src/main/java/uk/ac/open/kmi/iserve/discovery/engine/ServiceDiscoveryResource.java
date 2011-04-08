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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.openrdf.repository.RepositoryException;

import com.sun.jersey.api.NotFoundException;

//import com.sun.jersey.api.NotFoundException;

import uk.ac.open.kmi.iserve.commons.io.IOUtil;
import uk.ac.open.kmi.iserve.commons.io.RDFRepositoryConnector;
import uk.ac.open.kmi.iserve.discovery.api.DiscoveryException;
import uk.ac.open.kmi.iserve.discovery.api.IServiceDiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.api.util.DiscoveryUtil;
import uk.ac.open.kmi.iserve.discovery.disco.AllServicesPlugin;
import uk.ac.open.kmi.iserve.discovery.disco.RDFSClassificationDiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.disco.RDFSInputOutputDiscoveryPlugin;
import uk.ac.open.kmi.iserve.discovery.engine.util.AtomUtil;
import uk.ac.open.kmi.iserve.discovery.imatcher.IMatcherDiscoveryPlugin;
import uk.ac.open.kmi.iserve.sal.config.SalConfig;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;

@Path("/disco/svc/{name}")
public class ServiceDiscoveryResource {
	
	private Map<String, IServiceDiscoveryPlugin> plugins;
	private SalConfig config;
	private RDFRepositoryConnector connector;
	private ServiceManager serviceManager;

	public ServiceDiscoveryResource() throws RepositoryException, 
		TransformerConfigurationException, IOException, WSDLException, 
		ParserConfigurationException {
		init();
		plugins = new HashMap<String, IServiceDiscoveryPlugin>();

		IServiceDiscoveryPlugin plugin = new RDFSInputOutputDiscoveryPlugin();
		plugins.put(plugin.getName(), plugin);

		plugin = new IMatcherDiscoveryPlugin(connector);
		plugins.put(plugin.getName(), plugin);

		plugin = new RDFSClassificationDiscoveryPlugin(connector, false);
		plugins.put(plugin.getName(), plugin);

		plugin = new AllServicesPlugin(connector);
		plugins.put(plugin.getName(), plugin);
	}

	@GET
	@Produces({MediaType.APPLICATION_ATOM_XML,
			MediaType.APPLICATION_JSON,
			MediaType.TEXT_XML})
	public Response discover(@PathParam("name") String name, @Context UriInfo ui) throws WebApplicationException {
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		// find discovery plug-in
		IServiceDiscoveryPlugin plugin = plugins.get(name);
		if ( plugin == null ) {
			throw new NotFoundException("Plugin named " + name + " is not found");
		}

		Set<Entry> matchingResults;
		try {
			matchingResults = plugin.discover(queryParams);
		} catch (DiscoveryException e) {
			throw new WebApplicationException(e, e.getStatus());
		}

		// process the results
		Feed feed = DiscoveryUtil.getAbderaInstance().getFactory().newFeed();
		feed.setId(ui.getRequestUri().toString());
		feed.addLink(ui.getRequestUri().toString(),"self");
		feed.setTitle(plugin.getFeedTitle());
		feed.setUpdated(new Date());
		//FIXME: we should include the version
		feed.setGenerator(plugin.getUri(), null, plugin.getDescription()); 
		
		if ( matchingResults != null ) {
			for ( Entry result : matchingResults ) {
				feed.addEntry(result);
			}
		}
		
		return Response.ok(feed).build();
	}

	private void init() throws IOException, RepositoryException, TransformerConfigurationException, WSDLException, ParserConfigurationException {
		String baseDir = ServiceDiscoveryResource.class.getResource("/").getPath();
		baseDir = baseDir.replaceAll("%20", " ");

		Properties prop = IOUtil.readProperties(new File(baseDir + "../config.properties"));
		prop.setProperty(SalConfig.XSLT_PATH, baseDir + "../hrests.xslt");
		config = new SalConfig(prop);
		serviceManager = new ServiceManager(config);
		connector = serviceManager.getConnector();
	}

}
