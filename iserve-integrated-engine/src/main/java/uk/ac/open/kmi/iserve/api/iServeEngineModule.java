/*
 * Copyright (c) 2013. Knowledge Media Institute - The Open University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Created by IntelliJ IDEA.
 * User: cp3982
 * Date: 25/10/2013
 * Time: 17:46
 */
package uk.ac.open.kmi.iserve.api;

import com.google.inject.AbstractModule;
import uk.ac.open.kmi.iserve.api.impl.iServeEngineImpl;
import uk.ac.open.kmi.iserve.core.PluginModuleLoader;
import uk.ac.open.kmi.iserve.discovery.api.MatcherPluginModule;
import uk.ac.open.kmi.iserve.sal.manager.impl.RegistryManagementModule;

/**
 * Guice Module providing an Integrated iServe Engine
 */
public class iServeEngineModule extends AbstractModule {

    private final String configFile;

    public iServeEngineModule() {
        super();
        this.configFile = null;
    }

    public iServeEngineModule(String configFileName) {
        super();
        this.configFile = configFileName;
    }

    @Override
    protected void configure() {
        // Add Registry Management Module
        if (this.configFile == null) {
            install(new RegistryManagementModule());
        } else {
            install(new RegistryManagementModule(configFile));
        }
        // Load all matcher plugins
        install(PluginModuleLoader.of(MatcherPluginModule.class));

        // Bind the engine
        bind(iServeEngine.class).to(iServeEngineImpl.class);
    }
}
