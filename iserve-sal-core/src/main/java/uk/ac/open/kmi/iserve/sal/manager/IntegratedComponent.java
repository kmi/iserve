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

package uk.ac.open.kmi.iserve.sal.manager;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.SystemConfiguration;
import uk.ac.open.kmi.iserve.sal.exception.SalException;

import javax.inject.Named;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * IntegratedComponent is an abstract class for iServe Components that ensures that the components are interconnected
 * through an internal communication EventBus. iServe Components should extend this class for ensuring adequate
 * communication across modules.
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 06/09/2013
 */
public abstract class IntegratedComponent implements iServeComponent {

    private static final Logger log = LoggerFactory.getLogger(IntegratedComponent.class);

    private final EventBus eventBus;
    private final URI iserveUri;

    @Inject
    protected IntegratedComponent(EventBus eventBus, @Named(SystemConfiguration.ISERVE_URL_PROP) String iServeUri) throws SalException {

        log.debug("Creating integrated component with iServe URI - {}", iServeUri);

        this.eventBus = eventBus;
        eventBus.register(this);

        if (iServeUri == null) {
            log.error("The system requires the base URI for the server.");
            throw new SalException("The system requires the base URI for the server.");
        }

        // Ensure it has a final slash
        if (!iServeUri.endsWith("/")) {
            iServeUri = iServeUri + "/";
        }

        try {
            this.iserveUri = new URI(iServeUri);
        } catch (URISyntaxException e) {
            log.error("Incorrect iServe URI while initialising " + this.getClass().getSimpleName(), e);
            throw new SalException("Incorrect iServe URI while initialising " + this.getClass().getSimpleName(), e);
        }
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public URI getIserveUri() {
        return iserveUri;
    }

    /**
     * This method will be called when the server is being shutdown.
     * Ensure a clean shutdown.
     */
    @Override
    public void shutdown() {
        this.eventBus.unregister(this);
    }
}
