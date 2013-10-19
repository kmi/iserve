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

package es.usc.citius.composit.importer.wsc.wscxml;

import uk.ac.open.kmi.iserve.commons.io.ServiceWriter;
import uk.ac.open.kmi.iserve.commons.io.Syntax;
import uk.ac.open.kmi.iserve.commons.io.impl.ServiceWriterImpl;
import uk.ac.open.kmi.iserve.commons.model.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * @author Pablo Rodríguez Mier
 */
public class IOUtil {

    public static void dumpToRdf(Service s, File destination) throws FileNotFoundException {
        ServiceWriter writer = new ServiceWriterImpl();
        FileOutputStream out = new FileOutputStream(destination);
        Syntax syntax = Syntax.RDFXML;
        writer.serialise(s, out, syntax);
    }
}
