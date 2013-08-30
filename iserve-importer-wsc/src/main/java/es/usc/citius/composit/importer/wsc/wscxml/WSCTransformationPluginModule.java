package es.usc.citius.composit.importer.wsc.wscxml;


import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import uk.ac.open.kmi.iserve.commons.io.ServiceTransformer;
import uk.ac.open.kmi.iserve.commons.io.TransformationPluginModule;

import java.io.IOException;
import java.util.Properties;

public class WSCTransformationPluginModule extends AbstractModule implements TransformationPluginModule {
    @Override
    protected void configure() {
        MapBinder<String,ServiceTransformer> binder =
                MapBinder.newMapBinder(binder(), String.class, ServiceTransformer.class);
        // TODO Change binding type
        binder.addBinding("text/xml").to(WSCImporter.class);
        // Bind the configuration as well
        Names.bindProperties(binder(), getProperties());
    }

    private Properties getProperties() {
        try {
            Properties properties = new Properties();
            properties.load(getClass().getClassLoader().getResourceAsStream("wscimporter.properties"));
            return properties;
        } catch (IOException ex) {
            //log.error("Error obtaining plugin properties", ex);
        }
        return new Properties();
    }
}
