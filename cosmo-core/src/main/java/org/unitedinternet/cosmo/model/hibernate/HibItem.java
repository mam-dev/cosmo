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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyClass;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Index;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
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
 * Hibernate persistent Item.
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)


@Table(name = "item",
        indexes={@Index(name = "idx_itemtype",columnList = "itemtype" ),
                 @Index(name = "idx_itemuid",columnList = "uid" ),
                 @Index(name = "idx_itemname",columnList = "itemname" ),
        }
)
@DiscriminatorColumn(
        name="itemtype",
        discriminatorType=DiscriminatorType.STRING,
        length=16)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class HibItem extends HibAuditableObject implements Item {

    @Column(name = "uid", nullable = false, length=255)
    @NotNull
    @Length(min=1, max=255)
    @NaturalId
    private String uid;

    @Column(name = "itemname", nullable = false, length=255)
    @NotNull
    @Length(min=1, max=255)
    private String name;

    @Column(name = "displayname", length=1024)
    private String displayName;

    @Column(name = "clientcreatedate")
    @Type(type="long_timestamp")
    private Date clientCreationDate;

    @Column(name = "clientmodifieddate")
    @Type(type="long_timestamp")
    private Date clientModifiedDate;

    @Version
    @Column(name="version", nullable = false)
    private Integer version;

    private transient Boolean isActive = Boolean.TRUE;

    @OneToMany(targetEntity=HibAttribute.class, mappedBy = "item", 
            fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
    @MapKeyClass(HibQName.class)
    // turns out this creates a query that is unoptimized for MySQL
    //@Fetch(FetchMode.SUBSELECT)
    @BatchSize(size=50)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Map<QName, Attribute> attributes = new HashMap<QName, Attribute>(0);

    @OneToMany(targetEntity=HibTicket.class, mappedBy = "item", 
            fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<Ticket> tickets = new HashSet<Ticket>(0);

    // turns out this creates a query that is unoptimized for MySQL
    //@Fetch(FetchMode.SUBSELECT)
    @OneToMany(targetEntity=HibStamp.class, mappedBy = "item", 
            fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
    @BatchSize(size=50)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<Stamp> stamps = new HashSet<Stamp>(0);

    @OneToMany(targetEntity=HibTombstone.class, mappedBy="item", 
            fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
    protected Set<Tombstone> tombstones = new HashSet<Tombstone>(0);

    private transient Map<String, Stamp> stampMap = null;

    @OneToMany(targetEntity=HibCollectionItemDetails.class, mappedBy="primaryKey.item", 
            fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<CollectionItemDetails> parentDetails = new HashSet<CollectionItemDetails>(0);

    private transient Set<CollectionItem> parents = null;

    @ManyToOne(targetEntity=HibUser.class, fetch=FetchType.LAZY)
    @JoinColumn(name="ownerid", nullable = false)
    @NotNull
    private User owner;


    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getStamps()
     */
    public Set<Stamp> getStamps() {
        return Collections.unmodifiableSet(stamps);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getStampMap()
     */
    public Map<String, Stamp> getStampMap() {
        if(stampMap==null) {
            stampMap = new HashMap<String, Stamp>();
            for(Stamp stamp : stamps) {
                stampMap.put(stamp.getType(), stamp);
            }
        }

        return stampMap;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#addStamp(org.unitedinternet.cosmo.model.Stamp)
     */
    public void addStamp(Stamp stamp) {
        if (stamp == null) {
            throw new IllegalArgumentException("stamp cannot be null");
        }

        // remove old tombstone if exists
        for(Iterator<Tombstone> it=tombstones.iterator();it.hasNext();) {
            Tombstone ts = it.next();
            if(ts instanceof StampTombstone && ((StampTombstone) ts).getStampType().equals(stamp.getType())) {
                it.remove();
            }
        }

        stamp.setItem(this);
        stamps.add(stamp);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#removeStamp(org.unitedinternet.cosmo.model.Stamp)
     */
    public void removeStamp(Stamp stamp) {
        // only remove stamps that belong to item
        if(!stamps.contains(stamp)) {
            return;
        }

        stamps.remove(stamp);

        // add tombstone for tracking purposes
        tombstones.add(new HibStampTombstone(this, stamp));
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getStamp(java.lang.String)
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
     * @see org.unitedinternet.cosmo.model.Item#getStamp(java.lang.Class)
     */
    public Stamp getStamp(Class clazz) {
        for(Stamp stamp : stamps) {
            // only return stamp if it is an instance of the specified class
            if(clazz.isInstance(stamp)) {
                return stamp;
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getAttributes()
     */
    public Map<QName, Attribute> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#addTicket(org.unitedinternet.cosmo.model.Ticket)
     */
    public void addTicket(Ticket ticket) {
        ticket.setItem(this);
        tickets.add(ticket);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#removeTicket(org.unitedinternet.cosmo.model.Ticket)
     */
    public void removeTicket(Ticket ticket) {
        tickets.remove(ticket);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#addAttribute(org.unitedinternet.cosmo.model.Attribute)
     */
    public void addAttribute(Attribute attribute) {
        if (attribute == null) {
            throw new IllegalArgumentException("attribute cannot be null");
        }

        // remove old tombstone if exists
        for(Iterator<Tombstone> it=tombstones.iterator();it.hasNext();) {
            Tombstone ts = it.next();
            if(ts instanceof AttributeTombstone && 
               ((AttributeTombstone) ts).getQName().equals(attribute.getQName())) {
                it.remove();
            }
        }
        
        ((HibAttribute) attribute).validate();
        attribute.setItem(this);
        attributes.put(attribute.getQName(), attribute);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name) {
        removeAttribute(new HibQName(name));
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#removeAttribute(org.unitedinternet.cosmo.model.QName)
     */
    public void removeAttribute(QName qname) {
        if(attributes.containsKey(qname)) {
            attributes.remove(qname);
            tombstones.add(new HibAttributeTombstone(this, qname));
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#removeAttributes(java.lang.String)
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
     * @see org.unitedinternet.cosmo.model.Item#getAttribute(java.lang.String)
     */
    public Attribute getAttribute(String name) {
        return getAttribute(new HibQName(name));
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getAttribute(org.unitedinternet.cosmo.model.QName)
     */
    public Attribute getAttribute(QName qname) {
        return attributes.get(qname);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getAttributeValue(java.lang.String)
     */
    public Object getAttributeValue(String name) {
        return getAttributeValue(new HibQName(name));
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getAttributeValue(org.unitedinternet.cosmo.model.QName)
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
    public void setAttribute(String name, Object value) {
        setAttribute(new HibQName(name),value);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#setAttribute(org.unitedinternet.cosmo.model.QName, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void setAttribute(QName key, Object value) {
        HibAttribute attr = (HibAttribute) attributes.get(key);

        if(attr!=null) {
            attr.setValue(value);
            attr.validate();
        }
        else {
            throw new IllegalArgumentException("attribute " + key + " not found");
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getAttributes(java.lang.String)
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
     * @see org.unitedinternet.cosmo.model.Item#getClientCreationDate()
     */
    public Date getClientCreationDate() {
        return clientCreationDate;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#setClientCreationDate(java.util.Date)
     */
    public void setClientCreationDate(Date clientCreationDate) {
        this.clientCreationDate = clientCreationDate;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getClientModifiedDate()
     */
    public Date getClientModifiedDate() {
        return clientModifiedDate;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#setClientModifiedDate(java.util.Date)
     */
    public void setClientModifiedDate(Date clientModifiedDate) {
        this.clientModifiedDate = clientModifiedDate;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getName()
     */
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getDisplayName()
     */
    public String getDisplayName() {
        return displayName;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#setDisplayName(java.lang.String)
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getOwner()
     */
    public User getOwner() {
        return owner;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#setOwner(org.unitedinternet.cosmo.model.User)
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getUid()
     */
    public String getUid() {
        return uid;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#setUid(java.lang.String)
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getVersion()
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * @param parent collection to add item to
     */
    public void addParent(CollectionItem parent) {
        parentDetails.add(new HibCollectionItemDetails(parent,this));

        // clear cached parents
        parents = null;
    }

    public void removeParent(CollectionItem parent) {
        CollectionItemDetails cid = getParentDetails(parent);
        if(cid!=null) {
            parentDetails.remove(cid);
            // clear cached parents
            parents = null;
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getParents()
     */
    public Set<CollectionItem> getParents() {
        if(parents!=null) {
            return parents;
        }

        parents = new HashSet<CollectionItem>();
        for(CollectionItemDetails cid: parentDetails) {
            parents.add(cid.getCollection());
        }

        parents = Collections.unmodifiableSet(parents);

        return parents;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getParent()
     */
    public CollectionItem getParent() {
        if(getParents().size()==0) {
            return null;
        }

        return getParents().iterator().next();
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getParentDetails(org.unitedinternet.cosmo.model.CollectionItem)
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
     * @see org.unitedinternet.cosmo.model.Item#getIsActive()
     */
    public Boolean getIsActive() {
        return isActive;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#setIsActive(java.lang.Boolean)
     */
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getTickets()
     */
    public Set<Ticket> getTickets() {
        return Collections.unmodifiableSet(tickets);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#getTombstones()
     */
    public Set<Tombstone> getTombstones() {
        return Collections.unmodifiableSet(tombstones);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Item#addTombstone(org.unitedinternet.cosmo.model.Tombstone)
     */
    public void addTombstone(Tombstone tombstone) {
        tombstone.setItem(this);
        tombstones.add(tombstone);
    }


    /**
     * Item uid determines equality 
     */
    @Override
    public boolean equals(Object obj) {
        if(obj==null || uid==null) {
            return false;
        }
        if( ! (obj instanceof Item)) {
            return false;
        }

        return uid.equals(((Item) obj).getUid());
    }

    @Override
    public int hashCode() {
        if(uid==null) {
            return super.hashCode();
        }
        else {
            return uid.hashCode();
        }
    }

    @Override
    public String calculateEntityTag() {
        String uid = getUid() != null ? getUid() : "-";
        String modTime = getModifiedDate() != null ?
                Long.valueOf(getModifiedDate().getTime()).toString() : "-";
                String etag = uid + ":" + modTime;
                return encodeEntityTag(etag.getBytes(Charset.forName("UTF-8")));
    }

    protected void copyToItem(Item item) {
        item.setOwner(getOwner());
        item.setDisplayName(getDisplayName());

        // copy attributes
        for(Entry<QName, Attribute> entry: attributes.entrySet()) {
            item.addAttribute(entry.getValue().copy());
        }

        // copy stamps
        for(Stamp stamp: stamps) {
            item.addStamp(stamp.copy());
        }
    }
}
