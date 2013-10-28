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
package uk.ac.open.kmi.iserve.discovery.engine;

import org.apache.abdera.Abdera;

import javax.xml.namespace.QName;

/**
 * Class Description
 *
 * @author Carlos Pedrinaci (Knowledge Media Institute - The Open University)
 * @version $Rev$
 *          $LastChangedDate$
 *          $LastChangedBy$
 */
public class DiscoveryUtil {

    public static final String NS = "http://iserve.kmi.open.ac.uk/xmlns/";
    public static final QName MATCH_DEGREE =
            new QName(NS, "matchDegree");

    public static final String INTER_DEGREE = "inter";

    public static final String GSSOS_DEGREE = "gssos";

    public static final String SSSOG_DEGREE = "sssog";

    public static final String EXACT_DEGREE = "exact";


    /**
     * Abdera singleton.
     */
    private static Abdera abdera = null;

    public static synchronized Abdera getAbderaInstance() {
        if (abdera == null) abdera = new Abdera();
        return abdera;
    }

}
