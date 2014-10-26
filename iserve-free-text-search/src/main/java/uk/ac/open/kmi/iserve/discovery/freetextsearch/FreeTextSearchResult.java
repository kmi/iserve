package uk.ac.open.kmi.iserve.discovery.freetextsearch;

import java.net.URI;

/**
 * Created by Luca Panziera on 26/10/2014.
 */
public class FreeTextSearchResult {
    private URI uri;
    private String label;
    private String comment;
    private URI modelReference;

    public FreeTextSearchResult(URI uri) {
        this.uri = uri;
    }


    public URI getUri() {
        return uri;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public URI getModelReference() {
        return modelReference;
    }

    public void setModelReference(URI modelReference) {
        this.modelReference = modelReference;
    }
}
