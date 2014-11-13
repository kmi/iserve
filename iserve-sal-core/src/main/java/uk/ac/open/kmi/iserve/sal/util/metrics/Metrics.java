package uk.ac.open.kmi.iserve.sal.util.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Very simple singleton util class to manage different counters for evaluation
 * purposes.
 *
 * @author Pablo Rodríguez Mier
 */
public class Metrics {

    private static final class LazySingleton {
        private static final CounterMetrics DEFAULT_IMPL = new SimpleMetrics();
        private static final CounterMetrics DUMMY_IMPL = new DummyMetrics();
    }

    private Map<String, AtomicInteger> counters;
    private static boolean disabled = false;

    private Metrics() {
        counters = new HashMap<String, AtomicInteger>();
    }

    /**
     * Get the default Metrics instance.
     *
     * @return Metrics instance.
     */
    public static CounterMetrics get() {
        if (disabled) {
            return LazySingleton.DUMMY_IMPL;
        }
        return LazySingleton.DEFAULT_IMPL;
    }

    /**
     * Activate the metrics (if disabled).
     */
    public static void enable() {
        disabled = false;
    }

    /**
     * Disable metrics.
     */
    public static void disable() {
        disabled = true;
    }


}
