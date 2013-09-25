/*
 * Copyright (c) 2013. Knowledge Media Institute - The Open University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.open.kmi.iserve.commons.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Class capturing Services
 * <p/>
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 20/05/2013
 * Time: 17:08
 */
public class Service extends InvocableEntity {

    private List<Operation> operations;

    public Service(URI uri) {
        super(uri);
        this.operations = new ArrayList<Operation>();
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public void setOperations(List<Operation> operations) {
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
