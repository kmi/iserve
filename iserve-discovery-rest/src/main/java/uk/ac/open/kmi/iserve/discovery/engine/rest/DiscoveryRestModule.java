/*
 * Created by IntelliJ IDEA.
 * User: cp3982
 * Date: 30/10/2013
 * Time: 13:50
 */
package uk.ac.open.kmi.iserve.discovery.engine.rest;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;
import uk.ac.open.kmi.iserve.core.ConfigurationModule;
import uk.ac.open.kmi.iserve.core.PluginModuleLoader;
import uk.ac.open.kmi.iserve.discovery.api.MatcherPluginModule;
import uk.ac.open.kmi.iserve.sal.manager.impl.RegistryManagementModule;

public class DiscoveryRestModule extends AbstractModule {

    protected void configure() {

        install(new ConfigurationModule());
        install(new RegistryManagementModule());
        // Load all matcher plugins
        install(PluginModuleLoader.of(MatcherPluginModule.class));

        // ServletModule defines the "request" scope. GuiceFilter creates/destroys the scope on each incoming request.
        install(new ServletModule());
    }
}
