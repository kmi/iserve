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

package uk.ac.open.kmi.iserve.api;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.sal.manager.iServeManager;
import uk.ac.open.kmi.iserve.sal.manager.impl.iServeManagementModule;

/**
 * iServe Facade provides the main entry point for client applications to use iServe.
 *
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public class iServeFacade {

    private static final Logger log = LoggerFactory.getLogger(iServeFacade.class);

    private static iServeManager instance;

    /**
     * Obtains the iServeManager
     *
     * @return the iServeManager instance
     */
    public static iServeManager getRegistryManager() {

        Injector injector;
        if (instance == null) {
            injector = Guice.createInjector(new iServeManagementModule());
            instance = injector.getInstance(iServeManager.class);
        }

        return instance;
    }

}
