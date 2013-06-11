/*
 * Copyright (c) 2013. Knowledge Media Institute - The Open University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.open.kmi.iserve.importer.hrests;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import uk.ac.open.kmi.iserve.commons.io.ServiceReader;
import uk.ac.open.kmi.iserve.commons.io.ServiceReaderImpl;
import uk.ac.open.kmi.iserve.commons.io.Syntax;
import uk.ac.open.kmi.iserve.commons.model.Service;
import uk.ac.open.kmi.iserve.sal.ServiceImporter;
import uk.ac.open.kmi.iserve.sal.exception.ImporterException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HrestsImporter implements ServiceImporter {

    private Tidy parser;

    private File xsltFile;

    private Transformer transformer;

    public HrestsImporter(String xsltPath) throws TransformerConfigurationException {
        parser = new Tidy();
        xsltFile = new File(xsltPath);
        TransformerFactory xformFactory = TransformerFactory.newInstance();
        transformer = xformFactory.newTransformer(new StreamSource(this.xsltFile));
    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.sal.ServiceImporter#transform(java.io.InputStream)
     */
    @Override
    public List<Service> transform(InputStream originalDescription) throws ImporterException {
        // Exit early if no content is provided
        if (originalDescription == null) {
            return new ArrayList<Service>();
        }

        ByteArrayInputStream istream = null;

        Document document = parser.parseDOM(originalDescription, null);
        DOMSource source = new DOMSource(document);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        StreamResult stream = new StreamResult(bout);
        try {
            transformer.transform(source, stream);
            if (null == stream.getOutputStream()) {
                return new ArrayList<Service>();
            }
            ;

            // Now parse the resulting service descriptions
            byte[] data = ((ByteArrayOutputStream) stream.getOutputStream()).toByteArray();
            istream = new ByteArrayInputStream(data);
            ServiceReader reader = new ServiceReaderImpl();
            return reader.parse(istream, Syntax.RDFXML);
        } catch (TransformerException e) {
            throw new ImporterException(e);
        } finally {
            if (bout != null)
                try {
                    bout.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

            if (istream != null)
                try {
                    istream.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
        }

    }

    /**
     * Parses and transforms a file with service description(s), e.g. SAWSDL, OWL-S, hRESTS, etc., and
     * returns a list of Service objects defined in the file.
     *
     * @param originalDescription The file containing the semantic Web service description(s)
     * @return A List with the services transformed conforming to MSM model
     * @see uk.ac.open.kmi.iserve.sal.ServiceFormat for the list of supported formats
     */
    @Override
    public List<Service> transform(File originalDescription) throws ImporterException {
        try {
            return transform(new FileInputStream(originalDescription));
        } catch (FileNotFoundException e) {
            throw new ImporterException("Unable to process input file", e);
        }
    }

}
