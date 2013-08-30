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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.commons.cli.*;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import uk.ac.open.kmi.iserve.commons.io.*;
import uk.ac.open.kmi.iserve.commons.model.Service;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jacek.kopecky@open.ac.uk">Jacek Kopecky</a> (KMi - The Open University)
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 0.2
 */
public class HrestsTransformer implements ServiceTransformer {

    private static final Logger log = LoggerFactory.getLogger(HrestsTransformer.class);

    private static final String XSLT = "hrests.xslt";

    private Tidy parser;

    private File xsltFile;

    private Transformer transformer;

    // Include information about the software version
    private static final String VERSION_PROP_FILE = "plugin.properties";
    private static final String VERSION_PROP = "version";
    private static final String VERSION_UNKNOWN = "Unknown";
    private @Inject(optional=true) @Named("version") String version = VERSION_UNKNOWN;

    // Supported Media Type
    public static String mediaType = "text/html";

    // Supported File Extensions
    private static List<String> fileExtensions = new ArrayList<String>();

    static {
        fileExtensions.add("hrests");
        fileExtensions.add("html");
        fileExtensions.add("xhtml");
    }



    public HrestsTransformer() throws TransformerConfigurationException {
        parser = new Tidy();
        try {
            //URL xsltUrl = getClass().getResource(XSLT);
            URL xsltUrl = getClass().getClassLoader().getResource(XSLT);
            log.debug("Loading XSLT from {}", xsltUrl);
            xsltFile = new File(xsltUrl.toURI());
//            ClassLoader loader = Thread.currentThread().getContextClassLoader();
//            xsltFile = new File(loader.getResource(XSLT).toURI());
            TransformerFactory xformFactory = TransformerFactory.newInstance();
            transformer = xformFactory.newTransformer(new StreamSource(this.xsltFile));

            if (this.version == null) {
                obtainVersionInformation();
            }

        } catch (URISyntaxException e) {
            log.error("Wrong URI for the XSLT transformation file.");
            throw new TransformerConfigurationException("Wrong URI for the XSLT transformation file.", e);
        }

    }

    private void obtainVersionInformation() {
        log.info("Loading version information from {}", VERSION_PROP_FILE);
        PropertiesConfiguration config = null;
        try {
            config = new PropertiesConfiguration(VERSION_PROP_FILE);
            this.version = config.getString(VERSION_PROP, VERSION_UNKNOWN);
        } catch (ConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * Obtains the Media Type this plugin supports for transformation.
     * Although registered Media Types do not necessarily identify uniquely
     * a given semantic service description format it is used for identification
     * for now.
     *
     * @return the media type covered
     */
    @Override
    public String getSupportedMediaType() {
        return mediaType;
    }

    /**
     * Obtains the different file extensions that this plugin can be applied to.
     * Again this does not necessarily uniquely identify a format but helps filter the filesystem
     * when doing batch transformation and may also be used as final solution in cases where we fail
     * to identify a format.
     *
     * @return a List of file extensions supported
     */
    @Override
    public List<String> getSupportedFileExtensions() {
        return fileExtensions;
    }

    /**
     * Obtains the version of the plugin used. Relevant as this is based on plugins and 3rd party may
     * provide their own implementations. Having the version is useful for provenance information and debuggin
     * purposes.
     *
     * @return a String with the version of the plugin.
     */
    @Override
    public String getVersion() {
        return this.version;
    }

    /**
     * Parses and transforms a stream with service description(s), e.g. SAWSDL, OWL-S, hRESTS, etc., and
     * returns a list of Service objects defined in the stream.
     *
     * @param originalDescription The semantic Web service description(s)
     * @param baseUri             The base URI to use while transforming the service description
     * @return A List with the services transformed conforming to MSM model
     */
    @Override
    public List<Service> transform(InputStream originalDescription, String baseUri) throws TransformationException {
        // Exit early if no content is provided
        if (originalDescription == null) {
            return new ArrayList<Service>();
        }

        ByteArrayInputStream istream = null;

        Document document = parser.parseDOM(originalDescription, System.out);

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
            throw new TransformationException(e);
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

    public static void main(String[] args) throws TransformerConfigurationException {

        Options options = new Options();
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        // create the parser
        CommandLineParser parser = new GnuParser();

        options.addOption("h", "help", false, "print this message");
        options.addOption("i", "input", true, "input directory or file to transform");
        options.addOption("s", "save", false, "save result? (default: false)");
        options.addOption("f", "format", true, "format to use when serialising. Default: TTL.");

        // parse the command line arguments
        CommandLine line = null;
        String input = null;
        File inputFile;
        try {
            line = parser.parse(options, args);
            input = line.getOptionValue("i");
            if (line.hasOption("help")) {
                formatter.printHelp("OwlsImporter", options);
                return;
            }
            if (input == null) {
                // The input should not be null
                formatter.printHelp(80, "java " + HrestsTransformer.class.getCanonicalName(), "Options:", options, "You must provide an input", true);
                return;
            }
        } catch (ParseException e) {
            formatter.printHelp(80, "java " + HrestsTransformer.class.getCanonicalName(), "Options:", options, "Error parsing input", true);
        }

        inputFile = new File(input);
        if (inputFile == null || !inputFile.exists()) {
            formatter.printHelp(80, "java " + HrestsTransformer.class.getCanonicalName(), "Options:", options, "Input not found", true);
            System.out.println(inputFile.getAbsolutePath());
            return;
        }

        // Obtain input for transformation
        File[] toTransform;
        if (inputFile.isDirectory()) {
            // Get potential files to transform based on extension
            FilenameFilter owlsFilter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.endsWith(".htm") || name.endsWith(".html"));
                }
            };

            toTransform = inputFile.listFiles(owlsFilter);
        } else {
            toTransform = new File[1];
            toTransform[0] = inputFile;
        }

        // Obtain details for saving results
        File rdfDir = null;
        boolean save = line.hasOption("s");
        if (save) {
            rdfDir = new File(inputFile.getAbsolutePath() + "/RDF");
            rdfDir.mkdir();
        }

        // Obtain details for serialisation format
        String format = line.getOptionValue("f", uk.ac.open.kmi.iserve.commons.io.Syntax.TTL.getName());
        uk.ac.open.kmi.iserve.commons.io.Syntax syntax = uk.ac.open.kmi.iserve.commons.io.Syntax.valueOf(format);

        HrestsTransformer importer;
        ServiceWriter writer;

        importer = new HrestsTransformer();
        writer = new ServiceWriterImpl();

        List<Service> services;
        System.out.println("Transforming input");
        for (File file : toTransform) {
            try {
                services = importer.transform(new FileInputStream(file), null);

                if (services != null) {
                    System.out.println("Services obtained: " + services.size());

                    File resultFile = null;
                    if (rdfDir != null) {
                        String fileName = file.getName();
                        String newFileName = fileName.substring(0, fileName.length() - 4) + syntax.getExtension();
                        resultFile = new File(rdfDir.getAbsolutePath() + "/" + newFileName);
                    }

                    if (rdfDir != null) {
                        OutputStream out = null;
                        try {
                            out = new FileOutputStream(resultFile);
                            for (Service service : services) {
                                if (out != null) {
                                    writer.serialise(service, out, syntax);
                                    System.out.println("Service saved at: " + resultFile.getAbsolutePath());
                                } else {
                                    writer.serialise(service, System.out, syntax);
                                }
                            }
                        } finally {
                            if (out != null)
                                out.close();
                        }
                    }

                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TransformationException e) {
                e.printStackTrace();
            }
        }
    }
}
