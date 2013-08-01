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

package uk.ac.open.kmi.iserve.discovery.disco;

import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.commons.io.util.URIUtil;
import uk.ac.open.kmi.iserve.discovery.api.MatchResult;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static uk.ac.open.kmi.iserve.discovery.disco.DiscoMatchType.*;

/**
 * Class Description
 *
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public class Util {

    private static final Logger log = LoggerFactory.getLogger(Util.class);

    // Common variables used for querying the RDF repository
    public static final String VAR_SVC = "svc";
    public static final String VAR_SVC_LABEL = "labelSvc";
    public static final String VAR_OP = "op";
    public static final String VAR_OP_LABEL = "labelOp";

    public static String NL = System.getProperty("line.separator");

    /**
     * Given a query result using the default variable name for services
     * obtain its label
     *
     * @param resultRow the result from a SPARQL query
     * @return the label of the service of null if none was found
     */
    public static String getServiceLabel(QuerySolution resultRow) {
        return getStringValueFromRow(resultRow, VAR_SVC_LABEL);
    }

    /**
     * Given a query result using the default variable name for operations
     * obtain its label
     *
     * @param resultRow the result from a SPARQL query
     * @return the label of the operation or null if none was found
     */
    public static String getOperationLabel(QuerySolution resultRow) {
        return getStringValueFromRow(resultRow, VAR_OP_LABEL);
    }

    /**
     * Given a query result from a SPARQL query, obtain the given variable value
     * as a String
     *
     * @param resultRow    the result from a SPARQL query
     * @param variableName the name of the variable to obtain
     * @return the value or null if it could not be found
     */
    private static String getStringValueFromRow(QuerySolution resultRow, String variableName) {
        // Check the input and exit immediately if null
        if (resultRow == null) {
            return null;
        }

        String result = null;

        Resource res = resultRow.getResource(variableName);
        if (res != null && res.isLiteral()) {
            result = res.asLiteral().getString();
        }

        return result;
    }

    /**
     * Given a query result using the default variable name for services
     * obtain its label
     *
     * @param resultRow the result from a SPARQL query
     * @return the URL of the service or null if none was found
     */
    public static URL getServiceUrl(QuerySolution resultRow) {
        return getUrlValueFromRow(resultRow, VAR_SVC);
    }

    /**
     * Given a query result using the default variable name for operations
     * obtain its label
     *
     * @param resultRow the result from a SPARQL query
     * @return the URL of the operation or null if none was found
     */
    public static URL getOperationUrl(QuerySolution resultRow) {
        return getUrlValueFromRow(resultRow, VAR_OP);
    }

    /**
     * Given a query result from a SPARQL query, obtain the given variable value
     * as a URL
     *
     * @param resultRow    the result from a SPARQL query
     * @param variableName the name of the variable to obtain
     * @return the value or null if it could not be obtained
     */
    private static URL getUrlValueFromRow(QuerySolution resultRow, String variableName) {

        // Check the input and exit immediately if null
        if (resultRow == null) {
            return null;
        }
        URL result = null;
        Resource res = resultRow.getResource(variableName);

        // Ignore and track services that are blank nodes
        if (res.isAnon()) {
            log.warn("Blank node found and ignored " + res.toString());
        } else if (res.isURIResource()) {
            try {
                result = new URL(res.getURI());
            } catch (MalformedURLException e) {
                log.error("Malformed URL for node", e);
            } catch (ClassCastException e) {
                log.error("The node is not a URI", e);
            }
        }

        return result;
    }

    /**
     * Given a query result from a SPARQL query, check if the given variable has
     * a value or not
     *
     * @param resultRow    the result from a SPARQL query
     * @param variableName the name of the variable to obtain
     * @return true if the variable has a value, false otherwise
     */
    public static boolean isVariableSet(QuerySolution resultRow, String variableName) {

        if (resultRow != null) {
            return resultRow.contains(variableName);
        }

        return false;
    }

    /**
     * Given a query result from a SPARQL query, obtain the number value at the
     * given variable
     *
     * @param resultRow    the result from a SPARQL query
     * @param variableName the name of the variable to obtain
     * @return the Integer value, or null otherwise
     */
    public static Integer getIntegerValue(QuerySolution resultRow, String variableName) {

        if (resultRow != null) {
            Resource res = resultRow.getResource(variableName);
            if (res != null && res.isLiteral()) {
                Literal val = res.asLiteral();
                if (val != null) {
                    return Integer.valueOf(val.getInt());
                }
            }
        }

        return null;
    }

    /**
     * Generate a pattern for binding to a label
     *
     * @param var      the name of the variable
     * @param labelVar the name of the label varible
     * @return
     */
    public static String generateLabelPattern(String var, String labelVar) {
        return "?" + var + " <" + RDFS.label.getURI() + "> ?" + labelVar + " . ";
    }

    /**
     * Generate a patter for matching var to the subclasses of clazz
     *
     * @param var
     * @return
     */
    public static String generateSubclassPattern(String var, String clazz) {
        return "?" + var + " <" + RDFS.subClassOf.getURI() + "> <" + clazz.replace(">", "%3e") + "> .";
    }

    /**
     * Generate a pattern for matching var to the superclasses of clazz
     *
     * @param var
     * @return
     */
    public static String generateSuperclassPattern(String var, String clazz) {
        return "<" + clazz.replace(">", "%3e") + "> <" + RDFS.subClassOf.getURI() + "> ?" + var + " .";
    }

    /**
     * Generates a UNION SPARQL statement for the patterns passed in the input
     *
     * @param patterns the patterns to make a UNION of
     * @return the UNION SPARQL statement
     */
    public static String generateUnionStatement(List<String> patterns) {
        // Check the input and exit immediately if null
        if (patterns == null) {
            return "";
        }

        // This is a trivial UNION
        if (patterns.size() == 1) {
            return patterns.get(0);
        }

        // General case
        StringBuffer query = new StringBuffer();
        // Add the first one as a special case and then loop over the rest
        query.append("  {" + NL);
        query.append(patterns.get(0));
        query.append("  }" + NL);

        for (int i = 1; i < patterns.size(); i++) {
            query.append("  UNION {" + NL);
            query.append(patterns.get(i));
            query.append("  }" + NL);
        }

        return query.toString();
    }

    /**
     * Generate a pattern for obtaining the exact matches of a concept.
     * Basically we look for those that are subclasses and superclasses
     * Uses BIND -> Requires SPARQL 1.1
     * <p/>
     * TODO: Does not match if we don't know a certain element is a class!
     *
     * @param modelRefVar the model reference variable
     * @param currClass   the URL of the class we want to find exact matches for
     * @param bindingVar  the name of the variable we will bind results to for
     *                    ulterior inspection
     * @return the query pattern
     */
    public static String generateExactMatchPattern(String modelRefVar,
                                                   String currClass, String bindingVar) {

        StringBuffer query = new StringBuffer();
        query.append(Util.generateSubclassPattern(modelRefVar, currClass) + NL);
        query.append(Util.generateSuperclassPattern(modelRefVar, currClass) + NL);
        // Bind a variable for inspection
        query.append("BIND (true as ?" + bindingVar + ") ." + NL);
        return query.toString();
    }


    /**
     * Generate a pattern for obtaining the strict subclasses of a concept.
     * Uses BIND -> Requires SPARQL 1.1
     *
     * @param modelRefVar the model reference variable
     * @param currClass   the URL of the class we want to find subclasses for
     * @param bindingVar  the name of the variable we will bind results to for
     *                    ulterior inspection
     * @return the query pattern
     */
    public static String generateMatchStrictSubclassesPattern(String modelRefVar,
                                                              String currClass, String bindingVar) {
        StringBuffer query = new StringBuffer();
        query.append(Util.generateSubclassPattern(modelRefVar, currClass) + NL);
        query.append("FILTER NOT EXISTS { " + Util.generateSuperclassPattern(modelRefVar, currClass) + "}" + NL);
        // Bind a variable for inspection
        query.append("BIND (true as ?" + bindingVar + ") ." + NL);
        return query.toString();
    }

    /**
     * Generate a pattern for obtaining the strict superclasses of a concept.
     * Uses BIND -> Requires SPARQL 1.1
     *
     * @param modelRefVar the model reference variable
     * @param currClass   the URL of the class we want to find superclasses for
     * @param bindingVar  the name of the variable we will bind results to for
     *                    ulterior inspection
     * @return the query pattern
     */
    public static String generateMatchStrictSuperclassesPattern(String modelRefVar,
                                                                String currClass, String bindingVar) {
        StringBuffer query = new StringBuffer();
        query.append(Util.generateSuperclassPattern(modelRefVar, currClass) + NL);
        query.append("FILTER NOT EXISTS { " + Util.generateSubclassPattern(modelRefVar, currClass) + "}" + NL);
        // Bind a variable for inspection
        query.append("BIND (true as ?" + bindingVar + ") ." + NL);
        return query.toString();
    }

    /**
     * Given the match between concepts obtain the match type
     *
     * @param isSubsume true if the concept match is subsumes
     * @param isPlugin  true if the concept match is plugin
     * @return Exact, Plugin or Subsumes depending on the values
     */
    public static DiscoMatchType getMatchType(boolean isSubsume, boolean isPlugin) {
        if (isSubsume) {
            if (isPlugin) {
                // If plugin and subsume -> this is an exact match
                return Exact;
            }
            return Subsume;
        } else {
            if (isPlugin) {
                return Plugin;
            }
        }
        return Fail;
    }

    /**
     * Given the best and worst match figure out the match type for the composite.
     * The composite match type is determined by the worst case except when it is
     * a FAIL. In this case, if the best is EXACT or PLUGIN the composite match
     * will be PARTIAL_PLUGIN. If the best is SUBSUMES it is PARTIAL_SUBSUMES.
     *
     * @param bestMatch  best match type within the composite match
     * @param worstMatch worst match type within the composite match
     * @return match type for the composite match
     */
    public static DiscoMatchType calculateCompositeMatchType(DiscoMatchType bestMatch,
                                                             DiscoMatchType worstMatch) {

        switch (worstMatch) {
            case Exact:
                return Exact;

            case Plugin:
                return Plugin;

            case Subsume:
                return Subsume;

            case Fail:
                switch (bestMatch) {
                    case Exact:
                        return PartialPlugin;

                    case Plugin:
                        return PartialPlugin;

                    case Subsume:
                        return PartialSubsume;

                    default:
                        log.warn("This match type is not supported: " +
                                bestMatch.name());

                        return Fail;
                }

            default:
                log.warn("This match type is not supported: " +
                        worstMatch.name());

                return Fail;
        }
    }

    /**
     * Obtain the match url given the query row and the kind of discovery
     * we are carrying out
     *
     * @param row                the result from a SPARQL query
     * @param operationDiscovery true if we are doing operation discovery
     * @return the URL for the match result
     */
    public static URL getMatchUrl(QuerySolution row, boolean operationDiscovery) {
        // Check the input and return immediately if null
        if (row == null) {
            return null;
        }

        URL matchUrl;

        if (operationDiscovery) {
            matchUrl = Util.getOperationUrl(row);
        } else {
            matchUrl = Util.getServiceUrl(row);
        }

        return matchUrl;
    }

    /**
     * @param row                the result from a SPARQL query
     * @param operationDiscovery true if we are doing operation discovery
     * @return
     */
    public static String getMatchLabel(QuerySolution row, boolean operationDiscovery) {
        // Check the input and return immediately if null
        if (row == null) {
            return null;
        }

        String matchLabel;

        if (operationDiscovery) {
            matchLabel = Util.getOperationLabel(row);
        } else {
            matchLabel = Util.getServiceLabel(row);
        }

        return matchLabel;
    }

    /**
     * Obtain or generate a label for the match result given a row resulting
     * from a query.
     *
     * @param row                the result from a SPARQL query
     * @param operationDiscovery true if we are doing operation discovery
     * @return
     */
    public static String getOrGenerateMatchLabel(QuerySolution row, boolean operationDiscovery) {
        String label;
        if (operationDiscovery) {
            label = getOrGenerateOperationLabel(row);
        } else {
            label = getOrGenerateServiceLabel(row);
        }
        return label;
    }

    /**
     * Obtain or generate a label for a service.
     * TODO: Deal better with WSDL naming convention
     *
     * @param row the result from a SPARQL query
     * @return
     */
    private static String getOrGenerateServiceLabel(QuerySolution row) {
        String label = getServiceLabel(row);
        if (label == null) {
            URL svcUrl = getServiceUrl(row);
            label = URIUtil.generateItemLabel(svcUrl);
        }

        return label;
    }

    /**
     * Obtain or generate a label for an operation.
     * <p/>
     * TODO: Deal better with WSDL naming convention
     *
     * @param row the result from a SPARQL query
     * @return
     */
    private static String getOrGenerateOperationLabel(QuerySolution row) {
        String result;
        String svcLabel = getOrGenerateServiceLabel(row);
        String opLabel = getOperationLabel(row);
        if (opLabel == null) {
            URL opUrl = getOperationUrl(row);
            result = URIUtil.generateItemLabel(svcLabel, opUrl);
        } else {
            result = svcLabel + "." + opLabel;
        }
        return result;
    }

    /**
     * Given two sets of matches find out the differences
     * TODO: Probably useful for others. Place somewhere else. The discovery-api module is an option but it forces to have guava as a dependency, do we want to?
     *
     * @param inputMatches
     * @param inputMatchesAlt
     */
    public static void compareResults(Map<URL, MatchResult> inputMatches,
                                      Map<URL, MatchResult> inputMatchesAlt) {

        MapDifference<URL, MatchResult> diff = Maps.difference(inputMatches, inputMatchesAlt);
        System.out.println("Comparing Match Results Maps");

        Map<URL, MatchResult> common = diff.entriesInCommon();
        System.out.println("Entries in common");
        showMapDetails(common);

        Map<URL, MatchResult> onlyLeft = diff.entriesOnlyOnLeft();
        System.out.println("Entries only in the left map");
        showMapDetails(common);

        Map<URL, MatchResult> onlyRight = diff.entriesOnlyOnRight();
        System.out.println("Entries only in the right map");
        showMapDetails(common);

        Map<URL, ValueDifference<MatchResult>> diffValues = diff.entriesDiffering();
        System.out.println("Differing values");
        for (Entry<URL, ValueDifference<MatchResult>> entry : diffValues.entrySet()) {
            MatchResult resultLeft = entry.getValue().leftValue();
            MatchResult resultRight = entry.getValue().rightValue();

            System.out.println("Match " + entry.getKey().toString());
            System.out.println("Left value details: ");
            System.out.println("Match explanation: " + resultLeft.getExplanation());

            System.out.println("Right value details: ");
            System.out.println("Match explanation: " + resultRight.getExplanation());
        }

    }

    /**
     * Expose the data within the map
     *
     * @param map
     */
    private static void showMapDetails(Map<URL, MatchResult> map) {
        for (Entry<URL, MatchResult> entry : map.entrySet()) {
            log.info("Match " + entry.getKey().toString() + NL);
        }
    }

}
