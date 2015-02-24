package uk.ac.open.kmi.iserve.sal.util.caching;

/**
 * Created by Luca Panziera on 24/02/15.
 */
public class CacheException extends Exception {
    public CacheException() {
        super();
    }

    public CacheException(String message) {
        super(message);
    }

    public CacheException(Throwable cause) {
        super(cause);
    }

    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
