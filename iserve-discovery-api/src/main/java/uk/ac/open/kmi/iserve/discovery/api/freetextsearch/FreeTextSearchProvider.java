package uk.ac.open.kmi.iserve.discovery.api.freetextsearch;

import com.google.inject.*;
import uk.ac.open.kmi.iserve.core.ConfigurationModule;
import uk.ac.open.kmi.iserve.core.ConfigurationProperty;
import uk.ac.open.kmi.iserve.core.iServeProperty;

/**
 * Created by Luca Panziera on 26/08/2014.
 */
public class FreeTextSearchProvider extends AbstractModule {

    private ConfigurationModule configurationModule = new ConfigurationModule();

    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    FreeTextSearchPlugin provideFreeTextSearchPlugin(@iServeProperty(ConfigurationProperty.FREE_TEXT_SEARCH) String pluginClassName) {
        Injector injector = Guice.createInjector(configurationModule);
        try {
            return (FreeTextSearchPlugin) injector.getInstance(Class.forName(pluginClassName));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
