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

import uk.ac.open.kmi.iserve.sal.model.common.URI;

public class URIImpl implements URI {

	private static final long serialVersionUID = 6851415228104134895L;

	private String uriString;

	public URIImpl() {
		uriString = "";
	}

	public URIImpl(String uriString) {
		this.uriString = uriString;
	}

	public String getNameSpace() {
		if ( uriString.endsWith("/") ) {
			return uriString;
		}
		int localNameIdx = getLocalNameIndex(uriString);
		if ( localNameIdx >= uriString.length() ) {
			return uriString;
		}
		return uriString.substring(0, localNameIdx);
	}

	public String getLocalName() {
		if ( uriString.endsWith("/") ) {
			return "";
		}
		int localNameIdx = getLocalNameIndex(uriString);
		if ( localNameIdx >= uriString.length() ) {
			return uriString;
		}
		return uriString.substring(localNameIdx);
	}

	@Override
	public String toString() {
		return uriString;
	}

	public String toSPARQL() {
		return "<" + uriString + ">";
	}

	private int getLocalNameIndex(String uri) {
		int separatorIdx = uri.indexOf('#');

		if (separatorIdx < 0) {
			separatorIdx = uri.lastIndexOf('/');
		}

		if (separatorIdx < 0) {
			separatorIdx = uri.lastIndexOf(':');
		}

		if (separatorIdx < 0) {
			throw new IllegalArgumentException("No separator character founds in URI: " + uri);
		}

		return separatorIdx + 1;
	}

	@Override
	public boolean equals(Object obj) {
		String objUriString = ((URIImpl) obj).toString();
		return uriString.equalsIgnoreCase(objUriString);
	}

}
