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

package uk.ac.open.kmi.iserve.discovery.api.impl;

import uk.ac.open.kmi.iserve.discovery.api.MatchType;
import uk.ac.open.kmi.iserve.discovery.api.MatchTypes;

/**
 * EnumMatchTypes
 * TODO: Provide Description
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @author <a href="mailto:pablo.rodriguez.mier@usc.es">Pablo Rodriguez Mier</a> (CITIUS - Universidad de Santiago de Compostela)
 * @since 26/07/2013
 */
public abstract class EnumMatchTypes<T extends Enum<T> & MatchType<T>> implements MatchTypes<MatchType> {

    Class<T> enumClass;

    /**
     * Empty package-private constructor
     */
    EnumMatchTypes() {
    }

    public static <T extends Enum<T> & MatchType<T>> EnumMatchTypes<T> of(Class<T> element) {

        EnumMatchTypes<T> result = new EnumMatchTypes<T>() {

            public T getLowest() {
                return (T) enumClass.getEnumConstants()[0];
            }

            public T getHighest() {
                return (T) enumClass.getEnumConstants()[enumClass.getEnumConstants().length - 1];
            }

            @Override
            public MatchType getNext(MatchType element) {
                Class type = element.getClass();
                if (type != enumClass)
                    return null;

                T[] values = enumClass.getEnumConstants();
                int ordinal = ((Enum) element).ordinal();

                if (ordinal < values.length - 1) {
                    return values[ordinal + 1];
                } else {
                    return null;
                }
            }

            @Override
            public MatchType getPrevious(MatchType element) {
                Class type = element.getClass();
                if (type != enumClass)
                    return null;

                T[] values = enumClass.getEnumConstants();
                int ordinal = ((Enum) element).ordinal();

                if (ordinal > 0) {
                    return values[ordinal - 1];
                } else {
                    return null;
                }
            }
        };

        result.enumClass = element;
        return result;
    }
}
