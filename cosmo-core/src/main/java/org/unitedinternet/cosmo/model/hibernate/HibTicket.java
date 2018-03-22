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
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.TicketType;
import org.unitedinternet.cosmo.model.User;

/**
 * Hibernate persistent Ticket.
 */
@Entity
@Table(name="tickets")
public class HibTicket extends HibAuditableObject implements Comparable<Ticket>, Ticket {

    private static final long serialVersionUID = -3333589463226954251L;
  

    @Column(name = "ticketkey", unique = true, nullable = false, length = 255)
    @NotNull
    private String key;
    
    @Column(name = "tickettimeout", nullable = false, length=255)
    private String timeout;
    
    @ElementCollection
    @JoinTable(
            name="ticket_privilege",
            joinColumns = @JoinColumn(name="ticketid")
    )
    @Fetch(FetchMode.JOIN)
    @Column(name="privilege", nullable=false, length=255)
    private Set<String> privileges;
    
    @Column(name = "creationdate")
    @org.hibernate.annotations.Type(type="timestamp")
    private Date created;
    
    @ManyToOne(targetEntity=HibUser.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "ownerid")
    private User owner;
    
    @ManyToOne(targetEntity=HibItem.class, fetch=FetchType.LAZY)
    @JoinColumn(name="itemid")
    private Item item;

    /**
     */
    public HibTicket() {
        privileges = new HashSet<String>();
    }

    /**
     */
    public HibTicket(TicketType type) {
        this();
        setTypePrivileges(type);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Ticket#getKey()
     */
    public String getKey() {
        return key;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Ticket#setKey(java.lang.String)
     */
    public void setKey(String key) {
        this.key = key;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Ticket#getTimeout()
     */
    public String getTimeout() {
        return timeout;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Ticket#setTimeout(java.lang.String)
     */
    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Ticket#setTimeout(java.lang.Integer)
     */
    public void setTimeout(Integer timeout) {
        this.timeout = "Second-" + timeout;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Ticket#getPrivileges()
     */
    public Set<String> getPrivileges() {
        return privileges;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Ticket#setPrivileges(java.util.Set)
     */
    public void setPrivileges(Set<String> privileges) {
        this.privileges = privileges;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Ticket#getType()
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

    private void setTypePrivileges(TicketType type) {
        for (String p : type.getPrivileges()) {
            privileges.add(p);
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Ticket#getCreated()
     */
    public Date getCreated() {
        return created;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Ticket#setCreated(java.util.Date)
     */
    public void setCreated(Date created) {
        this.created = created;
    }
   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Ticket#getOwner()
     */
    public User getOwner() {
        return owner;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Ticket#setOwner(org.unitedinternet.cosmo.model.User)
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Ticket#hasTimedOut()
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
     * @see org.unitedinternet.cosmo.model.Ticket#isGranted(org.unitedinternet.cosmo.model.Item)
     */
    public boolean isGranted(Item item) {
        
        if(item==null) {
            return false;
        }
        
        for (Ticket ticket : item.getTickets()) {
            if (ticket.equals(this)) {
                return true;
            }
        }
        
        for(Item parent: item.getParents()) {
            if(isGranted(parent)) {
                return true;
            }
        }
            
        return false;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Ticket#isReadOnly()
     */
    public boolean isReadOnly() {
        TicketType type = getType();
        return type != null && type.equals(TicketType.READ_ONLY);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Ticket#isReadWrite()
     */
    public boolean isReadWrite() {
        TicketType type = getType();
        return type != null && type.equals(TicketType.READ_WRITE);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Ticket#isFreeBusy()
     */
    public boolean isFreeBusy() {
        TicketType type = getType();
        return type != null && type.equals(TicketType.FREE_BUSY);
    }

    /**
     */
    public boolean equals(Object o) {
        if (! (o instanceof HibTicket)) {
            return false;
        }
        HibTicket it = (HibTicket) o;
        return new EqualsBuilder().
            append(key, it.key).
            append(timeout, it.timeout).
            append(privileges, it.privileges).
            isEquals();
    }

    /**
     */
    public int hashCode() {
        return new HashCodeBuilder(3, 5).
            append(key).
            append(timeout).
            append(privileges).
            toHashCode();
    }

    /**
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
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Ticket t) {
        return key.compareTo(t.getKey());
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Ticket#getItem()
     */
    public Item getItem() {
        return item;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Ticket#setItem(org.unitedinternet.cosmo.model.Item)
     */
    public void setItem(Item item) {
        this.item = item;
    }
    
    public String calculateEntityTag() {
        // Tickets are globally unique by key and are immutable
        return encodeEntityTag(this.key.getBytes(Charset.forName("UTF-8")));
    }
}
