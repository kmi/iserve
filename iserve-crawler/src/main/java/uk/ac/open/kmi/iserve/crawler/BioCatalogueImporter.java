package uk.ac.open.kmi.iserve.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.httpclient.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.iserve.client.rest.IServeHttpClient;
import uk.ac.open.kmi.iserve.client.rest.exception.MimeTypeDetectionException;
import uk.ac.open.kmi.iserve.commons.io.IOUtil;

public class BioCatalogueImporter {

	private static final Logger log = LoggerFactory.getLogger(BioCatalogueImporter.class);

	private static final String DEFAULT_PASSWORD = "iserveAdmin";
	private static final String DEFAULT_USER = "root";
	private static final String DEFAULT_SERVER_URL = "http://vph-services.kmi.open.ac.uk/vph-iserve";

	/**
	 * Load all the known WSDL services from BioCatalogue
	 * 
	 *  TODO: The current version relies on a local folder. Should use directly
	 *  the API from BioCatalogue so that we can keep it up to date.
	 *  
	 *  TODO: The current version does not import the REST services
	 *  
	 *  TODO: The current version does not upload the classification nor the tags
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		log.info("Command-line arguments: " + Arrays.toString(args));

		Options options = new Options();
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		// create the parser
		CommandLineParser parser = new GnuParser();

		options.addOption("h", "help", false, "print this message");
		options.addOption("u", "user", true, "iServe admin user (default '" + DEFAULT_USER + "')");
		options.addOption("p", "password", true, "iServe admin password (default '" + DEFAULT_PASSWORD + "')");
		options.addOption("s", "server", true, "iServe server URL (default '" + DEFAULT_SERVER_URL + "')");
		options.addOption("i", "input", true, "input directory to import");

		// parse the command line arguments
		CommandLine line = null;
		try {
			line = parser.parse(options, args);
			String input = line.getOptionValue("i");
			if (line.hasOption("help")) {
				formatter.printHelp("BioCatalogueImporter", options);
			}
			if (input == null) {
				// The input should not be null
				formatter.printHelp("BioCatalogueImporter", options);
				return;
			}
		} catch (ParseException e) {
			formatter.printHelp("BioCatalogueImporter", options);
		}

		String user = line.getOptionValue("u", DEFAULT_USER);
		String pass = line.getOptionValue("p", DEFAULT_PASSWORD);
		String server = line.getOptionValue("s", DEFAULT_SERVER_URL);

		// The input should not be null
		String input = line.getOptionValue("i");

		try {
			IServeHttpClient iServeClient = new IServeHttpClient(server, user, pass); 
			//			iServeClient.setProxy("wwwcache.open.ac.uk", 8080);
			inputWsdl(input, iServeClient);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	private static void inputWsdl(String path, IServeHttpClient iServeClient) {
		// Only get WSDLs
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".wsdl");
			}
		};

		File dir = new File(path);

		File[] children = dir.listFiles(filter);
		if (children == null) {
			// Either dir does not exist or is not a directory
		} else {
			for (int i=0; i<children.length; i++) {
				// Get filename of file or directory
				File file = children[i];
				System.out.println("Uploading file:" + file.getPath());
				try {
					String contents = IOUtil.readString(file);
					String result = iServeClient.addService(contents, null);
					System.out.println(result);
				} catch (HttpException e) {
					System.out.println(file.getName());
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println(file.getName());
					e.printStackTrace();
				} catch (MimeTypeDetectionException e) {
					System.out.println(file.getName());
					e.printStackTrace();
				}

			}
		}
	}
}

