package es.usc.citius.composit.importer.wsc.wscxml;

import uk.ac.open.kmi.iserve.commons.io.ServiceWriter;
import uk.ac.open.kmi.iserve.commons.io.ServiceWriterImpl;
import uk.ac.open.kmi.iserve.commons.io.Syntax;
import uk.ac.open.kmi.iserve.commons.model.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * @author Pablo Rodríguez Mier
 */
public class IOUtil {

    public static void dumpToRdf(Service s, File destination) throws FileNotFoundException {
        ServiceWriter writer = new ServiceWriterImpl();
        FileOutputStream out = new FileOutputStream(destination);
        Syntax syntax = Syntax.RDFXML;
        writer.serialise(s, out, syntax);
    }
}
