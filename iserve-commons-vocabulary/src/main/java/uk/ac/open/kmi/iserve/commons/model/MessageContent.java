package uk.ac.open.kmi.iserve.commons.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Message Content Class
 *
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 20/05/2013
 * Time: 17:42
 */
public class MessageContent extends MessagePart {

    private Collection<URI> liftingSchemaMappings;
    private Collection<URI> loweringSchemaMappings;

    public MessageContent(URI uri) {
        super(uri);
        this.liftingSchemaMappings = new ArrayList<URI>();
        this.loweringSchemaMappings = new ArrayList<URI>();
    }

    public Collection<URI> getLiftingSchemaMappings() {
        return liftingSchemaMappings;
    }

    public void setLiftingSchemaMappings(Collection<URI> liftingSchemaMappings) {
        this.liftingSchemaMappings = liftingSchemaMappings;
    }

    public Collection<URI> getLoweringSchemaMappings() {
        return loweringSchemaMappings;
    }

    public void setLoweringSchemaMappings(Collection<URI> loweringSchemaMappings) {
        this.loweringSchemaMappings = loweringSchemaMappings;
    }

    public boolean addLiftingSchemaMapping(URI mapping) {
        if (mapping != null) {
            return this.liftingSchemaMappings.add(mapping);
        }
        return false;
    }

    public boolean removeLiftingSchemaMapping(URI mapping) {
        if (mapping != null) {
            return this.liftingSchemaMappings.remove(mapping);
        }
        return false;
    }

    public boolean addLoweringSchemaMapping(URI mapping) {
        if (mapping != null) {
            return this.loweringSchemaMappings.add(mapping);
        }
        return false;
    }

    public boolean removeLoweringSchemaMapping(URI mapping) {
        if (mapping != null) {
            return this.loweringSchemaMappings.remove(mapping);
        }
        return false;
    }
}
