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

package es.usc.citius.composit.importer.wsc.wscxml;


import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import uk.ac.open.kmi.iserve.commons.io.ServiceTransformer;
import uk.ac.open.kmi.iserve.commons.io.TransformationPluginModule;

import java.io.IOException;
import java.util.Properties;

public class WSCTransformationPluginModule extends AbstractModule implements TransformationPluginModule {
    @Override
    protected void configure() {
        MapBinder<String, ServiceTransformer> binder =
                MapBinder.newMapBinder(binder(), String.class, ServiceTransformer.class);
        // TODO Change binding type
        binder.addBinding("text/xml").to(WSCImporter.class);
        // Bind the configuration as well
        Names.bindProperties(binder(), getProperties());
    }

    private Properties getProperties() {
        try {
            Properties properties = new Properties();
            properties.load(getClass().getClassLoader().getResourceAsStream("wscimporter.properties"));
            return properties;
        } catch (IOException ex) {
            //log.error("Error obtaining plugin properties", ex);
        }
        return new Properties();
    }
}
