/*
 * CollectionItemExternal.java Dec 15, 2015
 * 
 * Copyright (c) 2015 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.dao.external;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.unitedinternet.cosmo.model.Attribute;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionItemDetails;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.QName;
import org.unitedinternet.cosmo.model.Stamp;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.Tombstone;
import org.unitedinternet.cosmo.model.User;

public class ExternalCollectionItem implements CollectionItem {

    private CollectionItem delegate;
    private Set<? extends Item> children;

    ExternalCollectionItem(CollectionItem delegate, Set<? extends Item> children) {
        this.delegate = delegate;
        this.children = children;
    }

    public CollectionItem getDelegate() {
        return delegate;
    }

    public Date getCreationDate() {
        return delegate.getCreationDate();
    }

    @SuppressWarnings("unchecked")
    public Set<Item> getChildren() {
        return (Set<Item>) children;
    }

    public Date getModifiedDate() {
        return delegate.getModifiedDate();
    }

    public Set<Stamp> getStamps() {
        return delegate.getStamps();
    }

    public CollectionItemDetails getChildDetails(Item item) {
        return delegate.getChildDetails(item);
    }

    public void updateTimestamp() {
        delegate.updateTimestamp();
    }

    public Item getChild(String uid) {
        return delegate.getChild(uid);
    }

    public String getEntityTag() {
       return delegate.getEntityTag();
    }

    public Map<String, Stamp> getStampMap() {
        return delegate.getStampMap();
    }

    public void addStamp(Stamp stamp) {
        delegate.addStamp(stamp);
    }

    public Item getChildByName(String name) {
        return delegate.getChildByName(name);
    }

    public boolean isExcludeFreeBusyRollup() {
        return delegate.isExcludeFreeBusyRollup();
    }

    public void setExcludeFreeBusyRollup(boolean flag) {
        delegate.setExcludeFreeBusyRollup(flag);
    }

    public void removeStamp(Stamp stamp) {
        delegate.removeStamp(stamp);
    }

    public EntityFactory getFactory() {
        return delegate.getFactory();
    }

    public Long getHue() {
        return delegate.getHue();
    }

    public void setHue(Long value) {
        delegate.setHue(value);
    }

    public int generateHash() {
        return delegate.generateHash();
    }

    public Stamp getStamp(String type) {
        return delegate.getStamp(type);
    }

    public Stamp getStamp(Class<?> clazz) {
        return delegate.getStamp(clazz);
    }

    public Map<QName, Attribute> getAttributes() {
        return delegate.getAttributes();
    }

    public void addTicket(Ticket ticket) {
        delegate.addTicket(ticket);
    }

    public void removeTicket(Ticket ticket) {
        delegate.removeTicket(ticket);
    }

    public void addAttribute(Attribute attribute) {
        delegate.addAttribute(attribute);
    }

    public void removeAttribute(String name) {
        delegate.removeAttribute(name);
    }

    public void removeAttribute(QName qname) {
        delegate.removeAttribute(qname);
    }

    public void removeAttributes(String namespace) {
        delegate.removeAttributes(namespace);
    }

    public Attribute getAttribute(String name) {
        return delegate.getAttribute(name);
    }

    public Attribute getAttribute(QName qname) {
        return delegate.getAttribute(qname);
    }

    public Object getAttributeValue(String name) {
        return delegate.getAttributeValue(name);
    }

    public Object getAttributeValue(QName qname) {
        return delegate.getAttributeValue(qname);
    }

    public void setAttribute(String name, Object value) {
        delegate.setAttribute(name, value);
    }

    public void setAttribute(QName key, Object value) {
        delegate.setAttribute(key, value);
    }

    public Map<String, Attribute> getAttributes(String namespace) {
        return delegate.getAttributes(namespace);
    }

    public Date getClientCreationDate() {
        return delegate.getClientCreationDate();
    }

    public void setClientCreationDate(Date clientCreationDate) {
        delegate.setClientCreationDate(clientCreationDate);
    }

    public Date getClientModifiedDate() {
        return delegate.getClientModifiedDate();
    }

    public void setClientModifiedDate(Date clientModifiedDate) {
        delegate.setClientModifiedDate(clientModifiedDate);
    }

    public String getName() {
        return delegate.getName();
    }

    public void setName(String name) {
        delegate.setName(name);
    }

    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    public void setDisplayName(String displayName) {
        delegate.setDisplayName(displayName);
    }

    public User getOwner() {
        return delegate.getOwner();
    }

    public void setOwner(User owner) {
        delegate.setOwner(owner);
    }

    public String getUid() {
        return delegate.getUid();
    }

    public void setUid(String uid) {
        delegate.setUid(uid);
    }

    public Set<CollectionItem> getParents() {
        return delegate.getParents();
    }

    public CollectionItemDetails getParentDetails(CollectionItem parent) {
        return delegate.getParentDetails(parent);
    }

    public CollectionItem getParent() {
        return delegate.getParent();
    }

    public Boolean getIsActive() {
        return delegate.getIsActive();
    }

    public void setIsActive(Boolean isActive) {
        delegate.setIsActive(isActive);
    }

    public Set<Ticket> getTickets() {
        return delegate.getTickets();
    }

    public Set<Tombstone> getTombstones() {
        return delegate.getTombstones();
    }

    public Item copy() {
        return delegate.copy();
    }
}