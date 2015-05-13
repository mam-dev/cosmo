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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.unitedinternet.cosmo.model.Attribute;
import org.unitedinternet.cosmo.model.AttributeTombstone;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionItemDetails;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.QName;
import org.unitedinternet.cosmo.model.Stamp;
import org.unitedinternet.cosmo.model.StampTombstone;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.Tombstone;
import org.unitedinternet.cosmo.model.User;


/**
 * Abstract base class for an item on server.  All
 * content in cosmo extends from Item.
 */
public abstract class MockItem extends MockAuditableObject implements Item {

   
    private String uid;
    
   
    private String name;
    
    
    private String displayName;
    
    
    private Date clientCreationDate;
    
    
    private Date clientModifiedDate;
    
    private Integer version = 0;
    
    private transient Boolean isActive = Boolean.TRUE;
    
    
    private Map<QName, Attribute> attributes = new HashMap<QName, Attribute>(0);
    
    
    private Set<Ticket> tickets = new HashSet<Ticket>(0);
    
    
    private Set<Stamp> stamps = new HashSet<Stamp>(0);
    
    
    private Set<Tombstone> tombstones = new HashSet<Tombstone>(0);
    
    private transient Map<String, Stamp> stampMap = null;
    
    private Set<CollectionItemDetails> parentDetails = new HashSet<CollectionItemDetails>(0);
    
