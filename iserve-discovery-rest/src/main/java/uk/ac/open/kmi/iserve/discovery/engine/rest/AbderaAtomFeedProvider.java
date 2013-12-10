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
import org.apache.abdera.model.Feed;
import org.apache.abdera.writer.Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;

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
import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Taken from Apache Wink
 */
@Produces({MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_JSON})
@Consumes(MediaType.APPLICATION_ATOM_XML)
@Provider
public class AbderaAtomFeedProvider implements MessageBodyWriter<Feed>, MessageBodyReader<Feed> {

    private static final Logger log = LoggerFactory.getLogger(AbderaAtomFeedProvider.class);
    private static final Abdera ATOM_ENGINE = new Abdera();

    public long getSize(Feed feed,
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
        return Feed.class.isAssignableFrom(type);
    }

    public void writeTo(Feed feed,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException {
        if (MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType)) {
            Writer w = ATOM_ENGINE.getWriterFactory().getWriter("json"); //$NON-NLS-1$
            feed.writeTo(w, entityStream);
        } else {
            feed.writeTo(entityStream);
        }
    }

    public boolean isReadable(Class<?> type,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType) {
        return Feed.class == type;
    }

    public Feed readFrom(Class<Feed> type,
                         Type genericType,
                         Annotation[] annotations,
                         MediaType mediaType,
                         MultivaluedMap<String, String> httpHeaders,
                         InputStream entityStream) throws IOException {
        Document<Feed> doc = ATOM_ENGINE.getParser().parse(entityStream);
        return doc.getRoot();
    }

    /**
     * TODO: Implement properly
     * <p/>
     * We require the request URL
     *
     * @param matchingResults
     * @return
     */
    public static Feed generateFeed(String request, String matcherDetails, Map<URI, MatchResult> matchingResults) {

        Feed feed = initialiseFeed(request, matcherDetails);

        // Return empty feed if null
        if (matchingResults == null) {
            return feed;
        }

        int numResults = matchingResults.size();
        log.debug(matchingResults.keySet().toString());

        Set<Map.Entry<URI, MatchResult>> entries = matchingResults.entrySet();
        for (Map.Entry<URI, MatchResult> entry : entries) {
            Entry rssEntry = createMatchResultEntry(entry.getKey(), entry.getValue());
            feed.addEntry(rssEntry);
        }

        return feed;
    }

    private static Entry createMatchResultEntry(URI matchUri, MatchResult matchResult) {

        Entry rssEntry =
                ATOM_ENGINE.newEntry();
        rssEntry.setId(matchUri.toASCIIString());
        rssEntry.addLink(matchUri.toASCIIString(), "alternate");
        rssEntry.setTitle(matchUri.toASCIIString());
        //			String content = "Matching degree: " + degree;
        //			ExtensibleElement e = rssEntry.addExtension(entry.getValue().);
        //			e.setAttributeValue("score", entry.getValue().getScore());
        //			e.setText(degree);
        rssEntry.setContent(matchResult.getExplanation());
        return rssEntry;
    }

    private static Feed initialiseFeed(String request, String matcherDetails) {
        // Basic initialisation
        Feed feed = ATOM_ENGINE.getFactory().newFeed();
        feed.setUpdated(new Date());
        //FIXME: we should include the version
        feed.setGenerator(request, null, matcherDetails);
        feed.setId(request);
        feed.addLink(request, "self");
        feed.setTitle("Match Results");

        return feed;
    }

    public Feed generateFeed(String request, String matcherDetails, MatchResult matchResult) {

        Feed feed = initialiseFeed(request, matcherDetails);

        // Return empty feed if null
        if (matchResult == null) {
            return feed;
        }

        feed.addEntry(createMatchResultEntry(matchResult.getMatchedResource(), matchResult));
        return feed;
    }
}
