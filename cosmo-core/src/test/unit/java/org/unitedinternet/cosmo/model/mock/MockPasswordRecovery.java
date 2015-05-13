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
package org.unitedinternet.cosmo.model.mock;

import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.unitedinternet.cosmo.model.PasswordRecovery;
import org.unitedinternet.cosmo.model.User;

/**
 * An entity representing a password change request.
 * 
 * There should be a single password change request corresponding
 * to each password recovery request in the system. 
 */
public class MockPasswordRecovery implements PasswordRecovery {

    private static final long DEFAULT_TIMEOUT = 1000*60*60*24*3; // 3 days
   
    private String key;
    
    
    private Date created;
    
    private long timeout;
    
    private User user;
    
    /**
     * Contructor.
     */
    public MockPasswordRecovery(){
        this(null, null);
    }
    
    /**
     * Constructor.
     * @param user The user.
     * @param key The key.
     */
    public MockPasswordRecovery(User user, String key) {
        this(user, key, DEFAULT_TIMEOUT);
    }
    
    /**
     * Constructor.
     * @param user The user.
     * @param key The key.
     * @param timeout The timeout.
     */
    public MockPasswordRecovery(User user, String key, long timeout) {
        this.user = user;
        this.key = key;
        this.timeout = timeout;
        this.created = new Date();
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfacePasswordRecovery#getKey()
     */
    /**
     * Gets key.
     * @return The key.
     */
    public String getKey() {
        return key;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfacePasswordRecovery#setKey(java.lang.String)
     */
    /**
     * Sets key.
     * @param key The key.
     */
    public void setKey(String key) {
        this.key = key;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfacePasswordRecovery#getTimeout()
     */
    /**
     * Gets timeout.
     * @return The timeout.
     */
    public long getTimeout() {
        return timeout;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfacePasswordRecovery#setTimeout(long)
     */
    /**
     * Sets timeout.
     * @param timeout The timeout.
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfacePasswordRecovery#getCreated()
     */
    /**
     * Gets created.
     * @return The date.
     */
    public Date getCreated() {
        return created;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfacePasswordRecovery#setCreated(java.util.Date)
     */
    /**
     * Sets created.
     * @param created The date.
     */
    public void setCreated(Date created) {
        this.created = created;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfacePasswordRecovery#getUser()
     */
    /**
     * Gets user.
     * @return The user.
     */
    public User getUser() {
        return user;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfacePasswordRecovery#setUser(org.unitedinternet.cosmo.model.copy.User)
     */
    /**
     * Sets user.
     * @param user The user.
     */
    public void setUser(User user) {
        this.user = user;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfacePasswordRecovery#hasExpired()
     */
    /**
     * HasExpired.
     * @return if the date is expired or not.
     */
    public boolean hasExpired() {
        Date now = new Date();
        return now.after(new Date(created.getTime() + timeout));
    }

    /**
     * Equals.
     * {@inheritDoc}
     * @param o The object.
     * @return The boolean equals.
     */
    public boolean equals(Object o) {
        if (! (o instanceof MockPasswordRecovery)) {
            return false;
        }
        MockPasswordRecovery it = (MockPasswordRecovery) o;
        return new EqualsBuilder().
            append(key, it.key).
            append(user, it.user).
            append(created, it.created).
            append(timeout, it.timeout).
            isEquals();
    }

    /**
     * HashCode.
     * {@inheritDoc}
     * @return The hashCode.
     */
    public int hashCode() {
        return new HashCodeBuilder(3, 5).
            append(key).
            append(user).
            append(created).
            append(timeout).
            toHashCode();
    }

    /**
     * ToString.
     * {@inheritDoc}
     * @return The string.
     */
    public String toString() {
        return new ToStringBuilder(this).
            append("key", key).
            append("user", user).
            append("created", created).
            append("timeout", timeout).
            toString();
    }

}
