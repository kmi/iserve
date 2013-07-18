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

/**
 * PDDL is a simple wrapper for PDDL Expressions
 * Should eventually be extended if necessary with validation, etc.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 17/07/2013
 */
public class PddlExpression {

    private String expressionContent;

    public PddlExpression(String expressionContent) {
        this.expressionContent = expressionContent;
    }

    public String getExpressionContent() {
        return expressionContent;
    }

    public void setExpressionContent(String expressionContent) {
        this.expressionContent = expressionContent;
    }
}
