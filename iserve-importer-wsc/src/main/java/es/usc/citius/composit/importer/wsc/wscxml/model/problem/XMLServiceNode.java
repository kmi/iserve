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
 *
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
