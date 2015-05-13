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
package org.unitedinternet.cosmo.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A filter that provides the X-Http-Override-Method header. If this header is present,
 * its value will be used as the HTTP method for the current request. This allows
 * HTTP clients with limited HTTP method support to make requests with any method.
 * 
 * @author travis
 *
 */
public class HttpOverrideFilter implements Filter {

    private static final Log LOG = LogFactory.getLog(HttpOverrideFilter.class);

    public void destroy() {
        // TODO Auto-generated method stub

    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        if (request instanceof HttpServletRequest && 
            ((HttpServletRequest) request).getHeader("X-Http-Method-Override") != null &&
            LOG.isDebugEnabled()){
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            	//Fix Log Forging - fortify
            	//Writing unvalidated user input to log files can allow an attacker to forge log
            	//entries or inject malicious content into the logs.
                LOG.debug("Overriding " + httpRequest.getMethod() + " with " 
                        + httpRequest.getHeader("X-Http-Method-Override"));

            chain.doFilter(new HttpOverrideRequestWrapper(httpRequest), 
                    response);
            return;
        }

        chain.doFilter(request, response);
    }


    public void init(FilterConfig arg0) throws ServletException {
        // TODO Auto-generated method stub

    }

    static class HttpOverrideRequestWrapper extends HttpServletRequestWrapper {

        private HttpOverrideRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getMethod() {
            String overrideMethod = this.getHeader("X-Http-Method-Override");
            if (overrideMethod != null){
                return overrideMethod;
            } else {
                return super.getMethod();
            }
        }



    }

}
