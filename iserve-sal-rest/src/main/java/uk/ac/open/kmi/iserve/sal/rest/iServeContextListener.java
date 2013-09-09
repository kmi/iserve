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

package uk.ac.open.kmi.iserve.sal.rest;/*
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.sal.manager.impl.iServeFacade;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * iServeContextListener
 * Basic Servlet Context Listener implementation to ensure that proper initialisation and shutdown take place.
 * <p/>
 * Author: Carlos Pedrinaci (KMi - The Open University)
 * Date: 19/06/2013
 * Time: 16:01
 */
public class iServeContextListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(iServeContextListener.class);

    /**
     * Notification that the web application initialization
     * process is starting.
     * All ServletContextListeners are notified of context
     * initialisation before any filter or servlet in the web
     * application is initialized.
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("iServe initialising");
    }

    /**
     * Notification that the servlet context is about to be shut down. All servlets
     * have been destroy()ed before any ServletContextListeners are notified of context
     * destruction.
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("iServe shutting down");
        iServeFacade.getInstance().shutdown();
    }
}
