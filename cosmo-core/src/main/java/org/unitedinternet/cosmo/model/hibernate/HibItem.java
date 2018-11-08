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
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyClass;
import javax.persistence.OneToMany;
import javax.persistence.Table;
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
@SuppressWarnings("serial")
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
            fetch=FetchType.EAGER, cascade=CascadeType.ALL, orphanRemoval=true)
    @MapKeyClass(HibQName.class)
    // turns out this creates a query that is unoptimized for MySQL
    //@Fetch(FetchMode.SUBSELECT)
    @BatchSize(size=50)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Map<QName, Attribute> attributes = new HashMap<QName, Attribute>(0);

    @OneToMany(targetEntity=HibTicket.class, mappedBy = "item", 
            fetch=FetchType.EAGER, cascade=CascadeType.ALL, orphanRemoval=true)
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


    @Override
    public Set<Stamp> getStamps() {
        return Collections.unmodifiableSet(stamps);
    }

    @Override
    public Map<String, Stamp> getStampMap() {
        if(stampMap==null) {
            stampMap = new HashMap<String, Stamp>();
            for(Stamp stamp : stamps) {
                stampMap.put(stamp.getType(), stamp);
            }
        }

        return stampMap;
    }

    @Override
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

    @Override
    public void removeStamp(Stamp stamp) {
        // only remove stamps that belong to item
        if(!stamps.contains(stamp)) {
            return;
        }

        stamps.remove(stamp);

        // add tombstone for tracking purposes
        tombstones.add(new HibStampTombstone(this, stamp));
    }

    @Override
    public Stamp getStamp(String type) {
        for(Stamp stamp : stamps) {
            // only return stamp if it matches class and is active
            if(stamp.getType().equals(type)) {
                return stamp;
            }
        }

        return null;
    }

    @Override
    public Stamp getStamp(Class<?> clazz) {
        for(Stamp stamp : stamps) {
            // only return stamp if it is an instance of the specified class
            if(clazz.isInstance(stamp)) {
                return stamp;
            }
        }

        return null;
    }

    @Override
    public Map<QName, Attribute> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    @Override
    public void addTicket(Ticket ticket) {
        ticket.setItem(this);
        tickets.add(ticket);
    }

    @Override
    public void removeTicket(Ticket ticket) {
        tickets.remove(ticket);
    }

    @Override
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

    @Override
    public void removeAttribute(String name) {
        removeAttribute(new HibQName(name));
    }

    @Override
    public void removeAttribute(QName qname) {
        if(attributes.containsKey(qname)) {
            attributes.remove(qname);
            tombstones.add(new HibAttributeTombstone(this, qname));
        }
    }

    @Override
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

    @Override
    public Attribute getAttribute(String name) {
        return getAttribute(new HibQName(name));
    }

    @Override
    public Attribute getAttribute(QName qname) {
        return attributes.get(qname);
    }

    @Override
    public Object getAttributeValue(String name) {
        return getAttributeValue(new HibQName(name));
    }

    @Override
    public Object getAttributeValue(QName qname) {
        Attribute attr = attributes.get(qname);
        if (attr == null) {
            return attr;
        }
        return attr.getValue();
    }

    @Override
    public void setAttribute(String name, Object value) {
        setAttribute(new HibQName(name),value);
    }

    @Override
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

    @Override
    public Map<String, Attribute> getAttributes(String namespace) {
        HashMap<String, Attribute> attrs = new HashMap<String, Attribute>();
        for(Entry<QName, Attribute> e: attributes.entrySet()) {
            if(e.getKey().getNamespace().equals(namespace)) {
                attrs.put(e.getKey().getLocalName(), e.getValue());
            }
        }

        return attrs;
    }


    @Override
    public Date getClientCreationDate() {
        return clientCreationDate;
    }

    @Override
    public void setClientCreationDate(Date clientCreationDate) {
        this.clientCreationDate = clientCreationDate;
    }

    @Override
    public Date getClientModifiedDate() {
        return clientModifiedDate;
    }

    @Override
    public void setClientModifiedDate(Date clientModifiedDate) {
        this.clientModifiedDate = clientModifiedDate;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public User getOwner() {
        return owner;
    }

    @Override
    public void setOwner(User owner) {
        this.owner = owner;
    }

    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public void setUid(String uid) {
        this.uid = uid;
    }

    
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

    @Override
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

    @Override
    public CollectionItem getParent() {
        if(getParents().size()==0) {
            return null;
        }

        return getParents().iterator().next();
    }

    @Override
    public CollectionItemDetails getParentDetails(CollectionItem parent) {
        for(CollectionItemDetails cid: parentDetails) {
            if(cid.getCollection().equals(parent)) {
                return cid;
            }
        }
        
        return null;
    }

    @Override
    public Boolean getIsActive() {
        return isActive;
    }

    @Override
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public Set<Ticket> getTickets() {
        return Collections.unmodifiableSet(tickets);
    }

    @Override
    public Set<Tombstone> getTombstones() {
        return Collections.unmodifiableSet(tombstones);
    }
    
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
