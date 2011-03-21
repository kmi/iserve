/*
   Copyright 2011  Knowledge Media Institute - The Open University

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
package uk.ac.open.kmi.iserve2.sal.rest.resource;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpException;
import org.junit.Before;
import org.junit.Test;

import uk.ac.open.kmi.iserve.client.rest.IServeHttpClient;
import uk.ac.open.kmi.iserve.client.rest.exception.MimeTypeDetectionException;

/**
 * Class Description
 * 
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 * @version $Rev$
 * $LastChangedDate$ 
 * $LastChangedBy$
 */
public class ServiceResourceTest {

	String queryString = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" +
	"PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\n" +
	"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
	"PREFIX owl:<http://www.w3.org/2002/07/owl#>\n" +
	"PREFIX wsl:<http://www.wsmo.org/ns/wsmo-lite#>\n" +
	"PREFIX sawsdl:<http://www.w3.org/ns/sawsdl#>\n" +
	"PREFIX msm:<http://cms-wg.sti2.org/ns/minimal-service-model#>\n\n" +
	"SELECT ?s WHERE {\n" +
	"?s rdf:type msm:Service. \n" +
	"?s sawsdl:modelReference ?modelref . \n" +
	"?modelref rdfs:subClassOf <http://www.service-finder.eu/ontologies/ServiceCategories#Category> . \n" +
	"}\n";
	
	private static final String USER_NAME = "dong";
	
	private static final String PASSWORD = "dong";

	private static final String ISERVE_URL = "http://localhost:9080/iserve2-sal-rest";
	
	private IServeHttpClient iServeClient;
	
	private List<String> testFolders;
	
	private FilenameFilter wsdlFilter;
	
	private FilenameFilter htmlFilter;
	
	private FilenameFilter owlsFilter;
	
	private String workingDir; 
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		iServeClient = new IServeHttpClient(ISERVE_URL, USER_NAME, PASSWORD);
		testFolders = new ArrayList<String>();
		workingDir = System.getProperty("user.dir");
//		testFolders.add(workingDir + "/src/test/resources/jgd-services");
		testFolders.add(workingDir + "/src/test/resources/soa4re");
		
		FilenameFilter wsdlFilter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.endsWith(".wsdl");
		    }
		};
		
		FilenameFilter htmlFilter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return (name.endsWith(".html") || name.endsWith(".htm"));
		    }
		};
		
		FilenameFilter owlsFilter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return (name.endsWith(".owl") || name.endsWith(".owls")) ;
		    }
		};
		
	}

	/**
	 * Test method for {@link uk.ac.open.kmi.iserve2.sal.rest.resource.ServiceResource#listServicesAsHtml()}.
	 */
	@Test
	public final void testListServicesAsHtml() {
		String result;
		try {
			result = iServeClient.listServices();
			System.out.println(result);
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Test method for {@link uk.ac.open.kmi.iserve2.sal.rest.resource.ServiceResource#listServicesAsRdf()}.
	 */
	@Test
	public final void testListServicesAsRdf() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link uk.ac.open.kmi.iserve2.sal.rest.resource.ServiceResource#getServiceAsHtml(java.lang.String)}.
	 */
	@Test
	public final void testGetServiceAsHtml() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link uk.ac.open.kmi.iserve2.sal.rest.resource.ServiceResource#getServiceAsXml(java.lang.String)}.
	 */
	@Test
	public final void testGetServiceAsXml() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link uk.ac.open.kmi.iserve2.sal.rest.resource.ServiceResource#getServiceAsRdfXml(java.lang.String)}.
	 */
	@Test
	public final void testGetServiceAsRdfXml() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link uk.ac.open.kmi.iserve2.sal.rest.resource.ServiceResource#getServiceAsTurtle(java.lang.String)}.
	 */
	@Test
	public final void testGetServiceAsTurtle() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link uk.ac.open.kmi.iserve2.sal.rest.resource.ServiceResource#getServiceAsNtriples(java.lang.String)}.
	 */
	@Test
	public final void testGetServiceAsNtriples() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link uk.ac.open.kmi.iserve2.sal.rest.resource.ServiceResource#addService(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testAddService() {
		// Add all the test collections
		System.out.println("Uploading test collections");
		for (String testFolder : testFolders) {
			File dir = new File(testFolder);
			System.out.println("Test collection: " + testFolder);
				
			// Test services
			System.out.println("Uploading services");
			File[] hrestsFiles = dir.listFiles();
			uploadFiles(hrestsFiles);
		}
	}

	/**
	 * @param files
	 */
	private void uploadFiles(File[] files) {
		String fileContent;
		String result;
		for (File file : files) {
			fileContent = readFile(file);
			try {
				result = iServeClient.addService(fileContent, ISERVE_URL + 
						"/tests/" + file.getName());
				System.out.println("Service uploaded: " + file.getAbsolutePath());
				System.out.println("Result: " + result);
			} catch (HttpException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MimeTypeDetectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}

	/**
	 * @param file
	 * @return
	 */
	private String readFile(File file) {
		BufferedReader br = null;
		String ret = null;
		try {
			br =  new BufferedReader(new FileReader(file));
	        String line = null;
	        StringBuffer sb = new StringBuffer((int)file.length());
	        while( (line = br.readLine() ) != null ) {
	            sb.append(line).append("\n");
	        }
	        ret = sb.toString();
	    } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
	        if(br!=null) {try{br.close();} catch(Exception e){} }
	    }
		return ret;
	}

	/**
	 * Test method for {@link uk.ac.open.kmi.iserve2.sal.rest.resource.ServiceResource#deleteService(java.lang.String)}.
	 */
	@Test
	public final void testDeleteService() {
		fail("Not yet implemented"); // TODO
	}

}
