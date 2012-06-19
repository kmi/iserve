package uk.ac.open.kmi.iserve.sal.rest.auth.oauth;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.openid4java.association.AssociationException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;
import uk.ac.open.kmi.iserve.sal.model.oauth.RequestToken;

@Path("/authorize")
public class AuthorizeResource {

	private static final String loginHtml = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n" +
		"<html>\n" +
		"<head>\n" +
		"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
		"<title>Login iServe with OpenID</title>\n" +
		"</head>\n" +
		"<body>\n" +
		"	<form action=\"authorize\" method=\"post\">\n" +
		"		<table>\n" +
		"			<input type=\"hidden\" name=\"oauth_token\" id=\"oauth_token\" value=\"<<<oauth_token>>>\" />\n" +
		"			<tr><td>OpenID</td><td><input type=\"text\" name=\"OpenID\" /></td></tr>\n" +
		"			<tr><td><input type=\"submit\" name=\"login\" value=\"Login\" /></td></tr>\n" +
		"		</table>\n" +
		"	</form>\n" +
		"</body>\n" +
		"</html>";

	private static final String authHtml = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n" +
		"<html>\n" +
		"<head>\n" +
		"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
		"<title>Authorization</title>\n" +
		"</head>\n" +
		"<body>\n" +
		"	<form action=\"authorize\" method=\"post\">\n" +
		"		<table>\n" +
		"			<input type=\"hidden\" name=\"oauth_token\" id=\"oauth_token\" value=\"<<<oauth_token>>>\" />\n" +
		"			<input type=\"hidden\" name=\"OpenID\" id=\"OpenID\" value=\"<<<openid>>>\" />\n" +
		"			<tr><td><<<customer_key>>> request for access to <<<openid>>>'s iServe account.</td></tr>" +
		"			<tr><td>	<input type=\"submit\" name=\"allow\" id=\"allow\" value=\"Allow\" />" +
		"             <input type=\"submit\" name=\"deny\" id=\"deny\" value=\"Deny\" /></td></tr>\n" +
		"		</table>\n" +
		"	</form>\n" +
		"</body>\n" +
		"</html>";

	private static ConsumerManager manager = null;

	public AuthorizeResource() throws ConsumerException, RepositoryException, IOException {
		// TODO: set the proxy for OpenID consumer
		if ( manager == null ) {
			manager = new ConsumerManager();
		}
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response authorize(@QueryParam("oauth_token") String oauthToken, @Context HttpServletRequest request) throws MessageException, DiscoveryException, AssociationException {
		if ( oauthToken == null ) {
			System.out.println("oauthToken == null");
		}
		if ( request.getSession().getAttribute("openid-disc") == null ) {
			return Response.ok().entity(loginHtml.replaceAll("<<<oauth_token>>>", oauthToken)).build();
		} else {
			ParameterList response = new ParameterList(request.getParameterMap());
			// retrieve the previously stored discovery information
			DiscoveryInformation discovered = (DiscoveryInformation) request.getSession().getAttribute("openid-disc");
			// extract the receiving URL from the HTTP request
			StringBuffer receivingURL = request.getRequestURL();
			String queryString = request.getQueryString();
			if ( queryString != null && queryString.length() > 0 ) {
				receivingURL.append("?").append(queryString);
			}

			// verify the response; ConsumerManager needs to be the same
			// (static) instance used to place the authentication request
			VerificationResult verification = manager.verify(
					receivingURL.toString(),
					response, discovered);

            // examine the verification result and extract the verified identifier
            Identifier verified = verification.getVerifiedId();
            if (verified != null) {
            	HttpSession session = request.getSession();
    			session.setAttribute("logged-in", verified.toString());
    			session.removeAttribute("openid-disc");
    			RequestToken requestToken = ManagerSingleton.getInstance().findRequestToken(oauthToken);
    			String entityString = authHtml.replaceAll("<<<customer_key>>>", requestToken.getConsumer().toString()).replaceAll("<<<openid>>>", verified.toString())
    					.replace("<<<oauth_token>>>", oauthToken);
            	return Response.ok().entity(entityString).build();
            } else {
              	return Response.status(400).entity("error").build();
            }
		}
	}

	//TODO: Fix authentication here!
	@POST
	@Produces(MediaType.TEXT_HTML)
	public Response login(@FormParam("oauth_token") String oauthToken,
			@FormParam("OpenID") String userSuppliedString,
			@FormParam("allow") String allowString,
			@FormParam("deny") String denyString,
			@Context HttpServletRequest request) throws DiscoveryException, MessageException, ConsumerException, URISyntaxException {
		if ( allowString != null || denyString != null ) {
			if ( allowString != null && allowString.endsWith("Allow") ) {
				// save the authorized oauth_token
				// TODO: callback
				ManagerSingleton.getInstance().saveAuthorization(oauthToken, userSuppliedString, null);
				return Response.ok().entity("Go to Access Token").build();
			} else if ( denyString != null && denyString.endsWith("Deny")) {
				return Response.status(Status.BAD_REQUEST).entity("Denied by the user").build();				
			}
			return Response.status(Status.BAD_REQUEST).entity("Error occurs during authorization").build();
		} else {
			String returnToUrl = "http://localhost:8080/iserve/oauth/authorize?oauth_token=" + oauthToken;
			// perform discovery on the user-supplied identifier
			List discoveries = manager.discover(userSuppliedString);
	
			// attempt to associate with the OpenID provider
			// and retrieve one service endpoint for authentication
			DiscoveryInformation discovered = manager.associate(discoveries);
	
			// store the discovery information in the user's session
			request.getSession().setAttribute("openid-disc", discovered);
	
			// obtain a AuthRequest message to be sent to the OpenID provider
			AuthRequest authReq = manager.authenticate(discovered, returnToUrl);
	//		if (!discovered.isVersion2() ) {
				// Option 1: GET HTTP-redirect to the OpenID Provider endpoint
				// The only method supported in OpenID 1.x
				// redirect-URL usually limited ~2048 bytes
				return Response.seeOther(new URI(authReq.getDestinationUrl(true))).build();
	//		} else {
				// Option 2: HTML FORM Redirection (Allows payloads >2048 bytes)
	//			RequestDispatcher dispatcher =
	//                    getServletContext().getRequestDispatcher("formredirection.jsp");
	//            httpReq.setAttribute("parameterMap", authReq.getParameterMap());
	//            httpReq.setAttribute("destinationUrl", authReq.getDestinationUrl(false));
	//            dispatcher.forward(httpReq, httpResp);
	//			return null;
	//		}
		}
	}

}
