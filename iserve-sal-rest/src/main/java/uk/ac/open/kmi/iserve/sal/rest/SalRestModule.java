/*
 * Created by IntelliJ IDEA.
 * User: cp3982
 * Date: 30/10/2013
 * Time: 13:50
 */
package uk.ac.open.kmi.iserve.sal.rest;

import com.google.inject.AbstractModule;
import uk.ac.open.kmi.iserve.core.ConfigurationModule;
import uk.ac.open.kmi.iserve.sal.manager.impl.RegistryManagementModule;

public class SalRestModule extends AbstractModule {

    protected void configure() {
        install(new ConfigurationModule());
        install(new RegistryManagementModule());
    }
}
