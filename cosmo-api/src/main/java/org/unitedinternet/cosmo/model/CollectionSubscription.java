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
 * Models a collection subscription between a sharer and a sharee.
 * 
 * @author daniel grigore
 */
public interface CollectionSubscription extends AuditableObject {

    /**
     * Gets the collection item shared by sharer.
     * 
     * @return the collection item shared by sharer
     */
    public CollectionItem getTargetCollection();

    public void setTargetCollection(CollectionItem targetCollection);

    /**
     * Gets the owner of this subscription (sharee).
     * 
     * @return the owner of this subscription (sharee).
     */
    public User getOwner();

    public void setOwner(User owner);

    /**
     * Gets the ticket set for this subscription based on which sharee privileges will be calculated.
     * 
     * @return the ticket set for this subscription.
     */
    public Ticket getTicket();

    public void setTicket(Ticket ticket);

    /**
     * Gets the sharee collection of this subscription. The sharee collection can be <code>null</code> if sharee did not
     * accepted the share request.
     * 
     * @return the sharee collection of this subscription
     */
    public CollectionItem getProxyCollection();

    public void setProxyCollection(CollectionItem proxyCollection);

}