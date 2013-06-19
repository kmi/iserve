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

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.ServiceWriterImpl;
import uk.ac.open.kmi.iserve.commons.io.URIUtil;
import uk.ac.open.kmi.iserve.commons.model.Service;
import uk.ac.open.kmi.iserve.commons.vocabulary.*;
import uk.ac.open.kmi.iserve.sal.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.exception.SalException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * This class is a parametric tool for crawling for RDF data.
 * <p/>
 * todo parameters: timeout, max allowed file size, max allowed files, number of
 * threads, request throttling (global, per-host) todo add logging with
 * ScutterVocab?
 *
 * @author Jacek Kopecky
 */
public class KnowledgeBaseManager extends BaseSemanticManager {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseManager.class);
    // Set backed by a ConcurrentHashMap to avoid race conditions
    private Set<String> loadedModels;
    private int NUM_THREADS = 4;

    private ExecutorService executor;

    /**
     * default constructor
     */
    public KnowledgeBaseManager(SystemConfiguration configuration) throws SalException {
        super(configuration);
        this.loadedModels = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        this.loadedModels.add(RDF.getURI());
        this.loadedModels.add(RDFS.getURI());
        this.loadedModels.add(OWL.NS);
        this.loadedModels.add(WSMO_LITE.NS);
        this.loadedModels.add(SAWSDL.NS);
        this.loadedModels.add(MSM.NS);
        this.loadedModels.add(HRESTS.NS);
        this.loadedModels.add(MSM_WSDL.NS);
        this.loadedModels.add("http://www.w3.org/ns/wsdl-extensions#");  // for WSDLX safety

        // Set default values for Document Manager
        OntDocumentManager dm = OntDocumentManager.getInstance();
        dm.setProcessImports(true);
        dm.setCacheModels(true);

        // set the executor
//        executor = Executors.newFixedThreadPool(NUM_THREADS);
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * This method will be called when the server is being shutdown.
     * Ensure a clean shutdown.
     */
    public void shutdown() {
        log.info("Shutting down Knowledge Base crawlers.");
        this.executor.shutdown();
        // waiting 2 seconds
        try {
            this.executor.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for threads to conclude crawling.", e);
        }
    }

    public boolean containsModel(String modelUri) {
        return this.loadedModels.contains(modelUri);
    }

    public void uploadModel(String modelUri, Model model, boolean forceUpdate) {

        if (modelUri != null && model != null && (!this.loadedModels.contains(modelUri) || forceUpdate)) {
            this.loadedModels.add(modelUri);
            // We are assuming here that uploading won't fail!
            // There is no way to figure this out at the moment with Jena (other than querying after)
            putGraph(modelUri, model);
        }
    }

    public void fetchModelsForService(Service svc) {
        Set<String> modelUris = obtainReferencedModelUris(svc);
        for (String modelUri : modelUris) {
            if (!this.loadedModels.contains(modelUri)) {
                Callable<Boolean> task = new CrawlCallable(this, modelUri);
                Future<Boolean> future = this.executor.submit(task);
                // We should eventually check these..
            }
        }
    }

    private Set<String> obtainReferencedModelUris(Service svc) {
        Set<String> result = new HashSet<String>();
        if (svc != null) {
            ServiceWriterImpl writer = new ServiceWriterImpl();
            Model svcModel = writer.generateModel(svc);

            // Get all model refs
            RDFNode node;
            NodeIterator modelRefs = svcModel.listObjectsOfProperty(SAWSDL.modelReference);
            while (modelRefs.hasNext()) {
                node = modelRefs.next();
                result.add(URIUtil.getNameSpace(node.asResource().getURI()));
            }
        }

        return result;
    }
}
