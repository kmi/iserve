package uk.ac.open.kmi.iserve.composit;


import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.model.*;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.disco.DiscoMatchType;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;
import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;

import java.net.URI;
import java.util.*;

public class RelevantServicesFinder {

    private static final Logger log = LoggerFactory.getLogger(RelevantServicesFinder.class);
    private ConceptMatcher matcher;
    private ServiceManager serviceManager;

    public RelevantServicesFinder(ConceptMatcher matcher, ServiceManager serviceManager){
        this.matcher = matcher;
        this.serviceManager = serviceManager;
    }

    public void createFullMatchGraph(Set<Operation> operations){

    }

    /**
     * Build a graph without cycles matching inputs and outputs from operations.
     * A output from a layer i>j (in the list of operations) will never match an input
     * from layer j.
     * @param operations
     * @param atLeast
     */
    public MatchGraph graph(List<Set<Operation>> operations, DiscoMatchType atLeast){
        Multimap<URI, URI> graphMatch = HashMultimap.create();
        Multimap<Operation, Operation> operationGraph = HashMultimap.create();
        for(int i=0; i<operations.size()-1;i++){
            Set<Operation> currentOps = operations.get(i);
            for(int j=i+1;j<operations.size();j++){
                Set<Operation> targetOps = operations.get(j);
                // Check all matches between inputs and outputs of layer i and j
                for(Operation op1 : currentOps){
                    for(Operation op2 : targetOps){
                        boolean match = false;
                        for(URI output : getOutputs(op1)){
                            for(URI input : getInputs(op2)){
                                // Check match
                                MatchResult result = this.matcher.match(output,input);
                                if (result.getMatchType().compareTo(atLeast)>=0){
                                    graphMatch.get(input).add(output);
                                    match=true;
                                }
                            }
                        }
                        if (match){
                            operationGraph.get(op1).add(op2);
                            log.info("{} connected with {}", op1.getUri(), op2.getUri());
                        }
                    }
                }
            }
        }
        return new MatchGraph(graphMatch, operationGraph);
    }

    // TODO; This class should use a higher-level component (pre-configured) for matching instead of the ConceptMatcher.
    // The reason is that the responsibility of checking valid matches (for example, atLeast(plugin), atMost(exact), only(exact)...
    // is of that component.
    public List<Set<Operation>> search(Set<URI> availableInputs, DiscoMatchType atLeast) throws Exception {
        List<Set<Operation>> layers = new ArrayList<Set<Operation>>();
        Set<URI> newInputs = new HashSet<URI>();
        Set<Operation> allRelevantOps = new HashSet<Operation>();
        Map<Operation, Set<URI>> nonMatchedInputs = new HashMap<Operation, Set<URI>>();
        newInputs.addAll(availableInputs);

        // Perform a service initialization. This should be done without importing all, just
        // using URIs.
        // TODO; Do not import services, use URIs
        Set<Service> services = new HashSet<Service>();
        for(URI srvURI : serviceManager.listServices()){
            services.add(serviceManager.getService(srvURI));
        }
        int pass = 0;
        while(!newInputs.isEmpty()){
            Stopwatch passWatch = new Stopwatch().start();
            Set<Operation> relevantOps = new HashSet<Operation>();
            Set<URI> relevantOutputs = new HashSet<URI>();
            Set<Service> relevantServices = new HashSet<Service>();
            for(Service srv : services){
                log.debug("Checking {}", srv.getUri());
                // Load operations
                operations:
                for(Operation op : srv.getOperations()) {
                    if (allRelevantOps.contains(op)) continue;
                    // Fast? check if there is some input that matches
                    if (consumesAny(newInputs, op, atLeast)){
                        if (isInvokable(availableInputs, op, atLeast)){
                            log.debug(" >> Invokable!");
                            relevantOps.add(op);
                            Set<URI> outputs = getOutputs(op);
                            relevantOutputs.addAll(outputs);
                            relevantServices.add(srv);
                            break operations;
                        }
                    }
                }
            }
            // The new inputs:
            newInputs.clear();
            // Remove concepts that were used before
            relevantOutputs.removeAll(availableInputs);
            // New inputs that potentially lead to new discovered services
            newInputs.addAll(relevantOutputs);
            availableInputs.addAll(newInputs);
            allRelevantOps.addAll(relevantOps);
            layers.add(relevantOps);
            pass++;
            log.info("{} Pass, total candidates {}. Available inputs {}. New Inputs {}. Iteration time {}", pass, relevantOps.size(), availableInputs.size(), newInputs.size(), passWatch.toString());
            passWatch.reset();
        }
        return layers;
    }

