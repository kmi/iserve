package uk.ac.open.kmi.iserve.discovery.freetextsearch;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import uk.ac.open.kmi.iserve.core.ConfigurationModule;
import uk.ac.open.kmi.iserve.core.SystemConfiguration;

/**
 * Created by Luca Panziera on 26/08/2014.
 */
public class FreeTextSearchProvider extends AbstractModule {

    private ConfigurationModule configurationModule = new ConfigurationModule();

    @Override
    protected void configure() {
        install(configurationModule);
    }

    @Provides
    FreeTextSearchPlugin provideFreeTextSearchPlugin(@Named(SystemConfiguration.FREE_TEXT_SEARCH_PLUGIN) String pluginClassName) {
        Injector injector = Guice.createInjector(configurationModule);
        try {
            return (FreeTextSearchPlugin) injector.getInstance(Class.forName(pluginClassName));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
