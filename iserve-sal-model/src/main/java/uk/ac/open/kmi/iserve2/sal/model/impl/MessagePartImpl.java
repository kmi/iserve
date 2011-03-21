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
package uk.ac.open.kmi.iserve2.sal.model.impl;

import java.util.List;

import uk.ac.open.kmi.iserve2.sal.model.common.URI;
import uk.ac.open.kmi.iserve2.sal.model.service.MessagePart;
import uk.ac.open.kmi.iserve2.sal.model.service.ModelReference;

public class MessagePartImpl extends SemanticEntityImpl implements MessagePart {

	private static final long serialVersionUID = 6222476625836587768L;

	private List<MessagePart> messageParts;

	private List<MessagePart> optionalParts;

	private List<MessagePart> mandatoryParts;

	public MessagePartImpl() {
		super();
	}

	public MessagePartImpl(URI uri) {
		super(uri);
	}

	public MessagePartImpl(URI uri, List<ModelReference> modelReferences) {
		super(uri, modelReferences);
	}

	public List<MessagePart> getAllParts() {
		return messageParts;
	}

	public void setParts(List<MessagePart> parts) {
		messageParts = parts;
	}

	public List<MessagePart> getMandatoryParts() {
		return mandatoryParts;
	}

	public void setMandatoryParts(List<MessagePart> parts) {
		mandatoryParts = parts;
	}

	public List<MessagePart> getOptionalParts() {
		return optionalParts;
	}

	public void setOptionalParts(List<MessagePart> parts) {
		optionalParts = parts;
	}

}
