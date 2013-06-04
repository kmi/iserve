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

import uk.ac.open.kmi.iserve.commons.model.Service;
import uk.ac.open.kmi.iserve.sal.exception.ImporterException;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * Abstract class to be extended by Service Importers
 * // TODO: Rename and move to other package
 * 
 * @author Dong Liu (Knowledge Media Institute - The Open University)
 */
public interface ServiceImporter {

	/**
     * Transform a stream with service description(s), e.g. SAWSDL, OWL-S, hRESTS, etc., into
     * Minimal Service Model (MSM), Service instances.
     *
     * @param orginalDescription The semantic Web service description(s)
     * @return A List with the services transformed conforming to MSM model
     */
    public abstract List<Service> transform(InputStream orginalDescription) throws ImporterException;

    /**
     * Transform a file into Minimal Service Model (MSM).
     *
     * @param orginalFile The file containing the semantic Web service description(s)
     * @return A List with the services transformed conforming to MSM model
     */
    public abstract List<Service> transform(File orginalFile) throws ImporterException;

}
