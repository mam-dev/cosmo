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

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.TicketType;
import org.unitedinternet.cosmo.model.User;

/**
 * A bean encapsulating the information about a ticket used in the
 * bodies of ticket requests and responses.
 *
 * This class does not perform any validation on the ticket info,
 * leaving that responsibility to those objects which manipulate
 * ticket info.
 *
 * Similarly, the class does not know how to convert itself to or from
 * XML.
 */
public class MockTicket extends MockAuditableObject implements Comparable<Ticket>, Ticket {

    
    private String key;
    
    
    private String timeout;
    
    
    private Set<String> privileges;
    
    private Date created;
    
    private User owner;
    
    private Item item;

    /**
     */
    public MockTicket() {
        privileges = new HashSet<String>();
    }

    /**
     * Contructor.
     * @param type The ticket type.
     */
    public MockTicket(TicketType type) {
        this();
        setTypePrivileges(type);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTicket#getKey()
     */
    /**
     * Gets type.
     * @return The type.
     */
    public String getKey() {
        return key;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTicket#setKey(java.lang.String)
     */
    /**
     * Sets key.
     * @param key The key.
     */
    public void setKey(String key) {
        this.key = key;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTicket#getTimeout()
     */
    /**
     * Gets timeout.
     * @return The timeout.
     */
    public String getTimeout() {
        return timeout;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTicket#setTimeout(java.lang.String)
     */
    /**
     * Sets timeout.
     * @param timeout The timeout.
     */
    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTicket#setTimeout(java.lang.Integer)
     */
    /**
     * Sets timeout.
     * @param timeout The timeout.
     */
    public void setTimeout(Integer timeout) {
        this.timeout = "Second-" + timeout;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTicket#getPrivileges()
     */
    /**
     * Gets privileges.
     * @return The privileges.
     */
    public Set<String> getPrivileges() {
        return privileges;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTicket#setPrivileges(java.util.Set)
     */
    /**
     * Sets privileges.
     * @param privileges The privileges.
     */
    public void setPrivileges(Set<String> privileges) {
        this.privileges = privileges;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTicket#getType()
     */
    /**
     * Gets type.
     * @return The ticket type.
     */
    public TicketType getType() {
        if (privileges.contains(PRIVILEGE_READ)) {
            if (privileges.contains(PRIVILEGE_WRITE)) {
                return TicketType.READ_WRITE;
            }
            else {
                return TicketType.READ_ONLY;
            }
        }
        if (privileges.contains(PRIVILEGE_FREEBUSY)) {
            return TicketType.FREE_BUSY;
        }
        return null;
    }

    /**
     * Sets type privileges.
     * @param type The ticket type.
     */
    private void setTypePrivileges(TicketType type) {
        for (String p : type.getPrivileges()) {
            privileges.add(p);
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTicket#getCreated()
     */
    /**
     * Gets created.
     * @return The date.
     */
    public Date getCreated() {
        return created;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTicket#setCreated(java.util.Date)
     */
    /**
     * Sets created date.
     * @param created The created date.
     */
    public void setCreated(Date created) {
        this.created = created;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTicket#getOwner()
     */
    /**
     * Gets owner.
     * @return The user.
     */
    public User getOwner() {
        return owner;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTicket#setOwner(org.unitedinternet.cosmo.model.copy.User)
     */
    /**
     * Sets owner.
     * @param owner The owner.
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTicket#hasTimedOut()
     */
    /**
     * Has timeout.
     * @return The boolean: hasTimeout.
     */
    public boolean hasTimedOut() {
        if (timeout == null || timeout.equals(TIMEOUT_INFINITE)) {
            return false;
        }

        int seconds = Integer.parseInt(timeout.substring(7));

        Calendar expiry = Calendar.getInstance();
        expiry.setTime(created);
        expiry.add(Calendar.SECOND, seconds);

        return Calendar.getInstance().after(expiry);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTicket#isGranted(org.unitedinternet.cosmo.model.copy.Item)
     */
    /**
     * Is granted.
     * @param item The item.
     * @return the boolean for is or not granted.
     */
    public boolean isGranted(Item item) {
        
        if (item == null) {
            return false;
        }
        
        for (Ticket ticket : item.getTickets()) {
            if (ticket.equals(this)) {
                return true;
            }
        }
        
        for(Item parent: item.getParents()) {
            if (isGranted(parent)) {
                return true;
            }
        }
            
        return false;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTicket#isReadOnly()
     */
    /**
     * Is read only.
     * @return The boolean if is read only.
     */
    public boolean isReadOnly() {
        TicketType type = getType();
        return type != null && type.equals(TicketType.READ_ONLY);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTicket#isReadWrite()
     */
    /**
     * Is read write.
     * @return The boolean for is read write.
     */
    public boolean isReadWrite() {
        TicketType type = getType();
        return type != null && type.equals(TicketType.READ_WRITE);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTicket#isFreeBusy()
     */
    /**
     * Is free busy.
     * @return The boolean for is free busy.
     */
    public boolean isFreeBusy() {
        TicketType type = getType();
        return type != null && type.equals(TicketType.FREE_BUSY);
    }

    /**
     * Equals.
     * {@inheritDoc}
     * @param o The object.
     * @return The boolean for equals.
     */
    public boolean equals(Object o) {
        if (! (o instanceof MockTicket)) {
            return false;
        }
        MockTicket it = (MockTicket) o;
        return new EqualsBuilder().
            append(key, it.key).
            append(timeout, it.timeout).
            append(privileges, it.privileges).
            isEquals();
    }

    /**
     * Hashcode.
     * {@inheritDoc}
     * @return The hashcode.
     */
    public int hashCode() {
        return new HashCodeBuilder(3, 5).
            append(key).
            append(timeout).
            append(privileges).
            toHashCode();
    }

    /**
     * toString.
     * {@inheritDoc}
     * @return The string.
     */
    public String toString() {
        StringBuilder buf = new StringBuilder(key);
        TicketType type = getType();
        if (type != null) {
            buf.append(" (").append(type).append(")");
        }
        return buf.toString();
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTicket#compareTo(org.unitedinternet.cosmo.model.copy.Ticket)
     */
    /**
     * Compare to.
     * @param t The ticket.
     * @return The result of compare.
     */
    public int compareTo(Ticket t) {
        return key.compareTo(t.getKey());
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTicket#getItem()
     */
    /**
     * Gets item.
     * @return The item.
     */
    public Item getItem() {
        return item;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTicket#setItem(org.unitedinternet.cosmo.model.copy.Item)
     */
    /**
     * Sets item.
     * @param item The item.
     */
    public void setItem(Item item) {
        this.item = item;
    }
    
    /**
     * Calculate entity tag.
     * {@inheritDoc}
     * @return The entity tag.
     */
    public String calculateEntityTag() {
        // Tickets are globally unique by key and are immutable
        return encodeEntityTag(this.key.getBytes());
    }
}
