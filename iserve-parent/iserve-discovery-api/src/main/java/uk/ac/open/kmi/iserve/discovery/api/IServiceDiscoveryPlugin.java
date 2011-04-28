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

import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.abdera.model.Entry;

public interface IServiceDiscoveryPlugin {

	public String getName();

	public String getDescription();

	public String getUri();

	public String getFeedTitle();

	public Set<Entry> discover(MultivaluedMap<String, String> parameters) throws DiscoveryException;

}
