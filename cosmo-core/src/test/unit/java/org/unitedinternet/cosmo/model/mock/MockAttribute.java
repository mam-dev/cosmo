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

import org.unitedinternet.cosmo.model.Attribute;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.QName;

/**
 * Represents an attribute associated with an Item.
 * An attribute consists of a QName (qualified name)
 * and a value.  The QName is composed from a namespace
 * and a localname.  The QName and Item determine
 * attribute uniqueness.  This means for a given Item
 * and QName, there can be only one attribute.
 * 
 * There are many different types of attributes 
 * (String, Integer, Binary, Boolean, etc.)
 * 
 */
public abstract class MockAttribute extends MockAuditableObject implements java.io.Serializable, Attribute {

    // Fields
    private QName qname;
    
    private Item item;

    // Constructors
    /** default constructor */
    public MockAttribute() {
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceAttribute#getQName()
     */
    /**
     * Gets QName.
     * @return QName.
     */
    public QName getQName() {
        return qname;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceAttribute#setQName(org.unitedinternet.cosmo.model.copy.QName)
     */
    /**
     * Sets QName.
     * @param qname The name.
     */
    public void setQName(QName qname) {
        this.qname = qname;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceAttribute#getName()
     */
    /**
     * GetsName.
     * @return name.
     */
    public String getName() {
        if (qname==null) {
            return null;
        }
        
        return qname.getLocalName();
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceAttribute#getItem()
     */
    /**
     * Gets item.
     * @return item.
     */
    public Item getItem() {
        return item;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceAttribute#setItem(org.unitedinternet.cosmo.model.copy.Item)
     */
    /**
     * Sets item.
     * @param item The item.
     */
    public void setItem(Item item) {
        this.item = item;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceAttribute#getValue()
     */
    /**
     * GetsValue.
     * @return The object.
     */
    public abstract Object getValue();

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceAttribute#setValue(java.lang.Object)
     */
    /**
     * Sets value.
     * @param value The value.
     */
    public abstract void setValue(Object value);

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceAttribute#copy()
     */
    /**
     * Copy.
     * @return Attribute.
     */
    public abstract Attribute copy();
    
    /**
     * Return string representation
     * @return String.
     */
    public String toString() {
        Object value = getValue();
        if (value == null) {
            return "null";
        }
        return value.toString();
    }
    
    /**
     * Validate.
     */
    public abstract void validate();

}
