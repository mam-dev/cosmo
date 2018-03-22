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
package org.unitedinternet.cosmo.model.hibernate;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.unitedinternet.cosmo.model.QName;

/**
 * Hibernate persistent QName.
 */
@SuppressWarnings("serial")
@Embeddable
public class HibQName implements QName,Serializable {
    
    public static final String DEFAULT_NAMESPACE = "org.unitedinternet.cosmo.default";
    
    private String namespace = null;
    private String localName = null;
    
    public HibQName() {}
    
    /**
     * Create new QName with specified namespace and local name.
     * @param namespace namespace
     * @param localName local name
     */
    public HibQName(String namespace, String localName) {
        this.namespace = namespace;
        this.localName = localName;
    }
    
    /**
     * Create a new QName with the specified local name.  The namespace
     * is the fully qualified name of the specified class.
     * @param clazz class to generate namespace from
     * @param localName local name
     */
    public HibQName(Class<?> clazz, String localName) {
        this(clazz.getName(), localName);
    }
    
    /**
     * Create new QName with default namespace and specified local name.
     * @param localName local name
     */
    public HibQName(String localName) {
        this(DEFAULT_NAMESPACE, localName);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.QName#getLocalName()
     */
    public String getLocalName() {
        return localName;
    }
   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.QName#setLocalName(java.lang.String)
     */
    public void setLocalName(String localName) {
        this.localName = localName;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.QName#getNamespace()
     */
    public String getNamespace() {
        return namespace;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.QName#setNamespace(java.lang.String)
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.QName#copy()
     */
    public QName copy() {
        return new HibQName(namespace, localName);
    }
   
    public int hashCode() {
        return new HashCodeBuilder(13, 27).
            append(namespace).append(localName).toHashCode();
    }
  
    public boolean equals(Object o) {
        if (! (o instanceof HibQName)) {
            return false;
        }
        HibQName it = (HibQName) o;
        return new EqualsBuilder().
            append(namespace, it.namespace).
            append(localName, it.localName).
            isEquals();
    }

    /** */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{").
            append(namespace).
            append("}").
            append(localName);
        return buf.toString();
    }
}
