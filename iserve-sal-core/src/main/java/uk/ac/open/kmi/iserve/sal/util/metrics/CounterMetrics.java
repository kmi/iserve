package uk.ac.open.kmi.iserve.sal.util.metrics;


/**
 * Interface to manage simple metric counters.
 *
 * @author Pablo Rodríguez Mier
 */
public interface CounterMetrics {
    /**
     * Get the value of the counter. Creates a new counter
     * initialized to 0 if there is no counter registered with that name.
     *
     * @param name name of the counter
     * @return value of the counter.
     */
    int counter(String name);

    /**
     * Increment the value of the counter.
     *
     * @param name name of the counter
     * @return new incremented value.
     */
    int increment(String name);

    /**
     * Increment a counter by a factor.
     *
     * @param name  name of the counter.
     * @param count units to be incremented.
     * @return new incremented value.
     */
    int incrementBy(String name, int count);

    void reset();
}
