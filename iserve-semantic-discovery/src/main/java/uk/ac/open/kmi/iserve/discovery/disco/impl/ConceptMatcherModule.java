package uk.ac.open.kmi.iserve.discovery.disco.impl;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.ConfigurationProperty;
import uk.ac.open.kmi.iserve.core.iServeProperty;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;

/**
 * Created by Luca Panziera on 09/02/15.
 */
public class ConceptMatcherModule extends AbstractModule {
    private static final Logger log = LoggerFactory.getLogger(ConceptMatcherModule.class);
    private String className;

    @Inject
    public ConceptMatcherModule(@iServeProperty(ConfigurationProperty.CONCEPT_MATCHER) String pluginClassName) {
        className = pluginClassName;
    }

    @Override
    protected void configure() {
        try {
            Class c = Class.forName(className);
            bind(ConceptMatcher.class).to(c).asEagerSingleton();
            log.debug("Injected Concept Matcher: {}", className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }


}
