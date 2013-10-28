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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.discovery.api.ConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.disco.impl.SparqlIndexedLogicConceptMatcher;
import uk.ac.open.kmi.iserve.discovery.disco.impl.SparqlLogicConceptMatcher;

/**
 * MatchersFactoryTest
 *
 * @author <a href="mailto:carlos.pedrinaci@open.ac.uk">Carlos Pedrinaci</a> (KMi - The Open University)
 * @since 18/09/2013
 */
public class MatchersFactoryTest {

    private static final Logger log = LoggerFactory.getLogger(MatchersFactoryTest.class);

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testPluginLoading() {

        ConceptMatcher matcher;

        // check we get one
        matcher = MatchersFactory.createConceptMatcher();
        Assert.assertNotNull(matcher);
        log.info("Obtained a concept matcher - {}", matcher.toString());

        // Check we get one of the class requested
        matcher = MatchersFactory.createConceptMatcher(SparqlLogicConceptMatcher.class.getName());
        Assert.assertNotNull(matcher);
        log.info("Obtained a SparqlLogicConceptMatcher - {}", matcher.toString());
        Assert.assertEquals(matcher.getClass(), SparqlLogicConceptMatcher.class);

        // Check we get one of the class requested
        matcher = MatchersFactory.createConceptMatcher(SparqlIndexedLogicConceptMatcher.class.getName());
        Assert.assertNotNull(matcher);
        log.info("Obtained a SparqlIndexedLogicConceptMatcher - {}", matcher.toString());
        Assert.assertEquals(matcher.getClass(), SparqlIndexedLogicConceptMatcher.class);

    }

}
