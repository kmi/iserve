package es.usc.citius.composit.importer.wsc.wscxml;


import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import uk.ac.open.kmi.iserve.commons.io.ServiceTransformer;
import uk.ac.open.kmi.iserve.commons.io.TransformationPluginModule;

public class WSCTransformationPluginModule extends AbstractModule implements TransformationPluginModule {
    @Override
    protected void configure() {
        MapBinder<String,ServiceTransformer> binder =
                MapBinder.newMapBinder(binder(), String.class, ServiceTransformer.class);
        // TODO Change binding
        binder.addBinding("application/rdf+xml").to(WSCImporter.class);
    }
}
