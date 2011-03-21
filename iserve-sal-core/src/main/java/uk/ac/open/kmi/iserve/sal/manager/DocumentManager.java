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
package uk.ac.open.kmi.iserve.sal.manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import uk.ac.open.kmi.iserve.commons.io.FileUtil;
import uk.ac.open.kmi.iserve.commons.io.IOUtil;
import uk.ac.open.kmi.iserve.commons.io.StringUtil;
import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.sal.config.SalConfig;
import uk.ac.open.kmi.iserve.sal.exception.DocumentException;

public class DocumentManager {

	private SalConfig config;

	public DocumentManager(SalConfig config) {
		this.config = config;
	}

	public List<String> listDocument() {
		List<String> result = new ArrayList<String>();
		File wsdlFolder = new File(config.getDocFolderPath());
		File[] folderList = wsdlFolder.listFiles();
		for ( int i = 0; i < folderList.length; i++ ) {
			if ( folderList[i].isDirectory() == true ) {
				String[] fileList = folderList[i].list();
				if ( fileList.length > 0 ) {
					String prefix = config.getImporterConfig().getUriPrefix() + MSM.DOCUMENT_INFIX;
					result.add(prefix + folderList[i].getName() + "/" + fileList[0]);
				}
			}
		}
		return result;
	}

	public String addDocument(String fileName, String serviceDescription) throws DocumentException {
		String uuid = generateUniqueId();
		String folder = config.getImporterConfig().getDocFolderPath() + uuid; 
		String filePath = folder + "/" + fileName;
		try {
			FileUtil.createDirIfNotExists(new File(folder));
			IOUtil.writeString(serviceDescription, new File(filePath));
		} catch (IOException e) {
			throw new DocumentException(e);
		}
		return config.getImporterConfig().getUriPrefix() + MSM.DOCUMENT_INFIX + uuid + "/" + fileName;
	}

	public String deleteDocument(String documentUri) throws DocumentException {
		// delete from hard disk
		String prefix = config.getImporterConfig().getUriPrefix() + MSM.DOCUMENT_INFIX;
		String relativePath = StringUtil.subStrings(documentUri, prefix);
		String filePath = config.getImporterConfig().getDocFolderPath() + relativePath;

		File file = new File(filePath);
		File docFolder = new File(file.getParent());
		FileUtil.deltree(docFolder);

		return documentUri;
	}

	public String deleteDocumentById(String documentId) throws DocumentException {
		String result =  null;
		File docFolder = new File(config.getImporterConfig().getDocFolderPath() + documentId);
		if ( docFolder.isDirectory() == true ) {
			String[] files = docFolder.list();
			if ( files.length > 0 ) {
				result = config.getImporterConfig().getDocFolderPath() + documentId + "/" + files[0];
			}
		}
		FileUtil.deltree(docFolder);
		return result;
	}

	public String getDocument(String documentUri) throws DocumentException {
		String prefix = config.getImporterConfig().getUriPrefix() + MSM.DOCUMENT_INFIX;
		String relativePath = StringUtil.subStrings(documentUri, prefix);
		String filePath = config.getImporterConfig().getDocFolderPath() + relativePath;

		File file = new File(filePath);
		String result =  null;
		try {
			result = IOUtil.readString(file);
		} catch (IOException e) {
			throw new DocumentException(e);
		}
		return result;
	}

	public String getDocumentById(String documentId) throws DocumentException {
		String result =  null;
		File folder = new File(config.getImporterConfig().getDocFolderPath() + documentId);
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
