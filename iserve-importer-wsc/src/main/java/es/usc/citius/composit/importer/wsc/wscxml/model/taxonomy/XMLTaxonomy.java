/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package es.usc.citius.composit.importer.wsc.wscxml.model.taxonomy;

import es.usc.citius.composit.importer.wsc.wscxml.model.XMLInstance;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Pablo Rodriguez Mier
 */

@XmlRootElement(name="taxonomy")
@XmlAccessorType(XmlAccessType.FIELD)
public class XMLTaxonomy {
    @XmlElement(name="concept")
    private XMLConcept concept;

    public XMLConcept getConcept() {
        return concept;
    }

    public void setConcept(XMLConcept concept) {
        this.concept = concept;
    }

    public XMLConcept searchConceptForInstance(String _instance){
        XMLConcept con = searchConceptForInstance(this.concept, _instance);
        if (con == null){
            // Si no se encontró nada, manda excepcion
            throw new RuntimeException("searchConceptForInstance: Invalid concept for instance " + _instance);
        } else {
            return con;
        }
    }

    /**
     * Busca cual es el concepto asociado a una instancia según
     * la jerarquía de conceptos e instancias del archivo Taxonomy.xml del
     * Web Service Challenge
     * @param _root Concepto raíz
     * @param _instance Instancia que se busca
     * @return Concepto padre o null en caso de que no exista
     */
    private XMLConcept searchConceptForInstance(XMLConcept _root, String _instance){
        // Si el concepto tiene una instancia que se llame igual, devuelve
        // el concepto
        if (containsInstance(_root, _instance)){
            return _root;
        } else {
            XMLConcept found;
            // En caso contrario, sigue buscando en sus hijos
            if (_root != null && _root.getConcepts() != null){
                for(XMLConcept con : _root.getConcepts()){
                   found = searchConceptForInstance(con, _instance);
                   if (found != null) return found;
                }
            }
            // Si no se encontró nada, devuelve null
            return null;
        }

    }

    /**
     * Comprueba si un concepto contiene una instancia determinada
     * @param _concept Concepto en el que se va a buscar
     * @param _instance String con el nombre de la instancia a buscar
     * @return
     */
    private boolean containsInstance(XMLConcept _concept, String _instance){
        for(XMLInstance instance : _concept.getInstances()){
            if (instance.getName().equals(_instance)){
                return true;
            }
        }
        return false;
    }


}
