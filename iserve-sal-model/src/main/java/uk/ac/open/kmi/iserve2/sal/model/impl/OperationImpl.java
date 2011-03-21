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
import uk.ac.open.kmi.iserve2.sal.model.service.MessageContent;
import uk.ac.open.kmi.iserve2.sal.model.service.ModelReference;
import uk.ac.open.kmi.iserve2.sal.model.service.Operation;

public class OperationImpl extends SemanticEntityImpl implements Operation {

	/**
	 * serial version UID
	 */
	private static final long serialVersionUID = -4901474957994049306L;

	private List<MessageContent> inputs;

	private List<MessageContent> outputs;

	private List<MessageContent> inputFaults;

	private List<MessageContent> outputFaults;

	private List<URI> addresses;

	public OperationImpl() {
		super();
		inputs = null;
		outputs = null;
		inputFaults = null;
		outputFaults = null;
	}

	public OperationImpl(URI uri) {
		super(uri);
		inputs = null;
		outputs = null;
		inputFaults = null;
		outputFaults = null;
	}

	public OperationImpl(URI uri, List<ModelReference> modelReferences) {
		super(uri, modelReferences);
		inputs = null;
		outputs = null;
		inputFaults = null;
		outputFaults = null;
	}

	public List<MessageContent> getInputFaults() {
		return inputFaults;
	}

	public List<MessageContent> getInputs() {
		return inputs;
	}

	public List<MessageContent> getOutputFaults() {
		return outputFaults;
	}

	public List<MessageContent> getOutputs() {
		return outputs;
	}

	public void setInputFaults(List<MessageContent> inputFaults) {
		this.inputFaults = inputFaults;
	}

	public void setInputs(List<MessageContent> inputs) {
		this.inputs = inputs;
	}

	public void setOutputFaults(List<MessageContent> outputFaults) {
		this.outputFaults = outputFaults;
	}

	public void setOutputs(List<MessageContent> outputs) {
		this.outputs = outputs;
	}

	public List<URI> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<URI> addresses) {
		this.addresses = addresses;
	}

}
