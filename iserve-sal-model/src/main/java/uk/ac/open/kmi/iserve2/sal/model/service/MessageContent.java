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
package uk.ac.open.kmi.iserve2.sal.model.service;

import java.util.List;

import uk.ac.open.kmi.iserve2.sal.model.common.URI;

public interface MessageContent extends MessagePart {

	public List<URI> getLoweringSchemaMappings();

	public void setLoweringSchemaMappings(List<URI> schemaMappings);

	public List<URI> getLiftingSchemaMappings();

	public void setLiftingSchemaMappings(List<URI> schemaMappings);

}
