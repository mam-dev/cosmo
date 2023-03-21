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

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
            fail("absolute urlPath parsed successfully");
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
        assertNotNull(up, "path did not parse successfully");
    }

    /**
     * Tests unsuccessful parse.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testUnsuccessfulParse() throws Exception {
        String urlPath = "/bcm/stuff/jonez";
        UserPath up = UserPath.parse(urlPath);
        assertNull(up, "non-user path parsed successfuly");
    }

    /**
     * Tests parse no path info.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testParseNoPathInfo() throws Exception {
        String urlPath = "/user/jonez/foobar";
        UserPath up = UserPath.parse(urlPath);
        assertNull(up, "path with disallowed pathInfo parsed successfully");
    }

    /**
     * Tests parse with path info.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testParseWithPathInfo() throws Exception {
        String urlPath = "/user/jonez/foobar";
        UserPath up = UserPath.parse(urlPath, true);
        assertNotNull(up, "path with allowed pathInfo did not parse successfully");
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
        assertNotNull(up, "path did not parse successfully");
        assertNotNull(up.getUsername(), "username not found");
        assertEquals(username, up.getUsername(), "found incorrect username");
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
        assertNotNull(up, "path did not parse successfully");
        assertNotNull(up.getUsername(), "username not found");
        assertEquals(username, up.getUsername(), "found incorrect username");
        assertNotNull(up.getPathInfo(), "path info not found");
        assertEquals(pathInfo, up.getPathInfo(), "found incorrect path info");
    }
}
