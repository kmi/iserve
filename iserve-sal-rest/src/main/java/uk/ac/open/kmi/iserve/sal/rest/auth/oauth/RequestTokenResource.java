//package uk.ac.open.kmi.iserve.sal.rest.auth.oauth;
//
//import java.io.IOException;
//import java.security.NoSuchAlgorithmException;
//
//import javax.ws.rs.GET;
//import javax.ws.rs.Path;
//import javax.ws.rs.Produces;
//import javax.ws.rs.core.Context;
//import javax.ws.rs.core.MediaType;
//import javax.ws.rs.core.Response;
//import javax.ws.rs.core.Response.Status;
//
//import org.openrdf.repository.RepositoryException;
//
//import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;
//import uk.ac.open.kmi.iserve.sal.model.oauth.Consumer;
//
//import com.sun.jersey.api.core.HttpContext;
//import com.sun.jersey.oauth.server.OAuthServerRequest;
//import com.sun.jersey.oauth.signature.OAuthParameters;
//import com.sun.jersey.oauth.signature.OAuthSecrets;
//import com.sun.jersey.oauth.signature.OAuthSignature;
//import com.sun.jersey.oauth.signature.OAuthSignatureException;
//
//@Path("/request_token")
//public class RequestTokenResource {
//
//	public RequestTokenResource() throws RepositoryException, IOException { }
//
//	@GET
//	@Produces(MediaType.TEXT_PLAIN)
//	public Response generateRequestToken(@Context HttpContext hc) throws OAuthSignatureException, NoSuchAlgorithmException {
//		// wrap incoming request for OAuth signature verification
//		OAuthServerRequest request = new OAuthServerRequest(hc.getRequest());
//
//		// get incoming OAuth parameters
//		OAuthParameters params = new OAuthParameters();
//		params.readRequest(request);
//
//		OAuthSecrets secrets = new OAuthSecrets();
//		// set secrets based on consumer key and/or token in parameters
//		Consumer consumer = ManagerSingleton.getInstance().findConsumer(params.getConsumerKey());
//		if ( consumer != null && consumer.getConsumerKey() != null && consumer.getConsumerSecret() != null ) {
//			secrets.setConsumerSecret(consumer.getConsumerSecret());
//		} else {
//			return Response.status(Status.BAD_REQUEST).entity("Consumer is not registered: " + params.getConsumerKey()).build();
//		}
//
//		boolean verified = OAuthSignature.verify(request, params, secrets);
//		if ( verified == false ) {
//			return Response.status(Status.BAD_REQUEST).entity("signature_invalid").build();
//		}
//		String consumer_key = params.getConsumerKey();
//		String[] requestToken = KeyGenerator.generateRequestToken(consumer_key);
//		ManagerSingleton.getInstance().saveRequestToken(requestToken[0], requestToken[1], consumer_key);
//		return Response.ok().entity("oauth_token=" + requestToken[0] + "&oauth_token_secret=" + requestToken[1]).build();
//	}
//
//}
