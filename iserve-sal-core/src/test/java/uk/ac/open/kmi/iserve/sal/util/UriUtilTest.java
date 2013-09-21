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

package uk.ac.open.kmi.iserve.sal.util;

import junit.framework.Assert;
import org.junit.Test;

import java.net.URI;

/**
 * UriUtilTest
 * TODO: Provide Description
 * <p/>
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 20/06/2013
 * Time: 17:30
 */
public class UriUtilTest {

    @Test
    public void testGetUniqueId() throws Exception {

        String uniqueId;
        String retrievedId;
        URI resourceUri;

        URI basePath = URI.create("http://localhost:9090/myApp/path");
        for (int i = 0; i < 10; i++) {
            uniqueId = UriUtil.generateUniqueId();
            resourceUri = new URI(basePath + "/" + uniqueId);
            retrievedId = UriUtil.getUniqueId(resourceUri, basePath);
            Assert.assertEquals("The id's should be identical. Get Unique Id failed without fragment", uniqueId, retrievedId);
        }

        for (int i = 0; i < 10; i++) {
            uniqueId = UriUtil.generateUniqueId();
            resourceUri = new URI(basePath + "/" + uniqueId + "#" + i);
            retrievedId = UriUtil.getUniqueId(resourceUri, basePath);
            Assert.assertEquals("The id's should be identical. Get Unique Id failed with fragment", uniqueId, retrievedId);
        }

        // Test almost identical base
        resourceUri = URI.create("http://localhost:9090/anotherApp/path");
        retrievedId = UriUtil.getUniqueId(resourceUri, basePath);
        Assert.assertNull("The id should be null as they are not relative.", retrievedId);

        // Test completely different base
        resourceUri = URI.create("http://otherHost:9090/anotherApp/path");
        retrievedId = UriUtil.getUniqueId(resourceUri, basePath);
        Assert.assertNull("The id should be null as they are not relative.", retrievedId);

    }

}
