package uk.ac.open.kmi.iserve.sal.util.caching;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by Luca Panziera on 04/02/15.
 */
public interface Cache<K, M> extends ConcurrentMap<K, M> {
}
