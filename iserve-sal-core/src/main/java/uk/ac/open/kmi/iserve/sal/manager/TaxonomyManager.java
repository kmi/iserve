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
package uk.ac.open.kmi.iserve.sal.manager;

import uk.ac.open.kmi.iserve.sal.exception.TaxonomyException;

import java.net.URI;
import java.util.List;

/**
 * Class Description
 *
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public interface TaxonomyManager {

    public abstract List<String> listTaxonomies();

    public abstract boolean addTaxonomy(URI taxonomyUri)
            throws TaxonomyException;

}