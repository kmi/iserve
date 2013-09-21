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

package uk.ac.open.kmi.iserve.commons.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Annotable Resource Class provides common methods to those classes that
 * can be annotated with modelReferences
 * <p/>
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 20/05/2013
 * Time: 17:18
 */
public class AnnotableResource extends Resource {

    /**
     * General modelRefs (FCs, but also domain models)
     * This could be used for any type of modelReference but it is recommended
     * to be used only for FCs and domain models.
     * <p/>
     * For NFPs, Conditions and Effects use the specific methods that will ensure
     * the proper rdf:type statements are also maintained.
     */
    private List<Resource> modelReferences;
    private List<NonFunctionalProperty> nfps;

    public AnnotableResource(URI uri) {
        super(uri);
        this.modelReferences = new ArrayList<Resource>();
        this.nfps = new ArrayList<NonFunctionalProperty>();
    }

    public List<Resource> getModelReferences() {
        return modelReferences;
    }

    public void setModelReferences(List<Resource> modelReferences) {
        this.modelReferences = modelReferences;
    }

    public List<NonFunctionalProperty> getNfps() {
        return nfps;
    }

    public void setNfps(List<NonFunctionalProperty> nfps) {
        this.nfps = nfps;
    }

    public boolean addModelReference(Resource resource) {
        return modelReferences.add(resource);
    }

    public boolean removeModelReference(Resource resource) {
        return modelReferences.remove(resource);
    }

    public boolean addNonFunctionalProperty(NonFunctionalProperty nfp) {
        return nfps.add(nfp);
    }

    public boolean removeNonFunctionalProperty(NonFunctionalProperty nfp) {
        return nfps.remove(nfp);
    }
}
