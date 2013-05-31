package uk.ac.open.kmi.iserve.commons.model;

import java.net.URI;

/**
 * Resources
 * Resources can be attached a number of metadata properties such as
 * label, comment, creator, seeAlso and source
 *
 * Resources can also be attached a WSDL grounding for now although this
 * may better be handled differently
 *
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 20/05/2013
 * Time: 17:12
 */
public class Resource {

    private URI uri;

    private String label;

    private String comment;

    private URI creator;

    private URI seeAlso;

    private URI source;

    // this is specific to a kind of grounding, may need moving around
    private URI wsdlGrounding;

    public Resource(URI uri) {
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

    public URI getCreator() {
        return creator;
    }

    public void setCreator(URI creator) {
        this.creator = creator;
    }

    public URI getSeeAlso() {
        return seeAlso;
    }

    public void setSeeAlso(URI seeAlso) {
        this.seeAlso = seeAlso;
    }

    public URI getSource() {
        return source;
    }

    public void setSource(URI source) {
        this.source = source;
    }

    public URI getWsdlGrounding() {
        return wsdlGrounding;
    }

    public void setWsdlGrounding(URI wsdlGrounding) {
        this.wsdlGrounding = wsdlGrounding;
    }

}
