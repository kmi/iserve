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
package uk.ac.open.kmi.iserve.sal;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.iserve.commons.io.IOUtil;
import uk.ac.open.kmi.iserve.sal.config.SalConfig;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;

public class ServiceManagerTest {

	private ServiceManager serviceManager;

	public ServiceManagerTest() throws IOException, RepositoryException, TransformerConfigurationException, WSDLException, ParserConfigurationException {
		Properties prop = IOUtil.readProperties(new File("/Users/dl3962/Workspace/JWS/iserve-webapp/src/main/webapp/WEB-INF/config.properties"));
		prop.setProperty(SalConfig.XSLT_PATH, "/Users/dl3962/Workspace/JWS/iserve-webapp/src/main/webapp/servicebrowser/im/hrests.xslt");
		SalConfig config = new SalConfig(prop);
		serviceManager = new ServiceManager(config);
	}

//	public void testQuery() {
//		serviceManager.testResultIO();
//	}

//	public void testAddServices() throws IOException, ServiceException {
//		// hRESTS
//		String contents = IOUtil.readString(new File("/Users/dl3962/Workspace/gtd/Action/iServe/data/testing-20100125/hrestAnnotation.html"));
//		String serviceUri = serviceManager.addService("hrestAnnotation.html", contents, "http://test.com/hrestAnnotation.html");
//		System.out.println(serviceUri);
//
//		// OWL-S
//		contents = IOUtil.readString(new File("/Users/dl3962/Workspace/gtd/Action/iServe/data/testing-20100125/vehicle_price_service.owls"));
//		serviceUri = serviceManager.addService("vehicle_price_service.owls", contents, "http://test.com/vehicle_price_service.owls");
//		System.out.println(serviceUri);
//
//		// SAWSDL 1.1
//		contents = IOUtil.readString(new File("/Users/dl3962/Workspace/gtd/Action/iServe/data/testing-20100125/title_videomediarecommendedprice_service.wsdl"));
//		serviceUri = serviceManager.addService("title_videomediarecommendedprice_service.wsdl", contents, "http://test.com/title_videomediarecommendedprice_service.wsdl");
//		System.out.println(serviceUri);

		// SAWSDL 2.0
//		String contents = IOUtil.readString(new File("/Users/dl3962/Workspace/gtd/Action/iServe/data/testing-20100125/00all.wsdl"));
//		String contents = IOUtil.readString(new File("/Users/dl3962/Downloads/service.wsdl"));
//		String serviceUri = serviceManager.addService("00all.wsdl", contents, "http://test.com/service.wsdl");
//		System.out.println(serviceUri);		
//	}

//	public void testDeleteAllServices() throws ServiceException {
//		List<String> serviceUriList = serviceManager.listService();
//		for ( String serviceUri : serviceUriList ) {
//			serviceManager.deleteService(serviceUri);
//			System.out.println(serviceUri);
//		}
//	}

//	public static void main(String[] args) throws IOException, RepositoryException, TransformerConfigurationException, WSDLException, ParserConfigurationException, ServiceException {
//		ServiceManagerTest test = new ServiceManagerTest();
////		test.testQuery();
//		test.testAddServices();
////		test.testDeleteAllServices();
//	}

}
