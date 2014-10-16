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

import java.io.File;
import java.net.URL;
import java.util.Properties;

/**
 * SystemConfiguration
 * Helper HashMap with all configuration properties.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 15/10/2014
 */
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
            return ConfigurationConverter.getProperties(properties);
        } catch (ConfigurationException e) {
            log.warn("Problems loading configuration file {}. Trying default locations.", configFileUrl);
            log.warn("Original error", e);
            return new Properties();
        }
    }

    private Properties readConfiguration(String configFilePath) {
        PropertiesConfiguration properties;
        File configFile = new File(configFilePath);
        if (!configFile.isAbsolute()) {
            if (ISERVE_HOME != null) {
                // Do it relative to ISERVE_HOME
                log.debug("ISERVE_HOME set to - {}", ISERVE_HOME);
                configFile = new File(ISERVE_HOME + "/conf", configFilePath);
                if (configFile.exists()) {
                    log.debug("Attempting to read configuration file - {}", configFile);
                    try {
                        properties = new PropertiesConfiguration(configFile);
                    } catch (ConfigurationException e) {
                        log.warn("Problems loading configuration file {}. Falling back to default values.", configFile.toURI());
                        log.warn("Original error", e);
                        return new Properties();
                    }
                }
            }
        }

        log.debug("Attempting to read configuration file - {}", configFilePath);
        try {
            properties = new PropertiesConfiguration(configFilePath);
        } catch (ConfigurationException e) {
            log.warn("Problems loading configuration file {}. Falling back to default values.", configFile.toURI());
            log.warn("Original error", e);
            return new Properties();
        }

        return ConfigurationConverter.getProperties(properties);
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
