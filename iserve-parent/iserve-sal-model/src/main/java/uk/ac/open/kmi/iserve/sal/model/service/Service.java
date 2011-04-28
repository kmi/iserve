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
package uk.ac.open.kmi.iserve.sal.model.service;

import java.util.Date;
import java.util.List;

import uk.ac.open.kmi.iserve.sal.model.common.URI;
import uk.ac.open.kmi.iserve.sal.model.review.Review;

public interface Service extends SemanticEntity {

	public List<URI> getSources();

	public void setSources(List<URI> sources);

	public List<Operation> getOperations();

	public void setOperations(List<Operation> operations);

	public Date getLastUpdated();

	public void setLastUpdated(Date updateTime);

	public URI getCreatedBy();

	public void setCreateBy(URI authorFoafId);

	public List<Review> getReviews();

	public void setReviews(List<Review> reviews);

}
