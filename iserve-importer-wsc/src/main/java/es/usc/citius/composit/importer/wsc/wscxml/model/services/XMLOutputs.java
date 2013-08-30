/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package es.usc.citius.composit.importer.wsc.wscxml.model.services;

import es.usc.citius.composit.importer.wsc.wscxml.model.XMLInstance;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;

/**
 *
 * @author Pablo Rodriguez Mier
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class XMLOutputs {
    @XmlElement(name="instance")
    private ArrayList<XMLInstance> instances;

    public ArrayList<XMLInstance> getInstances() {
        return instances;
    }

    public void setInstances(ArrayList<XMLInstance> instances) {
        this.instances = instances;
    }

}
