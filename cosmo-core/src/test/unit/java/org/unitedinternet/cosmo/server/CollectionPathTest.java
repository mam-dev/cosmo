/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.server;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test case for <code>CollectionPath</code>.
 */
public class CollectionPathTest {

    /**
     * Tests absolute url path.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testAbsoluteUrlPath() throws Exception {
        String badUrlPath = "http://dead.beef/";
        try {
            CollectionPath.parse(badUrlPath);
            Assert.fail("absolute urlPath parsed successfully");
        } catch (IllegalArgumentException e) {}
    }

    /**
     * Tests successful parse. 
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testSuccessfulParse() throws Exception {
        String urlPath = "/collection/deadbeef";
        CollectionPath cp = CollectionPath.parse(urlPath);
        Assert.assertNotNull("path did not parse successfully", cp);
    }

    /**
     * Test unsuccessful parse.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testUnsuccessfulParse() throws Exception {
        String urlPath = "/bcm/stuff/deadbeef";
        CollectionPath cp = CollectionPath.parse(urlPath);
        Assert.assertNull("non-collection path parsed successfuly", cp);
    }

    /**
     * Tests parse no path info.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testParseNoPathInfo() throws Exception {
        String urlPath = "/collection/deadbeef/foobar";
        CollectionPath cp = CollectionPath.parse(urlPath);
        Assert.assertNull("path with disallowed pathInfo parsed successfully", cp);
    }

    /**
     * Tests parse with path info.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testParseWithPathInfo() throws Exception {
        String urlPath = "/collection/deadbeef/foobar";
        CollectionPath cp = CollectionPath.parse(urlPath, true);
        Assert.assertNotNull("path with allowed pathInfo did not parse successfully",
                      cp);
    }

    /**
     * Tests get uid.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testGetUid() throws Exception {
        String uid = "deadbeef";
        String urlPath = "/collection/" + uid;
        CollectionPath cp = CollectionPath.parse(urlPath);
        Assert.assertNotNull("path did not parse successfully", cp);
        Assert.assertNotNull("uid not found", cp.getUid());
        Assert.assertEquals("found incorrect uid", uid, cp.getUid());
    }

    /**
     * Tests get uid with path info.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testGetUidWithPathInfo() throws Exception {
        String uid = "deadbeef";
        String pathInfo = "/foobar";
        String urlPath = "/collection/" + uid + pathInfo;
        CollectionPath cp = CollectionPath.parse(urlPath, true);
        Assert.assertNotNull("path did not parse successfully", cp);
        Assert.assertNotNull("uid not found", cp.getUid());
        Assert.assertEquals("found incorrect uid", uid, cp.getUid());
        Assert.assertNotNull("path info not found", cp.getPathInfo());
        Assert.assertEquals("found incorrect path info", pathInfo, cp.getPathInfo());
    }
}
