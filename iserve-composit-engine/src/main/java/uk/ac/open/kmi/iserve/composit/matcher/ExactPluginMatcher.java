package uk.ac.open.kmi.iserve.composit.matcher;


import es.usc.citius.composit.core.match.function.MatchFunction;
import es.usc.citius.composit.core.model.Resource;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;
import uk.ac.open.kmi.iserve.discovery.disco.DiscoMatchType;
import uk.ac.open.kmi.iserve.discovery.disco.impl.LogicConceptMatcher;

import java.net.URI;

public class ExactPluginMatcher implements MatchFunction<Resource> {

    private ConceptMatcher matcher = new LogicConceptMatcher();

    public ExactPluginMatcher(){

    }

    @Override
    public boolean match(Resource source, Resource target) {
        MatchResult result = matcher.match(source.getURI(), target.getURI());
        if (result.getMatchType().compareTo(DiscoMatchType.Plugin)>=0){
            return true;
        }
        return false;
    }
}
