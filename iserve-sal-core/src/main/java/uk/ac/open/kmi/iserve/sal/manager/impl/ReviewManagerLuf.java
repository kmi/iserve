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
package uk.ac.open.kmi.iserve.sal.manager.impl;

import java.net.URI;
import java.util.List;

import uk.ac.open.kmi.iserve.sal.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.manager.ReviewManager;
import uk.ac.open.kmi.iserve.sal.model.impl.URIImpl;
import uk.ac.open.kmi.iserve.sal.util.LufConnector;

public class ReviewManagerLuf implements ReviewManager {

	/**
	 * Constructor for the Review Manager. Protected to avoid external access.
	 * Any access to this should take place through the iServeManager
	 * 
	 * @param configuration
	 */
	protected ReviewManagerLuf(SystemConfiguration configuration) {
		// initialize the connector to LUF
		LufConnector lufConnector = new LufConnector(new URIImpl(configuration.getLufUrl()));
		if ( configuration.getProxyHostName() != null && configuration.getProxyHostName() != "" ||
				configuration.getProxyPort() != null && configuration.getProxyPort() != "" ) {
			lufConnector.setProxy(configuration.getProxyHostName(), Integer.valueOf(configuration.getProxyPort()));
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#addRating(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean addRating(URI agentUri, URI serviceUri, String rating) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#addComment(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean addComment(URI agentUri, URI serviceUri, String comment) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#addTag(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean addTag(URI agentUri, URI serviceUri, String tag) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#getRatings(java.net.URI)
	 */
	@Override
	public List<String> getRatings(URI serviceUri) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#getComments(java.net.URI)
	 */
	@Override
	public List<String> getComments(URI serviceUri) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#getTags(java.net.URI)
	 */
	@Override
	public List<String> getTags(URI serviceUri) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#updateRating(java.net.URI, java.net.URI, java.lang.String)
	 */
	@Override
	public boolean updateRating(URI agentUri, URI serviceUri, String rating) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#deleteRating(java.net.URI, java.net.URI)
	 */
	@Override
	public boolean deleteRating(URI agentUri, URI serviceUri) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#deleteComment(java.net.URI, java.net.URI)
	 */
	@Override
	public boolean deleteComment(URI agentUri, URI serviceUri) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.ReviewManager#deleteTag(java.net.URI, java.net.URI, java.lang.String)
	 */
	@Override
	public boolean deleteTag(URI agentUri, URI serviceUri, String tag) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}


}
