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
 * @author Pablo Rodriguez Mier
 */
@XmlRootElement(name="realizations")
@XmlAccessorType(XmlAccessType.FIELD)
public class XMLRealizations {
    @XmlElement(name="service")
    private ArrayList<XMLService> services;

    public ArrayList<XMLService> getServices() {
        return services;
    }

    public void setServices(ArrayList<XMLService> services) {
        this.services = services;
    }


}
