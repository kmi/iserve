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

import uk.ac.open.kmi.iserve.commons.io.Syntax;

import java.util.EnumMap;
import java.util.EnumSet;

public enum ServiceFormat {
    UNSUPPORTED("Unsupported", "err", null),
    HRESTS("hRESTS", "html", MediaType.TEXT_HTML),
    SAWSDL("SAWSDL", "wsdl", MediaType.APPLICATION_WSDL_XML),
    OWLS("OWL-S", "owls", MediaType.APPLICATION_OWL_XML),
    MSM_RDF("MSM/RDF", "rdf", MediaType.APPLICATION_RDF_XML),
    MSM_TTL("MSM/TTL", "ttl", MediaType.TEXT_TURTLE),
    MSM_N3("MSM/N3", "n3", MediaType.TEXT_N3),
    MSM_N_TRIPLE("MSM/N-TRIPLE", "nt", MediaType.TEXT_PLAIN);

    public static final EnumSet<ServiceFormat> NATIVE_FORMATS = EnumSet.of(MSM_RDF, MSM_TTL, MSM_N3, MSM_N_TRIPLE);
    public static final EnumSet<ServiceFormat> EXTERNAL_FORMATS = EnumSet.complementOf(NATIVE_FORMATS);

    // Map mapping Service Formats to native Syntax parsers
    public static final EnumMap<ServiceFormat, Syntax> NATIVE_PARSERS_MAP;

    static {
        NATIVE_PARSERS_MAP = new EnumMap<ServiceFormat, Syntax>(ServiceFormat.class);
        NATIVE_PARSERS_MAP.put(MSM_RDF, Syntax.RDFXML);
        NATIVE_PARSERS_MAP.put(MSM_TTL, Syntax.TTL);
        NATIVE_PARSERS_MAP.put(MSM_N3, Syntax.N3);
        NATIVE_PARSERS_MAP.put(MSM_N_TRIPLE, Syntax.N_TRIPLE);
    }

    private final String name;
    private final String fileExtension;
    private final MediaType mediaType;

    ServiceFormat(String name, String fileExtension, MediaType mediaType) {
        this.name = name;
        this.fileExtension = fileExtension;
        this.mediaType = mediaType;
    }

    /**
     * @return the fileExtension
     */
    public String getFileExtension() {
        return this.fileExtension;
    }

    public String getName() {
        return name;
    }

    public MediaType getMediaType() {
        return mediaType;
    }
}
