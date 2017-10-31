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

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionSubscription;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;

/**
 * Hibernate persistent CollectionSubscription.
 */

@Entity
@SuppressWarnings("serial")
@Table(name = "subscription")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class HibCollectionSubscription extends HibAuditableObject implements CollectionSubscription {

    @ManyToOne(targetEntity = HibCollectionItem.class)
    @JoinColumn(name = "target_collection_id")
    private CollectionItem targetCollection;
    
    @ManyToOne(targetEntity = HibUser.class)
    @JoinColumn(name = "ownerid")
    private User owner;
    
    @OneToOne(targetEntity = HibTicket.class)
    @JoinColumn(name = "ticketid")
    private Ticket ticket;
    
    @OneToOne(targetEntity = HibCollectionSubscriptionItem.class, orphanRemoval = true)
    @JoinColumn(name = "proxy_collection_id")
    private CollectionItem proxyCollection;

    /**
     * Default constructor.
     */
    public HibCollectionSubscription() {
        super();
    }

    @Override
    public CollectionItem getTargetCollection() {
        return this.targetCollection;
    }

    @Override
    public void setTargetCollection(CollectionItem targetCollection) {
        this.targetCollection = targetCollection;
    }

    @Override
    public User getOwner() {
        return this.owner;
    }

    @Override
    public void setOwner(User owner) {
        this.owner = owner;
    }

    @Override
    public Ticket getTicket() {
        return this.ticket;
    }

    @Override
    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    @Override
    public CollectionItem getProxyCollection() {
        return this.proxyCollection;
    }

    @Override
    public void setProxyCollection(CollectionItem proxyCollection) {
        this.proxyCollection = proxyCollection;
    }

    public String calculateEntityTag() {
        String targetUid = this.targetCollection != null ? this.targetCollection.getUid() : "-";
        String ownerUid = getOwner() != null && getOwner().getUid() != null ? getOwner().getUid() : "-";
        String modTime = getModifiedDate() != null ? Long.valueOf(getModifiedDate().getTime()).toString() : "-";
        String etag = targetUid + ":" + ownerUid + ":" + ":" + modTime;
        return encodeEntityTag(etag.getBytes(Charset.forName("UTF-8")));
    }
}
