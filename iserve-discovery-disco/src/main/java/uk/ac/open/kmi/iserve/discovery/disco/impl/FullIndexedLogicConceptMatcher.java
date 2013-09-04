package uk.ac.open.kmi.iserve.discovery.disco.impl;


import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.disco.DiscoMatchType;
import uk.ac.open.kmi.iserve.sal.manager.KnowledgeBaseManager;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;
import uk.ac.open.kmi.iserve.sal.manager.impl.ManagerSingleton;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FullIndexedLogicConceptMatcher extends AbstractLogicConceptMatcher {
    private static final Logger log = LoggerFactory.getLogger(FullIndexedLogicConceptMatcher.class);
    private Table<URI, URI, MatchResult> indexedMatches;
    private ServiceManager manager;
    private KnowledgeBaseManager kbManager;
    private AbstractLogicConceptMatcher matcher;

    public FullIndexedLogicConceptMatcher(ServiceManager manager, KnowledgeBaseManager kbManager, AbstractLogicConceptMatcher matcher){
        this.manager = manager;
        this.matcher = matcher;
        this.kbManager = kbManager;
        this.indexedMatches = populate();
    }

    private Table<URI, URI, MatchResult> populate(){
        Set<URI> classes = new HashSet<URI>(this.kbManager.listConcepts(null));
        return matcher.listMatchesAtLeastOfType(classes, DiscoMatchType.Plugin);
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
    public MatchResult match(final URI origin, final URI destination) {
        MatchResult result = this.indexedMatches.get(origin, destination);
        // If there are no entries for origin,dest assume fail.
        if (result == null) {
            result = new MatchResult() {
                @Override
                public URI getResourceToMatch() {
                    return origin;
                }

                @Override
                public URI getMatchedResource() {
                    return destination;
                }

                @Override
                public MatchType getMatchType() {
                    return DiscoMatchType.Fail;
                }

                @Override
                public ConceptMatcher getMatcher() {
                    return FullIndexedLogicConceptMatcher.this;
                }

                @Override
                public String getExplanation() {
                    return origin + "-[" + getMatchType().name() + "]->" + destination;
                }
            };
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
    public Table<URI, URI, MatchResult> listMatchesOfType(Set<URI> origins, MatchType type) {
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
    public Table<URI, URI, MatchResult> listMatchesAtMostOfType(Set<URI> origins, MatchType maxType) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<URI, MatchResult> listMatchesWithinRange(URI origin, MatchType minType, MatchType maxType) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Table<URI, URI, MatchResult> listMatchesWithinRange(Set<URI> origins, MatchType minType, MatchType maxType) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Table<URI, URI, MatchResult> listMatchesAtLeastOfType(Set<URI> origins, MatchType minType) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
