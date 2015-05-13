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
import java.util.Set;

/**
 * Represents a ticket that is used to grant access to
 * an Item.
 */
public interface Ticket extends AuditableObject {

    /** */
    public static final String TIMEOUT_INFINITE = "Infinite";
    /** */
    public static final String PRIVILEGE_READ = "read";
    /** */
    public static final String PRIVILEGE_WRITE = "write";
    /** */
    public static final String PRIVILEGE_FREEBUSY = "freebusy";
    
    public String getKey();

    public void setKey(String key);

    /**
     */
    public String getTimeout();

    /**
     */
    public void setTimeout(String timeout);

    /**
     */
    public void setTimeout(Integer timeout);

    /**
     */
    public Set<String> getPrivileges();

    /**
     */
    public void setPrivileges(Set<String> privileges);

    /**
     * Returns the ticket type if the ticket's privileges match up
     * with one of the predefined types, or <code>null</code>
     * otherwise.
     */
    public TicketType getType();

    /**
     */
    public Date getCreated();

    /**
     */
    public void setCreated(Date created);

    public User getOwner();

    public void setOwner(User owner);

    /**
     */
    public boolean hasTimedOut();

    /**
     * Determines whether or not the ticket is granted on the given
     * item or one of its ancestors.
     */
    public boolean isGranted(Item item);

    public boolean isReadOnly();

    public boolean isReadWrite();

    public boolean isFreeBusy();

    public int compareTo(Ticket t);

    public Item getItem();

    public void setItem(Item item);

}