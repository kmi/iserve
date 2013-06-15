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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class HrestsImporter implements ServiceImporter {

    private static final Logger log = LoggerFactory.getLogger(HrestsImporter.class);
    private static final String XSLT = "/hrests.xslt";

    private Tidy parser;

    private File xsltFile;

    private Transformer transformer;

    public HrestsImporter() throws TransformerConfigurationException {
        parser = new Tidy();
        try {
            xsltFile = new File(HrestsImporter.class.getResource(XSLT).toURI());
            TransformerFactory xformFactory = TransformerFactory.newInstance();
            transformer = xformFactory.newTransformer(new StreamSource(this.xsltFile));
        } catch (URISyntaxException e) {
            log.error("Wrong URI for the XSLT transformation file.");
            throw new TransformerConfigurationException("Wrong URI for the XSLT transformation file.", e);
        }
    }

    /**
     * Parses and transforms a stream with service description(s), e.g. SAWSDL, OWL-S, hRESTS, etc., and
     * returns a list of Service objects defined in the stream.
     *
     * @param originalDescription The semantic Web service description(s)
     * @return A List with the services transformed conforming to MSM model
     * @see uk.ac.open.kmi.iserve.sal.ServiceFormat for the list of supported formats
     */
    @Override
    public List<Service> transform(InputStream originalDescription) throws ImporterException {
        return transform(originalDescription, null);
    }

    /**
     * Parses and transforms a stream with service description(s), e.g. SAWSDL, OWL-S, hRESTS, etc., and
     * returns a list of Service objects defined in the stream.
     *
     * @param originalDescription The semantic Web service description(s)
     * @param baseUri             The base URI to use while transforming the service description
     * @return A List with the services transformed conforming to MSM model
     * @see uk.ac.open.kmi.iserve.sal.ServiceFormat for the list of supported formats
     */
    @Override
    public List<Service> transform(InputStream originalDescription, String baseUri) throws ImporterException {
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

            // Now parse the resulting service descriptions
            byte[] data = ((ByteArrayOutputStream) stream.getOutputStream()).toByteArray();
            log.info("Resulting service:\n" + new String(data));

            istream = new ByteArrayInputStream(data);
            ServiceReader reader = new ServiceReaderImpl();
            return reader.parse(istream, baseUri, Syntax.RDFXML);
        } catch (TransformerException e) {
            throw new ImporterException(e);
        } finally {
            try {
                bout.close();
            } catch (IOException e) {
                log.error("Problem closing the hRESTS transformation output stream", e);
            }

            if (istream != null)
                try {
                    istream.close();
                } catch (IOException e) {
                    log.error("Problem closing the hRESTS input stream", e);
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
        return transform(originalDescription, null);
    }

    /**
     * Parses and transforms a file with service description(s), e.g. SAWSDL, OWL-S, hRESTS, etc., and
     * returns a list of Service objects defined in the file.
     *
     * @param originalDescription The file containing the semantic Web service description(s)
     * @param baseUri             The base URI to use while transforming the service description
     * @return A List with the services transformed conforming to MSM model
     * @see uk.ac.open.kmi.iserve.sal.ServiceFormat for the list of supported formats
     */
    @Override
    public List<Service> transform(File originalDescription, String baseUri) throws ImporterException {
        try {
            return transform(new FileInputStream(originalDescription), baseUri);
        } catch (FileNotFoundException e) {
            throw new ImporterException("Unable to process input file", e);
        }
    }

}
