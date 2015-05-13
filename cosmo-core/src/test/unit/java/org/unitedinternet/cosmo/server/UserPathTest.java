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
 * Test case for <code>UserPath</code>.
 */
public class UserPathTest {

    /**
     * Tests absolute url path.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testAbsoluteUrlPath() throws Exception {
        String badUrlPath = "http://dead.beef/";
        try {
            UserPath.parse(badUrlPath);
            Assert.fail("absolute urlPath parsed successfully");
        } catch (IllegalArgumentException e) {}
    }

    /**
     * Tests successful parse.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testSuccessfulParse() throws Exception {
        String urlPath = "/user/jonez";
        UserPath up = UserPath.parse(urlPath);
        Assert.assertNotNull("path did not parse successfully", up);
    }

    /**
     * Tests unsuccessful parse.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testUnsuccessfulParse() throws Exception {
        String urlPath = "/bcm/stuff/jonez";
        UserPath up = UserPath.parse(urlPath);
        Assert.assertNull("non-user path parsed successfuly", up);
    }

    /**
     * Tests parse no path info.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testParseNoPathInfo() throws Exception {
        String urlPath = "/user/jonez/foobar";
        UserPath up = UserPath.parse(urlPath);
        Assert.assertNull("path with disallowed pathInfo parsed successfully", up);
    }

    /**
     * Tests parse with path info.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testParseWithPathInfo() throws Exception {
        String urlPath = "/user/jonez/foobar";
        UserPath up = UserPath.parse(urlPath, true);
        Assert.assertNotNull("path with allowed pathInfo did not parse successfully",
                      up);
    }

    /**
     * Tests get username.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testGetUsername() throws Exception {
        String username = "jonez";
        String urlPath = "/user/" + username;
        UserPath up = UserPath.parse(urlPath);
        Assert.assertNotNull("path did not parse successfully", up);
        Assert.assertNotNull("username not found", up.getUsername());
        Assert.assertEquals("found incorrect username", username, up.getUsername());
    }

    /**
     * Tests get username with path info.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testGetUsernameWithPathInfo() throws Exception {
        String username = "jonez";
        String pathInfo = "/foobar";
        String urlPath = "/user/" + username + pathInfo;
        UserPath up = UserPath.parse(urlPath, true);
        Assert.assertNotNull("path did not parse successfully", up);
        Assert.assertNotNull("username not found", up.getUsername());
        Assert.assertEquals("found incorrect username", username, up.getUsername());
        Assert.assertNotNull("path info not found", up.getPathInfo());
        Assert.assertEquals("found incorrect path info", pathInfo, up.getPathInfo());
    }
}
