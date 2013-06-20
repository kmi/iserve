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

import java.net.URI;
import java.util.UUID;

/**
 * Class common URI manipulation
 *
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public class UriUtil {

    /**
     * Given the URI of a resource it returns the Unique ID.
     * In reality this obtains the first path element after the basePath.
     * Checks for those that are local to the server only. Developed both for hash and slash URIs.
     * <p/>
     * Works in iServe on the basis of the following assumption behind resource URIs:
     * <p/>
     * http://host:port/...some...path.../{uniqueResourceId}#fragment
     * or
     * http://host:port/...some...path.../{uniqueResourceId}/subPart
     * <p/>
     * The result should be {uniqueResourceId}
     *
     * @param resourceUri the actual resource URI
     * @param basePath    the base path of the URI
     * @return the uniqueId if it is a local URI to the server or null.
     */
    public static String getUniqueId(URI resourceUri, URI basePath) {
        String result = null;
        URI relativeUri = basePath.relativize(resourceUri);
        // If it is relative the first part of the path is the Unique ID
        if (!relativeUri.isAbsolute()) {
            result = relativeUri.toASCIIString();
            int slashIndex = result.indexOf('/');
            int hashIndex = result.indexOf('#');
            if (slashIndex > -1) {
                result = result.substring(0, slashIndex);
            } else if (hashIndex > -1) {
                result = result.substring(0, hashIndex);
            }
        }
        return result;
    }

    /**
     * Figure out whether the URI is actually local to the server. That is,
     * it tells whether the resouce is controlled by the server.
     *
     * @param resourceUri
     * @param serverUri   the server URI
     * @return
     */
    public static boolean isResourceLocalToServer(URI resourceUri, URI serverUri) {
        URI relativeUri = resourceUri.relativize(serverUri);
        return !relativeUri.isAbsolute();
    }

    public static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }
}
