package uk.ac.open.kmi.iserve.commons.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

/**
 * Annotable Resource Class provides common methods to those classes that
 * can be annotated with modelReferences
 *
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 20/05/2013
 * Time: 17:18
 */
public class AnnotableResource extends Resource {

    /**
     * Keep an organised list of model references based on their type
     * The modelReferences are stored in an EnumMap of model refs
     * indexed by the type. Easily control different types and loop over all of them with a
     * single unique interface.
     */
    private EnumMap<ModelReference.ModelReferenceType, List<ModelReference>> modelReferences;

    public AnnotableResource(URI uri) {
        super(uri);
        this.modelReferences = new EnumMap<ModelReference.ModelReferenceType, List<ModelReference>>(ModelReference.ModelReferenceType.class);
    }

    public Collection<List<ModelReference>> getModelReferences() {
        return modelReferences.values();
    }

    public List<ModelReference> getModelReferencesOfType(ModelReference.ModelReferenceType type) {
        return modelReferences.get(type);
    }

    public boolean addModelReference(ModelReference modelRef) {
        if (modelRef != null) {
            // Get the right array or create a new one
            List<ModelReference> list;
            if (this.modelReferences.containsKey(modelRef.getType())) {
                list = this.modelReferences.get(modelRef.getType());
            } else {
                list = new ArrayList<ModelReference>();
                this.modelReferences.put(modelRef.getType(), list);
            }
            return list.add(modelRef);
        }
        return false;
    }

    public boolean removeModelReference(ModelReference modelRef) {
        if (modelRef != null) {
            // Get the right array or create a new one
            if (this.modelReferences.containsKey(modelRef.getType())) {
                 return this.modelReferences.get(modelRef.getType()).remove(modelRef);
            }
            return false;
        }
        return false;
    }

}
