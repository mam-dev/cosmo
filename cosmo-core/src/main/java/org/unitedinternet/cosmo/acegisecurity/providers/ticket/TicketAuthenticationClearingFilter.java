/*
 * Copyright 2008 Open Source Applications Foundation
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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

/**
 * Servlet filter that detects if a ticket is associated with
 * the current context and clears the context.
 */
public class TicketAuthenticationClearingFilter implements Filter {
    
    private static final Logger LOG = LoggerFactory.getLogger(TicketAuthenticationClearingFilter.class);

    // Filter methods

    /**
     * Does nothing - we use IoC lifecycle methods instead
     * @param filterConfig The filter config.
     * @throws ServletException - if something is wrong this exception is thrown.
     */
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /**
     * Detects if a ticket is associated with
     * the current context and clears the context.
     * @param request The servlet request.
     * @param response The servlet response.
     * @param chain The filter chain.
     * @throws IOException - if something is wrong this exception is thrown.
     * @throws ServletException - if something is wrong this exception is thrown.
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                            throws IOException, ServletException {
        
        
        SecurityContext sc = SecurityContextHolder.getContext();
        if (sc.getAuthentication()!=null && sc.getAuthentication() instanceof TicketAuthenticationToken) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("found ticket authentication clearing...");
            }
            SecurityContextHolder.setContext(new SecurityContextImpl());
        }
        
        chain.doFilter(request, response);
    }

    /**
     * Does nothing - we use IoC lifecycle methods instead
     */
    public void destroy() {
    }

}
