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
package uk.ac.open.kmi.iserve2.importer.hrests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.openrdf.repository.RepositoryException;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import uk.ac.open.kmi.iserve2.importer.ImporterConfig;
import uk.ac.open.kmi.iserve2.importer.ImporterException;
import uk.ac.open.kmi.iserve2.importer.ServiceImporter;

public class HrestsImporter extends ServiceImporter {

	private Tidy parser;

	private File xsltFile;

	private Transformer transformer;

	public HrestsImporter(ImporterConfig config, String xsltPath) throws RepositoryException, TransformerConfigurationException {
		super(config);
		parser = new Tidy();

		xsltFile = new File(xsltPath);
		TransformerFactory xformFactory = TransformerFactory.newInstance();
		transformer = xformFactory.newTransformer(new StreamSource(this.xsltFile));
	}

	@Override
	protected InputStream transformStream(String serviceDescription) throws ImporterException {
		Document document = parser.parseDOM(new ByteArrayInputStream(serviceDescription.getBytes()), null);
		DOMSource source = new DOMSource(document);
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(bout);
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			throw new ImporterException(e);
		}
		if ( null == result.getOutputStream() )
			return null;
		String resultString = result.getOutputStream().toString();
		return new ByteArrayInputStream(resultString.getBytes());
	}

}
