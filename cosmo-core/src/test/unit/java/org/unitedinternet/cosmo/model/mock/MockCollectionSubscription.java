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

import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionSubscription;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;

/**
 * Represents a subscription to a shared collection.
 * A subscription belongs to a user and consists of 
 * a ticket key and a collection uid.
 */
public class MockCollectionSubscription extends MockAuditableObject implements CollectionSubscription {

    private User owner;
    
    
    private String displayName;
    
    private String ticketKey;
    
    private String collectionUid;
    
    /**
     * Constructor.
     */
    public MockCollectionSubscription() {
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCollectionSubscription#getCollectionUid()
     */
    /**
     * Gets collection uid.
     * @return collectuin uid.
     */
    public String getCollectionUid() {
        return collectionUid;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCollectionSubscription#setCollectionUid(java.lang.String)
     */
    /**
     * Sets collection uid.
     * @param collectionUid The collection uid.
     */
    public void setCollectionUid(String collectionUid) {
        this.collectionUid = collectionUid;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCollectionSubscription#setCollection(org.unitedinternet.cosmo.model.copy.CollectionItem)
     */
    /**
     * Sets collection.
     * @param collection The collection.
     */
    public void setCollection(CollectionItem collection) {
        this.collectionUid = collection.getUid();
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCollectionSubscription#getDisplayName()
     */
    /**
     * Gets display name.
     * @return The display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCollectionSubscription#setDisplayName(java.lang.String)
     */
    /**
     * Sets display name.
     * @param displayName The display name.
     */
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCollectionSubscription#getOwner()
     */
    /**
     * Gets owner.
     * @return user.
     */
    public User getOwner() {
        return owner;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCollectionSubscription#setOwner(org.unitedinternet.cosmo.model.copy.User)
     */
    /**
     * Sets owner.
     * @param owner The owner.
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCollectionSubscription#getTicketKey()
     */
    /**
     * Gets ticket key.
     * @return The ticket key.
     */
    public String getTicketKey() {
        return ticketKey;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCollectionSubscription#setTicketKey(java.lang.String)
     */
    /**
     * Sets ticket key.
     * @param ticketKey The ticket key.
     */
    public void setTicketKey(String ticketKey) {
        this.ticketKey = ticketKey;
    }  
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCollectionSubscription#setTicket(org.unitedinternet.cosmo.model.copy.Ticket)
     */
    /**
     * Sets ticket.
     * @param ticket The ticket.
     */
    public void setTicket(Ticket ticket) {
        this.ticketKey = ticket.getKey();
    }

    /**
     * Calculates entity tag.
     * {@inheritDoc}
     * @return The entity tag.
     */
    public String calculateEntityTag() {
        // subscription is unique by name for its owner
        String uid = (getOwner() != null && getOwner().getUid() != null) ?
            getOwner().getUid() : "-";
        String name = getDisplayName() != null ? getDisplayName() : "-";
        String modTime = getModifiedDate() != null ?
            Long.valueOf(getModifiedDate().getTime()).toString() : "-";
        String etag = uid + ":" + name + ":" + modTime;
        return encodeEntityTag(etag.getBytes());
    }
}
