package uk.ac.open.kmi.iserve.sal.manager.impl;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.Variable;
import org.ontoware.rdf2go.model.node.impl.URIImpl;
import org.ontoware.rdf2go.vocabulary.RDF;
import org.openrdf.rdf2go.RepositoryModel;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.iserve.commons.io.RDFRepositoryConnector;
import uk.ac.open.kmi.iserve.commons.vocabulary.OAUTH;
import uk.ac.open.kmi.iserve.sal.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.manager.KeyManager;
import uk.ac.open.kmi.iserve.sal.model.impl.AccessTokenImpl;
import uk.ac.open.kmi.iserve.sal.model.impl.ConsumerImpl;
import uk.ac.open.kmi.iserve.sal.model.impl.RequestTokenImpl;
import uk.ac.open.kmi.iserve.sal.model.oauth.AccessToken;
import uk.ac.open.kmi.iserve.sal.model.oauth.Consumer;
import uk.ac.open.kmi.iserve.sal.model.oauth.RequestToken;
import uk.ac.open.kmi.iserve.sal.model.oauth.Token;

/**
 * Class in charge of providing secured access to iServe
 * TODO: Check whether this could be pushed up and to which extent
 * 
 * @author ?
 */
public class KeyManagerRdf implements KeyManager {

	private SystemConfiguration configuration;
	
