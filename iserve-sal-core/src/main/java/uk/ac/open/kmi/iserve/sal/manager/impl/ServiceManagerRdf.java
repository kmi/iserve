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

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.modify.request.UpdateCreate;
import com.hp.hpl.jena.sparql.modify.request.UpdateDrop;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.ServiceReaderImpl;
import uk.ac.open.kmi.iserve.commons.io.ServiceWriterImpl;
import uk.ac.open.kmi.iserve.commons.io.URIUtil;
import uk.ac.open.kmi.iserve.commons.model.*;
import uk.ac.open.kmi.iserve.commons.model.util.Vocabularies;
import uk.ac.open.kmi.iserve.commons.vocabulary.MSM;
import uk.ac.open.kmi.iserve.sal.ServiceFormatDetector;
import uk.ac.open.kmi.iserve.sal.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;
import uk.ac.open.kmi.iserve.sal.util.UriUtil;

import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ServiceManagerRdf extends BaseSemanticManager implements ServiceManager {

	private static final Logger log = LoggerFactory.getLogger(ServiceManagerRdf.class);

	private ServiceFormatDetector formatDetector;

	/**
	 * Constructor for the Service Manager. Protected to avoid external access.
	 * Any access to this should take place through the iServeManager
     *
     * TODO: Provide means for applications to register to changes in the registry
	 * 
	 * @param configuration the system configuration
	 * @throws SalException
	 */
	protected ServiceManagerRdf(SystemConfiguration configuration) throws SalException {
		super(configuration);
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#listService()
	 */
	@Override
	public List<URI> listServices() {
		List<URI> result = new ArrayList<URI>();

		// If the SPARQL endpoint does not exist return immediately.
		if (this.getSparqlQueryEndpoint() == null) {
			return result;
		}

		String queryStr = "select DISTINCT ?s where { \n" +
				"?s " + "<" + RDF.type.getURI() + ">" +  " " +
                "<" + MSM.Service.getURI() + ">" +
				" . }";

		// Query the engine
		Query query = QueryFactory.create(queryStr);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(this.getSparqlQueryEndpoint().toASCIIString(), query);

		try {		
			// TODO: Remove profiling
			long startTime = System.currentTimeMillis();

			ResultSet qResults = qexec.execSelect();

			// TODO: Remove profiling
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			log.info("Time taken for querying the registry: " + duration);

			Resource resource;
			URI matchUri;
			// Iterate over the results obtained
			while ( qResults.hasNext() ) {
				QuerySolution soln = qResults.nextSolution();

				// Get the match URL
				resource = soln.getResource("s");

				if (resource != null && resource.isURIResource()) {
					matchUri = new URI(resource.getURI());
					result.add(matchUri);
				} else {
					log.warn("Skipping result as the URL is null");
					break;
				}
			}	
		} catch (URISyntaxException e) {
			log.error("Error obtaining match result. Expected a correct URI", e);
		} finally {
			qexec.close();
		}
		return result;
	}

    @Override
    public Service getService(URI serviceUri) throws ServiceException {

        if (serviceUri == null)
            return null;

        String queryStr = "CONSTRUCT { ?s ?p ?o } \n" +
                "WHERE \n" +
                "{ GRAPH <" + serviceUri.toASCIIString() + "> { ?s ?p ?o } } \n";

        Query query = QueryFactory.create(queryStr) ;
        QueryExecution qexec = QueryExecutionFactory.sparqlService(this.getSparqlQueryEndpoint().toASCIIString(), query);
        OntModel resultModel = ModelFactory.createOntologyModel();
        qexec.execConstruct(resultModel) ;
        qexec.close() ;

        // Parse the service. There should only be one in the response
        ServiceReaderImpl reader = new ServiceReaderImpl();
        List<Service> services = reader.parseService(resultModel);
        if (services != null && !services.isEmpty()) {
            return services.get(0);
        }

        return null;
    }

    @Override
    public List<Service> getServices(List<URI> serviceUris) throws ServiceException {
        List<Service> services = new ArrayList<Service>();
        Service svc;
        for (URI svcUri : serviceUris) {
            svc = this.getService(svcUri);
            if (svc != null) {
                services.add(svc);
            }
        }
        return services;
    }

    public URI addService(Service service) throws ServiceException {

        // Check input and exit early
        if (service == null)
            throw new ServiceException("No service provided.");

        if (this.getSparqlUpdateEndpoint() == null)
            throw new ServiceException("No SPARQL Update endpoint provided. Unable to store the service.");

        URI newServiceUri = UriUtil.generateUniqueResourceUri(this.getConfiguration().getServicesUri());
        // Replace URIs in service description
        replaceUris(service, newServiceUri);

        // Store in backend
        ServiceWriterImpl writer = new ServiceWriterImpl();
        Model svcModel = writer.generateModel(service);

        UpdateRequest request = UpdateFactory.create() ;
        request.setPrefixMapping(PrefixMapping.Factory.create().setNsPrefixes(Vocabularies.prefixes));
        request.add(new UpdateCreate(service.getUri().toASCIIString())) ;
        request.add(generateUpdateRequest(service.getUri().toASCIIString(), svcModel)) ;

        // Use remote form for dealing sesame
        log.info("Adding service: " + service.getUri().toASCIIString());
        // Use create form for Sesame-based engines. TODO: Generalise and push to config.
        UpdateProcessor processor = UpdateExecutionFactory.createRemoteForm(request, this.getSparqlUpdateEndpoint().toASCIIString());
        processor.execute(); // TODO: anyway to know if things went ok?
        log.info("Service added.");

        return newServiceUri;
    }

    private String generateUpdateRequest(String graphName, Model svcModel) {

        StringWriter out = new StringWriter();
        svcModel.write(out, uk.ac.open.kmi.iserve.commons.io.Syntax.TTL.getName());
        StringBuilder updateSB = new StringBuilder();
        updateSB.append("INSERT DATA { \n");
        updateSB.append("GRAPH <").append(graphName).append("> { \n");
        updateSB.append(out.getBuffer());
        updateSB.append("}\n");
        updateSB.append("}\n");

        String result = updateSB.toString();
        log.debug("Update statement generated:\n" + result);

        return result;
    }

    /**
     * Given a Resource and the new Uri base to use, modify each and every URL accordingly.
     * This methods maintains the naming used by the service already.
     * Recursive implementation that traverses the entire graph
     *
     * @param resource The service that will be modified
     * @param newUriBase The new URI base
     */
    private void replaceUris(uk.ac.open.kmi.iserve.commons.model.Resource resource, URI newUriBase) {

        // Exit early
        if (resource == null || newUriBase == null || !newUriBase.isAbsolute())
            return;

        resource.setUri(URIUtil.replaceNamespace(resource.getUri(), newUriBase));

        // Handling of Service
        if (resource instanceof Service) {
            // Replace Operations
            List<Operation> operations = ((Service)resource).getOperations();
            for (Operation op : operations) {
                replaceUris(op, newUriBase);
            }
        }

        // Handling of Operation
        if (resource instanceof Operation) {
            List<MessageContent> mcs;
            // Replace Inputs, Outputs, InFaults, and Outfaults
            mcs = ((Operation)resource).getInputs();
            for (MessageContent mc : mcs) {
                replaceUris(mc, newUriBase);
            }

            mcs = ((Operation)resource).getInputFaults();
            for (MessageContent mc : mcs) {
                replaceUris(mc, newUriBase);
            }

            mcs = ((Operation)resource).getOutputs();
            for (MessageContent mc : mcs) {
                replaceUris(mc, newUriBase);
            }

            mcs = ((Operation)resource).getOutputFaults();
            for (MessageContent mc : mcs) {
                replaceUris(mc, newUriBase);
            }
        }

        // Handling for MessageParts
        if (resource instanceof MessagePart) {
            // Deal with optional and mandatory parts
            List<MessagePart> mps;
            mps = ((MessagePart)resource).getMandatoryParts();
            for (MessagePart mp : mps) {
                replaceUris(mp, newUriBase);
            }

            mps = ((MessagePart)resource).getOptionalParts();
            for (MessagePart mp : mps) {
                replaceUris(mp, newUriBase);
            }
        }

        // Handling for Invocable Entities
        if (resource instanceof InvocableEntity) {
            // Replace Effects
            List<Effect> effects = ((InvocableEntity)resource).getEffects();
            for (Effect effect : effects) {
                replaceUris(effect, newUriBase);
            }

            // Replace Conditions
            List<Condition> conditions = ((InvocableEntity)resource).getConditions();
            for (Condition condition : conditions) {
                replaceUris(condition, newUriBase);
            }
        }

        // Handling for Annotable Resources
        if (resource instanceof AnnotableResource) {
            // Replace NFPs
            List<NonFunctionalProperty> nfps = ((AnnotableResource)resource).getNfps();
            for (NonFunctionalProperty nfp : nfps) {
                replaceUris(nfp, newUriBase);
            }
        }
    }

	/**
	 * Returns the URI of the document defining the service, i.e., without the 
	 * fragment.
	 * 
	 * @param serviceId the unique id of the service
	 * @return the URI of the service document
	 */
	private URI getServiceBaseUri(String serviceId) {
		return this.getConfiguration().getServicesUri().resolve(serviceId);
	}

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#deleteService(java.lang.String)
	 */
	@Override
	public boolean deleteService(URI serviceUri) throws ServiceException {

		if ( serviceUri == null ) {
			throw new ServiceException("Service URI is null.");
		}

        if (this.getSparqlUpdateEndpoint() == null)
            throw new ServiceException("No SPARQL Update endpoint provided. Unable to delete the service.");

        UpdateRequest request = UpdateFactory.create() ;
        request.setPrefixMapping(PrefixMapping.Factory.create().setNsPrefixes(Vocabularies.prefixes));
        request.add(new UpdateDrop(serviceUri.toASCIIString())) ;

        // Use remote form for dealing sesame
        log.info("Deleting service: " + serviceUri.toASCIIString());
        // Use create form for Sesame-based engines. TODO: Generalise and push to config.
        UpdateProcessor processor = UpdateExecutionFactory.createRemoteForm(request, this.getSparqlUpdateEndpoint().toASCIIString());
        processor.execute(); // TODO: anyway to know if things went ok?
        log.info("Service deleted.");

		return true;
	}

    @Override
    public boolean deleteService(Service service) throws ServiceException {
        return deleteService(service.getUri());
    }

	/* (non-Javadoc)
	 * @see uk.ac.open.kmi.iserve.sal.manager.ServiceManager#serviceExists(java.net.URI)
	 */
	@Override
	public boolean serviceExists(URI serviceUri) throws ServiceException {
		String queryStr = "ASK { \n" +
                "GRAPH <" + serviceUri.toASCIIString() + "> {" +
                "<" + serviceUri.toASCIIString() + "> " + RDF.type.getURI() + " " + MSM.Service + " }\n}";

        Query query = QueryFactory.create(queryStr) ;
        QueryExecution qexec = QueryExecutionFactory.sparqlService(this.getSparqlQueryEndpoint().toASCIIString(), query);
        return qexec.execAsk();
	}

}
