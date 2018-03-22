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
package org.unitedinternet.cosmo.model.mock;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.unitedinternet.cosmo.model.QName;

/**
 * Represents a qualified name. A qualified name 
 * consists of a namespace and a local name.
 * 
 * Attribtues are indexed by a qualified name.
 */
public class MockQName implements QName {
    
    public static final String DEFAULT_NAMESPACE = "org.unitedinternet.cosmo.default";
    
    private String namespace = null;
    private String localName = null;
    
    /**
     * Constructor.
     */
    public MockQName() {}
    
    /**
     * Create new QName with specified namespace and local name.
     * @param namespace namespace
     * @param localName local name
     */
    public MockQName(String namespace, String localName) {
        this.namespace = namespace;
        this.localName = localName;
    }
    
    /**
     * Create a new QName with the specified local name.  The namespace
     * is the fully qualified name of the specified class.
     * @param clazz class to generate namespace from
     * @param localName local name
     */
    public MockQName(@SuppressWarnings("rawtypes") Class clazz, String localName) {
        this(clazz.getName(), localName);
    }
    
    /**
     * Create new QName with default namespace and specified local name.
     * @param localName local name
     */
    public MockQName(String localName) {
        this(DEFAULT_NAMESPACE, localName);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceQName#getLocalName()
     */
    /**
     * Gets local name.
     * @return The local name.
     */
    public String getLocalName() {
        return localName;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceQName#setLocalName(java.lang.String)
     */
    /**
     * Sets local name.
     * @param localName The local name.
     */
    public void setLocalName(String localName) {
        this.localName = localName;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceQName#getNamespace()
     */
    /**
     * Gets namespace.
     * @return The namespace.
     */
    public String getNamespace() {
        return namespace;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceQName#setNamespace(java.lang.String)
     */
    /**
     * Sets namespace.
     * @param namespace The namespace.
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceQName#copy()
     */
    /**
     * Copy.
     * @return Name.
     */
    public QName copy() {
        return new MockQName(namespace, localName);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    /**
     * Hashcode.
     * @return The hashcode.
     */
    public int hashCode() {
        return new HashCodeBuilder(13, 27).
            append(namespace).append(localName).toHashCode();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    /**
     * Equals.
     * @param o The object.
     * @return The boolean equals.
     */
    public boolean equals(Object o) {
        if (! (o instanceof MockQName)) {
            return false;
        }
        MockQName it = (MockQName) o;
        return new EqualsBuilder().
            append(namespace, it.namespace).
            append(localName, it.localName).
            isEquals();
    }

    /**
     * String.
     * {@inheritDoc}
     * @return The string.
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{").
            append(namespace).
            append("}").
            append(localName);
        return buf.toString();
    }
}
