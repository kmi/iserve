package uk.ac.open.kmi.iserve.sal.util;

/**
 * Simple Triplet for convenience when parsing graph updates
 *
 * @param <T> The Subject type
 * @param <U> The Property type
 * @param <V> The Object type
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 19/11/15
 */

public class Triplet<T, U, V>
{
    T subject;
    U property;
    V object;

    public Triplet(T subject, U property, V object)
    {
        this.subject = subject;
        this.property = property;
        this.object = object;
    }

    public T getSubject(){ return subject;}
    public U getProperty(){ return property;}
    public V getObject(){ return object;}
}
