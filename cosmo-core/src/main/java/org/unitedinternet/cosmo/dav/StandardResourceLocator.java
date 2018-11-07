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
package org.unitedinternet.cosmo.dav;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.util.PathUtil;

/**
 * Standard implementation of {@link DavResourceLocator}.
 *
 * @see DavResourceLocator
 */
public class StandardResourceLocator implements DavResourceLocator {
    

    private URL context;
    private String path;
    private StandardResourceLocatorFactory factory;

    /**
     * @param context the URL specifying protocol, authority and unescaped
     * base path
     * @param path the unescaped dav-relative path of the resource
     * @param factory the locator factory
     */
    public StandardResourceLocator(URL context,
                                   String path,
                                   StandardResourceLocatorFactory factory) {
        this.context = context;
        this.path = path.endsWith("/") && ! path.equals("/") ?
            path.substring(0, path.length()-1) : path;
        this.factory = factory;
    }

    // DavResourceLocator methods

    public String getHref(boolean isCollection) {
        return getHref(false, isCollection);
    }

    public String getHref(boolean absolute,
                          boolean isCollection) {
        try {
            return buildHref(context, isCollection, absolute);
        } catch (Exception e) {
            throw new CosmoException(e);
        }
    }

    public URL getUrl(boolean isCollection) {
        try {
            return new URL(getHref(isCollection));
        } catch (Exception e) {
            throw new CosmoException(e);
        }
    }

    public URL getUrl(boolean absolute,
                      boolean isCollection) {
    try {
        return new URL(getHref(absolute, isCollection));
    } catch (Exception e) {
        throw new CosmoException(e);
    }
    }

    public String getPrefix() {
        return context.getProtocol() + "://" + context.getAuthority();
    }

    public String getBasePath() {
        return context.getPath();
    }

    public String getBaseHref() {
        return getBaseHref(false);
    }

    public String getBaseHref(boolean absolute) {
        try {
            if (absolute) {
                return context.toURI().toASCIIString();
            }
            return new URI(null, null, context.getPath(), null).
                toASCIIString();
        } catch (Exception e) {
            throw new CosmoException(e);
        }
    }

    public String getPath() {
        return path;
    }

    public URL getContext() {
        return context;
    }

    public DavResourceLocator getParentLocator() {
        return factory.createResourceLocatorByPath(context,
                                             PathUtil.getParentPath(path));
    }

    public DavResourceLocatorFactory getFactory() {
        return factory;
    }

    // our methods

    public int hashCode() {
        return context.toExternalForm().hashCode() + path.hashCode();
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (! (o instanceof StandardResourceLocator)) {
            return false;
        }
        StandardResourceLocator other = (StandardResourceLocator) o;
        return other.hashCode() == hashCode();
    }
    
    private String buildHref(URL context, boolean isCollection, boolean absolute) throws URISyntaxException {
        String protocol = context.getProtocol();
        String host = context.getHost();
        String path = isCollection && ! this.path.equals("/") ? this.path  + "/" : this.path;
        path = context.getPath() + path;
        int port = context.getPort();
        StringBuilder sb = new StringBuilder();
        
        if (protocol != null && absolute) {
            sb.append(protocol);
            sb.append(':');
        }
        
        if (host != null && absolute) {
            sb.append("//");
            
            boolean needBrackets = ((host.indexOf(':') >= 0)
                                && !host.startsWith("[")
                                && !host.endsWith("]"));
            if (needBrackets) {
                sb.append('[');
            }
            
            sb.append(host);
            
            if (needBrackets){ 
                sb.append(']');
            }
            
            if (port != -1) {
                sb.append(':');
                sb.append(port);
            }
            
        }
        
        if (path != null){
            sb.append(path);
        }
        
        if(isCollection && sb.charAt(sb.length()-1) != '/'){
            sb.append("/");
        }
        return sb.toString();
    }
}
