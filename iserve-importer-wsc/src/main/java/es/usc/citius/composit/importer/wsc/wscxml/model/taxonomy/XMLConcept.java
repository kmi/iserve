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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package es.usc.citius.composit.importer.wsc.wscxml.model.taxonomy;

import es.usc.citius.composit.importer.wsc.wscxml.model.XMLInstance;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;

/**
 * @author Pablo Rodriguez Mier
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class XMLConcept {
    @XmlAttribute(name = "name")
    private String name;
    @XmlElement(name = "instance")
    private ArrayList<XMLInstance> instances;
    @XmlElement(name = "concept")
    private ArrayList<XMLConcept> concepts;

    public ArrayList<XMLConcept> getConcepts() {
        return concepts;
    }

    public void setConcepts(ArrayList<XMLConcept> concepts) {
        this.concepts = concepts;
    }

    public ArrayList<XMLInstance> getInstances() {
        return instances;
    }

    public void setInstances(ArrayList<XMLInstance> instances) {
        this.instances = instances;
    }

    public String getName() {
        return name;
    }

}
