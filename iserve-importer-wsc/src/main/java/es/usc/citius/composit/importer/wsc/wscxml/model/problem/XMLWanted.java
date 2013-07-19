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
@XmlRootElement(name="wanted")
@XmlAccessorType(XmlAccessType.FIELD)
public class XMLWanted {
    @XmlElement(name="instance")
    private ArrayList<XMLInstance> outputs;

    public ArrayList<XMLInstance> getOutputs() {
        return outputs;
    }

    public void setOutputs(ArrayList<XMLInstance> outputs) {
        this.outputs = outputs;
    }
    
}
