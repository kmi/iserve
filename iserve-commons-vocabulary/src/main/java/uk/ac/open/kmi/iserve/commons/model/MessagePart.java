package uk.ac.open.kmi.iserve.commons.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Message Parts
 *
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 20/05/2013
 * Time: 17:43
 */
public class MessagePart extends AnnotableResource {

   private Collection<MessagePart> mandatoryParts;
    private Collection<MessagePart> optionalParts;

    public MessagePart(URI uri) {
        super(uri);
        this.mandatoryParts = new ArrayList<MessagePart>();
        this.optionalParts = new ArrayList<MessagePart>();
    }

    public Collection<MessagePart> getMandatoryParts() {
        return mandatoryParts;
    }

    public void setMandatoryParts(Collection<MessagePart> mandatoryParts) {
        this.mandatoryParts = mandatoryParts;
    }

    public Collection<MessagePart> getOptionalParts() {
        return optionalParts;
    }

    public void setOptionalParts(Collection<MessagePart> optionalParts) {
        this.optionalParts = optionalParts;
    }


    public boolean addOptionalPart(MessagePart part) {
        if (part != null) {
            return this.optionalParts.add(part);
        }
        return false;
    }

    public boolean removeOptionalPart(MessagePart part) {
        if (part != null) {
            return this.optionalParts.remove(part);
        }
        return false;
    }

    public boolean addMandatoryPart(MessagePart part) {
        if (part != null) {
            return this.mandatoryParts.add(part);
        }
        return false;
    }

    public boolean removeMandatoryPart(MessagePart part) {
        if (part != null) {
            return this.mandatoryParts.remove(part);
        }
        return false;
    }

//    @Override
//    public Model populateModel(Model model) {
//
//        // Add annotations from upper levels
//        Model result = super.populateModel(model);
//        com.hp.hpl.jena.rdf.model.Resource current = result.createResource(this.getUri().toASCIIString());
//        // Process mandatory parts
//        for (MessagePart part : mandatoryParts) {
//            current.addProperty(MSM.hasMandatoryPart,
//                    result.createResource(part.getUri().toASCIIString()));
//        }
//        // Process optional parts
//        for (MessagePart part : optionalParts) {
//            current.addProperty(MSM.hasOptionalPart,
//                    result.createResource(part.getUri().toASCIIString()));
//        }
//
//        return result;
//    }
}
