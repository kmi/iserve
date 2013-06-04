
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
