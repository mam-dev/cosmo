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
package org.unitedinternet.cosmo.acegisecurity.context;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

/**
 * Ensures that the security context for an HTTP request is not
 * retained for subsequent requests.
 * <p>
 * Associates a fresh <code>SecurityContext</code> with the HTTP
 * request and clears it when the response has been sent. This is
 * useful for stateless protocols like WebDAV that do not understand
 * HTTP sessions.
 * <p>
 * The created <code>SecurityContext</code> is an instance of the
 * class defined by the {@link #setContext(Class)} method (which
 * defaults to SecurityContextImpl).
 * <p>
 * This filter will only execute once per request, to resolve servlet
 * container (specifically Weblogic) incompatibilities.
 * <p>
 * This filter MUST be executed BEFORE any authentication processing
 * mechanisms (eg BASIC, CAS processing filters etc), which expect the
 * <code>SecurityContextHolder</code> to contain a valid
 * <code>SecurityContext</code> by the time they execute.
 *
 * @see SecurityContext
 * @see SecurityContextImpl
 * @see  SecurityContextHolder
 */
public class HttpRequestContextIntegrationFilter
    implements InitializingBean, Filter {
    
    private static final Logger LOG = LoggerFactory.getLogger(HttpRequestContextIntegrationFilter.class);
        
    private static final String FILTER_APPLIED =
        "__acegi_request_integration_filter_applied";

    private Class<?> context = SecurityContextImpl.class;

    /**
     * Sets context.
     * @param secureContext The secure context.
     */
    public void setContext(Class<?> secureContext) {
        this.context = secureContext;
    }

    /**
     * Gets context.
     * @return The context.
     */
    public Class<?> getContext() {
        return context;
    }

    // InitializingBean methods
    /**
     * After properties.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public void afterPropertiesSet() throws Exception {
        if (context == null ||
            ! SecurityContext.class.isAssignableFrom(context)) {
            throw new IllegalArgumentException("context must be defined and implement SecurityContext "
                    + "(typically use org.acegisecurity.context.SecurityContextImpl; existing class is "
                    + context + ")");
        }
    }

    // Filter methods

    /**
     * Destroy.
     */
    public void destroy() {
    }

    /**
     * Generates a new security context, continues the filter chain,
     * then clears the context by generating another new one.
     *
     * @param request the servlet request
     * @param response the servlet response
     * @param chain the filter chain
     * @throws IOException if an I/O error occurs
     * @throws ServletException if any other error occurs
     */
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
        throws IOException, ServletException {
        if (request.getAttribute(FILTER_APPLIED) != null) {
            // ensure that filter is applied only once per request
            chain.doFilter(request, response);
            return;
        }

        request.setAttribute(FILTER_APPLIED, Boolean.TRUE);

        if (LOG.isDebugEnabled()) {
            LOG.debug("New SecurityContext instance associated with SecurityContextHolder");
        }
        SecurityContextHolder.setContext(generateNewContext());

        try {
            chain.doFilter(request, response);
        } catch (IOException ioe) {
            throw ioe;
        } catch (ServletException se) {
            throw se;
        } finally {
            // do clean up, even if there was an exception
            SecurityContextHolder.clearContext();
            if (LOG.isDebugEnabled()) {
                LOG.debug("SecurityContextHolder refreshed, as request processing completed");
            }
        }
    }

    /**
     * Init.
     * {@inheritDoc}
     * @param filterConfig filter config.
     */
    public void init(FilterConfig filterConfig)
        throws ServletException {
    }

    // our methods

    /**
     * Returns a new instance of <code>SecurityContext</code> as
     * specified by this class's context class.
     *
     * @return the new context instance
     * @throws ServletException if an error occurs
     */
    public SecurityContext generateNewContext() throws ServletException {
        try {
            return (SecurityContext) context.newInstance();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
