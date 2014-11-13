/*
 * Copyright (c) 2014. Knowledge Media Institute - The Open University
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

import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Properties;

/**
 * SystemConfiguration
 * Helper HashMap with all configuration properties.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 15/10/2014
 */
@Singleton
public class SystemConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SystemConfiguration.class);

    private final String ISERVE_CONFIG_FILE = System.getProperty("iserve.config", "config.properties");

    private Properties configProperties;

    /**
     * Create a configuration based on the System property iserve.config or the default configuration file name
     * 'config.properties' within the classpath or home folder
     */
    public SystemConfiguration() {
        this.configProperties = this.readConfiguration(ISERVE_CONFIG_FILE);
    }


    /**
     * Creates a system configuration from the file indicated. The file will be searched
     * automatically in the following locations:
     * in the current directory
     * in the user home directory
     * in the classpath
     * <p/>
     * If no file is provided, we will fall back to default values
     *
     * @param configFilePath
     */
    public SystemConfiguration(String configFilePath) {
        this.configProperties = this.readConfiguration(configFilePath);
    }

    private Properties readConfiguration(String configFilePath) {

        // Use default configuration if unset
        if (configFilePath == null || configFilePath.isEmpty()) {
            log.warn("The URL of the configuration file is incorrect {}. Using default configuration.",
                    configFilePath);
            return new Properties();
        }

        // Else load properties using default procedure
        log.debug("Attempting to read configuration file - {} from classpath or home directory.", configFilePath);
        try {
            PropertiesConfiguration properties = new PropertiesConfiguration(configFilePath);
            log.info("Configuration loaded from {}", configFilePath);
            return ConfigurationConverter.getProperties(properties);
        } catch (ConfigurationException e) {
            log.warn("Unable to load configuration file - {}. Falling back to default values.", configFilePath);
            return new Properties();
        }
    }

    public String getString(ConfigurationProperty property) {
        String value = this.configProperties.getProperty(property.getName());
        return value != null ? value : property.getDefaultValue();
    }

    @Override
    public String toString() {
        return this.configProperties.toString();
    }

}
