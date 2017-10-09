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

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Represents an item on server.  All
 * content in cosmo extends from Item.
 */
public interface Item extends AuditableObject{

    /**
     * Return all stamps associated with Item.  Use
     * addStamp() and removeStamp() to manipulate set.
     * @return set of stamps associated with Item
     */
    public Set<Stamp> getStamps();

    /**
     * @return Map of Stamps indexed by Stamp type.
     */
    public Map<String, Stamp> getStampMap();

    /**
     * Add stamp to Item
     * @param stamp stamp to add
     */
    public void addStamp(Stamp stamp);

    /**
     * Remove stamp from Item.
     * @param stamp stamp to remove
     */
    public void removeStamp(Stamp stamp);

    /**
     * Get the stamp that corresponds to the specified type
     * @param type stamp type to return
     * @return stamp
     */
    public Stamp getStamp(String type);

    /**
     * Get the stamp that corresponds to the specified class
     * @param clazz class of stamp to return
     * @return stamp
     */
    public Stamp getStamp(Class<?> clazz);

    /**
     * Get all Attributes of Item.  Use addAttribute() and 
     * removeAttribute() to manipulate map.
     * @return
     */
    public Map<QName, Attribute> getAttributes();

    public void addTicket(Ticket ticket);

    public void removeTicket(Ticket ticket);

    public void addAttribute(Attribute attribute);

    /**
     * Remove attribute in default namespace with local name.
     * @param name local name of attribute to remove
     */
    public void removeAttribute(String name);

    /**
     * Remove attribute.
     * @param qname qualifed name of attribute to remove.
     */
    public void removeAttribute(QName qname);

    /**
     * Remove all attributes in a namespace.
     * @param namespace namespace of attributes to remove
     */
    public void removeAttributes(String namespace);

    /**
     * Get attribute in default namespace with local name.
     * @param name local name of attribute
     * @return attribute in default namespace with given name
     */
    public Attribute getAttribute(String name);

    /**
     * Get attribute with qualified name.
     * @param qname qualified name of attribute to retrieve
     * @return attribute with qualified name.
     */
    public Attribute getAttribute(QName qname);

    /**
     * Get attribute value with local name in default namespace
     * @param name local name of attribute
     * @return attribute value
     */
    public Object getAttributeValue(String name);

    /**
     * Get attribute value with qualified name
     * @param qname qualified name of attribute
     * @return attribute value
     */
    public Object getAttributeValue(QName qname);

    /**
     * Set attribute value of attribute with local name in default
     * namespace.
     * @param name local name of attribute
     * @param value value to update attribute
     */
    public void setAttribute(String name, Object value);

    /**
     * Set attribute value attribute with qualified name
     * @param key qualified name of attribute
     * @param value value to update attribute
     */
    public void setAttribute(QName key, Object value);

    /**
     * Return Attributes for a given namespace.  Attributes are returned
     * in a Map indexed by the name of the attribute.
     * @param namespace namespace of the Attributes to return
     * @return map of Attributes indexed by the name of the attribute
     */
    public Map<String, Attribute> getAttributes(String namespace);

    public Date getClientCreationDate();

    public void setClientCreationDate(Date clientCreationDate);

    public Date getClientModifiedDate();

    public void setClientModifiedDate(Date clientModifiedDate);

    public String getName();

    public void setName(String name);

    /**
     * @return Item's human readable name
     */
    public String getDisplayName();

    /**
     * @param displayName Item's human readable name
     */
    public void setDisplayName(String displayName);

    public User getOwner();

    public void setOwner(User owner);

    public String getUid();

    public void setUid(String uid);

    public Set<CollectionItem> getParents();
    
    /**
     * Each collection an item belongs to contains additional
     * attributes, and is represented as a CollectionItemDetails object.
     * @param parent parent collection
     * @return details about parent<-->child relationship
     */
    public CollectionItemDetails getParentDetails(CollectionItem parent);

    /**
     * Gets the parent of this item. Note that usually there is only one parent.
     */
    public CollectionItem getParent();

    /**
     * Transient attribute used to mark item for deletion.
     * @return true if item should be deleted
     */
    public Boolean getIsActive();

    /**
     * Transient attribute used to mark item for deletion.
     * @param isActive true if item should be deleted
     */
    public void setIsActive(Boolean isActive);

    /**
     * Get all Tickets on Item.  
     * @return set of tickets
     */
    public Set<Ticket> getTickets();

    public Set<Tombstone> getTombstones();

    public Item copy();

}