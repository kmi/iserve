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

package uk.ac.open.kmi.iserve.sal.manager.impl;

import junit.framework.Assert;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.ac.open.kmi.iserve.core.ConfigurationModule;
import uk.ac.open.kmi.iserve.sal.manager.RegistryManager;

/**
 * Created by Luca Panziera on 08/08/2014.
 */
@RunWith(JukitoRunner.class)
public class NfpManagerTest {

    @Test
    public void NfpManagerInjectionTest(RegistryManager registryManager) throws Exception {
        Assert.assertNotNull(registryManager.getNfpManager());
    }

    public static class InnerModule extends JukitoModule {
        @Override
        protected void configureTest() {
            // Get configuration
            install(new ConfigurationModule());

            // Add transformer module
            install(new RegistryManagementModule());

            // Necessary to verify interaction with the real object
            bindSpy(NfpManagerSparql.class);
        }
    }

}
