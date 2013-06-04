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

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.vocabulary.RDFS;

public class FilterByRdfType<T extends RDFNode> extends Filter<T> {

    private Resource rdfType = RDFS.Resource;

    public void setRdfType(Resource rdfType) {
        this.rdfType = rdfType;
    }

    @Override
    public boolean accept(T node) {

        if (node.canAs(Individual.class)) {
            Individual individual = node.as(Individual.class);
            if (individual.hasRDFType(rdfType))
                return true;

        }
        return false;
    }
}