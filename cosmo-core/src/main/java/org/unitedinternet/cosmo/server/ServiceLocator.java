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

import java.util.HashMap;
import java.util.Map;

import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.util.UriTemplate;

/**
 * This class encapsulates the addressing scheme for all client
 * services provided by Cosmo, those protocols and interfaces that
 * allow communication between clients and Cosmo.
 *
 * <h2>Service Addresses</h2>
 *
 * Each service is "mounted" in the server's URL-space at its own
 * prefix relative to the "application mount URL". This URL is
 * composed of the scheme, host, and port information (see RFC 1738),
 * followed by the context path of the Cosmo web application and the
 * servlet path of the protocol or interface.
 * <p>
 * For example, the URL <code>http://localhost:8080/cosmo/dav</code>
 * addresses the Cosmo WebDAV service.
 *
 * <h2>Collection Addresses</h2>
 *
 * Collections in the Cosmo database are addressed similarly
 * regardless of which service is used to access the data. See
 * {@link CollectionPath} for details on the makeup of collection
 * URLs.
 * <p>
 * Note that individual items contained within collections are not
 * addressable at this time.
 *
 * <h2>User Addresses</h2>
 *
 * Users are addressed similarly to collections. See
 * {@link UserPath} for details on the makeup of user URLs.
 *
 * @see ServiceLocatorFactory
 * @see CollectionPath
 * @see UserPath
 */
public class ServiceLocator implements ServerConstants {

    private static final String PATH_COLLECTION = "collection";
    private static final String PATH_ITEM = "item";
    private static final String PATH_USER = "user";

    private String appMountUrl;
    private String ticketKey;
    private ServiceLocatorFactory factory;

    /**
     * Returns a <code>ServiceLocator</code> instance that uses the
     * uses the given application mount URL as the base for all
     * service URLs.
     *
     * @param appMountUrl the application mount URL
     * @param factory the service location factory
     */
    public ServiceLocator(String appMountUrl,
                          ServiceLocatorFactory factory) {
        this(appMountUrl, null, factory);
    }

    /**
     * Returns a <code>ServiceLocator</code> instance that uses the
     * uses the given application mount URL as the base for and
     * includes the given ticket key in all service URLs.
     *
     * @param appMountUrl the application mount URL
     * @param factory the service location factory
     * @param ticketKey the ticket key
     */
    public ServiceLocator(String appMountUrl,
                          String ticketKey,
                          ServiceLocatorFactory factory) {
        this.appMountUrl = appMountUrl;
        this.ticketKey = ticketKey;
        this.factory = factory;
    }

    /**
     * Returns a map of base service URLs keyed by service id.
     */
    public Map<String,String> getBaseUrls() {
        HashMap<String,String> urls = new HashMap<String,String>();

        urls.put(SVC_DAV, getDavBase());
        urls.put(SVC_DAV_PRINCIPAL, getDavUserPrincipalUrl());
        return urls;
    }

    /**
     * Returns a map of URLs for the collection keyed by service id.
     */
    public Map<String,String> getCollectionUrls(CollectionItem collection) {
        HashMap<String,String> urls = new HashMap<String,String>();
        urls.put(SVC_DAV, getDavUrl(collection));
        return urls;
    }

    /**
     * Returns a map of URLs for the user keyed by service id.
     */
    public Map<String,String> getUserUrls(User user) {
        HashMap<String,String> urls = new HashMap<String,String>();
        urls.put(SVC_CMP, getCmpUrl(user));
        urls.put(SVC_DAV, getDavUrl(user));
        urls.put(SVC_DAV_PRINCIPAL, getDavPrincipalUrl(user));
        urls.put(SVC_DAV_CALENDAR_HOME, getDavCalendarHomeUrl(user));
        return urls;
    }



    /**
     * Returns the CMP URL of the user.
     */
    public String getCmpUrl(User user) {
        return calculateUserUrl(user, factory.getCmpPrefix());
    }

