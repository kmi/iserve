/*
   Copyright ${year}  Knowledge Media Institute - The Open University

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
package uk.ac.open.kmi.iserve.sal.rest.auth;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.openrdf.repository.RepositoryException;

import uk.ac.open.kmi.iserve.sal.exception.UserException;
import uk.ac.open.kmi.iserve.sal.manager.UserManager;
import uk.ac.open.kmi.iserve.sal.model.oauth.AccessToken;
import uk.ac.open.kmi.iserve.sal.model.oauth.Consumer;
import uk.ac.open.kmi.iserve.sal.model.user.User;
import uk.ac.open.kmi.iserve.sal.rest.Factory;
import uk.ac.open.kmi.iserve.sal.rest.auth.oauth.KeyManager;
import uk.ac.open.kmi.iserve.sal.util.MD5;

import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.oauth.server.OAuthServerRequest;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthSecrets;
import com.sun.jersey.oauth.signature.OAuthSignature;
import com.sun.jersey.oauth.signature.OAuthSignatureException;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

public class SecurityFilter implements ContainerRequestFilter {

	/**
	 * <p>
	 * The realm name to use in authentication challenges.
	 * </p>
	 */
	private static final String REALM = "iServe SAL RESTful API";

	/**
	 * <p>
	 * The URI information for this request.
	 * </p>
	 */
	@Context
	UriInfo uriInfo;

	private UserManager userManager;

	private KeyManager keyManager;

	public SecurityFilter() throws RepositoryException, IOException {
		userManager = Factory.getInstance().createUserManager();
		keyManager = Factory.getInstance().createKeyManager();
	}

	public ContainerRequest filter(ContainerRequest request) {
		if ( request.getPath().equals("authorize") )
			return request;
		try {
			authenticate(request);
		} catch (IOException e) {
			throw new MappableContainerException(e);
		}
		return request;
	}

	private void authenticate(ContainerRequest request) throws IOException {
		String method = request.getMethod();
		if ("GET".equalsIgnoreCase(method) == false) {
			String authentication = request
					.getHeaderValue(ContainerRequest.AUTHORIZATION);
			if (authentication == null) {
				throw new MappableContainerException(
						new AuthenticationException("Authentication credentials are required\r\n", REALM));
			}
			if (authentication.startsWith("Basic ")) {
				Authorizer authorizer = basicAuthenticate(authentication);
				request.setSecurityContext(authorizer);
			} else {
				Authorizer authorizer = oauthAuthenticate(request);
				request.setSecurityContext(authorizer);
			}
		}
	}

	private Authorizer oauthAuthenticate(ContainerRequest request) {
		// Read the OAuth parameters from the request
		OAuthServerRequest oauthServerRequest = new OAuthServerRequest(request);
		OAuthParameters params = new OAuthParameters();
		params.readRequest(oauthServerRequest);

		// Set the secret(s), against which we will verify the request
		OAuthSecrets secrets = new OAuthSecrets();
		Consumer consumer = keyManager.findConsumer(params.getConsumerKey());
		if ( consumer != null && consumer.getConsumerKey() != null && consumer.getConsumerSecret() != null ) {
			secrets.setConsumerSecret(consumer.getConsumerSecret());
		} else {
			throw new MappableContainerException(
					new AuthenticationException("Consumer is not registered: " + params.getConsumerKey(), REALM));
		}

		AccessToken accessToken = keyManager.findAccessToken(params.getToken());
		secrets.setTokenSecret(accessToken.getTokenSecret());

		// TODO: Check that the timestamp has not expired
//		String timestampStr = params.getTimestamp();

		boolean verified = false;
		try {
			verified = OAuthSignature.verify(oauthServerRequest, params, secrets);
		} catch (OAuthSignatureException e) {
			throw new MappableContainerException(
					new AuthenticationException(e.getMessage(), REALM));
		}
		if ( verified == false ) {
			throw new MappableContainerException(
					new AuthenticationException("signature_invalid", REALM));
		}
		User user = null;
		try {
			user = userManager.getUser(accessToken.getAccessAs());
		} catch (UserException e) {
			throw new MappableContainerException(e);
		}
		if ( user == null || user.getFoafId() == null || user.getFoafId().toString() == null ||
				"".equalsIgnoreCase(user.getFoafId().toString()) ) {
			throw new MappableContainerException(new AuthenticationException("Accessing as a non-existing user", REALM));
		}
		Authorizer authorizer = new Authorizer(user);
		authorizer.setAuthenticationScheme("OAuth");
		authorizer.setOAuthConsumer(accessToken.getRequestToken().getConsumer().toString());
		return authorizer;
	}

	private Authorizer basicAuthenticate(String authentication) throws IOException {
		authentication = authentication.substring("Basic ".length());
		String[] values = new String(Base64.base64Decode(authentication)).split(":");
		if (values.length < 2) {
			throw new MappableContainerException(new AuthenticationException(
					"Invalid syntax for username and password\r\n", REALM));
		}
		String username = values[0];
		String password = values[1];
		if ((username == null) || (password == null)) {
			throw new MappableContainerException(new AuthenticationException(
					"Missing username or password\r\n", REALM));
		}

		User user = null;
		try {
			user = userManager.getUser(username);
		} catch (UserException e) {
			throw new MappableContainerException(e);
		}

		try {
			password = MD5.digest(password);
		} catch (NoSuchAlgorithmException e1) {
		}

		if ( user == null || user.getFoafId() == null || user.getFoafId().toString() == null ||
				"".equalsIgnoreCase(user.getFoafId().toString()) ) {
			throw new MappableContainerException(new AuthenticationException("Invalid username or password\r\n", REALM));
		} else if ( user.getPassword() == null || "".equalsIgnoreCase(user.getPassword()) || !(password.equalsIgnoreCase(user.getPassword())) ) {
			throw new MappableContainerException(new AuthenticationException("Invalid username or password\r\n", REALM));
		}
		Authorizer authorizer = new Authorizer(user);
		authorizer.setAuthenticationScheme(SecurityContext.BASIC_AUTH);
		return authorizer;
	}

	public class Authorizer implements SecurityContext {

		public Authorizer(final User user) {
			this.principal = new Principal() {
				public String getName() {
					return user.getFoafId().toString();
				}
			};
		}

		private Principal principal;

		private String scheme;

		private String oauthConsumer;

		public Principal getUserPrincipal() {
			return principal;
		}

		public boolean isUserInRole(String role) {
			// TODO Auto-generated method stub
			return true;
		}

		public boolean isSecure() {
			return true;// "https".equals(uriInfo.getRequestUri().getScheme());
		}

		public String getAuthenticationScheme() {
			return scheme;
		}

		public void setAuthenticationScheme(String scheme) {
			this.scheme = scheme;
		}

		public void setOAuthConsumer(String oauthConsumer) {
			this.oauthConsumer = oauthConsumer;
		}

		public String getOAuthConsumer() {
			return oauthConsumer;
		}

	}

}
