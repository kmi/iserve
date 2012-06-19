/*
   Copyright 2012  Knowledge Media Institute - The Open University

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
package uk.ac.open.kmi.iserve.sal.manager;

import java.util.List;

import uk.ac.open.kmi.iserve.sal.exception.DocumentException;

/**
 * Class Description
 * 
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public interface DocumentManager {

	public abstract List<String> listDocument();

	public abstract String addDocument(String fileName,
			String serviceDescription) throws DocumentException;

	public abstract String deleteDocument(String documentUri)
			throws DocumentException;

	public abstract String deleteDocumentById(String documentId)
			throws DocumentException;

	public abstract String getDocument(String documentUri)
			throws DocumentException;

	public abstract String getDocumentById(String documentId)
			throws DocumentException;

}