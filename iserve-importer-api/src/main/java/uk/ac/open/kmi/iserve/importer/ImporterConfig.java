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

import uk.ac.open.kmi.iserve.commons.io.StringUtil;

public class ImporterConfig {

	private String uriPrefix;

	private String docFolderPath;

	private String repoServerUrl;

	private String repoName;

	public ImporterConfig() {
		setUriPrefix(null);
		setDocFolderPath(null);
		setRepoServerUrl(null);
		setRepoName(null);
	}

	public ImporterConfig(String uriPrefix, String docFolderPath, String repoServerUrl, String repoName) {
		setUriPrefix(uriPrefix);
		setDocFolderPath(docFolderPath);
		setRepoServerUrl(repoServerUrl);
		setRepoName(repoName);
	}

	public String getUriPrefix() {
		return uriPrefix;
	}

	public String getDocFolderPath() {
		return docFolderPath;
	}

	public void setUriPrefix(String uriPrefix) {
		this.uriPrefix = StringUtil.formatUri(uriPrefix);
	}

	public void setDocFolderPath(String docFolderPath) {
		this.docFolderPath = docFolderPath;
	}

	public String getRepoServerUrl() {
		return repoServerUrl;
	}

	public void setRepoServerUrl(String repoServerUrl) {
		this.repoServerUrl = repoServerUrl;
	}

	public String getRepoName() {
		return repoName;
	}

	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}

}
