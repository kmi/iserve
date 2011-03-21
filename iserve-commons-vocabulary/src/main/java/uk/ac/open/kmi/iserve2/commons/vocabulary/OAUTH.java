package uk.ac.open.kmi.iserve2.commons.vocabulary;

import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.URIImpl;

public class OAUTH {

	// Name spaces
	public static final String NS_URI = "http://oauth.net/vocab/oauth#";
	public static final String NS_PREFIX = "oauth";

	// Classes
	public static final String CONSUMER = NS_URI + "Consumer";
	public static final URI Consumer = new URIImpl(CONSUMER);

	public static final String SECRET = NS_URI + "Secret";
	public static final URI Secret = new URIImpl(SECRET);

	public static final String REQUEST_TOKEN = NS_URI + "RequestToken";
	public static final URI RequestToken = new URIImpl(REQUEST_TOKEN);

	public static final String ACCESS_TOKEN = NS_URI + "AccessToken";
	public static final URI AccessToken = new URIImpl(ACCESS_TOKEN);

	// Properties
	public static final String CONSUMER_SECRET = NS_URI + "consumer_secret";
	public static final URI consumer_secret = new URIImpl(CONSUMER_SECRET);

	public static final String CONSUMER_KEY = NS_URI + "consumer_key";
	public static final URI consumer_key = new URIImpl(CONSUMER_KEY);

	public static final String TOKEN_KEY = NS_URI + "token_key";
	public static final URI token_key = new URIImpl(TOKEN_KEY);

	public static final String TOKEN_SECRET = NS_URI + "token_secret";
	public static final URI token_secret = new URIImpl(TOKEN_SECRET);

	public static final String PLAINTEXT = NS_URI + "plaintext";
	public static final URI plaintext = new URIImpl(PLAINTEXT);

	public static final String TOKEN_CONSUMER = NS_URI + "token_consumer";
	public static final URI token_consumer = new URIImpl(TOKEN_CONSUMER);

	public static final String GRANTED_BY = NS_URI + "granted_by";
	public static final URI granted_by = new URIImpl(GRANTED_BY);

	public static final String GRANTED_AT = NS_URI + "granted_at";
	public static final URI granted_at = new URIImpl(GRANTED_AT);

	public static final String ACCESS_REQUEST = NS_URI + "access_request";
	public static final URI access_request = new URIImpl(ACCESS_REQUEST);

	public static final String ACCESS_AS = NS_URI + "access_as";
	public static final URI access_as = new URIImpl(ACCESS_AS);

	public static final String CALLBACK = NS_URI + "callback";
	public static final URI callback = new URIImpl(CALLBACK);
}
