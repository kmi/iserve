package uk.ac.open.kmi.iserve.discovery.disco.util.impl;

import com.google.common.collect.ImmutableMap;
import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.discovery.util.StringToMatchTypeConverter;

/**
 * Created by Luca Panziera on 08/07/15.
 */
public class StringToLogicConceptMatchTypeConverter implements StringToMatchTypeConverter {
    private ImmutableMap<String, LogicConceptMatchType> mappings = ImmutableMap.of("subsume", LogicConceptMatchType.Subsume, "plugin", LogicConceptMatchType.Plugin, "exact", LogicConceptMatchType.Exact);

    @Override
    public MatchType convert(String matching) {
        return mappings.get(matching);
    }
}
