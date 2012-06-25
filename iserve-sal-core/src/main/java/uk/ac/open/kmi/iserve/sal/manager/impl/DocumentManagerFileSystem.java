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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import uk.ac.open.kmi.iserve.commons.io.FileUtil;
import uk.ac.open.kmi.iserve.commons.io.IOUtil;
import uk.ac.open.kmi.iserve.commons.io.StringUtil;
import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.sal.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.exception.DocumentException;
import uk.ac.open.kmi.iserve.sal.manager.DocumentManager;

public class DocumentManagerFileSystem implements DocumentManager {
	
	private SystemConfiguration configuration;

	/**
	 * Constructor for the Document Manager. Protected to avoid external access.
	 * Any access to this should take place through the iServeManager
	 * 
	 * @param configuration
	 */
	protected DocumentManagerFileSystem(SystemConfiguration configuration) {
		this.configuration = configuration;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#listDocument()
	 */
	@Override
	public List<String> listDocument() {
		List<String> result = new ArrayList<String>();
		File wsdlFolder = new File(configuration.getDocumentsFolder());
		File[] folderList = wsdlFolder.listFiles();
		for ( int i = 0; i < folderList.length; i++ ) {
			if ( folderList[i].isDirectory() == true ) {
				String[] fileList = folderList[i].list();
				if ( fileList.length > 0 ) {
					String prefix = configuration.getIserveUrl() + MSM.DOCUMENT_INFIX;
					result.add(prefix + folderList[i].getName() + "/" + fileList[0]);
				}
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#addDocument(java.lang.String, java.lang.String)
	 */
	@Override
	public String addDocument(String fileName, String serviceDescription) throws DocumentException {
		String uuid = generateUniqueId();
		String folder = configuration.getDocumentsFolder() + uuid; 
		String filePath = folder + "/" + fileName;
		try {
			FileUtil.createDirIfNotExists(new File(folder));
			IOUtil.writeString(serviceDescription, new File(filePath));
		} catch (IOException e) {
			throw new DocumentException(e);
		}
		return configuration.getIserveUrl() + MSM.DOCUMENT_INFIX + uuid + "/" + fileName;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#deleteDocument(java.lang.String)
	 */
	@Override
	public String deleteDocument(String documentUri) throws DocumentException {
		// delete from hard disk
		String prefix = configuration.getIserveUrl() + MSM.DOCUMENT_INFIX;
		String relativePath = StringUtil.subStrings(documentUri, prefix);
		String filePath = configuration.getDocumentsFolder() + relativePath;

		File file = new File(filePath);
		File docFolder = new File(file.getParent());
		FileUtil.deltree(docFolder);

		return documentUri;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#deleteDocumentById(java.lang.String)
	 */
	@Override
	public String deleteDocumentById(String documentId) throws DocumentException {
		String result =  null;
		File docFolder = new File(configuration.getDocumentsFolder() + documentId);
		if ( docFolder.isDirectory() == true ) {
			String[] files = docFolder.list();
			if ( files.length > 0 ) {
				result = configuration.getDocumentsFolder() + documentId + "/" + files[0];
			}
		}
		FileUtil.deltree(docFolder);
		return result;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#getDocument(java.lang.String)
	 */
	@Override
	public String getDocument(String documentUri) throws DocumentException {
		String prefix = configuration.getIserveUrl() + MSM.DOCUMENT_INFIX;
		String relativePath = StringUtil.subStrings(documentUri, prefix);
		String filePath = configuration.getDocumentsFolder() + relativePath;

		File file = new File(filePath);
		String result =  null;
		try {
			result = IOUtil.readString(file);
		} catch (IOException e) {
			throw new DocumentException(e);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#getDocumentById(java.lang.String)
	 */
	@Override
	public String getDocumentById(String documentId) throws DocumentException {
		String result =  null;
		File folder = new File(configuration.getDocumentsFolder() + documentId);
		if ( folder.isDirectory() == true ) {
			File[] files = folder.listFiles();
			if ( files.length > 0 ) {
				File file = files[0];
				try {
					result = IOUtil.readString(file);
				} catch (IOException e) {
					throw new DocumentException(e);
				}
			}
		}
		return result;
	}

	private String generateUniqueId() {
		// FIXME: UUID may be too long in this case.
		String uid = UUID.randomUUID().toString();
		return uid;
	}

}
