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
import com.google.inject.name.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Module providing support for reading the configuration file and mapping the configuration
 * into {@code @Named} values. iServe modules should install this module if they need general configuration.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 19/09/2013
 */
public class ConfigurationModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationModule.class);

    private static final String DEFAULT_CONFIG_FILENAME = "config.properties";

    private Properties configProperties;

    public ConfigurationModule() {
        this.configProperties = getProperties(DEFAULT_CONFIG_FILENAME);
    }

    public ConfigurationModule(String configFileName) {
        this.configProperties = getProperties(configFileName);
    }

    public ConfigurationModule(Properties configProperties) {
        this.configProperties = configProperties;
    }

    @Override
    protected void configure() {
        // Bind the configuration file to @named values
        Names.bindProperties(binder(), configProperties);
    }

    private Properties getProperties(String fileName) {
        log.info("Loading configuration from - {}", fileName);
        try {
            Properties properties = new Properties();
            properties.load(getClass().getClassLoader().getResourceAsStream(fileName));
            return properties;
        } catch (IOException ex) {
            log.error("Error obtaining plugin properties", ex);
        }
        return new Properties();
    }
}
