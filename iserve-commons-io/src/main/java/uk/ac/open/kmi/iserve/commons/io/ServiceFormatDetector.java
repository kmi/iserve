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

import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * TODO: To be implemented
 */
public class ServiceFormatDetector {

    private Tika tika;

    public ServiceFormatDetector() {
        tika = new Tika();
    }

    public MediaType getContentType(InputStream is, String fileName) {

        try {
            // Wrap the stream so that Tika does not "consume it"
            BufferedInputStream stream = new BufferedInputStream(is);
            String mediaType = tika.detect(stream, fileName);
            return MediaType.parse(mediaType);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return null;
    }

}
