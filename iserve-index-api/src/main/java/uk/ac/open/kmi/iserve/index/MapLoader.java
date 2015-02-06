package uk.ac.open.kmi.iserve.index;


import java.util.Map;

public interface MapLoader {
    <K,V> void load(Map<K,V> map);
}
