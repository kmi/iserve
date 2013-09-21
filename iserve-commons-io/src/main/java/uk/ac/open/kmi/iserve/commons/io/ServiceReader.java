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

import uk.ac.open.kmi.iserve.commons.model.Service;

import java.io.InputStream;
import java.util.List;

/**
 * ServiceReader Interface
 * <p/>
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 04/06/2013
 * Time: 11:57
 */
public interface ServiceReader {

    /**
     * Read a stream in RDF and return the corresponding set of services
     * as Java Objects
     *
     * @param in      The input stream of MSM services
     * @param baseUri Base URI to use for parsing
     * @param syntax  used within the stream
     * @return The collection of Services parsed from the stream
     */
    public List<Service> parse(InputStream in, String baseUri, Syntax syntax);
}
