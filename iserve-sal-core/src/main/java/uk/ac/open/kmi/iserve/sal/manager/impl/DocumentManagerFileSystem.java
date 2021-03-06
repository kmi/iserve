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

package uk.ac.open.kmi.iserve.sal.manager.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.ConfigurationProperty;
import uk.ac.open.kmi.iserve.core.iServeProperty;
import uk.ac.open.kmi.iserve.sal.events.DocumentCreatedEvent;
import uk.ac.open.kmi.iserve.sal.events.DocumentDeletedEvent;
import uk.ac.open.kmi.iserve.sal.events.DocumentsClearedEvent;
import uk.ac.open.kmi.iserve.sal.exception.DocumentException;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.manager.DocumentManager;
import uk.ac.open.kmi.iserve.sal.manager.IntegratedComponent;
import uk.ac.open.kmi.iserve.sal.util.UriUtil;
import uk.ac.open.kmi.iserve.sal.util.caching.Cache;
import uk.ac.open.kmi.iserve.sal.util.caching.CacheException;
import uk.ac.open.kmi.iserve.sal.util.caching.CacheFactory;
import uk.ac.open.kmi.iserve.sal.util.caching.impl.InMemoryCache;
import uk.ac.open.kmi.iserve.sal.util.caching.impl.RedisCache;
import uk.ac.open.kmi.msm4j.io.util.FileUtil;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Singleton
public class DocumentManagerFileSystem extends IntegratedComponent implements DocumentManager {

    private static final Logger log = LoggerFactory.getLogger(DocumentManagerFileSystem.class);

    // Default path for service and documents URIs.
    // Note that any change here should also affect the REST API
    // Keep the trailing slash
    private static final String DEFAULT_DOC_URL_PATH = "id/documents/";
    public static final String MEDIA_TYPE_INDEX = "mediaTypeMap.json";

    private URI documentsInternalPath;
    private URI documentsPublicUri;

    private Cache<String, String> fileMediatypeMap;

    @Inject
    DocumentManagerFileSystem(EventBus eventBus,
                              @iServeProperty(ConfigurationProperty.ISERVE_URL) String iServeUri,
                              @iServeProperty(ConfigurationProperty.DOCUMENTS_FOLDER) String documentsFolderPath,
                              CacheFactory cacheFactory) throws SalException {

        super(eventBus, iServeUri);

        log.debug("Creating Document Manager. iServe URL - {}\n, Documents Folder - {}",
                iServeUri, documentsFolderPath);

        this.documentsPublicUri = this.getIserveUri().resolve(DEFAULT_DOC_URL_PATH);

        try {
            // Set the internal URI to the docs folder
            this.documentsInternalPath = new URI(documentsFolderPath + (documentsFolderPath.endsWith("/") ? "" : "/"));  // Ensure it has a final slash

            // Ensure we get an absolute file:/ URI
            if (!this.documentsInternalPath.isAbsolute()) {
                if (this.documentsInternalPath.toASCIIString().startsWith("/")) {
                    this.documentsInternalPath = this.getClass().getResource("/").toURI().resolve
                            (this.documentsInternalPath);
                } else {
                    // Obtain absolute URI
                    log.warn("Configuring document manager with relative path. Documents may be deleted when the application is redeployed.");
                    this.documentsInternalPath = this.getClass().getResource(".").toURI().resolve(this.documentsInternalPath);
                }
            }

            log.info("Documents internal path set to: {}", this.documentsInternalPath.toASCIIString());

        } catch (URISyntaxException e) {
            log.error("Incorrect iServe URI while initialising " + this.getClass().getSimpleName(), e);
            throw new SalException("Incorrect iServe URI while initialising " + this.getClass().getSimpleName(), e);
        }

        File file = new File(this.documentsInternalPath);
        if (!file.exists()) {
            if (file.mkdirs()) {
                log.info("Created documents folder.");
            } else {
                throw new DocumentException("Unable to create documents folder.");
            }
        } else {
            if (!file.isDirectory()) {
                throw new DocumentException("There already exists a file with the documents folder URI.");
            }
        }

        if (fileMediatypeMap == null) {
            try {
                fileMediatypeMap = cacheFactory.createPersistentCache("documents-media-type");

            } catch (CacheException e) {
                fileMediatypeMap = cacheFactory.createInMemoryCache("documents-media-type");
            }
            // It stores legacy map in persistent cache
            Map<String, String> legacyMap = getFileMediaTypeMap();
            if (legacyMap != null) {
                fileMediatypeMap.putAll(legacyMap);
                if (fileMediatypeMap instanceof RedisCache) {
                    // delete legacy file
                    File legacyFile = new File(URI.create(new StringBuilder(documentsInternalPath.toString()).append("/" + MEDIA_TYPE_INDEX).toString()));
                    legacyFile.delete();
                }
            }
        }
    }

