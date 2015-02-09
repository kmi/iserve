package uk.ac.open.kmi.iserve.discovery.disco.impl;

import com.google.inject.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.ConfigurationProperty;
import uk.ac.open.kmi.iserve.core.iServeProperty;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.sal.manager.impl.RegistryManagementModule;

/**
 * Created by Luca Panziera on 09/02/15.
 */
public class ConceptMatcherProvider extends AbstractModule {
    private static final Logger log = LoggerFactory.getLogger(ConceptMatcherProvider.class);

    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    ConceptMatcher provideConceptMatcher(@iServeProperty(ConfigurationProperty.CONCEPT_MATCHER) String pluginClassName) {
        Injector injector = Guice.createInjector(new RegistryManagementModule());
        try {
            log.debug("Injecting Concept matcher: {}", pluginClassName);
            return (ConceptMatcher) injector.getInstance(Class.forName(pluginClassName));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
