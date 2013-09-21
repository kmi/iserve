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

package es.usc.citius.composit.importer.wsc.wscxml.model.problem;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.ArrayList;

/**
 * @author pablo
 */
@XmlSeeAlso({XMLParallel.class, XMLSequence.class, XMLServiceDesc.class})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class XMLServiceNode {
    @XmlElementRef
    private ArrayList<XMLServiceNode> nodes;

    public ArrayList<XMLServiceNode> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<XMLServiceNode> nodes) {
        this.nodes = nodes;
    }

    //public abstract WSLevelAgreement.WSLAProperties calculateQoS(Map<String, WSLevelAgreement.WSLAProperties> map);

}
