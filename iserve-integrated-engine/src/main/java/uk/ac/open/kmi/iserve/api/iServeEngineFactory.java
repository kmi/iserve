package uk.ac.open.kmi.iserve.api;

import com.google.inject.Guice;
import com.google.inject.Injector;

import java.util.Properties;

/**
 * Factory for creating iServeEngine instances.
 * This factory takes care of the underlying initialisation and wiring with Guice on behalf of clients.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 30/10/2013
 */
public class iServeEngineFactory {

    public static iServeEngine createEngine() {
        Injector injector = Guice.createInjector(new iServeEngineModule());
        return injector.getInstance(iServeEngine.class);
    }

    public static iServeEngine createEngine(String configFile) {
        Injector injector = Guice.createInjector(new iServeEngineModule(configFile));
        return injector.getInstance(iServeEngine.class);
    }

    public static iServeEngine createEngine(Properties config) {
        Injector injector = Guice.createInjector(new iServeEngineModule(config));
        return injector.getInstance(iServeEngine.class);
    }

}
