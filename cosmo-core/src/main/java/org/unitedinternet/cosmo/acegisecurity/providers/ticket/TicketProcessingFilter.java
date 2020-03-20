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
package org.unitedinternet.cosmo.acegisecurity.providers.ticket;

import java.io.IOException;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.unitedinternet.cosmo.server.ServerUtils;

/**
 * Servlet filter that populates the
 * {@link org.springframework.security.ContextHolder} with a
 * {@link TicketAuthenticationToken} if needed.
 */
@Component
public class TicketProcessingFilter implements Filter {
    
    private static final Logger LOG = LoggerFactory.getLogger(TicketProcessingFilter.class);

    // Filter methods

    /**
     * Does nothing - we use IoC lifecycle methods instead.
     * @param filterConfig The filter config.
     * @throws ServletException - if something is wrong this exception is thrown.
     */
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /**
     * Examines HTTP servlet requests for ticket keys, creating a
     * {@link TicketAuthenticationToken} if any are found.
     *
     * Tokens are created with the
     * {@link #createAuthentication(String, Set)} method.
     *
     * A token's path is the path info of the request URI less any
     * trailing "/", or "/" if the URI represents the root resource.
     * @param request The servlet request.
     * @param response The servlet response.
     * @param chain The filter chain.
     * @throws IOException - if something is wrong this exception is thrown.
     * @throws ServletException - if something is wrong this exception is thrown.
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                                                throws IOException, ServletException {
        SecurityContext sc = SecurityContextHolder.getContext();
        if (sc.getAuthentication() == null && request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            Set<String> keys = ServerUtils.findTicketKeys(httpRequest);
            
            if (! keys.isEmpty()) {
                String path = httpRequest.getPathInfo();
                if (path == null || path.equals("")) {
                    path = "/";
                }
                if (! path.equals("/") && path.endsWith("/")) {
                    path = path.substring(0, path.length()-1);
                }
                // XXX: refactor so this path prefix is not
                // hardcoded .. or look at making security happen
                // after url-rewriting, not before
                if (path.startsWith("/atom/1.0")) {
                    path = path.substring(9);
                }

                Authentication token = createAuthentication(path, keys);
                sc.setAuthentication(token);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Replaced ContextHolder with ticket token: " +
                              sc.getAuthentication());
                }
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * Does nothing - we use IoC lifecycle methods instead
     */
    public void destroy() {
    }

    // our methods

    /**
     * Returns a {@link TicketAuthenticationToken} for the given
     * path and ticket keys.
     * @param path The given path.
     * @param keys The ticket keys.
     * @return The authentication.
     */
    protected Authentication createAuthentication(String path, Set<String> keys) {
        return new TicketAuthenticationToken(path, keys);
    }
}
