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
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 22/05/2013
 */
public class LogicalAxiom extends Resource {

    /**
     * Types of Logical Axioms.
     */
    public static enum Type {
        CONDITION,
        EFFECT
    }

    private Object typedValue;

    public LogicalAxiom(URI uri) {
        super(uri);
    }

    public Object getTypedValue() {
        return typedValue;
    }

    public void setTypedValue(Object typedValue) {
        this.typedValue = typedValue;
    }
}
