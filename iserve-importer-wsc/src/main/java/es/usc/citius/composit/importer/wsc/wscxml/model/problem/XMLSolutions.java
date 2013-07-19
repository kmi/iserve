/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package es.usc.citius.composit.importer.wsc.wscxml.model.problem;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

/**
 *
 * @author pablo
 */
@XmlRootElement(name="solutions")
@XmlAccessorType(XmlAccessType.FIELD)
public class XMLSolutions {
    @XmlElement(name="solution")
    private ArrayList<XMLSolution> solutions;

    public ArrayList<XMLSolution> getSolutions() {
        return solutions;
    }

    public void setSolutions(ArrayList<XMLSolution> solutions) {
        this.solutions = solutions;
    }

}
