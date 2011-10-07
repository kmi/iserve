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
package uk.ac.open.kmi.iserve.importer.sawsdl;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.iserve.commons.io.IOUtil;
import uk.ac.open.kmi.iserve.importer.ImporterConfig;
import uk.ac.open.kmi.iserve.importer.ImporterException;

public class SawsdlImporterTest {

	/**
	 * 
	 */
	private static final String REPOSITORY_NAME = "serv_repo_owlim";
	/**
	 * 
	 */
	private static final String SESAME_URL = "http://localhost:8080/openrdf-sesame";
	/**
	 * 
	 */
	private static final String DOC_FOLDER_PATH = "/Users/cp3982/Workspace/TempServicesFolder/";
	/**
	 * 
	 */
	private static final String ISERVE_URL = "http://localhost:9080/";
	private static final String TEST_COLLECTION_FOLDER = "/Users/cp3982/Workspace/Test Collections/sawsdl-tc1-modified";
	
	private SawsdlImporter importer;

	public SawsdlImporterTest() throws RepositoryException, WSDLException, ParserConfigurationException {
		ImporterConfig config = new ImporterConfig(ISERVE_URL, DOC_FOLDER_PATH,
				SESAME_URL, REPOSITORY_NAME);
		importer = new SawsdlImporter(config);
	}

	public void test() throws ImporterException {
		try {
//			importSpecificFiles();
			importSawsdlTc();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @throws IOException 
	 * @throws ImporterException 
	 * 
	 */
	private void importSawsdlTc() throws IOException, ImporterException {
		File dir = new File(TEST_COLLECTION_FOLDER);
		
		// Only get WSDLs
		FilenameFilter filter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.endsWith(".wsdl");
		    }
		};

		File[] children = dir.listFiles(filter);
		if (children == null) {
		    // Either dir does not exist or is not a directory
		} else {
		    for (int i=0; i<children.length; i++) {
		        // Get filename of file or directory
		        File file = children[i];
		        String contents = IOUtil.readString(file);
				String serviceUri = importer.importService(file.getName(), contents, null);
		        System.out.println(serviceUri);
		        
		    }
		}
	}

	/**
	 * @throws IOException 
	 * @throws ImporterException 
	 * 
	 */
	private void importSpecificFiles() throws IOException, ImporterException {
//		String contents = IOUtil.readString(new File("/Users/dl3962/Workspace/gtd/Action/iServe/data/testing-20100125/title_videomediarecommendedprice_service.wsdl"));
//		String serviceUri = importer.importService("title_videomediarecommendedprice_service.wsdl", contents, "http://test.com/test.html");
		String contents = IOUtil.readString(new File("/Users/dl3962/Workspace/gtd/Action/iServe/data/testing-20100125/00all.wsdl"));
		String serviceUri = importer.importService("00all.wsdl", contents, null);
		System.out.println(serviceUri);
	}

	public static void main(String[] args) {
		SawsdlImporterTest tester;
		try {
			tester = new SawsdlImporterTest();
			tester.test();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (ImporterException e) {
			e.printStackTrace();
		} catch (WSDLException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

}
