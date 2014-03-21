package uk.ac.open.kmi.iserve.sal.util.metrics;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Basic implementation of {@link uk.ac.open.kmi.iserve.sal.util.metrics.CounterMetrics}
 * using {@link java.util.concurrent.atomic.AtomicInteger}.
 */
public final class SimpleMetrics implements CounterMetrics {


    private Map<String, AtomicInteger> counters = new HashMap<String, AtomicInteger>();


    synchronized private void checkAndInit(String name) {
        AtomicInteger counter = counters.get(name);
        if (counter == null) {
            // Initialize a new counter
            counter = new AtomicInteger(0);
            counters.put(name, counter);
        }
    }

    public int counter(String name) {
        checkAndInit(name);
        return counters.get(name).intValue();
    }

    @Override
    public int increment(String name) {
        checkAndInit(name);
        return counters.get(name).incrementAndGet();
    }

    @Override
    public int incrementBy(String name, int count) {
        checkAndInit(name);
        return counters.get(name).addAndGet(count);
    }

    @Override
    public synchronized void reset() {
        for (AtomicInteger counter : counters.values()) {
            counter.set(0);
        }
    }

    @Override
    public String toString() {
        String output = "";
        for (Map.Entry<String, AtomicInteger> entry : this.counters.entrySet()) {
            output += String.format("%s : %d%n", entry.getKey(), entry.getValue().intValue());
        }
        return output;
    }
}
