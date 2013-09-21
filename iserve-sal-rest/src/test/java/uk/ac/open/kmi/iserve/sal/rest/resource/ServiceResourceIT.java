/*
 * Copyright (c) 2013. Knowledge Media Institute - The Open University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.open.kmi.iserve.sal.rest.resource;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.jayway.restassured.RestAssured;
import org.apache.commons.httpclient.Cookie;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.MediaType;
import uk.ac.open.kmi.iserve.commons.io.Syntax;
import uk.ac.open.kmi.iserve.commons.io.util.FilenameFilterBySyntax;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.manager.impl.iServeFacade;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Set;

import static com.jayway.restassured.RestAssured.given;

/**
 * Class Description
 *
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 * @version $Rev$
 *          $LastChangedDate$
 *          $LastChangedBy$
 */
public class ServiceResourceIT {

    private static final Logger log = LoggerFactory.getLogger(ServiceResourceIT.class);

    private static final String HOST = "http://localhost";
    private static final int PORT = 9090;
    //            private static final int PORT = 10000;
    private static final String BASE_CONTEXT = "/iserve";
    private static final String SERVICES_PATH = BASE_CONTEXT + "/services";
    private static final String WEB_APP_URI = HOST + ":" + PORT + BASE_CONTEXT;
    private static final String SERVICES_URI = WEB_APP_URI + "/services";

    private static final String OWLS_TC_SERVICES = "/OWLS-TC3-MSM";
    private static final String JGD_SERVICES = "/jgd-services";
    private static final String SOA4RE_SERVICES = "/soa4re";

    // Ensure that these are the ones listed in shiro.ini
    private static final String ROOT_USER = "root";
    private static final String ROOT_PASSWD = "secret";

    private URI testFolder;

    private File[] msmTtlTcFiles;

    // For HTML testing (logging via forms, etc)
    private final WebClient webClient = new WebClient();
    private iServeFacade manager;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

        testFolder = ServiceResourceIT.class.getResource(OWLS_TC_SERVICES).toURI();
        FilenameFilter ttlFilter = new FilenameFilterBySyntax(Syntax.TTL);
        File dir = new File(testFolder);
        msmTtlTcFiles = dir.listFiles(ttlFilter);
        int numServices = msmTtlTcFiles.length;

        // Set default values for rest assured
        RestAssured.baseURI = HOST;
        RestAssured.port = PORT;
        RestAssured.basePath = BASE_CONTEXT;

        // Setup Web client
        webClient.setThrowExceptionOnFailingStatusCode(true);
        CookieManager cookieMan = new CookieManager();
        cookieMan = webClient.getCookieManager();
        cookieMan.setCookiesEnabled(true);

        // Logout
        logOut();

        this.manager = iServeFacade.getInstance();

    }

    private void logOut() throws IOException {
        // Make sure we are logged out
        final HtmlPage homePage = webClient.getPage(WEB_APP_URI);
        try {
            homePage.getAnchorByHref("/logout.jsp").click();
        } catch (ElementNotFoundException e) {
            //Ignore
        }
    }

    @Test
    @Ignore("To be fixed again")
    public void logIn() throws FailingHttpStatusCodeException, MalformedURLException, IOException, InterruptedException {

        HtmlPage page = webClient.getPage(WEB_APP_URI + "/login.jsp");
        HtmlForm form = page.getFormByName("loginform");
        form.<HtmlInput>getInputByName("username").setValueAttribute(ROOT_USER);
        form.<HtmlInput>getInputByName("password").setValueAttribute(ROOT_PASSWD);
        page = form.<HtmlInput>getInputByName("submit").click();
        // This'll throw an expection if not logged in
        page.getAnchorByHref("/iserve/logout.jsp");
    }

    /**
     * Test method for AddService.
     */
    @Test
    @Ignore("To be fixed again")
    public final void testAddService() throws IOException {
        // Clean the whole thing before testing
        try {
            manager.clearRegistry();
        } catch (SalException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // Add all the test collections
        log.info("Uploading services");
        log.info("Test collection: " + testFolder);

        log.info("Trying without logging in");
        given().log().all().                                                                // Log requests
                redirects().follow(true).                                                       // Follow redirects introduced by Shiro
                multiPart("file", msmTtlTcFiles[0], MediaType.TEXT_TURTLE.getMediaType()).      // Submit file
                expect().log().all().                                                           // Log responses
                response().statusCode(302).                                                     // We should get a 302 that takes us to the login page
                when().post("/services");

        log.info("Correctly redirected");

        // Now log into Shiro with "rememberMe" set and try again
        log.info("Now logging in");

        HtmlPage page = webClient.getPage(WEB_APP_URI + "/login.jsp");
        HtmlForm form = page.getFormByName("loginform");
        form.<HtmlInput>getInputByName("username").setValueAttribute(ROOT_USER);
        form.<HtmlInput>getInputByName("password").setValueAttribute(ROOT_PASSWD);
//        HtmlCheckBoxInput checkbox = form.getInputByName("rememberMe");
//        checkbox.setChecked(true);
        page = form.<HtmlInput>getInputByName("submit").click();
        Cookie cookie = webClient.getCookieManager().getCookie("JSESSIONID");
        log.info("Cookie data: " + cookie.toString());

        // Test oriented towards individuals.
        // To be updated with application oriented logging.


        log.info("Uploading services");
        for (File file : msmTtlTcFiles) {
            // rest assured
            given().log().all().                                                // Log requests
                    sessionId(cookie.getValue()).                                   // Keep the session along
                    redirects().follow(true).                                       // Follow redirects introduced by Shiro
                    multiPart("file", file, MediaType.TEXT_TURTLE.getMediaType()).  // Submit file
                    expect().log().all().                                           // Log responses
                    response().statusCode(201).                                     // We should get a 201 created (if we have the rights)
                    when().post("/services");
        }
    }

    @Test
    @Ignore("To be fixed again")
    public void testDeleteService() throws Exception {

        Set<URI> existingServices = manager.getServiceManager().listServices();

        log.info("Deleting services");
        // Try to delete endpoint
        given().expect().response().statusCode(405).
                when().delete("/services");

        // Try to delete non existing svcs (directly at the graph level)
        for (int i = 0; i < 10; i++) {
            given().expect().response().statusCode(404).
                    when().delete("/services/" + i);
        }

        // Try to delete non existing svcs
        for (int i = 0; i < 10; i++) {
            given().expect().response().statusCode(404).
                    when().delete("/services/" + i + "/serviceName");
        }

        // Try to delete services using their entire URIs
        for (URI uri : existingServices) {
            String relativeUri = URI.create(SERVICES_URI).relativize(uri).toASCIIString();
            log.info("Deleting service id: " + uri);
            given().expect().response().statusCode(410).
                    when().delete("/services/" + relativeUri);
        }

    }
}
