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

import java.util.HashMap;
import java.util.Map;

/**
 * MediaType is an Enum including the main media types related to services.
 * This Enum covers those that are natively supported by iServe (without transformation).
 * Additionally provides a mapping between these and Syntax information necessary for RDF parsing.
 * <p/>
 * <p/>
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 11/06/2013
 */
public enum MediaType {

    TEXT_TURTLE("text/turtle"),
    APPLICATION_X_TURTLE("application/x-turtle"), // Legacy
    TEXT_N3("text/n3"),
    TEXT_PLAIN("text/plain"),
    APPLICATION_RDF_XML("application/rdf+xml");

    // Map mapping Media Types to Service Formats
    public static final Map<String, Syntax> NATIVE_MEDIATYPE_SYNTAX_MAP;

    static {
        NATIVE_MEDIATYPE_SYNTAX_MAP = new HashMap<String, Syntax>();
        NATIVE_MEDIATYPE_SYNTAX_MAP.put(TEXT_TURTLE.getMediaType(), Syntax.TTL);
        NATIVE_MEDIATYPE_SYNTAX_MAP.put(APPLICATION_X_TURTLE.getMediaType(), Syntax.TTL);
        NATIVE_MEDIATYPE_SYNTAX_MAP.put(TEXT_N3.getMediaType(), Syntax.N3);
        NATIVE_MEDIATYPE_SYNTAX_MAP.put(TEXT_PLAIN.getMediaType(), Syntax.N_TRIPLE);
        NATIVE_MEDIATYPE_SYNTAX_MAP.put(APPLICATION_RDF_XML.getMediaType(), Syntax.RDFXML);
    }

    private final String mediaType;

    MediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaType() {
        return mediaType;
    }
}
