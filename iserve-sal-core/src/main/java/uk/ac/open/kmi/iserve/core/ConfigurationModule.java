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

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import uk.ac.open.kmi.iserve.core.impl.iServePropertyImpl;

/**
 * Module providing support for reading the configuration file and mapping the configuration
 * into {@code @Named} values. iServe modules should install this module if they need general configuration.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 19/09/2013
 */
public class ConfigurationModule extends AbstractModule {

    private String configurationFile = null;

    public ConfigurationModule() {
    }

    public ConfigurationModule(String configFileName) {
        this.configurationFile = configFileName;
    }

    @Override
    protected void configure() {
        SystemConfiguration configuration;
        if (this.configurationFile != null) {
            configuration = new SystemConfiguration(this.configurationFile);
        } else {
            configuration = new SystemConfiguration();
        }

        for (ConfigurationProperty property : ConfigurationProperty.values()) {
            String value = getValue(configuration, property);
            bind(Key.get(property.getValueType(), new iServePropertyImpl(property))).toInstance(value);
        }

        bind(SystemConfiguration.class).toInstance(configuration);
    }

    private String getValue(SystemConfiguration configuration, ConfigurationProperty property) {
        String value = configuration.getString(property);
        return value == null ? property.getDefaultValue() : value;
    }
}
