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

package uk.ac.open.kmi.iserve.sal.manager;

import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * Interface that defines the operations a NFP Manager should offer
 * If other implementations backed by different types of storage systems are
 * needed we should update this.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @author <a href="mailto:luca.panziera@open.ac.uk">Luca Panziera</a> (KMi - The Open University)
 */
public interface NfpManager extends iServeComponent {
    // CREATE (return boolean or throw exception?)

    public void createPropertyValue(URI resource, URI property, Object value);

    public void createPropertyValuesOfResource(Map<URI, Object> propertyValueMap, URI resource);

    public void createPropertyValueOfResources(Map<URI, Object> resourceValueMap, URI property);

    // READ

    public Object getPropertyValue(URI resource, URI property, Class valueClass);

    public Map<URI, Object> getPropertyValueOfResources(Set<URI> resource, URI property, Class valueClass);

    public Map<URI, Object> getPropertyValuesOfResource(URI resource, Map<URI, Class> propertyValueClassMap);

    public Map<URI, Object> getAllPropertyValuesOfResource(URI resource);

    public Map<URI, Map<URI, Object>> getPropertyValuesOfResources(Set<URI> resources, Map<URI, Class> propertyValueClassMap);

    // UPDATE (return boolean or throw exception?)

    public void updatePropertyValue(URI resource, URI property, Object value);

    public void updatePropertyValuesOfResource(Map<URI, Object> propertyValueMap, URI resource);

    public void updatePropertyValueOfResources(Map<URI, Object> resourceValueMap, URI property);

    // DELETE (return boolean or throw exception?)

    public void deletePropertyValue(URI resource, URI property);

    public void deletePropertyValueOfResources(Set<URI> resources, URI property);

    public void deletePropertyValuesOfResource(URI resource, Set<URI> properties);

    public void deleteAllPropertyValuesOfResource(URI resource);

    public void deleteAllValuesOfProperty(URI property);

    public void clearPropertyValues();

}