/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.security.mock.MockSecurityManager;
import org.unitedinternet.cosmo.security.mock.MockTicketPrincipal;
import org.unitedinternet.cosmo.security.mock.MockUserPrincipal;
import org.w3c.dom.Document;

/**
 * Base class for executing servlet tests in a mock servlet container.
 */
public abstract class BaseMockServletTestCase {
    protected static final DocumentBuilderFactory BUILDER_FACTORY =
        DocumentBuilderFactory.newInstance();

    private MockSecurityManager securityManager;
    private MockServletContext servletContext;
    private MockServletConfig servletConfig;

  /**
   * 
   * @throws Exception - if something is wrong this exception is thrown.
   */
    @Before
    public void setUp() throws Exception {
        securityManager = new MockSecurityManager();
        servletContext = new MockServletContext();
        servletConfig = new MockServletConfig(servletContext);
    }

    /**
     * Create mock request.
     * @param method The method.
     * @param pathInfo Info path.
     * @return request = Mock http servlet request.
     */
    protected MockHttpServletRequest createMockRequest(String method,
                                                       String pathInfo) {
        MockHttpServletRequest request =
            new MockHttpServletRequest(servletContext, method,
                                       getServletPath() + pathInfo);
        request.setServletPath(getServletPath());
        request.setPathInfo(pathInfo);
        request.addHeader("Host", request.getServerName() + ":" +
                          request.getServerPort());
        return request;
    }

    /**
     * Log user.
     * @param user. The user.
     */
    protected void logInUser(User user) {
        securityManager.setUpMockSecurityContext(new MockUserPrincipal(user));
    }

    /**
     * Log in ticket.
     * @param ticket The ticket.
     */
    protected void logInTicket(Ticket ticket) {
        securityManager.setUpMockSecurityContext(new MockTicketPrincipal(ticket));
    }
    

    /**
     * Reads xml response.
     * @param response The response.
     * @return xml response.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    protected Document readXmlResponse(MockHttpServletResponse response)
        throws Exception {
        ByteArrayInputStream in =
            new ByteArrayInputStream(response.getContentAsByteArray());
        BUILDER_FACTORY.setNamespaceAware(true);
        return BUILDER_FACTORY.newDocumentBuilder().parse(in);
    }

    /**
     * Returns absolute url.
     * @param request The request.
     * @param path The path.
     * @return Absolute url.
     */
    protected String toAbsoluteUrl(MockHttpServletRequest request,
                                   String path) {
        StringBuilder url = new StringBuilder(request.getScheme());
        url.append("://").append(request.getServerName());
        if ((request.isSecure() && request.getServerPort() != 443) ||
            (request.getServerPort() != 80)) {
            url.append(":").append(request.getServerPort());
        }
        if (! request.getContextPath().equals("/")) {
            url.append(request.getContextPath());
        }
        url.append(path);
        return url.toString();
    }

    /**
     * Gets servlet path.
     * @return The servlet path.
     */
    public abstract String getServletPath();

    /**
     * Gets security manager.
     * @return The security manager.
     */
    public MockSecurityManager getSecurityManager() {
        return securityManager;
    }

    /**
     * Gets servlet context.
     * @return The servlet context.
     */
    public MockServletContext getServletContext() {
        return servletContext;
    }

    /**
     * Gets servlet config.
     * @return The servlet config.
     */
    public MockServletConfig getServletConfig() {
        return servletConfig;
    }
}
