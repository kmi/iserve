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

package uk.ac.open.kmi.iserve.importer.owls;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import uk.ac.open.kmi.iserve.commons.io.ServiceTransformer;
import uk.ac.open.kmi.iserve.commons.io.TransformationPluginModule;

/**
 * OwlsTransformationPlugin is a Guice module providing support for transforming OWL-S services into MSM
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 18/07/2013
 */
public class OwlsTransformationPlugin extends AbstractModule implements TransformationPluginModule {

    @Override
    protected void configure() {
        MapBinder<String, ServiceTransformer> binder = MapBinder.newMapBinder(binder(), String.class, ServiceTransformer.class);
        binder.addBinding(OwlsTransformer.mediaType).to(OwlsTransformer.class);
    }
}
