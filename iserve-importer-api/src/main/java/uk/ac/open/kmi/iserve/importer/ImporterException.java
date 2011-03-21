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
package uk.ac.open.kmi.iserve.importer;

public class ImporterException extends Exception {

	private static final long serialVersionUID = -2069203220017813003L;

	public ImporterException() {
		super();
	}

	public ImporterException(String message) {
		super(message);
	}

	public ImporterException(Throwable cause) {
		super(cause);
	}

	public ImporterException(String message, Throwable cause) {
		super(message, cause);
	}

}
