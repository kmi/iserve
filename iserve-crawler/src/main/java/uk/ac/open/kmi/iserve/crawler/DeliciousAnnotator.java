/*
   Copyright 2012  Knowledge Media Institute - The Open University

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
package uk.ac.open.kmi.iserve.crawler;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.node.BlankNode;
import org.ontoware.rdf2go.model.node.DatatypeLiteral;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.vocabulary.XSD;
import org.openrdf.rdf2go.RepositoryModel;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.open.kmi.data.acquisition.DeliciousClient;
import uk.ac.open.kmi.data.acquisition.TagFeed;
import uk.ac.open.kmi.iserve.commons.io.RDFRepositoryConnector;

/**
 * Class Description
 * 
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public class DeliciousAnnotator {

	private static final Logger LOG = LoggerFactory.getLogger(DeliciousAnnotator.class);

	private static final String DEFAULT_SESAME_SERVER = "http://kmi-web17.open.ac.uk:8080/openrdf-sesame";
	private static final String DEFAULT_REPO_NAME = "Validator";
	private static final String LPW_BASE_URI = "http://iserve.kmi.open.ac.uk/ontology/lpw#"; // MUST not end with "/" or #

	private static final String getValidatedApisQuery = 
			"PREFIX lpw:<" + LPW_BASE_URI + "> " +
					"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
					"SELECT DISTINCT ?home ?val ?result " +
					"WHERE { " +
					"  ?api rdf:type lpw:API ." +
					"  ?api lpw:apiHome ?home ." +
					"  ?val lpw:validates ?home ." +
					"  ?val lpw:results ?result ." +
					"}";

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		LOG.info("Command-line arguments: " + Arrays.toString(args));

		Options options = new Options();
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		// create the parser
		CommandLineParser parser = new GnuParser();

		options.addOption("h", "help", false, "print this message");
		options.addOption("s", "server", true, "Sesame server URL (default '" + DEFAULT_SESAME_SERVER + "')");
		options.addOption("r", "repository", true, "Sesame repository (default '" + DEFAULT_REPO_NAME + "')");

		// parse the command line arguments
		CommandLine line = null;
		try {
			line = parser.parse(options, args);
			if (line.hasOption("help")) {
				formatter.printHelp("DeliciousAnnotator", options);
			}

			String repo = line.getOptionValue("r", DEFAULT_REPO_NAME);
			String server = line.getOptionValue("s", DEFAULT_SESAME_SERVER);

			obtainDeliciousTags(server, repo);
		} catch (ParseException e) {
			formatter.printHelp("DeliciousAnnotator", options);
		}
	}

	/**
	 * @param server
	 * @param repo
	 */
	private static void obtainDeliciousTags(String server, String repo) {
		int processedAPIs = 0;
		int taggedAPIs = 0;

		//			URI proxy;
		//			proxy = new URI("http://wwwcache.open.ac.uk:80");
		//			DeliciousClient client = new DeliciousClient(proxy);

		DeliciousClient client = new DeliciousClient();

		RepositoryModel model = null;
		RDFRepositoryConnector repositoryConnector = null;
		try {
			repositoryConnector = new RDFRepositoryConnector(server, repo);
			model = repositoryConnector.openRepositoryModel();

			List<URI> validateApis = getValidatedApis(model);
			for (URI apiUri : validateApis) {
				processedAPIs = processedAPIs + 1;
				TagFeed deliciousTagFeed = client.getTagFeedJSON(apiUri);
				updateApiEntry(model, apiUri, deliciousTagFeed);
				
				Thread.sleep(2000);
			}
		} catch (RepositoryException e) {
			LOG.error("Error connecting to repository", e);
		} catch (InterruptedException e) {
			LOG.error("Interrupted exception", e);
		} finally {
			repositoryConnector.closeRepositoryModel(model);
		}
	}


	/**
	 * @param model 
	 * @param apiUri
	 * @param deliciousTagFeed
	 */
	private static void updateApiEntry(RepositoryModel model, URI apiHomeUri, TagFeed deliciousTagFeed) {

		if (model != null && apiHomeUri != null && deliciousTagFeed != null) {
			if (!model.isOpen()) {
				model.open();
			}

			// Store 
			// hasDeliciousPosts 9
			// hasDeliciousTopTag -> _x -> hasTag "sms"
			//							-> hasNumberOfPosts 9
			org.ontoware.rdf2go.model.node.URI postsUri = 
					model.createURI(LPW_BASE_URI + "hasDeliciousPosts");
			org.ontoware.rdf2go.model.node.URI topTagUri = 
					model.createURI(LPW_BASE_URI + "hasDeliciousTopTag");
			org.ontoware.rdf2go.model.node.URI hasTagUri = 
					model.createURI(LPW_BASE_URI + "hasTag");
			org.ontoware.rdf2go.model.node.URI hasNumPostsUri = 
					model.createURI(LPW_BASE_URI + "hasNumberOfPosts");

			// Obtain the Resource of the API entry
			LOG.debug("Query for the API:");
			org.ontoware.rdf2go.model.node.URI apiUri = model.createURI(apiHomeUri.toASCIIString());
			String queryString = "SELECT DISTINCT ?api WHERE { ?api <" + LPW_BASE_URI + "apiHome> " + apiUri.toSPARQL()+" }";
			LOG.debug(queryString);
			QueryResultTable sparqlResults = model.sparqlSelect(queryString);
			if (sparqlResults != null && sparqlResults.iterator().hasNext()) {
				QueryRow row = sparqlResults.iterator().next();
				LOG.debug(row.toString());

				Node apiNode = row.getValue("api");

				// Add Delicious Post Numbers
				org.ontoware.rdf2go.model.node.URI typeInteger = XSD._integer; 
				DatatypeLiteral totalPosts = model.
						createDatatypeLiteral(String.valueOf(deliciousTagFeed.getTotal_posts()), typeInteger);
				model.addStatement(apiNode.asResource(), hasNumPostsUri, totalPosts);

				// Add each of the top tags
				Map<String, String> topTagsMap = deliciousTagFeed.getTop_tags();
				for (Entry<String, String> topTag : topTagsMap.entrySet()) {			
					// Add Top Tag
					BlankNode bn = model.createBlankNode();
					model.addStatement(bn, hasTagUri, topTag.getKey());

					DatatypeLiteral number = model.createDatatypeLiteral(topTag.getValue(), typeInteger);
					model.addStatement(bn, hasNumPostsUri, number);

					model.addStatement(apiNode.asResource(), topTagUri, bn);
				}
				LOG.info("Updated API entry with Delicious tags.");
			} else {
				LOG.warn("API not in the registry?! " + apiHomeUri);
			}
		}
	}


	/**
	 * @param model
	 * @return
	 */
	private static List<URI> getValidatedApis(RepositoryModel model) {

		List<URI> result = new ArrayList<URI>();

		if ( model != null ) {
			if (!model.isOpen()) {
				model.open();
			}

			LOG.info("Obtaining list of validated APIs");
			LOG.info(getValidatedApisQuery);

			int totalEntries = 0;
			QueryResultTable sparqlResults = model.sparqlSelect(getValidatedApisQuery);
			ClosableIterator<QueryRow> it = sparqlResults.iterator();

			while (sparqlResults != null && it.hasNext()) {
				QueryRow row = it.next();
				totalEntries++;

				Node homeNode = row.getValue("home");
				org.ontoware.rdf2go.model.node.URI homeUri = homeNode.asURI();

				Node resultNode = row.getValue("result");
				org.ontoware.rdf2go.model.node.DatatypeLiteral resultValue = resultNode.asDatatypeLiteral();
				if (Boolean.valueOf(resultValue.getValue())) {
					result.add(homeUri.asJavaURI());
				} else {
					LOG.info("The page was not validated as a Web API doc. " + homeUri.toString());
				}
			}
			// Close RDF2Go objects
			it.close();
		}
		return result;
	}
}