    /**
     * Returns the WebDAV base URL.
     */
    public String getDavBase() {
        return calculateBaseUrl(factory.getDavPrefix());
    }

    /**
     * Returns the WebDAV user principal collection URL.
     */
    public String getDavUserPrincipalUrl() {
        StringBuilder buf = new StringBuilder();
        buf.append(appMountUrl).append(factory.getDavPrefix()).
            append(org.unitedinternet.cosmo.dav.ExtendedDavConstants.
                   TEMPLATE_USERS.bind());
        return buf.toString();
    }

    /**
     * Returns the WebDAV URL of the item.
     */
    public String getDavUrl(Item item) {
        return calculateItemUrl(item, factory.getDavPrefix());
    }

    /**
     * Returns the WebDAV URL of the user.
     */
    public String getDavUrl(User user) {
         StringBuilder buf = new StringBuilder();
         buf.append(appMountUrl).append(factory.getDavPrefix()).
             append(org.unitedinternet.cosmo.dav.ExtendedDavConstants.
                    TEMPLATE_HOME.bind(user.getUsername()));
         return buf.toString();
    }

    /**
     * Returns the WebDAV principal URL of the user.
     */
    public String getDavPrincipalUrl(User user) {
         StringBuilder buf = new StringBuilder();
         buf.append(appMountUrl).append(factory.getDavPrefix()).
            append(org.unitedinternet.cosmo.dav.ExtendedDavConstants.
                    TEMPLATE_USER.bind(user.getUsername()));
         return buf.toString();
    }

    /**
     * Returns the CalDAV calendar home URL of the user.
     */
    public String getDavCalendarHomeUrl(User user) {
        StringBuilder buf = new StringBuilder();
        buf.append(appMountUrl).append(factory.getDavPrefix()).
            append(org.unitedinternet.cosmo.dav.ExtendedDavConstants.
                   TEMPLATE_HOME.bind(user.getUsername())).
                   append("/");
        return buf.toString();
    }


    public String getAppMountUrl() {
        return appMountUrl;
    }

    public String getTicketKey() {
        return ticketKey;
    }

    public ServiceLocatorFactory getFactory() {
        return factory;
    }

    private String calculateBaseUrl(String servicePrefix) {
        StringBuilder buf = new StringBuilder(appMountUrl);

        buf.append(servicePrefix).append("/");

        return buf.toString();
    }

    private String calculateItemUrl(Item item,
                                    String servicePrefix) {
        return calculateItemUrl(item, servicePrefix, true);
    }

    private String calculateItemUrl(Item item,
                                    String servicePrefix,
                                    boolean absolute) {
        String pathPrefix = item instanceof CollectionItem ?
            PATH_COLLECTION : PATH_ITEM;
        return calculateItemUrl(item.getUid(), pathPrefix, servicePrefix,
                                absolute);
    }

   

    private String calculateItemUrl(String uid,
                                    String pathPrefix,
                                    String servicePrefix,
                                    boolean absolute) {
        StringBuilder buf = new StringBuilder();

        if (absolute) {
            buf.append(appMountUrl).append(servicePrefix).append("/");
        }

        buf.append(pathPrefix).append("/").append(uid);

        if (ticketKey != null) {
            buf.append("?").append(PARAM_TICKET).append("=").append(ticketKey);
        }

        return buf.toString();
    }

    private String calculateUserUrl(User user,
                                    String servicePrefix) {
        return calculateUserUrl(user, servicePrefix, true);
    }

    private String calculateUserUrl(User user,
                                    String servicePrefix,
                                    boolean absolute) {
        StringBuilder buf = new StringBuilder();

        if (absolute) {
            buf.append(appMountUrl).append(servicePrefix).append("/");
        }

        buf.append(PATH_USER).append("/").
            append(UriTemplate.escapeSegment(user.getUsername()));

        return buf.toString();
    }

    
}
