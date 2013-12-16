package uk.ac.open.kmi.iserve.api;

import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.sal.manager.RegistryManager;

import java.util.Set;

/**
 * Interface for the integrated iServe Engine
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 30/10/2013
 */
public interface iServeEngine {

    /**
     * Registry Manager getter
     *
     * @return the Registry Manager
     */
    RegistryManager getRegistryManager();

    /**
     * Obtains the default Concept Matcher available.
     *
     * @return the Concept Matcher
     */
    ConceptMatcher getDefaultConceptMatcher();

    /**
     * Obtains a concrete Concept Matcher given the className
     *
     * @param className the class name of the matcher we want to obtain
     * @return the concept matcher
     */
    ConceptMatcher getConceptMatcher(String className);

    /**
     * Lists all the known concept matchers available in the engine
     *
     * @return a Set with all the concept matcher implementations available
     */
    Set<String> listAvailableMatchers();

    void shutdown();
}
