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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.sal.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.exception.SalException;

import java.net.URI;

public class BaseSemanticManager {
	
	private static final Logger log = LoggerFactory.getLogger(BaseSemanticManager.class);
	
	private SystemConfiguration configuration;
	private URI sparqlQueryEndpoint;
    private URI sparqlUpdateEndpoint;

	protected BaseSemanticManager(SystemConfiguration configuration) throws SalException {
		this.configuration = configuration;
		this.sparqlQueryEndpoint = configuration.getDataSparqlUri();
        this.sparqlUpdateEndpoint = configuration.getSparqlUpdateUri();
		if (this.sparqlQueryEndpoint == null) {
			log.error(BaseSemanticManager.class.getSimpleName() + " requires a SPARQL Query endpoint.");
			throw new SalException(BaseSemanticManager.class.getSimpleName() + " requires a SPARQL Query endpoint.");
		}

        if (this.sparqlUpdateEndpoint == null) {
            log.warn(BaseSemanticManager.class.getSimpleName() + " requires a SPARQL Update endpoint to modify data.");
        }
	}

	/**
	 * @return the sparqlQueryEndpoint
	 */
	public URI getSparqlQueryEndpoint() {
		return this.sparqlQueryEndpoint;
	}

    /**
     * @return the SPARQL update endpoint
     */
    public URI getSparqlUpdateEndpoint() {
        return sparqlUpdateEndpoint;
    }

    /**
	 * @return the configuration
	 */
	public SystemConfiguration getConfiguration() {
		return this.configuration;
	}
	
}
