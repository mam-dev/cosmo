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
package org.unitedinternet.cosmo.model;

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
public interface Attribute extends AuditableObject {

    public QName getQName();

    public void setQName(QName qname);

    /**
     * For backwards compatability.  Return the local name.
     * @return local name of attribute
     */
    public String getName();

    /**
     * @return Item attribute belongs to
     */
    public Item getItem();

    /**
     * @param item
     *            Item attribute belongs to
     */
    public void setItem(Item item);

    /**
     * @return the attribute value
     */
    public Object getValue();

    /**
     * @param value
     *            the attribute value
     */
    public void setValue(Object value);

    /**
     * Return a new instance of Attribute containing a copy of the Attribute
     * 
     * @return copy of Attribute
     */
    public Attribute copy();

}