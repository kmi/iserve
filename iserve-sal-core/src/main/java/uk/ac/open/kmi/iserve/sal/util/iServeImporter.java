package uk.ac.open.kmi.iserve.sal.util;
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

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.ConfigurationModule;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.manager.RegistryManager;
import uk.ac.open.kmi.iserve.sal.manager.impl.RegistryManagementModule;
import uk.ac.open.kmi.msm4j.io.MediaType;

import javax.inject.Inject;
import java.io.*;
import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * Utility class for uploading service descriptions and entire folders with service descriptions to an
 * iServe instance. Includes a command line executable interface too.
 * <p/>
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 05/06/2013
 * Time: 11:39
 */
public class iServeImporter {

    private static final Logger log = LoggerFactory.getLogger(iServeImporter.class);

    private final RegistryManager registryManager;

    @Inject
    protected iServeImporter(RegistryManager registryManager) {
        this.registryManager = registryManager;
    }

    public RegistryManager getRegistryManager() {
        return registryManager;
    }


    public int importServices(File file, String mediaType) {

        // Validate input
        if (mediaType == null || file == null || !file.exists()) {
            log.warn("No file was provided or it does not exist.");
            return 0;
        }

        // Obtain input for transformation
        File[] toTransform;
        if (file.isDirectory()) {
            FilenameFilter fileFilter = this.getRegistryManager().getFilenameFilter(mediaType);
            toTransform = file.listFiles(fileFilter);
        } else {
            toTransform = new File[1];
            toTransform[0] = file;
        }

        return this.importServices(toTransform, mediaType);
    }

    public int importServices(File[] toTransform, String mediaType) {

        // Validate input
        if (mediaType == null || toTransform.length == 0)
            return 0;

        log.info("Importing {} file(s) with media type - {}", toTransform.length, mediaType);

        // Updload all the files
        List<URI> importedUris;
        InputStream in = null;
        int result = 0;
        for (File file : toTransform) {
            try {
                log.info("Importing services from file: {}", file);
                in = new FileInputStream(file);
                importedUris = this.registryManager.importServices(in, mediaType);
                result += importedUris.size();
            } catch (FileNotFoundException e) {
                log.error("Could not find file {}", file, e);
                return result;
            } catch (SalException e) {
                log.error("Error importing file {}", file, e);
                return result;
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        log.error("Error closing file {}", file, e);
                    }
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {

        Options options = new Options();
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        // create the parser
        CommandLineParser parser = new GnuParser();

        options.addOption("h", "help", false, "print this message");
        options.addOption("i", "input", true, "input directory or file to transform");
        options.addOption("c", "configuration", true, "iServe configuration file. Default: config" +
                ".properties");
        options.addOption("f", "format", true, "format to use for parsing the files. Default: " + MediaType.TEXT_TURTLE.getMediaType());
        options.addOption("d", "delete", false, "delete the registry before uploading.");

        // parse the command line arguments
        CommandLine line;
        String input = null;
        String configFileName = null;
        File inputFile;
        String mediaType = null;
        boolean clearRegistry = false;
        try {
            line = parser.parse(options, args);
            input = line.getOptionValue("i");
            if (line.hasOption("help")) {
                formatter.printHelp(iServeImporter.class.getCanonicalName(), options);
                System.exit(0);
            }
            if (input == null) {
                // The input should not be null
                formatter.printHelp(80, "java " + iServeImporter.class.getCanonicalName(), "Options:", options, "You must provide an input", true);
                System.exit(-2);
            }

            configFileName = line.getOptionValue("c");

            mediaType = line.getOptionValue("f", MediaType.TEXT_TURTLE.getMediaType());

            clearRegistry = line.hasOption("d");


        } catch (ParseException e) {
            formatter.printHelp(80, "java " + iServeImporter.class.getCanonicalName(), "Options:", options,
                    "Error parsing arguments", true);
            System.exit(-1);
        }

        inputFile = new File(input);
        if (inputFile == null || !inputFile.exists()) {
            formatter.printHelp(80, "java " + iServeImporter.class.getCanonicalName(), "Options:", options, "Input not found", true);
            System.out.println(inputFile.getAbsolutePath());
            System.exit(-2);
        }

        // Initialise the injector
        Injector injector = null;
        if (configFileName != null) {
            File configFile = new File(configFileName);
            if (!configFile.exists()) {
                log.warn("Configuration file - {} - not found. Attempting to use default configuration " +
                        "file.");
                injector = Guice.createInjector(new ConfigurationModule(),
                        new RegistryManagementModule());
            } else {
                injector = Guice.createInjector(new ConfigurationModule(configFileName),
                        new RegistryManagementModule());
            }
        } else {
            formatter.printHelp(80, "java " + iServeImporter.class.getCanonicalName(), "Options:", options,
                    "Configuration file not found", true);
            System.out.println(configFileName);
            System.exit(-2);
        }

        iServeImporter importer = injector.getInstance(iServeImporter.class);

        if (clearRegistry) {
            try {
                importer.getRegistryManager().clearRegistry();
            } catch (SalException e) {
                log.error("Problems clearing the registry", e);
                System.exit(-3);
            }
        }

        // Check the format is supported
        if (!importer.getRegistryManager().canImport(mediaType)) {
            Set<String> supportedTypes = importer.getRegistryManager().getSupportedInputMediaTypes();
            StringBuilder builder = new StringBuilder();
            for (String type : supportedTypes) {
                builder.append(type).append("\n");
            }

            formatter.printHelp(80, "java " + iServeImporter.class.getCanonicalName(), "Options:", options,
                    "\nMedia type - " + mediaType + " - is not supported. Supported media types are: \n" +
                            builder.toString(),
                    true);
            System.exit(-4);
        }

        int result = importer.importServices(inputFile, mediaType);
        log.info("Imported {} files from {}", result, inputFile.toURI().toASCIIString());
        System.exit(result);
    }
}
