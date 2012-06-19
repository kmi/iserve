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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Properties;

import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import org.openrdf.repository.RepositoryException;
import org.xml.sax.SAXException;

import uk.ac.open.kmi.iserve.importer.ImporterConfig;
import uk.ac.open.kmi.iserve.sal.ServiceImporter;
import uk.ac.open.kmi.iserve.sal.exception.ImporterException;

public class SawsdlImporter implements ServiceImporter {

	private static final int vUnknown = -1;

	private static final int v11 = 1;

	private static final int v20 = 2;

	private Sawsdl11Transformer sawsdl11Transformer;

	private Sawsdl20Transformer sawsdl20Transformer;

	public SawsdlImporter() throws WSDLException, ParserConfigurationException {
		sawsdl11Transformer = new Sawsdl11Transformer();
		sawsdl20Transformer = new Sawsdl20Transformer();
	}

	public void setProxy(String proxyHost, String proxyPort) {
		if ( proxyHost != null && proxyPort != null ) {
			Properties prop = System.getProperties();
			prop.put("http.proxyHost", proxyHost);
			prop.put("http.proxyPort", proxyPort);
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.ServiceImporter#transformStream(java.lang.String)
	 */
	@Override
	public InputStream transformStream(String serviceDescription) throws ImporterException {
		String resultString = null;
		try {
			int v = getVersion(serviceDescription);
			if ( v == v11 ) {
				resultString = sawsdl11Transformer.transform(serviceDescription);
			} else if ( v == v20 ) {
				resultString = sawsdl20Transformer.transform(serviceDescription);
			} else {
				throw new ImporterException("Unknown version of WSDL, can not find either http://schemas.xmlsoap.org/wsdl or http://www.w3.org/ns/wsdl ");
			}
		} catch (IOException e) {
			throw new ImporterException(e);
		} catch (WSDLException e) {
			throw new ImporterException(e);
		} catch (org.apache.woden.WSDLException e) {
			throw new ImporterException(e);
		} catch (SAXException e) {
			throw new ImporterException(e);
		}
		if ( null == resultString ) {
			return null;
		}
		return new ByteArrayInputStream(resultString.getBytes());
	}

	private int getVersion(String serviceDescription) throws IOException {
		StringReader reader = new StringReader(serviceDescription);
		BufferedReader br = null;
		br = new BufferedReader(reader);
		String line = null;
		while ((line = br.readLine()) != null) {
			line = line.toLowerCase();
			if ( line.contains("www.w3.org/ns/wsdl") ) {
				br.close();
				return v20;
			} else if ( line.contains("schemas.xmlsoap.org/wsdl") ) {
				br.close();
				return v11;
			}
		}
		reader.close();
		if (br != null) {
			br.close();
		}
		return vUnknown;
	}

}
