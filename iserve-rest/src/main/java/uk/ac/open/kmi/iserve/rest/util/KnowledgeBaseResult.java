package uk.ac.open.kmi.iserve.rest.util;

import java.net.URI;

/**
 * Created by Luca Panziera on 29/04/15.
 */
public class KnowledgeBaseResult {

    private URI uri;
    private Object label;

    public KnowledgeBaseResult(URI uri, Object label) {
        setUri(uri);
        setLabel(label);
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public Object getLabel() {
        return label;
    }

    public void setLabel(Object label) {
        this.label = label;
    }
}
