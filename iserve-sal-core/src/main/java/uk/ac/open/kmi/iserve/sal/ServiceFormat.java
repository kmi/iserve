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
    UNSUPPORTED("Unsupported", "err"),
    HRESTS("hRESTS", "html"),
    SAWSDL("SAWSDL", "wsdl"),
    OWLS("OWL-S", "owls"),
    MSM_RDF("MSM/RDF", "rdf"),
    MSM_TTL("MSM/TTL", "ttl"),
    MSM_N3("MSM/N3", "n3"),
    MSM_N_TRIPLE("MSM/N-TRIPLE", "nt");

    public static final EnumSet<ServiceFormat> NATIVE_FORMATS = EnumSet.of(MSM_RDF, MSM_TTL, MSM_N3, MSM_N_TRIPLE);
    public static final EnumSet<ServiceFormat> EXTERNAL_FORMATS = EnumSet.complementOf(NATIVE_FORMATS);
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

    ServiceFormat(String name, String fileExtension) {
        this.name = name;
        this.fileExtension = fileExtension;
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
}
