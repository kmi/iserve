package uk.ac.open.kmi.iserve.commons.model;

/**
 * Created with IntelliJ IDEA.
 * User: cp3982
 * Date: 22/05/2013
 * Time: 02:10
 * To change this template use File | Settings | File Templates.
 *
 * TODO: Move to the proper package
 */
public enum Syntax {

    RDFXML ("RDF/XML"),
    RDFXML_ABBREV ("RDF/XML-ABBREV"),
    N_TRIPLE ("N-TRIPLE"),
    N3 ("N3"),
    TTL ("TURTLE");
//    OWLS ("OWLS");

    private final String name;

    Syntax(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
