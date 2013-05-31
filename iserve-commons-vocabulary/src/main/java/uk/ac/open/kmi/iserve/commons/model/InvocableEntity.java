package uk.ac.open.kmi.iserve.commons.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

/**
 * Invocable Entity Class. These entities are those that can
 * be attached logical axioms to. In general Operations and Services.
 *
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 22/05/2013
 * Time: 13:26
 */
public class InvocableEntity extends AnnotableResource {

    /**
     * Keep an organised list of axioms based on their type
     * The axioms are stored in an EnumMap of axioms indexed by the type.
     * Doing so allows us to easily control different types and loop over all of them with a
     * single unique interface.
     */
    private EnumMap<LogicalAxiom.Type, List<LogicalAxiom>> axioms;

    public InvocableEntity(URI uri) {
        super(uri);
        this.axioms = new EnumMap<LogicalAxiom.Type, List<LogicalAxiom>>(LogicalAxiom.Type.class);
    }

    public Collection<List<LogicalAxiom>> getAxioms() {
        return axioms.values();
    }

    public List<LogicalAxiom> getAxiomsOfType(LogicalAxiom.Type type) {
        return axioms.get(type);
    }

    public boolean addLogicalAxiom(LogicalAxiom axiom) {
        if (axiom != null) {
            // Get the right array or create a new one
            List<LogicalAxiom> list;
            if (this.axioms.containsKey(axiom.getType())) {
                list = this.axioms.get(axiom.getType());
            } else {
                list = new ArrayList<LogicalAxiom>();
                axioms.put(axiom.getType(), list);
            }
            return list.add(axiom);
        }
        return false;
    }

    public boolean removeModelReference(LogicalAxiom axiom) {
        if (axiom != null) {
            // Get the right array
            if (axioms.containsKey(axiom.getType())) {
                return axioms.get(axiom.getType()).remove(axiom);
            }
        }
        return false;
    }
}
