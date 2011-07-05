package uk.ac.open.kmi.iserve.client.rest;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.tika.Tika;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.*;

import uk.ac.open.kmi.iserve.client.rest.exception.MimeTypeDetectionException;

public class IServeHttpClient {

	private String servicesUrl;

	private String documentsUrl;

	private String queryUrl;

	private String serverUrl;

	private String host;

	private int port;

	private String userName;

	private String password;

	private Tika tika;

	private HttpClient client; 

	public IServeHttpClient(String serverUrl, String userName, String password) throws MalformedURLException {
		if ( serverUrl.endsWith("/") ) {
			int len = serverUrl.length();
			serverUrl = serverUrl.substring(0, len - 1);
		}
		this.serverUrl = serverUrl;
		URL url = new URL(this.serverUrl);
		this.host = url.getHost();
		this.port = url.getPort();
		if ( -1 == this.port ) {
			this.port = 80;
		}
		this.servicesUrl = serverUrl + "/data/services/";
		this.documentsUrl = serverUrl + "/data/documents/";
		this.queryUrl = serverUrl + "/data/execute-query";
		this.userName = userName;
		this.password = password;
		this.tika = new Tika();
		this.client = new HttpClient();
		this.client.getState().setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials(userName, password));
	}

	public void setProxy(String proxyHost, int proxyPort) {
		if ( null == this.client ) {
			return;
		}
		this.client.getHostConfiguration().setProxy(proxyHost, proxyPort);
	}

	public String listServices() throws HttpException, IOException {
		GetMethod listServicesMethod = new GetMethod(servicesUrl);
		try {
			client.executeMethod(listServicesMethod);
			InputStream is = listServicesMethod.getResponseBodyAsStream();
			return convertStreamToString(is);
		} finally {
			listServicesMethod.releaseConnection();
		}
	}

	public String addService(String serviceDescription, String source) throws HttpException, IOException, MimeTypeDetectionException {
		PostMethod addServiceMethod = new PostMethod(servicesUrl);
		try {
			if ( source != null && source != "" ) {
				addServiceMethod.addRequestHeader("Content-Location", source);
			}
			String mimeTypeString = null;
			try {
				mimeTypeString = detectMimeType(serviceDescription);
			} catch (IOException e) {
				throw new MimeTypeDetectionException(e);
			}
			if (null == mimeTypeString) {
				throw new MimeTypeDetectionException("Cannot detect the MIME type of " + serviceDescription);
			}
			addServiceMethod.addRequestHeader("Content-Type", mimeTypeString);
			addServiceMethod.setRequestBody(serviceDescription);
			client.executeMethod(addServiceMethod);
			InputStream is = addServiceMethod.getResponseBodyAsStream();
			return convertStreamToString(is);
		} finally {
			addServiceMethod.releaseConnection();
		}
	}

	public String addDocument(String serviceDescription, String source) throws HttpException, IOException, MimeTypeDetectionException {
		PostMethod addDocumentMethod = new PostMethod(documentsUrl);
		try {
			if ( source != null && source != "" ) {
				addDocumentMethod.addRequestHeader("Content-Location", source);
			}
			String mimeTypeString = null;
			try {
				mimeTypeString = detectMimeType(serviceDescription);
			} catch (IOException e) {
				throw new MimeTypeDetectionException(e);
			}
			if (null == mimeTypeString) {
				throw new MimeTypeDetectionException("Cannot detect the MIME type of " + serviceDescription);
			}
			addDocumentMethod.addRequestHeader("Content-Type", mimeTypeString);
			addDocumentMethod.setRequestBody(serviceDescription);
			client.executeMethod(addDocumentMethod);
			InputStream is = addDocumentMethod.getResponseBodyAsStream();
			return convertStreamToString(is);
		} finally {
			addDocumentMethod.releaseConnection();
		}
	}

	public String getService(String serviceUriString) throws HttpException, IOException {
		if ( serviceUriString.contains("/resource/services/") ) {
			serviceUriString = serviceUriString.replaceFirst("/resource/services/", "/data/services/");
		} else if ( serviceUriString.contains("/page/services/") ) {
			serviceUriString = serviceUriString.replaceFirst("/page/services/", "/data/services/");
		}
		GetMethod getServiceMethod = new GetMethod(serviceUriString);
		getServiceMethod.addRequestHeader("Accept", "application/rdf+xml");
		try {
			client.executeMethod(getServiceMethod);
			InputStream is = getServiceMethod.getResponseBodyAsStream();
			return convertStreamToString(is);
		} finally {
			getServiceMethod.releaseConnection();
		}
	}

	public String removeService(String serviceUriString) throws HttpException, IOException {
		this.client.getState().setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials(userName, password));
		if ( serviceUriString.contains("/resource/services/") ) {
			serviceUriString = serviceUriString.replaceFirst("/resource/services/", "/data/services/");
		} else if ( serviceUriString.contains("/page/services/") ) {
			serviceUriString = serviceUriString.replaceFirst("/page/services/", "/data/services/");
		}
		DeleteMethod removeServiceMethod = new DeleteMethod(serviceUriString);
		try {
			client.executeMethod(removeServiceMethod);
			InputStream is = removeServiceMethod.getResponseBodyAsStream();
			return convertStreamToString(is);
		} finally {
			removeServiceMethod.releaseConnection();
		}
	}

	public String removeDocument(String documentUriString) throws HttpException, IOException {
		this.client.getState().setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials(userName, password));
		if ( documentUriString.contains("/resource/documents/") ) {
			documentUriString = documentUriString.replaceFirst("/resource/documents/", "/data/documents/");
		} else if ( documentUriString.contains("/page/services/") ) {
			documentUriString = documentUriString.replaceFirst("/page/documents/", "/data/documents/");
		}
		DeleteMethod removeDocumentMethod = new DeleteMethod(documentUriString);
		try {
			client.executeMethod(removeDocumentMethod);
			InputStream is = removeDocumentMethod.getResponseBodyAsStream();
			return convertStreamToString(is);
		} finally {
			removeDocumentMethod.releaseConnection();
		}
	}

	public String listDocuments() throws HttpException, IOException {
		GetMethod listDocumentsMethod = new GetMethod(documentsUrl);
		try {
			client.executeMethod(listDocumentsMethod);
			InputStream is = listDocumentsMethod.getResponseBodyAsStream();
			return convertStreamToString(is);
		} finally {
			listDocumentsMethod.releaseConnection();
		}
	}

	public String getDocument(String documentUriString) throws HttpException, IOException {
		if ( documentUriString.contains("/resource/documents/") ) {
			documentUriString = documentUriString.replaceFirst("/resource/documents/", "/page/documents/");
		}
		GetMethod getDocumentMethod = new GetMethod(documentUriString);
		try {
			client.executeMethod(getDocumentMethod);
			InputStream is = getDocumentMethod.getResponseBodyAsStream();
			return convertStreamToString(is);
		} finally {
			getDocumentMethod.releaseConnection();
		}
	}

	public String executeQuery(String queryString) throws HttpException, IOException {
		String queryUriString = this.queryUrl + "?query=" + URLEncoder.encode(queryString, "UTF-8");		
		GetMethod queryMethod = new GetMethod(queryUriString);
		try {
			client.executeMethod(queryMethod);
			InputStream is = queryMethod.getResponseBodyAsStream();
			return convertStreamToString(is);
		} finally {
			queryMethod.releaseConnection();
		}
	}

	private String detectMimeType(String content) throws IOException {
		InputStream stream = new ByteArrayInputStream(content.getBytes());
		return tika.detect(stream);
	}

	private String convertStreamToString(InputStream is) {   
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		StringBuffer buffer = new StringBuffer();
		try {
			String line = "";
			while ( (line = in.readLine()) != null ) {
				buffer.append(line);
				buffer.append('\n');
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer.toString();
	}

//	public static void main(String[] args) throws MimeTypeDetectionException {
//		try {
//			String queryString = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" +
//			"PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\n" +
//			"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//			"PREFIX owl:<http://www.w3.org/2002/07/owl#>\n" +
//			"PREFIX wsl:<http://www.wsmo.org/ns/wsmo-lite#>\n" +
//			"PREFIX sawsdl:<http://www.w3.org/ns/sawsdl#>\n" +
//			"PREFIX msm:<http://cms-wg.sti2.org/ns/minimal-service-model#>\n\n" +
//			"SELECT ?s WHERE {\n" +
//			"?s rdf:type msm:Service. \n" +
//			"?s sawsdl:modelReference ?modelref . \n" +
//			"?modelref rdfs:subClassOf <http://www.service-finder.eu/ontologies/ServiceCategories#Category> . \n" +
//			"}\n";
//			IServeHttpClient iServeClient = new IServeHttpClient("http://localhost:8080/iserve", "dong", "dong");
//			String result = iServeClient.listServices();
//			System.out.println(result);
//			String result = iServeClient.listDocuments();
//			System.out.println(result);
//			String result = iServeClient.executeQuery(queryString);
//			System.out.println(result);
//
//			String hRests = readRDFFile("/Users/dl3962/Workspace/gtd/Action/iServe/data/testing-20100125/hrestAnnotation.html");
//			String result = iServeClient.addService(hRests, "http://abc.com/abc");
//			System.out.println(result);
//			String serviceUriString = "http://iserve.kmi.open.ac.uk/resource/services/d206371e-46b1-4f59-8674-04414c815fcd";
//			String result = iServeClient.getService(serviceUriString);
//			System.out.println(result);
//			String result = iServeClient.removeService(serviceUriString);
//			System.out.println(result);
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

//	private static String readRDFFile(String path) {
//		File f = new File(path);
//		BufferedReader br = null;
//		String ret = null;
//		try {
//			br =  new BufferedReader(new FileReader(f));
//            String line = null;
//            StringBuffer sb = new StringBuffer((int)f.length());
//            while( (line = br.readLine() ) != null ) {
//                sb.append(line).append("\n");
//            }
//            ret = sb.toString();
//        } catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//            if(br!=null) {try{br.close();} catch(Exception e){} }
//        }
//		return ret;
//	}

}
