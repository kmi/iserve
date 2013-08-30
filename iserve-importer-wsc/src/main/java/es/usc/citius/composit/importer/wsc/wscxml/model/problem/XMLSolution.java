/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package es.usc.citius.composit.importer.wsc.wscxml.model.problem;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author pablo
 */
@XmlRootElement(name="solution")
@XmlAccessorType(XmlAccessType.FIELD)
public class XMLSolution {
    @XmlElementRef
    private XMLServiceNode rootnode;

    public XMLServiceNode getRootnode() {
        return rootnode;
    }

    public void setRootnode(XMLServiceNode rootnode) {
        this.rootnode = rootnode;
    }

}
