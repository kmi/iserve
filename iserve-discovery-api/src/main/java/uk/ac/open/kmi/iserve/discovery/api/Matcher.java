package uk.ac.open.kmi.iserve.discovery.api;

/**
 * The interface {@code Matcher} defines the basic methods to perform a match
 * between two elements.
 *
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 * @author Pablo Rodriguez Mier (CITIUS - Universidad de Santiago de Compostela)
 */
public interface Matcher<E> {

    /**
     * Perform a match between two elements (from {@code origin} to {@code destination})
     * and returns the result.
     *
     * @param origin Element to match
     * @param destination Matched element
     * @return {@link MatchResult} with the result of the matching.
     */
    MatchResult match(E origin, E destination);

    // Information about the matcher
    String getMatcherDescription();
    String getMatcherVersion();
}
