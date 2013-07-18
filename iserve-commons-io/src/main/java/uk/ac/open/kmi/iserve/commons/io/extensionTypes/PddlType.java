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

package uk.ac.open.kmi.iserve.commons.io.extensionTypes;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.rdf.model.Literal;

/**
 * PddlType is an extension type for dealing with Literals capturing PDDL expressions.
 * This type is only necessary for handling the OWL-S descriptions part of OWL-S TC4
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 17/07/2013
 */
public class PddlType extends BaseDatatype {

    public static final String theTypeURI = "http://127.0.0.1/ontology/PDDLExpression.owl#PDDL";
    public static final RDFDatatype TYPE = new PddlType();

    /**
     * private constructor - single global instance
     */
    private PddlType() {
        super(theTypeURI);
    }

    /**
     * Convert a value of this datatype out
     * to lexical form.
     */
    public String unparse(Object value) {
        return ((PddlExpression) value).getExpressionContent();
    }

    /**
     * Parse a lexical form of this datatype to a value
     *
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    public Object parse(String lexicalForm) throws DatatypeFormatException {
        return new PddlExpression(lexicalForm);
    }

    /**
     * Compares two instances of values of the given datatype.
     */
    public Boolean isEqual(Literal value1, Literal value2) {
        return value1.getDatatype() == value2.getDatatype()
                && value1.getValue().equals(value2.getValue());
    }

    /**
     * Returns the java class which is used to represent value
     * instances of this datatype.
     */
    @Override
    public Class<?> getJavaClass() {
        return PddlExpression.class;
    }
}
