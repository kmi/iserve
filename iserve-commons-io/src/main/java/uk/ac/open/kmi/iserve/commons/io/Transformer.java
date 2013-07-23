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

import com.google.inject.*;
import uk.ac.open.kmi.iserve.commons.model.Service;

import java.io.*;
import java.util.*;

/**
 * Transformer is a Singleton Facade to any registered Service Transformation implementations.
 * Transformation implementations are automatically detected and registered at runtime
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 18/07/2013
 */
@Singleton
public class Transformer {
    /**
     * Map containing the loaded plugins indexed by media type. This map is automatically populated at runtime
     * based on the detected implementations in the classpath.
     */
    @Inject(optional = true)
    Map<String, ServiceTransformer> loadedPluginsMap = new HashMap<String, ServiceTransformer>();

    private static Transformer _instance;

    public Transformer() {
    }

    public static Transformer getInstance() {

        if (_instance == null) {
            // Load all implementation plugins available
            List<Module> modlist = new ArrayList<Module>();
            ServiceLoader<TransformationPluginModule> extensions = ServiceLoader.load(TransformationPluginModule.class);
            for (TransformationPluginModule ext : extensions) {
                modlist.add(ext);         // add each found plugin module
            }

            Injector inj = Guice.createInjector(modlist);
            _instance = inj.getInstance(Transformer.class);
        }

        return _instance;
    }

    /**
     * Checks whether the Transformer has support for a given media type.
     *
     * @param mediaType the media type to check for
     * @return true if it can be transformed, false otherwise.
     */
    public boolean canTransform(String mediaType) {
        return this.loadedPluginsMap.containsKey(mediaType);
    }

    /**
     * Gets the corresponding file extension to a given media type by checking with the appropriate transformer
     *
     * @param mediaType the media type for which to obtain the file extension
     * @return the file extension or null if it is not supported. Callers are advised to check first that the
     *         media type is supported {@see canTransform} .
     */
    public String getFileExtension(String mediaType) {
        ServiceTransformer transformer = this.loadedPluginsMap.get(mediaType);
        if (transformer != null)
            return transformer.getSupportedFileExtensions().get(0);

        return null;
    }

    /**
     * Parses and transforms a file with service description(s), e.g. SAWSDL, OWL-S, hRESTS, etc., and
     * returns a list of Service objects defined in the file.
     *
     * @param originalDescription The file containing the semantic Web service description(s)
     * @param mediaType           The media type of the file to transform. This is used to select the right transformer.
     * @return A List with the services transformed conforming to MSM model
     */
    public List<Service> transform(File originalDescription, String mediaType) throws TransformationException {
        return transform(originalDescription, null, mediaType);
    }

    /**
     * Parses and transforms a file with service description(s), e.g. SAWSDL, OWL-S, hRESTS, etc., and
     * returns a list of Service objects defined in the file.
     *
     * @param originalDescription The file containing the semantic Web service description(s)
     * @param baseUri             The base URI to use while transforming the service description
     * @param mediaType           The media type of the file to transform. This is used to select the right transformer.
     * @return A List with the services transformed conforming to MSM model
     */
    public List<Service> transform(File originalDescription, String baseUri, String mediaType) throws TransformationException {

        if (originalDescription != null && originalDescription.exists()) {
            // Open the file and transform it
            InputStream in = null;
            try {
                in = new FileInputStream(originalDescription);
                return transform(in, baseUri, mediaType);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new TransformationException("Unable to open input file", e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new TransformationException("Error while closing input file", e);
                    }
                }
            }
        }
        // Return an empty array if it could not be transformed.
        return new ArrayList<Service>();
    }

    /**
     * Parses and transforms a stream with service description(s), e.g. SAWSDL, OWL-S, hRESTS, etc., and
     * returns a list of Service objects defined in the stream.
     *
     * @param originalDescription The semantic Web service description(s)
     * @param mediaType           The media type of the file to transform. This is used to select the right transformer.
     * @return A List with the services transformed conforming to MSM model
     */
    public List<Service> transform(InputStream originalDescription, String mediaType) throws TransformationException {
        return transform(originalDescription, null);
    }

    /**
     * Parses and transforms a stream with service description(s), e.g. SAWSDL, OWL-S, hRESTS, etc., and
     * returns a list of Service objects defined in the stream.
     *
     * @param originalDescription The semantic Web service description(s)
     * @param baseUri             The base URI to use while transforming the service description
     * @param mediaType           The media type of the file to transform. This is used to select the right transformer.
     * @return A List with the services transformed conforming to MSM model
     */
    public List<Service> transform(InputStream originalDescription, String baseUri, String mediaType) throws TransformationException {

        List<Service> result = new ArrayList<Service>();

        if (originalDescription != null) {
            // Obtain or guess media type
            if (mediaType == null) {
                // TODO: Try to guess media type
                mediaType = "Unknown";
            }
            // Obtain the appropriate transformer
            if (this.loadedPluginsMap.containsKey(mediaType)) {
                // Invoke it
                result = this.loadedPluginsMap.get(mediaType).transform(originalDescription, baseUri);
            } else {
                // No transformer available
                throw new TransformationException(String.format("There is no available transformer for this media type: %s", mediaType));
            }
        }
        // Return results
        return result;
    }

}
