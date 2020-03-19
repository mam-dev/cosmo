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
package org.unitedinternet.cosmo.filters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.icalendar.ICalendarClientFilterManager;

/**
 * A filter used to initialize the client identifier for
 * the ICalendarClientFilterManager, which is responsible for
 * exporting icalendar tailored to a specific client.
 * 
 * The filter relies on a map of regex expression keys that
 * map to a client identifier key.
 */
public class ClientICalendarFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(ClientICalendarFilter.class);

    private ICalendarClientFilterManager filterManager;
    private Map<String, String> clientKeyMap = new HashMap<String, String>();
    
   
    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        // translate User-Agent to client identifier key
        String userAgent = translateUserAgent(request);
        
        try {
            if(LOG.isDebugEnabled()) { 
                LOG.debug("Setting client to: {}", userAgent);
            }
            filterManager.setClient(userAgent);
            chain.doFilter(request, response);
        } finally {
            filterManager.setClient(null);
        }
        
    }
    
    private String translateUserAgent(ServletRequest request) {
        if( ! (request instanceof HttpServletRequest) 
            || ((HttpServletRequest)request).getHeader("User-Agent") == null) {
            return null;
        }
        
        String agent = ((HttpServletRequest)request).getHeader("User-Agent");
        
        // Translate User-Agent header into client key by finding match using rules in clientKeyMap.
        for(Entry<String, String> entry :clientKeyMap.entrySet()) {
            if(agent.matches(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        return agent;
    }
    
    public void setClientKeyMap(Map<String, String> clientKeyMap) {
        this.clientKeyMap = clientKeyMap;
    }
    
    public void setFilterManager(ICalendarClientFilterManager filterManager) {
        this.filterManager = filterManager;
    }
    
    public void init(FilterConfig arg0) throws ServletException {

    }
}
