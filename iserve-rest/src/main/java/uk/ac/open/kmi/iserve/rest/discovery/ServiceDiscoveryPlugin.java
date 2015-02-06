/*
 * Copyright (c) 2013. Knowledge Media Institute - The Open University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.open.kmi.iserve.rest.discovery;

import uk.ac.open.kmi.iserve.discovery.api.MatchResult;

import javax.ws.rs.core.MultivaluedMap;
import java.net.URL;
import java.util.Map;

/**
 * Interface that Service Discovery Plugins should implement
 *
 * @author Dong Liu (Knowledge Media Institute - The Open University)
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public interface ServiceDiscoveryPlugin extends DiscoveryPlugin {

    /**
     * Discover matching services based on the key-value pairs providing values
     * for the plugin discovery parameters. The results are indexed by the URL
     * of the matching result in order to better support merging results
     *
     * @param parameters the invocation parameters for the discovery plugin.
     *                   These parameters are plugin dependent {@link DiscoveryPlugin}
     * @return the matching results not necessarily ordered. The ordering shall
     * take place at a later stage prior to returning the results.
     * @throws uk.ac.open.kmi.iserve.rest.discovery.DiscoveryException
     */
    public Map<URL, MatchResult> discoverServices(MultivaluedMap<String, String> parameters) throws DiscoveryException;

}
