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
import uk.ac.open.kmi.iserve.sal.model.service.ModelReference;
import uk.ac.open.kmi.iserve.sal.model.service.SemanticEntity;

public class SemanticEntityImpl extends EntityImpl implements SemanticEntity {

	/**
	 * serial version UID
	 */
	private static final long serialVersionUID = -3335198648556486424L;

	private List<ModelReference> modelReferences;

	private List<URI> seeAlso;

	private List<String> labels;

	public SemanticEntityImpl() {
		super();
		modelReferences = null;
	}

	public SemanticEntityImpl(URI uri) {
		super(uri);
		modelReferences = null;
	}

	public SemanticEntityImpl(URI uri, List<ModelReference> modelReferences) {
		super(uri);
		setModelReferences(modelReferences);
	}

	public List<ModelReference> getModelReferences() {
		return modelReferences;
	}

	public void setModelReferences(List<ModelReference> modelReferences) {
		this.modelReferences = modelReferences;
	}

	public List<URI> getSeeAlso() {
		return seeAlso;
	}

	public void setSeeAlso(List<URI> seeAlso) {
		this.seeAlso = seeAlso;
	}

	public List<String> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

}
