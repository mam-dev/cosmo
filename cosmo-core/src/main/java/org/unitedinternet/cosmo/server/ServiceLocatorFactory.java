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

import javax.servlet.http.HttpServletRequest;

import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;

/**
 * This class produces instances of <code>ServiceLocator</code> that
 * can build URLs for services and collections as described in the
 * documentation for that class.
 * TODO remove unused protocols
 * @see ServiceLocator
 */
public class ServiceLocatorFactory {

    private static final Boolean ABSOLUTE_BY_DEFAULT = true;

    private String cmpPrefix;
    private String davPrefix;

    private CosmoSecurityManager securityManager;

    /**
     * Returns a <code>ServiceLocator</code> instance that returns
     * URLs based on the application mount URL calculated from
     * information in the given request.
     */
    public ServiceLocator createServiceLocator(HttpServletRequest request) {
        return createServiceLocator(request, ABSOLUTE_BY_DEFAULT);
    }
    
    /**
     * Returns a <code>ServiceLocator</code> instance that returns
     * relative URLs based on the application mount URL calculated from 
     * information in the given request.
     */
    public ServiceLocator createServiceLocator(HttpServletRequest request, 
                                               Boolean absoluteUrls) {
        Ticket ticket = securityManager.getSecurityContext().getTicket();
        return createServiceLocator(request, ticket == null ? null : ticket.getKey(), absoluteUrls);
    }

    /**
     * Returns a <code>ServiceLocator</code> instance that returns
     * URLs based on the application mount URL calculated from
     * information in the given request and including the given ticket.
     */
    public ServiceLocator createServiceLocator(HttpServletRequest request,
                                               Ticket ticket) {
        return createServiceLocator(request, ticket, ABSOLUTE_BY_DEFAULT);
    }
    
    public ServiceLocator createServiceLocator(HttpServletRequest request,
                                               String ticketKey){
        return createServiceLocator(request, ticketKey, ABSOLUTE_BY_DEFAULT);
    }
    
    /**
     * Returns a <code>ServiceLocator</code> instance that returns
     * relative URLs based on the application mount URL calculated from 
     * information in the given request and including the given ticket
     */
    public ServiceLocator createServiceLocator(HttpServletRequest request,
                                               String ticketKey,
                                               Boolean absoluteUrls){
        String appMountUrl = calculateAppMountUrl(request, absoluteUrls);
        return createServiceLocator(appMountUrl, ticketKey);
    }

    public ServiceLocator createServiceLocator(HttpServletRequest request,
                                               Ticket ticket,
                                               Boolean absoluteUrls) {
        String ticketKey = ticket != null ? ticket.getKey() : null;
        return createServiceLocator(request, ticketKey, absoluteUrls);
    }

    public ServiceLocator createServiceLocator(String appMountUrl) {
        return createServiceLocator(appMountUrl, null);
    }

    public ServiceLocator createServiceLocator(String appMountUrl,
                                               String ticketKey) {
        return new ServiceLocator(appMountUrl, ticketKey, this);
    }



    /** */
    public String getCmpPrefix() {
        return cmpPrefix;
    }

    /** */
    public void setCmpPrefix(String prefix) {
        cmpPrefix = prefix;
    }

    /** */
    public String getDavPrefix() {
        return davPrefix;
    }

    /** */
    public void setDavPrefix(String prefix) {
        davPrefix = prefix;
    }

  

    /** */
    public CosmoSecurityManager getSecurityManager() {
        return securityManager;
    }

    /** */
    public void setSecurityManager(CosmoSecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    /**
     * Initializes the factory, sanity checking required properties
     * and defaulting optional properties.
     */
    public void init() {
        if (cmpPrefix == null) {
            throw new IllegalStateException("cmpPrefix must not be null");
        }
        if (davPrefix == null) {
            throw new IllegalStateException("davPrefix must not be null");
        }
        if (securityManager == null) {
            throw new IllegalStateException("securityManager must not be null");
        }
    }

    private String calculateAppMountUrl(HttpServletRequest request, Boolean absoluteUrls) {
        StringBuilder buf = new StringBuilder();

        if (absoluteUrls){
            buf.append(request.getScheme()).
                append("://").
                append(request.getServerName());
            if ( request.getScheme().equalsIgnoreCase("https") && request.getServerPort() != 443 ||
                 request.getScheme().equalsIgnoreCase("http") && request.getServerPort() != 80) {
                
                buf.append(":").append(request.getServerPort() );
            }
        }
        if (! request.getContextPath().equals("/")) {
            buf.append(request.getContextPath());
        }
        return buf.toString();
    }
    
}
