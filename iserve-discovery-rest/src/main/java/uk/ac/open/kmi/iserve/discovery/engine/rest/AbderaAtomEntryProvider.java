package uk.ac.open.kmi.iserve.discovery.engine.rest;
/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 *******************************************************************************/

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.writer.Writer;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;


/**
 * Taken from Apache Wink
 */
@Produces( {MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_JSON})
@Consumes(MediaType.APPLICATION_ATOM_XML)
@Provider
public class AbderaAtomEntryProvider implements MessageBodyReader<Entry>, MessageBodyWriter<Entry> {

    private static final Abdera ATOM_ENGINE = new Abdera();

    public long getSize(Entry entry,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType) {
        return -1;
    }

    public boolean isWriteable(Class<?> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType) {
        return Entry.class.isAssignableFrom(type);
    }

    public void writeTo(Entry entry,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException {
        if (MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType)) {
            Writer w = ATOM_ENGINE.getWriterFactory().getWriter("json"); //$NON-NLS-1$
            entry.writeTo(w, entityStream);
        } else {
            entry.writeTo(entityStream);
        }
    }

    public boolean isReadable(Class<?> type,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType) {
        return Entry.class == type;
    }

    public Entry readFrom(Class<Entry> type,
                          Type genericType,
                          Annotation[] annotations,
                          MediaType mediaType,
                          MultivaluedMap<String, String> httpHeaders,
                          InputStream entityStream) throws IOException {
        Document<Entry> doc = ATOM_ENGINE.getParser().parse(entityStream);
        return doc.getRoot();
    }
}