    private User owner;
  
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#getStamps()
     */
    /**
     * Gets stamps.
     * @return The stamps.
     */
    public Set<Stamp> getStamps() {
        return Collections.unmodifiableSet(stamps);
    }
    
    
    /**
     * Sets version.
     * @param version The version.
     */
    public void setVersion(Integer version) {
        this.version = version;
    }



    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#getStampMap()
     */
    /**
     * Gets stamp map.
     * @return The stamp map.
     */
    public Map<String, Stamp> getStampMap() {
        if (stampMap==null) {
            stampMap = new HashMap<String, Stamp>();
            for (Stamp stamp : stamps) {
                stampMap.put(stamp.getType(), stamp);
            }
        }
        
        return stampMap;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#addStamp(org.unitedinternet.cosmo.model.copy.Stamp)
     */
    /**
     * Adds stamp.
     * @param stamp The stamp.
     */
    public void addStamp(Stamp stamp) {
        if (stamp == null) {
            throw new IllegalArgumentException("stamp cannot be null");
        }

        // remove old tombstone if exists
        for(Iterator<Tombstone> it=tombstones.iterator();it.hasNext();) {
            Tombstone ts = it.next();
            if(ts instanceof StampTombstone) {
                if(((StampTombstone) ts).getStampType().equals(stamp.getType())) {
                    it.remove();
                }
            }
        }
        
        stamp.setItem(this);
        stamps.add(stamp);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#removeStamp(org.unitedinternet.cosmo.model.copy.Stamp)
     */
    /**
     * Removes stamp.
     * @param stamp The stamp.
     */
    public void removeStamp(Stamp stamp) {
        // only remove stamps that belong to item
        if (!stamps.contains(stamp)) {
            return;
        }
        
        stamps.remove(stamp);
        
        // add tombstone for tracking purposes
        tombstones.add(new MockStampTombstone(this, stamp));
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#getStamp(java.lang.String)
     */
    /**
     * Gets stamp.
     * @param type The type.
     * @return stamp.
     */
    public Stamp getStamp(String type) {
        for(Stamp stamp : stamps) {
            // only return stamp if it matches class and is active
            if(stamp.getType().equals(type)) {
                return stamp;
            }
        }
        
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#getStamp(java.lang.Class)
     */
    /**
     * Gets stamp.
     * @param clazz The class.
     * @return The stamp.
     */
    public Stamp getStamp(@SuppressWarnings("rawtypes") Class clazz) {
        for (Stamp stamp : stamps) {
            // only return stamp if it is an instance of the specified class
            if(clazz.isInstance(stamp)) {
                return stamp;
            }
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#getAttributes()
     */
    /**
     * Gets attributes.
     * @return The attributes.
     */
    public Map<QName, Attribute> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#addTicket(org.unitedinternet.cosmo.model.copy.Ticket)
     */
    /**
     * Adds ticket.
     * @param ticket The ticket.
     */
    public void addTicket(Ticket ticket) {
        ticket.setItem(this);
        tickets.add(ticket);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#removeTicket(org.unitedinternet.cosmo.model.copy.Ticket)
     */
    /**
     * Removes ticket.
     * @param ticket The ticket.
     */
    public void removeTicket(Ticket ticket) {
        tickets.remove(ticket);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#addAttribute(org.unitedinternet.cosmo.model.copy.Attribute)
     */
    /**
     * Adds attribute.
     * @param attribute The attribute.
     */
    public void addAttribute(Attribute attribute) {
        if (attribute == null) {
            throw new IllegalArgumentException("attribute cannot be null");
        }

        // remove old tombstone if exists
        for(Iterator<Tombstone> it=tombstones.iterator();it.hasNext();) {
            Tombstone ts = it.next();
            if (ts instanceof AttributeTombstone) {
                if(((AttributeTombstone) ts).getQName().equals(attribute.getQName())) {
                    it.remove();
                }
            }
        }
        
        ((MockAttribute) attribute).validate();
        attribute.setItem(this);
        attributes.put(attribute.getQName(), attribute);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#removeAttribute(java.lang.String)
     */
    /**
     * Removes attribute.
     * @param name The name.
     */
    public void removeAttribute(String name) {
       removeAttribute(new MockQName(name));
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#removeAttribute(org.unitedinternet.cosmo.model.copy.QName)
     */
    /**
     * Removes attribute.
     * @param qname The qname of the attribute.
     */
    public void removeAttribute(QName qname) {
        if(attributes.containsKey(qname)) {
            attributes.remove(qname);
            tombstones.add(new MockAttributeTombstone(this, qname));
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#removeAttributes(java.lang.String)
     */
    /**
     * Removes attributes.
     * @param namespace The namespace.
     */
    public void removeAttributes(String namespace) {
        ArrayList<QName> toRemove = new ArrayList<QName>();
        for (QName qname: attributes.keySet()) {
            if (qname.getNamespace().equals(namespace)) {
                toRemove.add(qname);
            }
        }
        
        for(QName qname: toRemove) {
            removeAttribute(qname);
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#getAttribute(java.lang.String)
     */
    /**
     * Gets attribute.
     * @param name The name.
     * @return The attribute.
     */
    public Attribute getAttribute(String name) {
        return getAttribute(new MockQName(name));
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#getAttribute(org.unitedinternet.cosmo.model.copy.QName)
     */
    /**
     * Gets attribute.
     * @param qname The qname of the attribute.
     * @return The attribute.
     */
    public Attribute getAttribute(QName qname) {
        return attributes.get(qname);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#getAttributeValue(java.lang.String)
     */
    /**
     * Gets attribute value.
     * @param name The name.
     * @return The attribute value.
     */
    public Object getAttributeValue(String name) {
       return getAttributeValue(new MockQName(name));
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#getAttributeValue(org.unitedinternet.cosmo.model.copy.QName)
     */
    /**
     * Gets attribute value.
     * @param qname The name of the attribute.
     * @return The attribute value.
     */
    public Object getAttributeValue(QName qname) {
        Attribute attr = attributes.get(qname);
        if (attr == null) {
            return attr;
        }
        return attr.getValue();
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#setAttribute(java.lang.String, java.lang.Object)
     */
    /**
     * Sets attribute.
     * @param name The name.
     * @param value The value.
     */
    public void setAttribute(String name, Object value) {
        setAttribute(new MockQName(name),value);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#setAttribute(org.unitedinternet.cosmo.model.QName, java.lang.Object)
     */
    /**
     * Sets attribute.
     * @param key The key.
     * @param value The value.
     */
    public void setAttribute(QName key, Object value) {
        MockAttribute attr = (MockAttribute) attributes.get(key);
    
        if(attr!=null) {
            attr.setValue(value);
            attr.validate();
        }
        else {
           throw new IllegalArgumentException("attribute " + key + " not found");
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#getAttributes(java.lang.String)
     */
    /**
     * Gets attributes.
     * @param namespace The namespace.
     * @return The attributes.
     */
    public Map<String, Attribute> getAttributes(String namespace) {
        HashMap<String, Attribute> attrs = new HashMap<String, Attribute>();
        for(Entry<QName, Attribute> e: attributes.entrySet()) {
            if(e.getKey().getNamespace().equals(namespace)) {
                attrs.put(e.getKey().getLocalName(), e.getValue());
            }
        }
        
        return attrs;
    }
    

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#getClientCreationDate()
     */
    /**
     * Gets client creation date.
     * @return The date.
     */
    public Date getClientCreationDate() {
        return clientCreationDate;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#setClientCreationDate(java.util.Date)
     */
    /**
     * Sets client creation date.
     * @param clientCreationDate The client creation date.
     */
    public void setClientCreationDate(Date clientCreationDate) {
        this.clientCreationDate = clientCreationDate;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#getClientModifiedDate()
     */
    /**
     * Gets client modified date.
     * @return The date.
     */
    public Date getClientModifiedDate() {
        return clientModifiedDate;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#setClientModifiedDate(java.util.Date)
     */
    /**
     * Sets client modified date.
     * @param clientModifiedDate The client modified date.
     */
    public void setClientModifiedDate(Date clientModifiedDate) {
        this.clientModifiedDate = clientModifiedDate;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#getName()
     */
    /**
     * Gets name.
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#setName(java.lang.String)
     */
    /**
     * Sets name.
     * @param name The name.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#getDisplayName()
     */
    /**
     * Gets display name.
     * @return The display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#setDisplayName(java.lang.String)
     */
    /**
     * Sets display name.
     * @param displayName The display name.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#getOwner()
     */
    /**
     * Gets owner.
     * @return The user.
     */
    public User getOwner() {
        return owner;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#setOwner(org.unitedinternet.cosmo.model.copy.User)
     */
    /**
     * Sets owner.
     * @param owner The owner.
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#getUid()
     */
    /**
     * Gets uid.
     * @return The uid.
     */
    public String getUid() {
        return uid;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#setUid(java.lang.String)
     */
    /**
     * Sets uid.
     * @param uid The uid.
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#getVersion()
     */
    /**
     * Gets version.
     * @return The version number.
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * Adds parent.
     * @param parent The collection item.
     */
    public void addParent(CollectionItem parent) {
        MockCollectionItemDetails cid = new MockCollectionItemDetails(parent, this);
        parentDetails.add(cid);
    }

    /**
     * Removes parent.
     * @param parent The colelction item.
     */
    public void removeParent(CollectionItem parent) {
        CollectionItemDetails cid = getParentDetails(parent);
        if (cid!=null) {
            parentDetails.remove(cid);
        }
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getParents()
     */
    /**
     * Gets parents.
     * @return The parents.
     */
    public Set<CollectionItem> getParents() {
        
        Set<CollectionItem> parents = new HashSet<CollectionItem>();
        for (CollectionItemDetails cid: parentDetails) {
            parents.add(cid.getCollection());
        }
        
        return Collections.unmodifiableSet(parents);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getParent()
     */
    /**
     * Gets parent.
     * @return The collection item.
     */
    public CollectionItem getParent() {
        if (getParents().size() == 0) {
            return null;
        }
        
        return getParents().iterator().next();
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getParentDetails(org.unitedinternet.cosmo.model.CollectionItem)
     */
    /**
     * Gets parent details.
     * @param parent The collection item.
     * @return The collection item details.
     */
    public CollectionItemDetails getParentDetails(CollectionItem parent) {
        for(CollectionItemDetails cid: parentDetails) {
            if(cid.getCollection().equals(parent)) {
                return cid;
            }
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#getIsActive()
     */
    /**
     * Gets is active.
     * @return If it is active or not.
     */
    public Boolean getIsActive() {
        return isActive;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#setIsActive(java.lang.Boolean)
     */
    /**
     * Sets is active.
     * @param isActive Is active.
     */
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#getTickets()
     */
    /**
     * Gets tickets.
     * @return The tickets.
     */
    public Set<Ticket> getTickets() {
        return tickets;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#getTombstones()
     */
    /**
     * Gets tombstones.
     * @return the tombstones.
     */
    public Set<Tombstone> getTombstones() {
        return tombstones;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#addTombstone(org.unitedinternet.cosmo.model.copy.Tombstone)
     */
    /**
     * Adds tombstone.
     * @param tombstone The tombstone.
     */
    public void addTombstone(Tombstone tombstone) {
        tombstone.setItem(this);
        tombstones.add(tombstone);
    }
    
    
    /**
     * Item uid determines equality
     * @param obj The object.
     * @return The boolean. 
     */
    @Override
    public boolean equals(Object obj) {
        if (obj==null || uid==null) {
            return false;
        }
        if ( ! (obj instanceof Item)) {
            return false;
        }
        
        return uid.equals(((Item) obj).getUid());
    }

    @Override
    public int hashCode() {
        if (uid==null) {
            return super.hashCode();
        }
        else {
            return uid.hashCode();
        }
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceItem#copy()
     */
    /**
     * Copy.
     * @return The item.
     */
    public abstract Item copy();
   
    /**
     * Calculates entity tag.
     * {@inheritDoc}
     * @return The entity tag.
     */
    @Override
    public String calculateEntityTag() {
        String uid = getUid() != null ? getUid() : "-";
        String modTime = getModifiedDate() != null ?
            Long.valueOf(getModifiedDate().getTime()).toString() : "-";
        String etag = uid + ":" + modTime;
        return encodeEntityTag(etag.getBytes());
    }

    /**
     * Copy to item.
     * @param item The item.
     */
    protected void copyToItem(Item item) {
        item.setOwner(getOwner());
        item.setName(getName());
        item.setDisplayName(getDisplayName());
        
        // copy attributes
        for (Entry<QName, Attribute> entry: attributes.entrySet()) {
            item.addAttribute(entry.getValue().copy());
        }
        
        // copy stamps
        for (Stamp stamp: stamps) {
            item.addStamp(stamp.copy());
        }
    }
}
