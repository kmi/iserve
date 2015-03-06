package uk.ac.open.kmi.iserve.discovery.api.freetextsearch;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import uk.ac.open.kmi.iserve.core.ConfigurationProperty;
import uk.ac.open.kmi.iserve.core.iServeProperty;

/**
 * Created by Luca Panziera on 26/08/2014.
 */
public class FreeTextSearchModule extends AbstractModule {


    private String className;

    @Inject
    public FreeTextSearchModule(@iServeProperty(ConfigurationProperty.FREE_TEXT_SEARCH) String pluginClassName) {
        className = pluginClassName;
    }

    @Override
    protected void configure() {
        try {
            Class c = Class.forName(className);
            bind(FreeTextSearchPlugin.class).to(c).asEagerSingleton();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

}
