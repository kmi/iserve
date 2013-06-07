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

/**
 * Enum Type with the Syntaxes Supported
 * <p/>
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 22/05/2013
 * Time: 02:10
 * <p/>
 * TODO: Move to the proper package
 */
public enum Syntax {

    RDFXML("RDF/XML", "rdf"),
    RDFXML_ABBREV("RDF/XML-ABBREV", "rdf"),
    N_TRIPLE("N-TRIPLE", "nt"),
    N3("N3", "n3"),
    TTL("TTL", "ttl"),
    N_QUADS("N-Quads", "nq"),
    RDF_JSON("RDF/JSON", "json"),
    TRIG("TriG", "trig");
//    OWLS ("OWLS");

    private final String name;
    private final String extension;

    Syntax(String name, String extension) {
        this.name = name;
        this.extension = extension;
    }

    public String getName() {
        return name;
    }

    public String getExtension() {
        return extension;
    }
}
