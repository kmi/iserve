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

package uk.ac.open.kmi.iserve.commons.io;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class URIUtil {

    public static String getLocalName(URI uri) {
        return getLocalName(uri.toASCIIString());
    }

    public static String getLocalName(URL url) {
        return getLocalName(url.toString());
    }

    public static String getLocalName(String uriString) {
        int len = uriString.length();
        if (uriString.endsWith("/")) {
            uriString = uriString.substring(0, len - 1);
        }
        int idx = uriString.indexOf('^');
        if (idx >= 0) {
            uriString = uriString.substring(0, idx);
        }
        int localNameIdx = getLocalNameIndex(uriString);
        return uriString.substring(localNameIdx);
    }

    private static int getLocalNameIndex(String uri) {
        int separatorIdx = uri.indexOf('#');

        if (separatorIdx < 0) {
            separatorIdx = uri.lastIndexOf('/');
        }

        if (separatorIdx < 0) {
            separatorIdx = uri.lastIndexOf(':');
        }

        if (separatorIdx < 0) {
            throw new IllegalArgumentException("No separator character founds in URI: " + uri);
        }

        return separatorIdx + 1;
    }

    public static String getNameSpace(URI uri) {
        String uriStr = uri.toASCIIString();
        int localNameIdx = getLocalNameIndex(uriStr);
        return uriStr.substring(0, localNameIdx - 1);
    }

    public static String getNameSpace(String uriString) {
        int localNameIdx = getLocalNameIndex(uriString);
        return uriString.substring(0, localNameIdx - 1);
    }

    public static String generateItemLabel(URL itemUrl) {
        return generateItemLabel(null, itemUrl);
    }

    public static String generateItemLabel(String prefix, URL itemUrl) {

        // Check the input and exit immediately if null
        if (itemUrl == null) {
            return null;
        }

        String result = null;
        if (prefix != null && !prefix.isEmpty()) {
            result = prefix + "." + URIUtil.getLocalName(itemUrl);
        } else {
            result = URIUtil.getLocalName(itemUrl);
        }

        return result;
    }

    public static URI replaceNamespace(URI originalUri, URI newNamespace) throws URISyntaxException {
        return new URI(newNamespace.getScheme(), newNamespace.getSchemeSpecificPart(), getLocalName(originalUri));
    }

}
