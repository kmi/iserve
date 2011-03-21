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
package uk.ac.open.kmi.iserve2.importer.owls;

import java.io.File;
import java.io.IOException;

import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.iserve2.commons.io.IOUtil;
import uk.ac.open.kmi.iserve2.importer.ImporterConfig;
import uk.ac.open.kmi.iserve2.importer.ImporterException;

public class OwlsImporterTest {

	private OwlsImporter importer;

	public OwlsImporterTest() throws RepositoryException {
		ImporterConfig config = new ImporterConfig("http://iserve.open.ac.uk/", "/Users/dl3962/Workspace/gtd/Action/iServe/testing-doc-home/",
				"http://localhost:8080/openrdf-sesame", "serv_repo_owlim");
		importer = new OwlsImporter(config);
	}

	public void test() throws ImporterException {
		try {
			String contents = IOUtil.readString(new File("/Users/dl3962/Workspace/gtd/Action/iServe/data/testing-20100125/vehicle_price_service.owls"));
			String serviceUri = importer.importService("vehicle_price_service.owls", contents, "http://test.com/test.html");
			System.out.println(serviceUri);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		OwlsImporterTest tester;
		try {
			tester = new OwlsImporterTest();
			tester.test();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (ImporterException e) {
			e.printStackTrace();
		}
	}

}
