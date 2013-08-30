/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package es.usc.citius.composit.importer.wsc.wscxml.model.problem;



import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;


/**
 *
 * @author Pablo Rodriguez Mier
 */

@XmlRootElement(name="serviceDesc")
@XmlAccessorType(XmlAccessType.FIELD)
public class XMLServiceDesc extends XMLServiceNode {
    @XmlElement(name="abstraction")
    private XMLAbstraction abstraction;
    @XmlElement(name="realizations")
    private XMLRealizations realizations;

    public XMLAbstraction getAbstraction() {
        return abstraction;
    }

    public void setAbstraction(XMLAbstraction abstraction) {
        this.abstraction = abstraction;
    }

    public XMLRealizations getRealizations() {
        return realizations;
    }

    public void setRealizations(XMLRealizations realizations) {
        this.realizations = realizations;
    }
    

    
}
