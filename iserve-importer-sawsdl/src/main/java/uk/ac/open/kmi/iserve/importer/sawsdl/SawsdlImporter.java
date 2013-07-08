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
package uk.ac.open.kmi.iserve.importer.sawsdl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import uk.ac.open.kmi.iserve.commons.model.Service;
import uk.ac.open.kmi.iserve.sal.ServiceImporter;
import uk.ac.open.kmi.iserve.sal.exception.ImporterException;

import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SawsdlImporter implements ServiceImporter {

    private static final Logger log = LoggerFactory.getLogger(SawsdlImporter.class);
    private static final int vUnknown = -1;

    private static final int v11 = 1;

    private static final int v20 = 2;

    public SawsdlImporter() throws WSDLException, ParserConfigurationException {
    }

    public void setProxy(String proxyHost, String proxyPort) {
        if (proxyHost != null && proxyPort != null) {
            Properties prop = System.getProperties();
            prop.put("http.proxyHost", proxyHost);
            prop.put("http.proxyPort", proxyPort);
        }
    }

    private int getVersion(InputStream serviceDescription) throws IOException {
        InputStreamReader reader = new InputStreamReader(serviceDescription);
        BufferedReader br = null;
        br = new BufferedReader(reader);
        String line = null;
        while ((line = br.readLine()) != null) {
            line = line.toLowerCase();
            if (line.contains("www.w3.org/ns/wsdl")) {
                br.close();
                return v20;
            } else if (line.contains("schemas.xmlsoap.org/wsdl")) {
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
        List<Service> result = null;
        try {
            int v = getVersion(originalDescription);
            if (v == v11) {
                result = new Sawsdl11Transformer().transform(originalDescription);
            } else if (v == v20) {
                result = new Sawsdl20Transformer().transform(originalDescription);
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
        } catch (ParserConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return result;
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
        if (originalDescription != null && originalDescription.exists()) {
            // Open the file and transform it
            InputStream in = null;
            try {
                in = new FileInputStream(originalDescription);
                return transform(in);
            } catch (FileNotFoundException e) {
                log.error("Unable to open input file", e);
                throw new ImporterException("Unable to open input file", e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        log.error("Error while closing input file", e);
                        throw new ImporterException("Error while closing input file", e);
                    }
                }
            }
        }
        // Return an empty array if it could not be transformed.
        return new ArrayList<Service>();
    }
}
