package uk.ac.open.kmi.iserve.discovery.util;

import uk.ac.open.kmi.iserve.discovery.api.MatchType;

/**
 * Created by Luca Panziera on 08/07/15.
 */
public interface StringToMatchTypeConverter {
    MatchType convert(String matching);
}
