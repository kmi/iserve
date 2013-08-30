/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package es.usc.citius.composit.importer.wsc.wscxml.model.problem;

import es.usc.citius.composit.importer.wsc.wscxml.model.taxonomy.XMLConcept;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;

/**
 *
 * @author pablo
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class XMLInputs {
    @XmlElement(name="concept")
    private ArrayList<XMLConcept> inputs;

    public ArrayList<XMLConcept> getInputs() {
        return inputs;
    }

    public void setInputs(ArrayList<XMLConcept> inputs) {
        this.inputs = inputs;
    }

}
