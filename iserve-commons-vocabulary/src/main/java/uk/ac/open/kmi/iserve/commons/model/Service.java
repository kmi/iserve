package uk.ac.open.kmi.iserve.commons.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Class capturing Services
 *
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 20/05/2013
 * Time: 17:08
 */
public class Service extends InvocableEntity {

    private Collection<Operation> operations;

    public Service(URI uri) {
        super(uri);
        this.operations = new ArrayList<Operation>();
    }

    public Collection<Operation> getOperations() {
        return operations;
    }

    public void setOperations(Collection<Operation> operations) {
        this.operations = operations;
    }


    public boolean addOperation(Operation op) {
        if (op != null) {
            return this.operations.add(op);
        }
        return false;
    }

    public boolean removeOperation(Operation op) {
        if (op != null) {
            return this.operations.remove(op);
        }
        return false;
    }
}
