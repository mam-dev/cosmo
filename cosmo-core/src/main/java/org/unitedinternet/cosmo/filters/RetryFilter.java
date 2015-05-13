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
package org.unitedinternet.cosmo.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitedinternet.cosmo.server.ServerConstants;

/**
 * Filter that searches for known exception types (either caught
 * as runtime exceptions or stored in a request attribute), and 
 * retries the request a number of times before failing.  
 * 
 * The filter can be configured to watch any HTTP method (PUT,POST,DELETE, etc)
 * and search for any exception type and retry any number of times.
 * 
 * This filter is useful for catching runtime exceptions such as
 * database deadlocks and other concurrency issues and retrying the 
 * request a number of times.
 */
public class RetryFilter implements Filter, ServerConstants {
    
    private static final Log LOG = LogFactory.getLog(RetryFilter.class);
    private int maxRetries = 10;
    private int maxMemoryBuffer = 1024*256;
    private Class<?>[] exceptions = new Class[] {};
    private String[] methods = new String[] {"PUT", "POST", "DELETE"};

    public void destroy() {
        // nothing to do
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        
        // only care about certain methods
        if(isFilterMethod(request) &&
           response instanceof HttpServletResponse &&
           request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse)response;
            
            // Wrap request so we can buffer the request in the event
            // it needs to be retried.  If the request isn't buffered,
            // then we can't consume the servlet inputstream again.
            request = new BufferedRequestWrapper(httpRequest, maxMemoryBuffer);
            
            // Wrap response so we can trap any 500 responses before they
            // get to the client
            response = new ResponseErrorWrapper(httpResponse);
            
            int attempts = 0;

            while(attempts <= maxRetries) {
                
                Exception ex = null;
                
                try {
                    chain.doFilter(request, response);
                } catch (RuntimeException e) {
                    // Catch runtime exceptions
                    if(isFilterException(e)) {
                        ex = e;
                    } else {
                        LOG.error("the server encountered an unexpected error", e);
                        sendError((ResponseErrorWrapper) response);
                    }
                }
                
                // If we didn't catch it, then look for the exception
                // in the request attributes
                if(ex==null) {
                    ex = findFilterException(httpRequest);
                }
                
                // If there was an exception that we were looking for
                // (either caught or found in the request), then prepare
                // to retry.
                if (ex != null) {
                    attempts++;

                    // Fail after maxRetries attempts
                    if(attempts > maxRetries) {
                    	//Fix Log Forging - java fortify
                    	//Writing unvalidated user input to log files can allow an attacker
                    	//to forge log entries or inject malicious content into the logs.
                        LOG.error("reached maximum retries for "
                            + httpRequest.getMethod() + " "
                            + httpRequest.getRequestURI());
                        if(!((ResponseErrorWrapper) response).flushError()) {
                            sendError((ResponseErrorWrapper) response);
                        }
                    }
                    // Otherwise, prepare to retry
                    else {
                        LOG.warn("caught: " + ex.getMessage() + " : retrying request "
                            + httpRequest.getMethod()
                            + " " + httpRequest.getRequestURI() + " "
                            + attempts);
                        ((ResponseErrorWrapper) response).clearError();
                        ((BufferedRequestWrapper) request).retryRequest();
                        Thread.yield();
                    }
                } 
                // Otherwise flush the error if necessary and
                // proceed as normal.
                else {
                    ((ResponseErrorWrapper) response).flushError();
                    return;
                }
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isFilterMethod(ServletRequest request) {
        if(!(request instanceof HttpServletRequest)){
           return false; 
        }
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String method = httpRequest.getMethod();
        
        for (String filterMethod : methods) {
            if (filterMethod.equalsIgnoreCase(method)) {
                return true;
            }
        }

        return false;
    }
    
    private boolean isFilterException(Exception e) {
        for(Class<?> exception: exceptions) {
            if(exception.isInstance(e)) {
                return true;
            }
        }

        return false;
    }

    private Exception findFilterException(HttpServletRequest request) {
        Exception e = (Exception)
            request.getAttribute(ATTR_SERVICE_EXCEPTION);
        if (e == null) {
            return null;
        }
        if(isFilterException(e)) {
            // clear for retry
            request.removeAttribute(ATTR_SERVICE_EXCEPTION);
            return e;
        }
        return null;
    }

    private void sendError(ResponseErrorWrapper response) throws IOException {
        
        // if error was already queued, flush it
        if(response.flushError()) {
            return;
        }
        
        // otherwise send a generic error and flush
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "the server was unable to complete the request");
        response.flushError();
    }

    public void init(FilterConfig config) throws ServletException {
       // nothing to do
    }
    
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public void setMaxMemoryBuffer(int maxMemoryBuffer) {
        this.maxMemoryBuffer = maxMemoryBuffer;
    }

    public void setExceptions(Class[] exceptions) {
        this.exceptions = exceptions;
    }

    public void setMethods(String[] methods) {
        this.methods = methods;
    }

}
