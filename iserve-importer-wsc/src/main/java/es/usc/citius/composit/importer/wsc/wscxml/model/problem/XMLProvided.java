/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package es.usc.citius.composit.importer.wsc.wscxml.model.problem;


import es.usc.citius.composit.importer.wsc.wscxml.model.XMLInstance;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

/**
 *
 * @author pablo
 */
@XmlRootElement(name="provided")
@XmlAccessorType(XmlAccessType.FIELD)
public class XMLProvided {
    @XmlElement(name="instance")
    private ArrayList<XMLInstance> inputs;

    public ArrayList<XMLInstance> getInputs() {
        return inputs;
    }

    public void setInputs(ArrayList<XMLInstance> inputs) {
        this.inputs = inputs;
    }

    
}
