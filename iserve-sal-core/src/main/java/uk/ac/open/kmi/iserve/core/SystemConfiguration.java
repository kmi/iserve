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
import java.net.MalformedURLException;
import java.net.URL;
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

    private final String ISERVE_HOME = System.getenv("ISERVE_HOME");
    private static final String DEFAULT_CONFIG_FILENAME = "config.properties";

    private Properties configProperties;

    public SystemConfiguration() {
        this(DEFAULT_CONFIG_FILENAME);
    }


    /**
     * Creates a system configuration from the file indicated.
     * If the configFilePath is relative and ISERVE_HOME environment variable is set we will first attempt
     * to locate the configuration file relative to ISERVE_HOME/conf
     *
     * If you do not specify an absolute path, and ISERVE_HOME is not set, the file will be searched
     * automatically in the following locations:
     * in the current directory
     * in the user home directory
     * in the classpath
     *
     * @param configFilePath
     */
    public SystemConfiguration(String configFilePath) {
        this.configProperties = this.readConfiguration(configFilePath);
    }

    public SystemConfiguration(URL configFileUri) {
        this.configProperties = this.readConfiguration(configFileUri);
    }

    private Properties readConfiguration(URL configFileUrl) {
        try {
            PropertiesConfiguration properties = new PropertiesConfiguration(configFileUrl);
            log.info("Configuration loaded from {}", configFileUrl);
            return ConfigurationConverter.getProperties(properties);
        } catch (ConfigurationException e) {
            log.warn("Unable to load configuration file {}. Using default values.", configFileUrl);
            return new Properties();
        }
    }

    private Properties readConfiguration(String configFilePath) {

        URL configFileUrl = null;

        // Load as URL if it starts with file:/
        if (configFilePath.startsWith("file:")) {
            try {
                configFileUrl = new URL(configFilePath);
                log.info("Configuration loaded from {}", configFilePath);
                return readConfiguration(configFileUrl);
            } catch (MalformedURLException e) {
                log.warn("The URL of the configuration file is incorrect {}. Using default configuration.",
                        configFilePath);
                return new Properties();
            }
        }

        // Use default configuration file if unset
        if (configFilePath == null || configFilePath.isEmpty()) {
            configFilePath = DEFAULT_CONFIG_FILENAME;
        }

        // If ISERVE_HOME is set load it relative to it
        if (ISERVE_HOME != null) {
            // Do it relative to ISERVE_HOME
            log.debug("ISERVE_HOME set to - {}", ISERVE_HOME);
            configFileUrl = SystemConfiguration.class.getResource(ISERVE_HOME + "/conf/" + configFilePath);
            return readConfiguration(configFileUrl);
        }

        // Load properties using default procedure
        log.debug("Attempting to read configuration file - {} from classpath or home directory.");
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
