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

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.ServiceWriter;
import uk.ac.open.kmi.iserve.commons.io.ServiceWriterImpl;
import uk.ac.open.kmi.iserve.commons.model.Service;
import uk.ac.open.kmi.iserve.sal.ServiceImporter;
import uk.ac.open.kmi.iserve.sal.exception.ImporterException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SawsdlImporter implements ServiceImporter {

    private static final Logger log = LoggerFactory.getLogger(SawsdlImporter.class);

    private SawsdlTransformer transformer;

    public SawsdlImporter() throws ImporterException {
        transformer = new SawsdlTransformer();
    }

    public void setProxy(String proxyHost, String proxyPort) {
        if (proxyHost != null && proxyPort != null) {
            Properties prop = System.getProperties();
            prop.put("http.proxyHost", proxyHost);
            prop.put("http.proxyPort", proxyPort);
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
        return transformer.transform(originalDescription, baseUri);
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

    public static void main(String[] args) throws ImporterException {

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
                formatter.printHelp("SawsdlImporter", options);
                return;
            }
            if (input == null) {
                // The input should not be null
                formatter.printHelp(80, "java " + SawsdlImporter.class.getCanonicalName(), "Options:", options, "You must provide an input", true);
                return;
            }
        } catch (ParseException e) {
            formatter.printHelp(80, "java " + SawsdlImporter.class.getCanonicalName(), "Options:", options, "Error parsing input", true);
        }

        inputFile = new File(input);
        if (inputFile == null || !inputFile.exists()) {
            formatter.printHelp(80, "java " + SawsdlImporter.class.getCanonicalName(), "Options:", options, "Input not found", true);
            System.out.println(inputFile.getAbsolutePath());
            return;
        }

        // Obtain input for transformation
        File[] toTransform;
        if (inputFile.isDirectory()) {
            // Get potential files to transform based on extension
            FilenameFilter owlsFilter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.endsWith(".wsdl") || name.endsWith(".sawsdl"));
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

        SawsdlImporter importer;
        ServiceWriter writer;

        importer = new SawsdlImporter();
        writer = new ServiceWriterImpl();

        List<Service> services;
        System.out.println("Transforming input");
        for (File file : toTransform) {
            try {
                services = importer.transform(file);

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
            } catch (ImporterException e) {
                e.printStackTrace();
            }
        }
    }

}
