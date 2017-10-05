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

import java.nio.charset.Charset;

import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionSubscription;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;

/**
 * Represents a subscription to a shared collection. A subscription belongs to a user and consists of a ticket key and a
 * collection uid.
 */
public class MockCollectionSubscription extends MockAuditableObject implements CollectionSubscription {

    private CollectionItem targetCollection;

    private User owner;

    private Ticket ticket;

    private CollectionItem proxyCollection;

    /**
     * Default constructor.
     */
    public MockCollectionSubscription() {
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
