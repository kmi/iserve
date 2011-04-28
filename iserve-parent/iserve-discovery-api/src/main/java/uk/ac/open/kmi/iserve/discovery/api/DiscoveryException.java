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
package uk.ac.open.kmi.iserve.discovery.api;

public class DiscoveryException extends Exception {

	private static final long serialVersionUID = -3945395620888930668L;

	private int status;

	public DiscoveryException() {
		super();
	}

	public DiscoveryException(String message) {
		super(message);
	}

	public DiscoveryException(Throwable cause) {
		super(cause);
	}

	public DiscoveryException(String message, Throwable cause) {
		super(message, cause);
	}

	public DiscoveryException(int status, String message) {
		super(message);
		this.setStatus(status);
	}

	public DiscoveryException(int status, Throwable cause) {
		super(cause);
		this.setStatus(status);
	}

	public DiscoveryException(int status, String message, Throwable cause) {
		super(message, cause);
		this.setStatus(status);
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}

}
