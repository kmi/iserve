package uk.ac.open.kmi.iserve.discovery.util;

import java.net.URL;
import java.util.Date;

/**
 * Created by Luca Panziera on 14/05/15.
 */
public class CallbackEvent {
    private URL callback;
    private Date date;

    public CallbackEvent(URL callback, Date date) {
        this.callback = callback;
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public URL getCallback() {
        return callback;
    }


}
