/*
 * Created by IntelliJ IDEA.
 * User: cp3982
 * Date: 30/10/2013
 * Time: 13:50
 */
package uk.ac.open.kmi.iserve.sal.rest;

import com.google.inject.servlet.ServletModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.ConfigurationModule;
import uk.ac.open.kmi.iserve.sal.manager.impl.RegistryManagementModule;

public class SalRestModule extends ServletModule {

    private Logger logger = LoggerFactory.getLogger(SalRestModule.class);

    @Override
    protected void configureServlets() {
        logger.debug("Loading SAL Rest module...");
        install(new ConfigurationModule());
        install(new RegistryManagementModule());
    }
}
