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

import uk.ac.open.kmi.iserve.sal.exception.LogException;

import java.net.URI;
import java.util.Date;

/**
 * Class Description
 * TODO: Complete and use PROVO
 *
 * @author Dong Liu (Knowledge Media Institute - The Open University)
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 */
public interface LogManager {

    // TODO: Change to use URIs
    public abstract void log(URI agentUri, URI actionUri, URI object,
                             Date time, String method) throws LogException;

}