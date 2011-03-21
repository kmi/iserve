package uk.ac.open.kmi.iserve2.sal.rest;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.MediaType;

import uk.ac.open.kmi.iserve2.commons.io.IOUtil;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.oauth.client.OAuthClientFilter;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthSecrets;

public class OAuthClientTest {

	// base URL for the API calls
	private static final String URL_API = "http://localhost:8083/iserve2/resource/services";

	private static final String URL_REQUEST_TOKEN = "http://localhost:8080/iserve2/oauth/request_token";
	// authorization URL
	private static final String URL_AUTHORIZE = "http://localhost:8080/iserve2/oauth/authorize";

	// access token URL
	private static final String URL_ACCESS_TOKEN = "http://localhost:8083/iserve2/oauth/access_token";

	private static final String CONSUMER_KEY = "27d02ae0d5288ec91438e70f6f39a674";

	private static final String CONSUMER_SECRET = "EjZbRUJVAPjbldMt";

	private static void requestAuthorization(Client client) throws IOException, URISyntaxException {
		// Create a resource to be used to make SmugMug API calls
		WebResource resource = client.resource(URL_REQUEST_TOKEN);

		// Set the OAuth parameters
		OAuthSecrets secrets = new OAuthSecrets().consumerSecret(CONSUMER_SECRET);
		OAuthParameters params = new OAuthParameters().consumerKey(CONSUMER_KEY)
				.signatureMethod("HMAC-SHA1").version("1.0");

		// Create the OAuth client filter
		OAuthClientFilter filter = new OAuthClientFilter(client.getProviders(), params, secrets);

		// Add the filter to the resource
		resource.addFilter(filter);

		// make the request
		ClientResponse response = resource.get(ClientResponse.class);
		int status = response.getStatus();
		if ( status != 200 ) {
			System.err.println("getRequestToken failed with response: " + response.getEntity(String.class));
		} else {
			String textEntity = response.getEntity(String.class);
			System.out.println(textEntity);
			String[] tokens = parseRequestToken(textEntity);
			Desktop.getDesktop().browse(new URI(URL_AUTHORIZE + "?oauth_token=" + tokens[0]));
		}
	}

	public static void requestAccessToken(Client client) {
		// Create a resource to be used to make SmugMug API calls
		WebResource resource = client.resource(URL_ACCESS_TOKEN);

		// Set the OAuth parameters
		OAuthSecrets secrets = new OAuthSecrets().consumerSecret(CONSUMER_SECRET);
		secrets.setTokenSecret("39cf9236cec590d0efacb3a10be87bc9");
		OAuthParameters params = new OAuthParameters().consumerKey(CONSUMER_KEY)
				.signatureMethod("HMAC-SHA1").version("1.0");
		params.setToken("479ea2c4f1f549044af5c834a493974e");

		// Create the OAuth client filter
		OAuthClientFilter filter = new OAuthClientFilter(client.getProviders(), params, secrets);

		// Add the filter to the resource
		resource.addFilter(filter);

		// make the request
		ClientResponse response = resource.get(ClientResponse.class);
		int status = response.getStatus();
		if ( status != 200 ) {
			System.err.println("getAccessToken failed with response: " + response.getEntity(String.class));
		} else {
			String textEntity = response.getEntity(String.class);
			System.out.println(textEntity);
//			String[] tokens = parseRequestToken(textEntity);
		}
	}

	public static void main(String[] args) throws Exception {
		// Create a Jersey client
		Client client = Client.create();
//		requestAuthorization(client);
//		accessToken(client);
		accessResource(client);
	}

	private static void accessResource(Client client) {
		String accessToken = "65adbefed569a92ca223743eb630c677";
		String accessTokenSecret = "6d9fec6e242471a6c1d0d4446a64453d";

		// Create a resource to be used to make SmugMug API calls
		WebResource resource = client.resource(URL_API);
		
		// Set the OAuth parameters
		OAuthSecrets secrets = new OAuthSecrets().consumerSecret(CONSUMER_SECRET);
		secrets.setTokenSecret(accessTokenSecret);
		OAuthParameters params = new OAuthParameters().consumerKey(CONSUMER_KEY)
				.signatureMethod("HMAC-SHA1").version("1.0");
		params.setToken(accessToken);

		// Create the OAuth client filter
		OAuthClientFilter filter = new OAuthClientFilter(client.getProviders(), params, secrets);

		// Add the filter to the resource
		resource.addFilter(filter);

		// make the request
		try {
			ClientResponse response = resource.type(MediaType.TEXT_HTML).post(ClientResponse.class,
					IOUtil.readString(new File("/Users/dl3962/Action/iServe/data/testing-20100125/hrestAnnotation.html")));
			int status = response.getStatus();
			if ( status != 201 ) {
				System.err.println("accessResource failed with response: " + response.getEntity(String.class));
			} else {
				String textEntity = response.getEntity(String.class);
				System.out.println(textEntity);
//				String[] tokens = parseRequestToken(textEntity);
			}
		} catch (UniformInterfaceException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String[] parseRequestToken(String oauthResponse) {
		String[] result = new String[2];
		String[] tokens = oauthResponse.split("&");
		for ( int i = 0; i < tokens.length; i++ ) {
			if ( tokens[i].startsWith("oauth_token=") ) {
				result[0] = tokens[i].substring("oauth_token=".length()); 
			}
			if ( tokens[i].startsWith("oauth_token_secret=") ) {
				result[1] = tokens[i].substring("oauth_token_secret=".length()); 
			}
		}
		return result;
	}

}
