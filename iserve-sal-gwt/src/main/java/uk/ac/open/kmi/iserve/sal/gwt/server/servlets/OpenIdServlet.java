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
package uk.ac.open.kmi.iserve.sal.gwt.server.servlets;

import com.google.gwt.user.client.rpc.RemoteService;

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
import org.openid4java.util.HttpClientFactory;
import org.openid4java.util.ProxyProperties;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpSession;

/**
 * A servlet in charge of the OpenID authentication process, from authentication
 * to verification.
 */

@SuppressWarnings( { "GwtInconsistentSerializableClass" })
public final class OpenIdServlet extends HttpServlet implements RemoteService {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5549586071428850281L;

	public static final String authParameter = "app-openid-auth";
	public static final String nameParameter = "app-openid-name";

	private ConsumerManager manager;

	public OpenIdServlet() {
		manager = null;
	}

	public void init() throws ServletException {
		super.init();
		String param = getServletContext().getInitParameter("config-file");
		if (param == null) {
			throw new UnavailableException("Missing context parameter 'config-file'");
		}
		String filePath = getServletContext().getRealPath("/") + "/WEB-INF/" + param;
		Properties props = new Properties();
		String proxyHostName = null;
		String proxyPort = null;
		try {
			InputStream in = new BufferedInputStream (new FileInputStream(filePath));
			props.load(in);
			proxyHostName = props.getProperty ("proxyHostName");
			proxyPort = props.getProperty ("proxyPort");
		} catch (Exception e) {
	        e.printStackTrace();
	    }
		if ( proxyHostName != null && proxyHostName != ""
			&& proxyPort != null && proxyPort != "" ) {
			// loaded from the properties file
			ProxyProperties proxyProps = new ProxyProperties();
			proxyProps.setProxyHostName("wwwcache.open.ac.uk");
			proxyProps.setProxyPort(80);
			HttpClientFactory.setProxyProperties(proxyProps);
		}

		try {
			manager = new ConsumerManager();
		} catch (ConsumerException e) {
			throw new RuntimeException("Error creating consumer manager", e);
		}
	}

