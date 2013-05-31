package uk.ac.open.kmi.iserve.commons.model;

import java.net.URI;

/**
 * Model Reference
 * This class captures modelReferences. We include the type in order to better
 * know the role that a given modelReference plays in a Service description.
 *
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 22/05/2013
 * Time: 01:43
 */
public class ModelReference {

    /**
     * Model Reference Types
     */
    public static enum ModelReferenceType {
        FUNC_CLASSIFICATION, NFP, CONDITION, EFFECT, UNSPECIFIED
    }

    URI reference;
    ModelReferenceType type = ModelReferenceType.UNSPECIFIED;

    public ModelReference(URI reference) {
        this.reference = reference;
    }

    public ModelReference(URI reference, ModelReferenceType type) {
        this.reference = reference;
        this.type = type;
    }

    public URI getReference() {
        return reference;
    }

    public void setReference(URI reference) {
        this.reference = reference;
    }

    public ModelReferenceType getType() {
        return type;
    }

    public void setType(ModelReferenceType type) {
        this.type = type;
    }


}
