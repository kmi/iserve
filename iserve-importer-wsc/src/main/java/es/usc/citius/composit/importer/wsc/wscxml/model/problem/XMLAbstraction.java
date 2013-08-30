/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package es.usc.citius.composit.importer.wsc.wscxml.model.problem;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author pablo
 */
@XmlRootElement(name="abstraction")
@XmlAccessorType(XmlAccessType.FIELD)
public class XMLAbstraction {
    //@XmlElement(name="input")
    private XMLInputs input;
    //@XmlElement(name="output")
    private XMLOutputs output;

    public XMLInputs getInput() {
        return input;
    }

    public void setInput(XMLInputs input) {
        this.input = input;
    }

    public XMLOutputs getOutput() {
        return output;
    }

    public void setOutput(XMLOutputs output) {
        this.output = output;
    }
    
}