	/**
	 * Implements the GET method by either sending it to an OpenID
	 * authentication or verification mechanism.
	 * 
	 * Checks the parameters of the GET method; if they contain the
	 * "authParameter" set to true, the authentication process is performed. If
	 * not, the verification process is performed (the parameters in the
	 * verification process are controlled by the OpenID provider).
	 * 
	 * @param request
	 *            The request sent to the servlet. Might come from the GWT
	 *            application or the OpenID provider.
	 * 
	 * @param response
	 *            The response sent to the user. Generally used to redirect the
	 *            user to the next step in the OpenID process.
	 * 
	 * @throws ServletException
	 *             Usually wrapping an OpenID process exception.
	 * @throws IOException
	 *             Usually when redirection could not be performed.
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (Boolean.valueOf(request.getParameter(authParameter))) {
			authenticate(request, response);
		} else {
			verify(request, response);
		}
	}

	/**
	 * Discovers the OpenID provider from the provided user string, and starts
	 * an authentication process against it.
	 * 
	 * This is done in three steps:
	 * <ol>
	 * <li>Discover the OpenID provider URL</li>
	 * <li>Create a unique cookie and send it to the user, so that after the
	 * provider redirects the user back we'll know what to do with him.</li>
	 * <li>Redirect the user to the provider URL, supplying the verification URL
	 * as a return point.</li>
	 * </ol>
	 * 
	 * @param request
	 *            The request for the OpenID authentication.
	 * @param response
	 *            The response, used to redirect the user.
	 * 
	 * @throws IOException
	 *             Occurs when a redirection is not successful.
	 * @throws ServletException
	 *             Wrapping an OpenID exception.
	 */
	private void authenticate(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String userSuppliedString = request.getParameter(nameParameter);
		try {
			// configure the return_to URL where your application will receive
            // the authentication responses from the OpenID provider
			String returnToUrl = URLEncoder.encode(request.getParameter("returnToUrl"));
			String urlAuth = request.getParameter("returnToUrl") + "/iServeBrowser/CallOpenID" + "?returnToUrl=" + returnToUrl;

			HttpSession session = request.getSession();
			session.removeAttribute("logged-in");

			// perform discovery on the user-supplied identifier
			List discoveries = manager.discover(userSuppliedString);

//	       	CookieManager.resetCookieValue(request, response, uniqueIdCookieName);
//			CookieManager.resetCookieValue(request, response, openIdCookieName);

            // attempt to associate with the OpenID provider
			// and retrieve one service endpoint for authentication
			DiscoveryInformation discovered = manager.associate(discoveries);

			// store the discovery information in the user's session
			request.getSession().setAttribute("openid-disc", discovered);

			// obtain a AuthRequest message to be sent to the OpenID provider
			AuthRequest authReq = manager.authenticate(discovered, urlAuth);

//			if ( !discovered.isVersion2() ) {
                // Option 1: GET HTTP-redirect to the OpenID Provider endpoint
                // The only method supported in OpenID 1.x
                // redirect-URL usually limited ~2048 bytes
                response.sendRedirect(authReq.getDestinationUrl(true));
//           } else {
                // Option 2: HTML FORM Redirection (Allows payloads >2048 bytes)
//                RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("formredirection.jsp");
//               request.setAttribute("parameterMap", authReq.getParameterMap());
//                request.setAttribute("destinationUrl", authReq.getDestinationUrl(false));
//                dispatcher.forward(request, response);
//            }
		} catch (DiscoveryException e) {
			throw new ServletException(e);
		} catch (MessageException e) {
			throw new ServletException(e);
		} catch (ConsumerException e) {
			throw new ServletException(e);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	/**
	 * Checks the response received by the OpenID provider, and saves the user
	 * identifier for later use if the authentication was sucesssful.
	 * 
	 * <b>Note</b>: While confusing, the OpenID provider's response is in fact
	 * encapsulated within the request; this is because it is the provider who
	 * requested the page, and sent the response as parameters.
	 * 
	 * This is done in three steps:
	 * <ol>
	 * <li>Verify the OpenID resposne.</li>
	 * <li>If verification was successful, retrieve the OpenID identifier of the
	 * user and save it for later use.</li>
	 * <li>Redirect the user back to the main page of the application, together
	 * with a cookie containing his OpneID identifier.</li>
	 * </ol>
	 * 
	 * @param request
	 *            The request, containing the OpenID provider's response as
	 *            parameters.
	 * @param response
	 *            The response, used to redirect the user back to the
	 *            application page.
	 * @throws IOException
	 *             Occurs when redirection is not successful.
	 * @throws ServletException
	 *             Wrapping an OpenID exception.
	 */
	private void verify(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String returnToUrl = request.getParameter("returnToUrl");
		try {

			// extract the parameters from the authentication response
			// (which comes in as a HTTP request from the OpenID provider)
			ParameterList responseParams = new ParameterList(request.getParameterMap());
	
			// retrieve the previously stored discovery information
			DiscoveryInformation discovered = (DiscoveryInformation) request.getSession().getAttribute("openid-disc");
	
			// extract the receiving URL from the HTTP request
			String receivingURL = request.getParameter("returnToUrl") + "/iServeBrowser/CallOpenID";//request.getRequestURL();
			String queryString = request.getQueryString();
			if (queryString != null && queryString.length() > 0)
				receivingURL = receivingURL + "?" + request.getQueryString();
	
			// verify the response; ConsumerManager needs to be the same
			// (static) instance used to place the authentication request
			VerificationResult verification = manager.verify(receivingURL,
			        responseParams, discovered);
			// examine the verification result and extract the verified identifier
            Identifier verified = verification.getVerifiedId();
            if (verified != null) {
    			HttpSession session = request.getSession();
    			// System.out.println("session " + session.getId());
    			session.setAttribute("logged-in", verified.toString());
				// CookieManager.setCookie(request, response, openIdCookieName, verified.toString());
            }
            response.sendRedirect(returnToUrl + "/browser.html");
		} catch (MessageException e) {
			throw new ServletException(e);
		} catch (DiscoveryException e) {
			throw new ServletException(e);
		} catch (AssociationException e) {
			throw new ServletException(e);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

}