	public KeyManagerRdf(SystemConfiguration configuration) throws RepositoryException { 	
		this.configuration = configuration;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.impl.KeyManager#saveConsumer(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void saveConsumer(String consumerUriString, String consumerKey, String consumerSecret) {
		
		RDFRepositoryConnector connector = ManagerSingleton.getInstance().getUsersRepositoryConnector();
		RepositoryModel model = connector.openRepositoryModel();
		
		URI consumerUri = new URIImpl(consumerUriString);
		model.addStatement(consumerUri, RDF.type, OAUTH.Consumer);
		model.addStatement(consumerUri, OAUTH.consumer_key, consumerKey);

		// create secret for consumer key
		URI consumerSecretUri = new URIImpl("http://" + configuration.getUriPrefix() + "/secret/" + consumerKey);
//		URI consumerSecretUri = new URIImpl("http://iserve.kmi.open.ac.uk/secret/" + consumerKey);
		model.addStatement(consumerSecretUri, RDF.type, OAUTH.Secret);
		model.addStatement(consumerSecretUri, OAUTH.plaintext, consumerSecret);
		model.addStatement(consumerUri, OAUTH.consumer_secret, consumerSecretUri);

		connector.closeRepositoryModel(model);
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.impl.KeyManager#findConsumer(java.lang.String)
	 */
	@Override
	public Consumer findConsumer(String consumerKey) {
		String queryString = "SELECT DISTINCT ?consumer ?secret_text WHERE {\n" +
				"	?consumer " + RDF.type.toSPARQL() + " " + OAUTH.Consumer.toSPARQL() + " . \n" +
				"	?consumer " + OAUTH.consumer_key.toSPARQL() + " \"" + consumerKey + "\" .\n" +
				"	?consumer " + OAUTH.consumer_secret.toSPARQL() + " ?secret .\n" +
				"	?secret " + OAUTH.plaintext.toSPARQL() + " ?secret_text .\n" +
				"}";
		RDFRepositoryConnector connector = ManagerSingleton.getInstance().getUsersRepositoryConnector();
		RepositoryModel model = connector.openRepositoryModel();
		
		QueryResultTable qrt = model.sparqlSelect(queryString);
		if ( qrt == null ) {
			connector.closeRepositoryModel(model);
			return null;
		}
		ClosableIterator<QueryRow> iter = qrt.iterator();
		if ( iter == null ) {
			connector.closeRepositoryModel(model);
			return null;
		}
		Consumer consumer = new ConsumerImpl();
		if ( iter.hasNext() ) {
			QueryRow row = iter.next();
			String uriString= row.getValue("consumer").toString();
			String consumerSecret = row.getValue("secret_text").toString();
			consumer.setURI(new uk.ac.open.kmi.iserve.sal.model.impl.URIImpl(uriString));
			consumer.setConsumerKey(consumerKey);
			consumer.setConsumerSecret(consumerSecret);
			iter.close();
		}
		connector.closeRepositoryModel(model);
		return consumer;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.impl.KeyManager#findRequestToken(java.lang.String)
	 */
	@Override
	public RequestToken findRequestToken(String requestTokenKey) {
		RequestToken requestToken = (RequestToken) findToken(requestTokenKey, OAUTH.RequestToken);
		// granted by and callback
		RDFRepositoryConnector connector = ManagerSingleton.getInstance().getUsersRepositoryConnector();
		RepositoryModel model = connector.openRepositoryModel();
		
		String queryString = "SELECT DISTINCT ?grant ?consumer ?callback WHERE {\n" +
				"	?token " + RDF.type.toSPARQL() + " " + OAUTH.RequestToken.toSPARQL() + " . \n" +
				"	?token " + OAUTH.token_key.toSPARQL() + " \"" + requestTokenKey + "\" . \n" +
				"	?token " + OAUTH.token_consumer.toSPARQL() + " ?consumer . \n" +
				"	OPTIONAL { ?token " + OAUTH.granted_by.toSPARQL() + " ?grant. }\n" +
				"	OPTIONAL { ?token " + OAUTH.callback.toSPARQL() + " ?callback . }\n" +
				"}";

		QueryResultTable qrt = model.sparqlSelect(queryString);
		if ( qrt == null ) {
			connector.closeRepositoryModel(model);
			return requestToken;
		}
		ClosableIterator<QueryRow> iter = qrt.iterator();
		if ( iter == null ) {
			connector.closeRepositoryModel(model);
			return requestToken;
		}
		while ( iter.hasNext() ) {
			QueryRow row = iter.next();
			if ( row.getValue("consumer") != null ) {
				String consumer = row.getValue("consumer").toString();
				requestToken.setConsumer(new uk.ac.open.kmi.iserve.sal.model.impl.URIImpl(consumer));
			}
			if ( row.getValue("grant") != null ) {
				String grant = row.getValue("grant").toString();
				requestToken.setGrantedBy(new uk.ac.open.kmi.iserve.sal.model.impl.URIImpl(grant));
			}
			if ( row.getValue("callback") != null ) {
				String callback = row.getValue("callback").toString();
				requestToken.setCallback(new uk.ac.open.kmi.iserve.sal.model.impl.URIImpl(callback));
			}
			iter.close();
		}

		connector.closeRepositoryModel(model);
		return requestToken;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.impl.KeyManager#findAccessToken(java.lang.String)
	 */
	@Override
	public AccessToken findAccessToken(String accessTokenKey) {
		AccessToken accessToken = (AccessToken) findToken(accessTokenKey, OAUTH.AccessToken);
		// "access as" and request token
		RDFRepositoryConnector connector = ManagerSingleton.getInstance().getUsersRepositoryConnector();
		RepositoryModel model = connector.openRepositoryModel();
		
		String queryString = "SELECT DISTINCT ?access_as ?request_token_key WHERE {\n" +
				"	?token " + RDF.type.toSPARQL() + " " + OAUTH.AccessToken.toSPARQL() + " . \n" +
				"	?token " + OAUTH.access_as.toSPARQL() + " ?access_as. \n" +
				"	?token " + OAUTH.access_request.toSPARQL() + " ?request_token .\n" +
				"	?request_token " + OAUTH.token_key.toSPARQL() + " ?request_token_key .\n" +
				"}";
		QueryResultTable qrt = model.sparqlSelect(queryString);
		if ( qrt == null ) {
			connector.closeRepositoryModel(model);
			return accessToken;
		}
		ClosableIterator<QueryRow> iter = qrt.iterator();
		if ( iter == null ) {
			connector.closeRepositoryModel(model);
			return accessToken;
		}
		if ( iter.hasNext() ) {
			QueryRow row = iter.next();
			if ( row.getValue("access_as") != null ) {
				String accessAs = row.getValue("access_as").toString();
				accessToken.setAccessAs(new uk.ac.open.kmi.iserve.sal.model.impl.URIImpl(accessAs));
			}
			if ( row.getValue("request_token_key") != null ) {
				String requestTokenKey = row.getValue("request_token_key").toString();
				accessToken.setRequestToken(findRequestToken(requestTokenKey));
			}
			iter.close();
		}

		connector.closeRepositoryModel(model);
		return accessToken;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.impl.KeyManager#saveAuthorization(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void saveAuthorization(String requestTokenKey, String openid, String callback) {
		RDFRepositoryConnector connector = ManagerSingleton.getInstance().getUsersRepositoryConnector();
		RepositoryModel model = connector.openRepositoryModel();
		
		RequestToken requestToken = findRequestToken(requestTokenKey);
		if ( requestToken != null ) {
			model.addStatement(new URIImpl(requestToken.getURI().toString()), OAUTH.granted_by, new URIImpl(openid));
			if ( callback != null && callback.equals("") == false ) {
				model.addStatement(new URIImpl(requestToken.getURI().toString()), OAUTH.callback, new URIImpl(callback));
			}
		}
		connector.closeRepositoryModel(model);
	}

	private URI saveToken(RepositoryModel model, String tokenKey, String tokenSecret, URI type) {
		URI tokenUri = new URIImpl("http://" + configuration.getUriPrefix() + "/token/" + tokenKey);

		model.addStatement(tokenUri, RDF.type, type);
		model.addStatement(tokenUri, OAUTH.token_key, tokenKey);

		// create secret for request token
		URI tokenSecretUri = new URIImpl("http://" + configuration.getUriPrefix() + "/secret/" + tokenKey);
		model.addStatement(tokenSecretUri, RDF.type, OAUTH.Secret);
		model.addStatement(tokenSecretUri, OAUTH.plaintext, tokenSecret);
		model.addStatement(tokenUri, OAUTH.token_secret, tokenSecretUri);
		return tokenUri;
	}

	private Token findToken(String tokenKey, URI type) {
		String queryString = "SELECT DISTINCT ?token ?secret_text WHERE {\n" +
				"	?token " + RDF.type.toSPARQL() + " " + type.toSPARQL() + " . \n" +
				"	?token " + OAUTH.token_key.toSPARQL() + " \"" + tokenKey + "\" .\n" +
				"	?token " + OAUTH.token_secret.toSPARQL() + " ?secret .\n" +
				"	?secret " + OAUTH.plaintext.toSPARQL() + " ?secret_text .\n" +
				"}";
		RDFRepositoryConnector connector = ManagerSingleton.getInstance().getUsersRepositoryConnector();
		RepositoryModel model = connector.openRepositoryModel();
		
		QueryResultTable qrt = model.sparqlSelect(queryString);
		if ( qrt == null ) {
			connector.closeRepositoryModel(model);
			return null;
		}
		ClosableIterator<QueryRow> iter = qrt.iterator();
		if ( iter == null ) {
			connector.closeRepositoryModel(model);
			return null;
		}
		Token token = null;
		if ( type.toString().equals(OAUTH.REQUEST_TOKEN) ) {
			token = new RequestTokenImpl();
		} else if ( type.toString().equals(OAUTH.ACCESS_TOKEN) ) {
			token = new AccessTokenImpl();
		}
		if ( iter.hasNext() ) {
			QueryRow row = iter.next();
			String uriString= row.getValue("token").toString();
			String tokenSecret = row.getValue("secret_text").toString();
			token.setURI(new uk.ac.open.kmi.iserve.sal.model.impl.URIImpl(uriString));
			token.setTokenKey(tokenKey);
			token.setTokenSecret(tokenSecret);
			iter.close();
		}
		connector.closeRepositoryModel(model);
		return token;
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.impl.KeyManager#saveRequestToken(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void saveRequestToken(String tokenKey, String tokenSecret, String consumerKey) {
		RDFRepositoryConnector connector = ManagerSingleton.getInstance().getUsersRepositoryConnector();
		RepositoryModel model = connector.openRepositoryModel();
		
		Consumer consumer = findConsumer(consumerKey);
		URI tokenUri = saveToken(model, tokenKey, tokenSecret, OAUTH.RequestToken);
		model.addStatement(tokenUri, OAUTH.token_consumer, new URIImpl(consumer.getURI().toString()));
		connector.closeRepositoryModel(model);
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.impl.KeyManager#deleteRequestToken(java.lang.String)
	 */
	@Override
	public void deleteRequestToken(String tokenKey) {
		RDFRepositoryConnector connector = ManagerSingleton.getInstance().getUsersRepositoryConnector();
		RepositoryModel model = connector.openRepositoryModel();
		
		RequestToken requestToken = findRequestToken(tokenKey);
		ClosableIterator<Statement> stmts = model.findStatements(new URIImpl(requestToken.getURI().toString()), Variable.ANY, Variable.ANY);
		if ( stmts != null ) {
			while ( stmts.hasNext() ) {
				Statement stmt = stmts.next();
				if ( stmt.getPredicate().toString().equals(OAUTH.TOKEN_SECRET) ) {
					deleteSecret(model, stmt.getObject().asURI());
				}
				model.removeStatement(stmt);
			}
			stmts.close();
		}
		connector.closeRepositoryModel(model);		
	}

	private void deleteSecret(RepositoryModel model, URI secret) {
		ClosableIterator<Statement> stmts = model.findStatements(secret, Variable.ANY, Variable.ANY);
		if ( stmts != null ) {
			while ( stmts.hasNext() ) {
				Statement stmt = stmts.next();
				model.removeStatement(stmt);
			}
			stmts.close();
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.impl.KeyManager#saveAccessToken(java.lang.String, java.lang.String, uk.ac.open.kmi.iserve.sal.model.oauth.RequestToken, java.lang.String)
	 */
	@Override
	public void saveAccessToken(String tokenKey, String tokenSecret, RequestToken requestToken, String accessAs) {
		RDFRepositoryConnector connector = ManagerSingleton.getInstance().getUsersRepositoryConnector();
		RepositoryModel model = connector.openRepositoryModel();
		
		URI tokenUri = saveToken(model, tokenKey, tokenSecret, OAUTH.AccessToken);
		model.addStatement(tokenUri, OAUTH.access_request, new URIImpl(requestToken.getURI().toString()));
		model.addStatement(tokenUri, OAUTH.access_as, new URIImpl(accessAs));
		connector.closeRepositoryModel(model);
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.impl.KeyManager#deleteAccessToken(java.lang.String)
	 */
	@Override
	public void deleteAccessToken(String tokenKey) {
		RDFRepositoryConnector connector = ManagerSingleton.getInstance().getUsersRepositoryConnector();
		RepositoryModel model = connector.openRepositoryModel();
		
		AccessToken accessToken = findAccessToken(tokenKey);
		ClosableIterator<Statement> stmts = model.findStatements(new URIImpl(accessToken.getURI().toString()), Variable.ANY, Variable.ANY);
		if ( stmts != null ) {
			while ( stmts.hasNext() ) {
				Statement stmt = stmts.next();
				if ( stmt.getPredicate().toString().equals(OAUTH.TOKEN_SECRET) ) {
					deleteSecret(model, stmt.getObject().asURI());
				}
				model.removeStatement(stmt);
			}
			stmts.close();
		}
		connector.closeRepositoryModel(model);		
	}

//	public static void main(String[] args) {
//		try {
//			KeyManager keyManager = new KeyManager("http://service-repository.kmi.open.ac.uk:8080/openrdf-sesame", "iserve-logs");
////			keyManager.saveConsumer("http://iserve-3party.example.com", "27d02ae0d5288ec91438e70f6f39a674", "EjZbRUJVAPjbldMt");
//			RequestToken requestToken = keyManager.findRequestToken("479ea2c4f1f549044af5c834a493974e");
//			System.out.println(requestToken.getGrantedBy());
////			Consumer consumer = keyManager.findConsumer("27d02ae0d5288ec91438e70f6f39a674");
////			System.out.println(consumer.getConsumerKey());
////			RequestToken requestToken = keyManager.findRequestToken("a30bd60e139fb98912028c39646b0a3f");
////			keyManager.deleteRequestToken("a30bd60e139fb98912028c39646b0a3f");
////			String[] accessTokenStrings = KeyGenerator.generateAccessToken(consumer.getConsumerKey());
////			keyManager.saveAccessToken(accessTokenStrings[0], accessTokenStrings[1], requestToken, "http://liudong.myopenid.com");
//
////			keyManager.saveAuthorization("a30bd60e139fb98912028c39646b0a3f", "http://liudong.myopenid.com", "http://iserve-3party.example.com");
////			String[] requestKeyStrings = KeyGenerator.generateRequestToken("27d02ae0d5288ec91438e70f6f39a674");
////			keyManager.saveRequestToken(requestKeyStrings[0], requestKeyStrings[1]);
//			
//		} catch (RepositoryException e) {
//			e.printStackTrace();
//		}
//	}

}
