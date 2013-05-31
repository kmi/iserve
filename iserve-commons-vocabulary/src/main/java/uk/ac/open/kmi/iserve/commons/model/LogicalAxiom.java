package uk.ac.open.kmi.iserve.commons.model;

import java.net.URI;

/**
 * Logical Axiom.
 * Logical Axioms are Conditions and Effects which may be specified in any language
 *
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 22/05/2013
 * Time: 12:45
 */
public class LogicalAxiom extends Resource {

    /**
     * Types of Logical Axioms.
     * In reality they are identically defined but this helps us
     * better track the role they play in a given service description
     */
    public static enum Type {
        CONDITION,
        EFFECT
    }

    /**
     * Rule language used in the logical axiom
     * We include for now a few reference ones but this should be extended as necessary
     */
    public static enum RuleLanguage {
        RIF ("RIF"),
        SPIN ("SPIN"),
        SPARQL ("SPARQL"),
        WSML ("WSML");

        private final String name;

        RuleLanguage(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private Type type;
    private String value;
    private RuleLanguage language;

    public LogicalAxiom(URI uri) {
        super(uri);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public RuleLanguage getLanguage() {
        return language;
    }

    public void setLanguage(RuleLanguage language) {
        this.language = language;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
