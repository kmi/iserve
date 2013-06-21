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

import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.FilenameFilterBySyntax;
import uk.ac.open.kmi.iserve.commons.io.Syntax;
import uk.ac.open.kmi.iserve.sal.MediaType;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;

import java.io.*;
import java.net.URI;
import java.util.List;

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
    //        private static final int PORT = 10000;
    private static final String BASE_CONTEXT = "/iserve";
    private static final String SERVICES_PATH = BASE_CONTEXT + "/services";
    private static final String WEB_APP_URI = HOST + ":" + PORT + BASE_CONTEXT;
    private static final String SERVICES_URI = WEB_APP_URI + "/services";


    private static final String OWLS_TC_SERVICES = "/OWLS-TC3-MSM";
    private static final String JGD_SERVICES = "/jgd-services";
    private static final String SOA4RE_SERVICES = "/soa4re";

    private URI testFolder;

    private File[] msmTtlTcFiles;

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

    }

    /**
     * Test method for AddService.
     */
    @Test
    public final void testAddService() {
        // Clean the whole thing before testing
        try {
            ManagerSingleton.getInstance().clearRegistry();
        } catch (SalException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // Add all the test collections
        log.info("Uploading services");
        log.info("Test collection: " + testFolder);

        InputStream is = null;
        log.info("Uploading services");
        for (File file : msmTtlTcFiles) {
            try {
                is = new FileInputStream(file);
                // rest assured
//                given().multiPart("file", file.getName(), is, MediaType.TEXT_TURTLE.getMediaType()).
                given().multiPart("file", file, MediaType.TEXT_TURTLE.getMediaType()).
                        expect().response().statusCode(201).
                        when().post("/services");

            } catch (FileNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } finally {
                if (is != null)
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
            }

        }
    }

    @Test
    public void testDeleteService() throws Exception {

        List<URI> existingServices = ManagerSingleton.getInstance().listServices();

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