    public List<Set<URI>> searchImproved(Set<URI> availableInputs, DiscoMatchType atLeast) throws Exception {
        List<Set<URI>> layers = new ArrayList<Set<URI>>();
        Set<URI> newOutputs = new HashSet<URI>();
        Set<URI> allRelevantOps = new HashSet<URI>();
        Set<URI> allConcepts = new HashSet<URI>(availableInputs);
        Map<URI, Set<URI>> unmatchedInputMap = new HashMap<URI, Set<URI>>();
        newOutputs.addAll(allConcepts);

        // Preload
        Set<Service> services = new HashSet<Service>();
        for(URI serviceUri : serviceManager.listServices()){
            services.add(serviceManager.getService(serviceUri));
        }

        Stopwatch timer = new Stopwatch().start();
        int pass = 0;
        while(!newOutputs.isEmpty()){
            Stopwatch passWatch = new Stopwatch().start();
            Set<URI> relevantOps = new HashSet<URI>();
            Set<URI> relevantOutputs = new HashSet<URI>();
            Set<URI> relevantServices = new HashSet<URI>();
            for(URI srv : serviceManager.listServices()){
                log.debug("Checking {}", srv);
                // Load operations
                operations:
                for(URI op : serviceManager.listOperations(srv)) {
                    if (allRelevantOps.contains(op)) continue;
                    // Get last unmatched inputs from op
                    Set<URI> opInputs = unmatchedInputMap.get(op);
                    if (opInputs == null){
                        opInputs = getInputs(op);
                    }
                    Set<URI> unmatched = notMatchedInputs(newOutputs, opInputs, atLeast);
                    // Update
                    unmatchedInputMap.put(op, unmatched);
                    // If there are no unmatched inputs, then the op is invokable
                    if (unmatched.isEmpty()){
                        log.debug(" >> Invokable!");
                        relevantOps.add(op);
                        Set<URI> outputs = getOutputs(op);
                        relevantOutputs.addAll(outputs);
                        relevantServices.add(srv);
                        break operations;
                    }
                }
            }
            newOutputs.clear();
            // Remove concepts that were used before
            relevantOutputs.removeAll(allConcepts);
            // New outputs that potentially lead to new discovered services
            newOutputs.addAll(relevantOutputs);
            // Track all new generated concepts (outputs) just to remove the used ones in following steps
            allConcepts.addAll(newOutputs);
            allRelevantOps.addAll(relevantOps);
            if (relevantOps.size()!=0){
                layers.add(relevantOps);
            }
            pass++;
            log.info("{} Pass, total candidates {}. Available concepts {}. New outputs {}. Iteration time {}", pass, relevantOps.size(), allConcepts.size(), newOutputs.size(), passWatch.toString());
            passWatch.reset();
        }
        log.info("Total time: {}", timer.stop().toString());
        return layers;
    }

