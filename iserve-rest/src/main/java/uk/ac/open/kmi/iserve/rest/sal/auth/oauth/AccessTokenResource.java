package uk.ac.open.kmi.iserve.rest.sal.auth.oauth;//package uk.ac.open.kmi.iserve.sal.rest.auth.oauth;
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
//import org.openxri.xri3.impl.parser.Parser.isegment;
//
//import uk.ac.open.kmi.iserve.sal.manager.impl.KeyManagerRdf;
//import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;
//import uk.ac.open.kmi.iserve.sal.model.oauth.Consumer;
//import uk.ac.open.kmi.iserve.sal.model.oauth.RequestToken;
//
//import com.sun.jersey.api.core.HttpContext;
//import com.sun.jersey.oauth.server.OAuthServerRequest;
//import com.sun.jersey.oauth.signature.OAuthParameters;
//import com.sun.jersey.oauth.signature.OAuthSecrets;
//import com.sun.jersey.oauth.signature.OAuthSignature;
//import com.sun.jersey.oauth.signature.OAuthSignatureException;
//
//@Path("/access_token")
//public class AccessTokenResource {
//
//	public AccessTokenResource() throws RepositoryException, IOException { }
//
//	@GET
//	@Produces(MediaType.TEXT_PLAIN)
//	public Response generateAccessToken(@Context HttpContext hc) throws OAuthSignatureException, NoSuchAlgorithmException {
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
//		RequestToken requestToken = ManagerSingleton.getInstance().findRequestToken(params.getToken());
//		secrets.setTokenSecret(requestToken.getTokenSecret());
//		boolean verified = OAuthSignature.verify(request, params, secrets);
//		if ( verified == false ) {
//			return Response.status(Status.BAD_REQUEST).entity("signature_invalid").build();
//		}
//		String[] accessToken = KeyGenerator.generateAccessToken(consumer.getConsumerKey());
//		if ( requestToken.getGrantedBy() == null ) {
//			return Response.status(Status.BAD_REQUEST).entity("unauthorized request token: " + requestToken.getTokenKey()).build();
//		}
//		ManagerSingleton.getInstance().saveAccessToken(accessToken[0], accessToken[1], requestToken, requestToken.getGrantedBy().toString());
//		return Response.ok().entity("oauth_token=" + accessToken[0] + "&oauth_token_secret=" + accessToken[1]).build();
//	}
//
//}
