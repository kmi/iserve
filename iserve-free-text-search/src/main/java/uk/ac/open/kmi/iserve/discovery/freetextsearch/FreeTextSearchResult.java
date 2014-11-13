package uk.ac.open.kmi.iserve.discovery.freetextsearch;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Luca Panziera on 26/10/2014.
 */
public class FreeTextSearchResult {
    private URI uri;
    private String label;
    private String comment;


    private Set<URI> modelReferences;

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

    public Set<URI> getModelReferences() {
        return modelReferences;
    }

    public void setModelReferences(Set<URI> modelReferences) {
        this.modelReferences = modelReferences;
    }

    public void addModelReference(URI modelReference) {
        if (modelReferences == null) {
            modelReferences = new HashSet<URI>();
        }
        modelReferences.add(modelReference);
    }

    public void removeModelReference(URI modelReference) {
        if (modelReferences == null) {
            modelReferences = new HashSet<URI>();
        }
        modelReferences.remove(modelReference);
    }

}
