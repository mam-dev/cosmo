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
 * TODO Add comments. Represents
 */
public interface CollectionSubscription extends AuditableObject {

    /**
     * Gets the collection item shared by sharer.
     * 
     * @return the collection item shared by sharer
     */
    public CollectionItem getTargetCollection();

    public void setTargetCollection(CollectionItem targetCollection);

    public User getOwner();

    public void setOwner(User owner);

    public Ticket getTicket();

    public void setTicket(Ticket ticket);

    public CollectionItem getProxyCollection();

    public void setProxyCollection(CollectionItem proxyCollection);

}