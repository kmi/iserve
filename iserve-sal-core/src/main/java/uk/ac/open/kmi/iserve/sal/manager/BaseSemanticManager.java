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
package uk.ac.open.kmi.iserve2.sal.manager;

import org.ontoware.rdf2go.model.Model;
import org.openrdf.rdf2go.RepositoryModel;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.iserve2.commons.io.RDFRepositoryConnector;

public class BaseSemanticManager {

	protected RDFRepositoryConnector repoConnector;

	public BaseSemanticManager(String repoServerUrl, String repoName) throws RepositoryException {
		repoConnector = new RDFRepositoryConnector(repoServerUrl, repoName);
	}

	public Model getModel() {
		RepositoryModel model = repoConnector.openRepositoryModel();
		return model;
	}

	public Model getModel(String contextUri) {
		if ( null == contextUri || contextUri.equalsIgnoreCase("") == true) {
			return null;
		}
		RepositoryModel model = repoConnector.openRepositoryModel(contextUri);
		return model;
	}

	public RDFRepositoryConnector getConnector() {
		return repoConnector;
	}


}
