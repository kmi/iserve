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

package uk.ac.open.kmi.iserve.commons.io.util;

import uk.ac.open.kmi.iserve.commons.io.ServiceTransformer;

import java.io.File;
import java.io.FilenameFilter;

/**
 * FilenameFilterForTransformer is a utility class for filtering files accepting only those that a given transformer
 * can in principle deal with. The filtering is solely based on the file name so this is no guarantee for the transformer
 * to be able to process the file.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 24/07/2013
 */
public class FilenameFilterForTransformer implements FilenameFilter {

    private ServiceTransformer transformer;

    public FilenameFilterForTransformer(ServiceTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public boolean accept(File file, String name) {

        for (String extension : transformer.getSupportedFileExtensions()) {
            if (name.endsWith("." + extension))
                return true;
        }

        return false;
    }
}
