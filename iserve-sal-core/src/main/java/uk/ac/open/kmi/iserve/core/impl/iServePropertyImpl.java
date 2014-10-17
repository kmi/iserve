/*
 * Copyright (c) 2014. Knowledge Media Institute - The Open University
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

package uk.ac.open.kmi.iserve.core.impl;

import uk.ac.open.kmi.iserve.core.ConfigurationProperty;
import uk.ac.open.kmi.iserve.core.iServeProperty;

import java.lang.annotation.Annotation;

/**
 * iServePropertyImpl
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 15/10/2014
 */
public class iServePropertyImpl implements iServeProperty {

    private final ConfigurationProperty value;

    public iServePropertyImpl(ConfigurationProperty value) {
        this.value = value;
    }

    @Override
    public ConfigurationProperty value() {
        return this.value;
    }

    @Override
    public int hashCode() {
        // This is specified in java.lang.Annotation.
        return (127 * "value".hashCode()) ^ value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof iServeProperty)) {
            return false;
        }

        iServeProperty other = (iServeProperty) o;
        return value.equals(other.value());
    }

    @Override
    public String toString() {
        return "@" + iServeProperty.class.getName() + "(value=" + value + ")";
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return iServeProperty.class;
    }
}
