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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * An unclassified WebDAV Exception.
 */
public class CosmoDavException extends org.apache.jackrabbit.webdav.DavException
    implements ExtendedDavConstants {

    private static final long serialVersionUID = 2980452139790396998L;
    private transient DavNamespaceContext nsc;

    public CosmoDavException(int code) {
        this(code, null, null);
    }

    public CosmoDavException(String message) {
        this(500, message, null);
    }

    public CosmoDavException(int code,
                        String message) {
        this(code, message, null);
    }

    public CosmoDavException(org.apache.jackrabbit.webdav.DavException e) {
        this(e.getErrorCode(), e.getMessage(), e);
    }

    public CosmoDavException(Throwable t) {
        this(500, t.getMessage(), t);
    }

    public CosmoDavException(int code,
                        String message,
                        Throwable t) {
        super(code, message, t, null);
        nsc = new DavNamespaceContext();
    }

    public boolean hasContent() {
        return true;
    }

    public DavNamespaceContext getNamespaceContext() {
        return nsc;
    }

    public void writeTo(XMLStreamWriter writer)
        throws XMLStreamException {
        writer.setNamespaceContext(nsc);
        writer.writeStartElement("DAV:", "error");
        for (String uri : nsc.getNamespaceURIs()) {
            writer.writeNamespace(nsc.getPrefix(uri), uri);
        }
        writeContent(writer);
        writer.writeEndElement();
    }

    protected void writeContent(XMLStreamWriter writer)
        throws XMLStreamException {
        writer.writeStartElement(NS_COSMO, "internal-server-error");
        if (getMessage() != null) {
            writer.writeCharacters(getMessage());
        }
        writer.writeEndElement();
    }

    public static class DavNamespaceContext implements NamespaceContext {
        private HashMap<String,String> uris;
        private HashMap<String,HashSet<String>> prefixes;

        /**
         * Constructor.
         */
        public DavNamespaceContext() {
            uris = new HashMap<String,String>();
            uris.put("D", "DAV:");
            uris.put(PRE_COSMO, NS_COSMO);

            prefixes = new HashMap<String,HashSet<String>>();

            HashSet<String> dav = new HashSet<String>(1);
            dav.add("D");
            prefixes.put("DAV:", dav);

            HashSet<String> cosmo = new HashSet<String>(1);
            cosmo.add(PRE_COSMO);
            prefixes.put(NS_COSMO, cosmo);
        }

        // NamespaceContext methods

        public String getNamespaceURI(String prefix) {
            return uris.get(prefix);
        }

        public String getPrefix(String namespaceURI) {
            if (prefixes.get(namespaceURI) == null) {
                return null;
            }
            return prefixes.get(namespaceURI).iterator().next();
        }

        public Iterator<String> getPrefixes(String namespaceURI) {
            if (prefixes.get(namespaceURI) == null) {
                return null;
            }
            return prefixes.get(namespaceURI).iterator();
        }

        // our methods

        public Set<String> getNamespaceURIs() {
            return prefixes.keySet();
        }

        public void addNamespace(String prefix, String namespaceURI) {
            uris.put(prefix, namespaceURI);

            HashSet<String> ns = new HashSet<String>(1);
            ns.add(prefix);
            prefixes.put(namespaceURI, ns);
        }
    }
}
