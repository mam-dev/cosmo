/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dav.impl;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.mock.web.MockHttpServletRequest;
import org.unitedinternet.cosmo.dav.BaseDavTestCase;

/**
 * Test case for <code>StandardDavRequest</code>.
 */
public class StandardDavRequestTest extends BaseDavTestCase {
    
    /**
     * Tests no depth.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testNoDepth() throws Exception {
        // no depth => depth infinity

        MockHttpServletRequest httpRequest =
            new MockHttpServletRequest();
        StandardDavRequest request =
            new StandardDavRequest(httpRequest,
                                   testHelper.getResourceLocatorFactory(),
                                   testHelper.getEntityFactory());

        assertEquals(DEPTH_INFINITY,
                     request.getDepth(),
                     "no depth not infinity");
    }

    /**
     * Tests bad depth.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testBadDepth() throws Exception {
        MockHttpServletRequest httpRequest =
            new MockHttpServletRequest();
        httpRequest.addHeader("Depth", "bad value");
        StandardDavRequest request =
            new StandardDavRequest(httpRequest,
                                   testHelper.getResourceLocatorFactory(),
                                   testHelper.getEntityFactory());

        try {
            int depth = request.getDepth();
            fail("got bad depth " + depth);
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * Tests empty prop find body.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testEmptyPropfindBody() throws Exception {
        // empty propfind body => allprop request

        MockHttpServletRequest httpRequest =
            new MockHttpServletRequest();
        httpRequest.setContentType("text/xml");
        StandardDavRequest request =
            new StandardDavRequest(httpRequest,
                                   testHelper.getResourceLocatorFactory(),
                                   testHelper.getEntityFactory());

        assertEquals(PROPFIND_ALL_PROP,
                     request.getPropFindType(),
                     "propfind type not allprop");
        assertTrue(request.getPropFindProperties().isEmpty(),
                   "propnames not empty");
    }
}
