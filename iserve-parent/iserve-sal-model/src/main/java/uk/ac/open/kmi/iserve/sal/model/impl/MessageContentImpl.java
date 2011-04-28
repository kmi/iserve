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
package uk.ac.open.kmi.iserve.sal.model.impl;

import java.util.List;

import uk.ac.open.kmi.iserve.sal.model.common.URI;
import uk.ac.open.kmi.iserve.sal.model.service.MessageContent;
import uk.ac.open.kmi.iserve.sal.model.service.ModelReference;

public class MessageContentImpl extends MessagePartImpl implements MessageContent {

	private static final long serialVersionUID = -4861565896145823699L;

	private List<URI> loweringSchemaMappings;

	private List<URI> liftingSchemaMappings;

	public MessageContentImpl() {
		super();
	}

	public MessageContentImpl(URI uri) {
		super(uri);
	}

	public MessageContentImpl(URI uri, List<ModelReference> modelReferences) {
		super(uri, modelReferences);
	}

	public List<URI> getLiftingSchemaMappings() {
		return liftingSchemaMappings;
	}

	public List<URI> getLoweringSchemaMappings() {
		return loweringSchemaMappings;
	}

	public void setLiftingSchemaMappings(List<URI> schemaMappings) {
		this.liftingSchemaMappings = schemaMappings;
	}

	public void setLoweringSchemaMappings(List<URI> schemaMappings) {
		this.loweringSchemaMappings = schemaMappings;
	}

}
