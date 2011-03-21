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

import java.util.Date;
import java.util.List;

import uk.ac.open.kmi.iserve2.sal.model.common.URI;
import uk.ac.open.kmi.iserve2.sal.model.review.Review;
import uk.ac.open.kmi.iserve2.sal.model.service.ModelReference;
import uk.ac.open.kmi.iserve2.sal.model.service.Operation;
import uk.ac.open.kmi.iserve2.sal.model.service.Service;

public class ServiceImpl extends SemanticEntityImpl implements Service {

	/**
	 * serial version UID
	 */
	private static final long serialVersionUID = -3082194983873036096L;

	private List<Operation> operations;

	private List<URI> sources;

	private Date lastUpdated;

	private URI author;

	private List<Review> reviews;

	public ServiceImpl() {
		super();
		operations = null;
	}

	public ServiceImpl(URI uri) {
		super(uri);
		operations = null;
	}

	public ServiceImpl(URI uri, List<ModelReference> modelReferences) {
		super(uri, modelReferences);
		operations = null;
	}

	public ServiceImpl(URI uri, List<ModelReference> modelReferences, List<Operation> operations) {
		super(uri, modelReferences);
		setOperations(operations);
	}

	public List<Operation> getOperations() {
		return operations;
	}

	public void setOperations(List<Operation> operations) {
		this.operations = operations;
	}

	public List<URI> getSources() {
		return sources;
	}

	public void setSources(List<URI> sources) {
		this.sources = sources;
	}

	public URI getCreatedBy() {
		return author;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setCreateBy(URI author) {
		this.author = author;
	}

	public void setLastUpdated(Date date) {
		this.lastUpdated = date;
	}

	public List<Review> getReviews() {
		return this.reviews;
	}

	public void setReviews(List<Review> reviews) {
		this.reviews = reviews;
	}

}
