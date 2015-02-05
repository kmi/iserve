package uk.ac.open.kmi.iserve.sal.util.caching;

/**
 * Created by Luca Panziera on 04/02/15.
 */
public interface CacheFactory {
    public Cache create(String name);
}
