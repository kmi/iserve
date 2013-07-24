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
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.jayway.restassured.RestAssured;
import org.apache.commons.httpclient.Cookie;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.MediaType;
import uk.ac.open.kmi.iserve.commons.io.Syntax;
import uk.ac.open.kmi.iserve.commons.io.util.FilenameFilterBySyntax;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;

/**
 * Based on the tests from Shiro samples-web
 */
public class ServiceResourceTest extends AbstractContainerTest {

    private static final Logger log = LoggerFactory.getLogger(ServiceResourceTest.class);

    private static final String HOST = "http://localhost";
    private static final int PORT = 9090;
    //            private static final int PORT = 10000;
    private static final String BASE_CONTEXT = "/iserve";
    private static final String REGISTRY_SERVICES_PATH = "/id/services";
    private static final String SERVICES_PATH = BASE_CONTEXT + REGISTRY_SERVICES_PATH;
    private static final String WEB_APP_URI = HOST + ":" + PORT + BASE_CONTEXT;
    private static final String HOME_PAGE_URI = WEB_APP_URI + "/jsp/home.jsp";
    private static final String SERVICES_URI = WEB_APP_URI + REGISTRY_SERVICES_PATH;

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

    /**
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {

        testFolder = ServiceResourceTest.class.getResource(OWLS_TC_SERVICES).toURI();
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
        webClient.setJavaScriptEnabled(true);
        webClient.setJavaScriptTimeout(300);
        CookieManager cookieMan = new CookieManager();
        cookieMan = webClient.getCookieManager();
        cookieMan.setCookiesEnabled(true);

        // Logout
        logOut();

    }

    private void logOut() throws IOException {
        // Make sure we are logged out
        final HtmlPage homePage = webClient.getPage(HOME_PAGE_URI);
        try {
            homePage.getAnchorByHref("logout.jsp").click();
        } catch (ElementNotFoundException e) {
            //Ignore
        }
    }

    @Test
    public void logIn() throws FailingHttpStatusCodeException, IOException, InterruptedException {

        HtmlPage page = performLogin(false);
        // This'll throw an expection if not logged in  -- FIXME
//        page.getAnchorByHref("logout.jsp");

    }

    private HtmlPage performLogin(boolean rememberMe) throws IOException {
        HtmlPage page = webClient.getPage(WEB_APP_URI + "/jsp/login.jsp");
        HtmlForm form = page.getFormByName("loginform");
        form.<HtmlInput>getInputByName("username").setValueAttribute(ROOT_USER);
        form.<HtmlInput>getInputByName("password").setValueAttribute(ROOT_PASSWD);
        if (rememberMe) {
            HtmlCheckBoxInput checkbox = form.getInputByName("rememberMe");
            checkbox.setChecked(true);
        }
        return form.<HtmlInput>getInputByName("submit").click();
    }

    /**
     * Test method for AddService.
     */
    @Test
    public final void testAddService() throws IOException {
        // Clean the whole thing before testing
        try {
            ManagerSingleton.getInstance().clearRegistry();
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
                when().post(REGISTRY_SERVICES_PATH);

        log.info("Correctly redirected");

        // Now log into Shiro and try again
        log.info("Now logging in");
        performLogin(false);
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
                    when().post(REGISTRY_SERVICES_PATH);
        }
    }

    @Test
    public void testDeleteService() throws Exception {

        String relativeUri;
        List<URI> existingServices = ManagerSingleton.getInstance().listServices();

        log.info("Trying to delete prior to logging");
        URI testUri = existingServices.get(0);
        relativeUri = URI.create(SERVICES_URI).relativize(testUri).toASCIIString();
        log.info("Trying to delete service id: " + testUri);
        given().log().all().
                expect().response().statusCode(405).
                when().delete(REGISTRY_SERVICES_PATH + relativeUri);

        // Delete all without logging
        log.info("Deleting services");
        // Try to delete endpoint
        given().log().all().
                redirects().follow(true).
                expect().log().all().
                response().statusCode(302).
                when().delete(REGISTRY_SERVICES_PATH);

        // Now login and run the rest of the tests
        log.info("Now logging in");
        performLogin(false);
        Cookie cookie = webClient.getCookieManager().getCookie("JSESSIONID");
        log.info("Cookie data: " + cookie.toString());

        // Try to delete non existing svcs (directly at the graph level)
        for (int i = 0; i < 10; i++) {
            given().log().all().
                    sessionId(cookie.getValue()).
                    redirects().follow(true).
                    expect().log().all().
                    response().statusCode(404).
                    when().delete(REGISTRY_SERVICES_PATH + i);
        }

        // Try to delete non existing svcs
        for (int i = 0; i < 10; i++) {
            given().log().all().
                    sessionId(cookie.getValue()).
                    redirects().follow(true).
                    expect().log().all().
                    response().statusCode(404).
                    when().delete(REGISTRY_SERVICES_PATH + i + "/serviceName");
        }

        // Try to delete 10 services using their entire URIs
        for (int i = 0; i < 10; i++) {
            URI uri = existingServices.get(i);
            relativeUri = URI.create(SERVICES_URI).relativize(uri).toASCIIString();
            log.info("Deleting service id: " + uri);
            given().log().all().
                    sessionId(cookie.getValue()).
                    redirects().follow(true).
                    expect().log().all().
                    response().statusCode(200).
                    when().delete(REGISTRY_SERVICES_PATH + relativeUri);
        }

        // Now try to delete the whole endpoint (i.e., clear)
        given().log().all().
                sessionId(cookie.getValue()).
                redirects().follow(true).
                expect().log().all().
                response().statusCode(200).
                when().delete(REGISTRY_SERVICES_PATH);

    }
}