    /**
     * @return
     */
    private URI getDocumentsInternalPath() {
        return this.documentsInternalPath;
    }

    /**
     * Obtain the relative URI of a document
     *
     * @param documentUri
     * @return
     */
    private URI getDocumentRelativeUri(URI documentUri) {
        return documentsPublicUri.relativize(documentUri);
    }

    /**
     * Obtain the Internal URI for a given document.
     *
     * @param documentUri
     * @return
     */
    private URI getDocumentInternalUri(URI documentUri) {
        return this.getDocumentsInternalPath().
                resolve(this.getDocumentRelativeUri(documentUri));
    }

    /**
     * @param file
     * @return
     */
    private URI getDocumentPublicUri(File file) {
        URI relativeUri = this.getDocumentsInternalPath().
                relativize(file.toURI());
        return documentsPublicUri.resolve(relativeUri);
    }

    @Override
    public Set<URI> listDocuments() throws DocumentException {
        ImmutableSet.Builder<URI> result = ImmutableSet.builder();
        File documentsFolder = new File(this.getDocumentsInternalPath());
        File[] docsList = documentsFolder.listFiles();
        for (File doc : docsList) {
            if (doc.isDirectory())
                result.add(this.getDocumentPublicUri(doc));
        }
        return result.build();
    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#getDocument(java.io.InputStream)
     */
    @Override
    public InputStream getDocument(URI documentUri) throws DocumentException {

        if (!documentExists(documentUri)) {
            return null;
        }

        File dir = new File(this.getDocumentInternalUri(documentUri));
        for (File file : dir.listFiles()) {
            if (file.getName().matches("^index\\..*")) {
                InputStream result = null;
                try {
                    result = new FileInputStream(file);
                } catch (IOException e) {
                    throw new DocumentException(e);
                }
                return result;
            }
        }
        return null;
    }

    @Override
    public String getDocumentMediaType(URI publicDocUri) throws DocumentException {
        if (!documentExists(publicDocUri)) {
            log.warn("Document {} does not exist", publicDocUri);
            return null;
        }

        File dir = new File(this.getDocumentInternalUri(publicDocUri));
        for (File file : dir.listFiles()) {
            if (file.getName().matches("^index\\..*")) {
                return this.fileMediatypeMap.get(file.getAbsolutePath());
            }
        }
        // Make it application/json by default
        return "application/json";
    }

//	/* (non-Javadoc)
//	 * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#addDocument(java.lang.String, java.lang.String)
//	 */
//	@Override
//	public URI addDocumentToService(String fileName, String docContent, String serviceId) throws DocumentException {
//		
//		URI folder = this.getDocumentsInternalPath().resolve(serviceId); 
//		URI fileUri = folder.resolve(fileName);
//		URI result = null;
//		try {
//			FileUtil.createDirIfNotExists(new File(folder));
//			File file = new File(fileUri);
//			IOUtil.writeString(docContent, file);
//			result = this.getDocumentPublicUri(file);
//		} catch (IOException e) {
//			throw new DocumentException(e);
//		}
//		return result;
//	}

    @Override
    public URI createDocument(InputStream docContent, String fileExtension, String mediaType) throws DocumentException {

        URI newDocUri = this.generateUniqueDocumentUri();
        try {
            URI internalDocUri = this.getDocumentInternalUri(newDocUri);
            File docDir = new File(internalDocUri);
            FileUtil.createDirIfNotExists(docDir);
            File file = new File(new StringBuilder(docDir.getAbsolutePath()).append("/index.").append(fileExtension).toString());
            FileOutputStream out = new FileOutputStream(file);
            FileUtil.createDirIfNotExists(file.getParentFile());
            int size = IOUtils.copy(docContent, out);
            log.info("Document internal URI - " + internalDocUri.toASCIIString() + " - " + size + " bytes.");
            storeMediaType(file.getAbsolutePath(), mediaType);
        } catch (IOException e) {
            throw new DocumentException("Unable to add document to service.", e);
        }

        // Generate Event
        this.getEventBus().post(new DocumentCreatedEvent(new Date(), newDocUri));

        return newDocUri;
    }

    private synchronized void storeMediaType(String absolutePath, String mediaType) {
        log.debug("Storing Media Type for file {}: {}", absolutePath, mediaType);
        fileMediatypeMap.put(absolutePath, mediaType);
        if (fileMediatypeMap instanceof InMemoryCache) {
            writeMediaTypeMapToDisk(fileMediatypeMap);
        }

    }

    private Map<String, String> getFileMediaTypeMap() {
        File mapFile = new File(URI.create(new StringBuilder(documentsInternalPath.toString()).append("/" + MEDIA_TYPE_INDEX).toString()));
        if (!mapFile.exists()) {
            return new HashMap<String, String>();
        } else {
            try {
                Gson gson = new GsonBuilder().create();
                Type typeOfHashMap = new TypeToken<Map<String, String>>() {
                }.getType();
                return gson.fromJson(new FileReader(mapFile), typeOfHashMap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void writeMediaTypeMapToDisk(Map<String, String> fileMediatypeMap) {
        try {
            File mapFile = new File(URI.create(new StringBuilder(documentsInternalPath.toString()).append("/" + MEDIA_TYPE_INDEX).toString()));
            Gson gson = new GsonBuilder().create();
            String json = gson.toJson(fileMediatypeMap);
            FileOutputStream fos = new FileOutputStream(mapFile);
            fos.write(json.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
         * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#createDocument(java.io.InputStream, java.lang.String )
         */
    @Override
    public URI createDocument(URI docRemoteLocation, String fileExtension, String mediaType) throws DocumentException {

        log.debug("Creating document from {} - File extension: {}", docRemoteLocation, fileExtension);
        URI newDocUri = this.generateUniqueDocumentUri();
        try {
            URI internalDocUri = this.getDocumentInternalUri(newDocUri);
            File docDir = new File(internalDocUri);
            FileUtil.createDirIfNotExists(docDir);
            File file = new File(new StringBuilder(docDir.getAbsolutePath()).append("/index.").append(fileExtension).toString());
            FileOutputStream out = new FileOutputStream(file);
            FileUtil.createDirIfNotExists(file.getParentFile());
            URLConnection connection = docRemoteLocation.toURL().openConnection();
            connection.addRequestProperty("Accept", mediaType);
            int size = IOUtils.copy(connection.getInputStream(), out);
            log.info("Document internal URI - " + internalDocUri.toASCIIString() + " - " + size + " bytes.");
            storeMediaType(file.getAbsolutePath(), mediaType);
            if (fileExtension.equals("json")) {
                storeSwaggerApiDeclarations(file, docDir.getAbsolutePath(), docRemoteLocation, mediaType);
            }
        } catch (IOException e) {
            throw new DocumentException("Unable to add document to service.", e);
        }

        // Generate Event
        this.getEventBus().post(new DocumentCreatedEvent(new Date(), newDocUri));

        return newDocUri;
    }

    private void storeSwaggerApiDeclarations(File rootDoc, String absolutePath, URI docRemoteLocation, String mediaType) {
        try {
            JsonElement jelement = new JsonParser().parse(new InputStreamReader(new FileInputStream(rootDoc)));
            JsonObject root = jelement.getAsJsonObject();
            JsonArray apis = root.getAsJsonArray("apis");
            for (JsonElement api : apis) {
                JsonObject apiDeclaration = api.getAsJsonObject();
                String path = apiDeclaration.get("path").toString().replaceAll("\"", "");
                String[] dirs = path.split("/");
                String parentPath = absolutePath;
                for (String dir : dirs) {
                    if (!dir.equals("")) {
                        try {
                            File localDir = new File(new StringBuilder(parentPath).append("/").append(dir).toString());
                            FileUtil.createDirIfNotExists(localDir);
                            parentPath = localDir.getAbsolutePath();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                File file = new File(new StringBuilder(parentPath).append("/index.json").toString());
                try {
                    URI apiDeclarationLocation = new URI(new StringBuilder(docRemoteLocation.toString()).append(path).toString());
                    FileOutputStream out = new FileOutputStream(file);
                    URLConnection connection = apiDeclarationLocation.toURL().openConnection();
                    connection.addRequestProperty("Accept", mediaType);
                    int size = IOUtils.copy(connection.getInputStream(), out);
                    log.info("Document internal URI - " + file.getAbsolutePath() + " - " + size + " bytes.");
                    storeMediaType(file.getAbsolutePath(), mediaType);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#deleteDocument(java.net.URI)
     */
    @Override
    public boolean deleteDocument(URI documentUri) throws DocumentException {
        // delete from hard disk
        URI fileUri = this.getDocumentInternalUri(documentUri);
        File file = new File(fileUri);
        if (file.exists()) {
            try {
                FileUtils.deleteDirectory(file);
                log.info("File deleted: " + file.getName());
                //delete entry in mediatype map

                for (String filePath : fileMediatypeMap.keySet()) {
                    if (filePath.matches(new StringBuilder(file.getAbsolutePath()).append(".*").toString())) {
                        fileMediatypeMap.remove(filePath);
                    }
                }
                if (fileMediatypeMap instanceof InMemoryCache) {
                    writeMediaTypeMapToDisk(fileMediatypeMap);
                }
                // Generate Event
                this.getEventBus().post(new DocumentDeletedEvent(new Date(), documentUri));
                return true;
            } catch (IOException e) {
                log.warn("File does not exist. Unable to delete it: " + file.getAbsolutePath());
                e.printStackTrace();

            }

        }
        return false;
    }

    /**
     * Deletes all the documents in iServe
     *
     * @return
     * @throws uk.ac.open.kmi.iserve.sal.exception.DocumentException
     */
    @Override
    public boolean clearDocuments() throws DocumentException {

        File internalFolder = new File(this.getDocumentsInternalPath());
        File[] files = internalFolder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                try {
                    FileUtils.deleteDirectory(file);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new DocumentException("Unable to delete " + file.getAbsolutePath());
                }
                log.info("Folder deleted: {}", file.getAbsolutePath());
            } else {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("File deleted: {}", file.getAbsolutePath());
                } else {
                    log.warn("Unable to delete file: {}", file.getAbsolutePath());
                }
            }
        }

        // Clear media types index
        File mapFile = new File(URI.create(new StringBuilder(documentsInternalPath.toString()).append("/" + MEDIA_TYPE_INDEX).toString()));
        if (mapFile.exists()) {
            mapFile.delete();
        }
        fileMediatypeMap.clear();

        // Generate Event
        this.getEventBus().post(new DocumentsClearedEvent(new Date()));

        return true;
    }

    /* (non-Javadoc)
     * @see uk.ac.open.kmi.iserve.sal.manager.DocumentManager#documentExists(java.net.URI)
     */
    @Override
    public boolean documentExists(URI documentUri) throws DocumentException {
        URI fileUri = getDocumentInternalUri(documentUri);
        File file = new File(fileUri);
        return file.exists();
    }

    private URI generateUniqueDocumentUri() {
        String uid = UriUtil.generateUniqueId();
        return documentsPublicUri.resolve(uid);
    }

    /**
     * This method will be called when the server is being shutdown.
     * Ensure a clean shutdown.
     */
    @Override
    public void shutdown() {
        if (fileMediatypeMap instanceof InMemoryCache) {
            writeMediaTypeMapToDisk(fileMediatypeMap);
        }
    }


}
