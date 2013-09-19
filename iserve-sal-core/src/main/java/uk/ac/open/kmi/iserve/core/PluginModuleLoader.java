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

package uk.ac.open.kmi.iserve.core;

/**
 * ModuleLoader generic implementation for a Module Loader that uses Java's Service Loader behind the scenes.
 * Supports the automated discovery and loading of modules.
 * Solution based on http://stackoverflow.com/questions/902639/has-anyone-used-serviceloader-together-with-guice
 *
 * @author Mark Renouf
 * @since 18/09/2013
 */

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import java.util.ServiceLoader;

public class PluginModuleLoader<M extends Module> extends AbstractModule {

    private final Class<M> type;

    public PluginModuleLoader(Class<M> type) {
        this.type = type;
    }

    public static <M extends Module> PluginModuleLoader<M> of(Class<M> type) {
        return new PluginModuleLoader<M>(type);
    }

    @Override
    protected void configure() {
        ServiceLoader<M> modules = ServiceLoader.load(type);
        for (Module module : modules) {
            install(module);
        }
    }
}
