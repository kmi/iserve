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
package uk.ac.open.kmi.iserve.discovery.api;

import java.util.Map;

/**
 * Interface that defined the core methods that Discovery Plugins should provide
 * Concrete implementations should implement the {@link ServiceDiscoveryPlugin}
 * interface, and/or the {@link OperationDiscoveryPlugin} interface.
 * 
 * @author Dong Liu (Knowledge Media Institute - The Open University)
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public interface DiscoveryPlugin {

	/**
	 * Obtain the name of the plugin
	 * 
	 * @return the plugin name
	 */
	public String getName();
	
	/**
	 * Obtains the version of the plugin
	 * 
	 * @return the plugin version
	 */
	public String getVersion();

	/**
	 * Obtains the description of the plugin
	 * 
	 * @return the plugin description
	 */
	public String getDescription();

	// TODO: Remove this
	public String getFeedTitle();
	
	/**
	 * Obtains information about the parameters supported by the engine
	 * 
	 * @return the parameters details (name, details)
	 */
	public Map<String, String> getParametersDetails();

}
