package uk.ac.open.kmi.iserve.rest.discovery.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Luca Panziera on 15/05/15.
 */
public class IoUtils {

    private static final int BUFFER_SIZE = 8192;

    public static long copy(InputStream is, OutputStream os) {
        byte[] buf = new byte[BUFFER_SIZE];
        long total = 0;
        int len = 0;
        try {
            while (-1 != (len = is.read(buf))) {
                os.write(buf, 0, len);
                total += len;
            }
        } catch (IOException ioe) {
            throw new RuntimeException("error reading stream", ioe);
        }
        return total;
    }

}