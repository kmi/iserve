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

/**
 * Logical Axiom.
 * Logical Axioms are Conditions and Effects which may be specified in any language
 *
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 22/05/2013
 * Time: 12:45
 */
public class LogicalAxiom extends Resource {

    /**
     * Types of Logical Axioms.
     * TODO: Remove eventually
     */
    public static enum Type {
        CONDITION,
        EFFECT
    }

    private String typedValue;

    public LogicalAxiom(URI uri) {
        super(uri);
    }

    public String getTypedValue() {
        return typedValue;
    }

    public void setTypedValue(String typedValue) {
        this.typedValue = typedValue;
    }
}