    public List<Set<URI>> searchImproved2(Set<URI> availableInputs, DiscoMatchType atLeast) throws Exception {
        List<Set<URI>> layers = new ArrayList<Set<URI>>();
        Set<URI> newOutputs = new HashSet<URI>();
        Set<URI> allRelevantOps = new HashSet<URI>();
        Set<URI> allConcepts = new HashSet<URI>(availableInputs);
        Map<URI, Set<URI>> unmatchedInputMap = new HashMap<URI, Set<URI>>();
        newOutputs.addAll(allConcepts);

        // Preload
        Set<Service> services = new HashSet<Service>();
        for(URI serviceUri : serviceManager.listServices()){
            services.add(serviceManager.getService(serviceUri));
        }

        Stopwatch timer = new Stopwatch().start();
        int pass = 0;
        while(!newOutputs.isEmpty()){
            Stopwatch passWatch = new Stopwatch().start();
            Set<URI> relevantOps = new HashSet<URI>();
            Set<URI> relevantOutputs = new HashSet<URI>();
            Set<URI> relevantServices = new HashSet<URI>();
            for(Service srv : services){
                log.debug("Checking {}", srv.getUri());
                // Load operations
                operations:
                for(Operation op : srv.getOperations()) {
                    if (allRelevantOps.contains(op.getUri())) continue;
                    // Get last unmatched inputs from op
                    Set<URI> opInputs = unmatchedInputMap.get(op.getUri());
                    if (opInputs == null){
                        opInputs = getInputs(op);
                    }
                    Set<URI> unmatched = notMatchedInputs(newOutputs, opInputs, atLeast);
                    // Update
                    unmatchedInputMap.put(op.getUri(), unmatched);
                    // If there are no unmatched inputs, then the op is invokable
                    if (unmatched.isEmpty()){
                        log.debug(" >> Invokable!");
                        relevantOps.add(op.getUri());
                        Set<URI> outputs = getOutputs(op);
                        relevantOutputs.addAll(outputs);
                        relevantServices.add(srv.getUri());
                        break operations;
                    }
                }
            }
            newOutputs.clear();
            // Remove concepts that were used before
            relevantOutputs.removeAll(allConcepts);
            // New outputs that potentially lead to new discovered services
            newOutputs.addAll(relevantOutputs);
            // Track all new generated concepts (outputs) just to remove the used ones in following steps
            allConcepts.addAll(newOutputs);
            allRelevantOps.addAll(relevantOps);
            if (relevantOps.size()!=0){
                layers.add(relevantOps);
            }
            pass++;
            log.info("{} Pass, total candidates {}. Available concepts {}. New outputs {}. Iteration time {}", pass, relevantOps.size(), allConcepts.size(), newOutputs.size(), passWatch.toString());
            passWatch.reset();
        }
        log.info("Total time: {}", timer.stop().toString());
        return layers;
    }

    private boolean consumesAny(Set<URI> inputs, Operation op, DiscoMatchType atLeast){
        Set<URI> opInputs = getInputs(op);
        for(URI from : inputs){
            for(URI to : opInputs){
                MatchResult match = this.matcher.match(from, to);
                if (match.getMatchType().compareTo(atLeast)>=0){
                    return true;
                }
            }
        }
        return false;
    }


    private Set<URI> notMatchedInputs(Set<URI> inputs, Set<URI> opInputs, DiscoMatchType atLeast){
        Set<URI> matched = new HashSet<URI>();
        for(URI from : inputs){
            for(URI to : opInputs){
                // skip if already matched
                if (matched.contains(to)) continue;

                MatchResult match = this.matcher.match(from, to);
                if (match.getMatchType().compareTo(atLeast)>=0){
                    matched.add(to);
                    if (matched.size()==opInputs.size()){
                        Collections.emptySet();
                    }
                }

            }
        }
        return Sets.newHashSet(Sets.difference(opInputs, matched));
    }

    private boolean isInvokable(Set<URI> availableInputs, Operation op, DiscoMatchType atLeast){
        Set<URI> opInputs = getInputs(op);
        Set<URI> matched = new HashSet<URI>();
        for(URI from : availableInputs){
            for(URI to : opInputs){
                // skip if already matched
                if (matched.contains(to)) continue;
                MatchResult match = this.matcher.match(from, to);
                if (match.getMatchType().compareTo(atLeast)>=0){
                    matched.add(to);
                    if (matched.size()==opInputs.size()){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Set<URI> getInputs(Operation op){
        Set<URI> models = new HashSet<URI>();
        for(MessageContent c : op.getInputs()){
            models.addAll(getModelReferences(c));
        }
        return models;
    }

    private Set<URI> getInputs(URI operation){
        Set<URI> models = new HashSet<URI>();
        for(URI input : serviceManager.listInputs(operation)){
            for(URI p : serviceManager.listMandatoryParts(input)){
                models.addAll(serviceManager.listModelReferences(p));
            }
        }
        return models;
    }

    private Set<URI> getOutputs(URI operation){
        Set<URI> models = new HashSet<URI>();
        for(URI output : serviceManager.listOutputs(operation)){
            for(URI p : serviceManager.listMandatoryParts(output)){
                models.addAll(serviceManager.listModelReferences(p));
            }
        }
        return models;
    }

    private Set<URI> getOutputs(Operation op){
        Set<URI> models = new HashSet<URI>();
        for(MessageContent c : op.getOutputs()){
            models.addAll(getModelReferences(c));
        }
        return models;
    }


    private Set<URI> getModelReferences(MessageContent msg){
        Set<URI> uris = new HashSet<URI>();
        for(MessagePart p : msg.getMandatoryParts()){
            for(Resource r : p.getModelReferences()){
                uris.add(r.getUri());
            }
        }
        return uris;
    }
}
