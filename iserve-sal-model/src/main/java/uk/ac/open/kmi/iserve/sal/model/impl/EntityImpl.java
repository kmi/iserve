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

import uk.ac.open.kmi.iserve2.sal.model.Entity;
import uk.ac.open.kmi.iserve2.sal.model.common.URI;

public class EntityImpl implements Entity {

	private static final long serialVersionUID = 8821249219254544789L;

	private URI uri;

	public EntityImpl() {
		uri = null;
	}

	public EntityImpl(URI uri) {
		setURI(uri);
	}

	public URI getURI() {
		return uri;
	}

	public void setURI(URI uri) {
		this.uri = uri;
	}

	@Override
	public boolean equals(Object obj) {
		URI objUri = ((EntityImpl) obj).getURI();
		return uri.equals(objUri);
	}

}
