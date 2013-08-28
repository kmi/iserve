package uk.ac.open.kmi.iserve.discovery.disco.impl;


import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.disco.DiscoMatchType;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;
import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PartialIndexedLogicConceptMatcher extends AbstractLogicConceptMatcher {
    private static final Logger log = LoggerFactory.getLogger(PartialIndexedLogicConceptMatcher.class);
    private Table<URI, URI, MatchResult> indexedMatches;
    private ServiceManager manager;
    private AbstractLogicConceptMatcher matcher;

    public PartialIndexedLogicConceptMatcher(ServiceManager manager, AbstractLogicConceptMatcher matcher){
        this.manager = manager;
        this.matcher = matcher;
        this.indexedMatches = populate();
    }



    private Table<URI, URI, MatchResult> populate(){
        Table<URI, URI, MatchResult> indexedMatches = HashBasedTable.create();
        Set<URI> serviceInputs = findAllInputs();
        Set<URI> serviceOutputs = findAllOutputs();
        int totalOutputs = serviceOutputs.size();
        int counter=0;
        for(URI output : serviceOutputs){
            counter++;
            for(URI input : serviceInputs){
                MatchResult result = matcher.match(output, input);
                indexedMatches.put(output,input,result);
            }
            log.info(output + " indexed (" + counter + "/" + totalOutputs + ")");
        }
        log.info("Index built");
        return indexedMatches;
    }

    private Set<URI> findAllInputs(){
        Set<URI> models = new HashSet<URI>();
        for(URI service : manager.listServices()){
            for(URI op : manager.listOperations(service)){
                for(URI input : manager.listInputs(op)){
                    models.addAll(getAllModels(input));
                }
            }
        }
        return models;
    }

    private Set<URI> findAllOutputs(){
        Set<URI> models = new HashSet<URI>();
        for(URI service : manager.listServices()){
            for(URI op : manager.listOperations(service)){
                for(URI output : manager.listOutputs(op)){
                    models.addAll(getAllModels(output));
                }
            }
        }
        return models;
    }

    private Set<URI> getAllModels(URI messageContent){
        Set<URI> models = new HashSet<URI>();
        for(URI mandatoryPart : manager.listMandatoryParts(messageContent)){
            models.addAll(manager.listModelReferences(mandatoryPart));
        }
        return models;
    }

    @Override
    public String getMatcherDescription() {
        return "Indexed Logic Concept Matcher";
    }

    @Override
    public String getMatcherVersion() {
        return "1.0";
    }

    @Override
    public MatchResult match(URI origin, URI destination) {
        MatchResult result = this.indexedMatches.get(origin, destination);
        // If both concepts are not indexed, then call the delegated matcher

        if (result == null){
            // Delegate
            result = this.matcher.match(origin, destination);
        }

        return result;
    }

    @Override
    public Table<URI, URI, MatchResult> match(Set<URI> origin, Set<URI> destination) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<URI, MatchResult> listMatchesOfType(URI origin, MatchType type) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<URI, MatchResult> listMatchesAtLeastOfType(URI origin, MatchType minType) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<URI, MatchResult> listMatchesAtMostOfType(URI origin, MatchType maxType) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<URI, MatchResult> listMatchesWithinRange(URI origin, MatchType minType, MatchType maxType) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Table<URI, URI, MatchResult> listMatchesAtLeastOfType(Set<URI> origins, MatchType minType) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
