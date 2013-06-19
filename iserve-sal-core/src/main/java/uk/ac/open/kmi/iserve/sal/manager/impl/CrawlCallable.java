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

/**
 * Callable implementation in charge of obtaining the necessary models from remote ontologies referred to by
 * a service description.
 * <p/>
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 19/06/2013
 * Time: 18:19
 */

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;


public class CrawlCallable implements Callable {

    private static final Logger log = LoggerFactory.getLogger(CrawlCallable.class);
    private KnowledgeBaseManager kbManager;
    private String modelUri;

    public CrawlCallable(KnowledgeBaseManager kbManager, String modelUri) {
        this.kbManager = kbManager;
        this.modelUri = modelUri;
    }

    /**
     * Given the model uri, this method takes care of retrieving the remote model and any imported documents
     * and uploads them all to the graph store within the same graph. This implementation relies on Jena's embedded
     * support for retrieving ontologies and their imports.
     * <p/>
     * ATTENTION: When deleting models (in cases where several models import a common model) we may remove the
     * first ontology uploaded, with it the imported model will also be deleted and as a result any other model
     * that imported the common one will not have it available any more.
     * TODO: To be fixed
     *
     * @return True if the model was fetched and uploaded or false otherwise
     */
    @Override
    public Boolean call() {

        // If the model has not been uploaded fetch it and upload the graph
        if (!this.kbManager.containsModel(this.modelUri)) {
            Model model = fetchModel(this.modelUri);
            this.kbManager.uploadModel(this.modelUri, model, false);
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    private Model fetchModel(String modelUri) {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        log.info("Fetching remote ontology document: " + modelUri);
        model.read(modelUri);
        log.debug("Remote ontology document fetched.");
        return model;
    }
}

