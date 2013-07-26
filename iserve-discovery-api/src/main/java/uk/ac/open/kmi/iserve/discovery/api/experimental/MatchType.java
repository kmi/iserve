package uk.ac.open.kmi.iserve.discovery.api.experimental;

// TODO Maybe is a better idea to create a component that holds all the types that are available and sorted by their priority
// TODO the matchtype comparable does not make sense. The value is given when all match types are declared
public interface MatchType  {
    String getMatchTypeName();
    String getMatchTypeDescription();
}
