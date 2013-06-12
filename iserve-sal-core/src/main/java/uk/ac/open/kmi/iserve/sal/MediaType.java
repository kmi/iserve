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

package uk.ac.open.kmi.iserve.sal;

import java.util.HashMap;
import java.util.Map;

/**
 * MediaType
 * TODO: Provide Description
 * <p/>
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 11/06/2013
 * Time: 18:33
 */
public enum MediaType {

    TEXT_TURTLE("text/turtle"),
    APPLICATION_X_TURTLE("application/x-turtle"), // Legacy
    TEXT_N3("text/n3"),
    TEXT_PLAIN("text/plain"),
    APPLICATION_RDF_XML("application/rdf+xml"),
    APPLICATION_WSDL_XML("application/wsdl+xml"),
    APPLICATION_OWL_XML("application/owl+xml"),
    TEXT_HTML("text/html");

    // Map mapping Media Types to Service Formats
    public static final Map<String, ServiceFormat> MEDIATYPE_FORMAT_MAP;

    static {
        MEDIATYPE_FORMAT_MAP = new HashMap<String, ServiceFormat>();
        MEDIATYPE_FORMAT_MAP.put(TEXT_TURTLE.getMediaType(), ServiceFormat.MSM_TTL);
        MEDIATYPE_FORMAT_MAP.put(APPLICATION_X_TURTLE.getMediaType(), ServiceFormat.MSM_TTL);
        MEDIATYPE_FORMAT_MAP.put(TEXT_N3.getMediaType(), ServiceFormat.MSM_N3);
        MEDIATYPE_FORMAT_MAP.put(TEXT_PLAIN.getMediaType(), ServiceFormat.MSM_N_TRIPLE);
        MEDIATYPE_FORMAT_MAP.put(APPLICATION_RDF_XML.getMediaType(), ServiceFormat.MSM_RDF);
        MEDIATYPE_FORMAT_MAP.put(APPLICATION_WSDL_XML.getMediaType(), ServiceFormat.SAWSDL);
        MEDIATYPE_FORMAT_MAP.put(APPLICATION_OWL_XML.getMediaType(), ServiceFormat.OWLS);
        MEDIATYPE_FORMAT_MAP.put(TEXT_HTML.getMediaType(), ServiceFormat.HRESTS);
    }

    private final String mediaType;

    MediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaType() {
        return mediaType;
    }
}
