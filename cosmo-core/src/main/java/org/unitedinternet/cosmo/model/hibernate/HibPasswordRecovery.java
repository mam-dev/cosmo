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
package org.unitedinternet.cosmo.model.hibernate;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.unitedinternet.cosmo.model.PasswordRecovery;
import org.unitedinternet.cosmo.model.User;

/**
 * Hibernate persistent PasswordRecovery.
 */
@Entity
@Table(name="pwrecovery")
public class HibPasswordRecovery extends BaseModelObject implements PasswordRecovery {

    private static final long serialVersionUID = 854107654491442548L;

    private static final long DEFAULT_TIMEOUT = 1000*60*60*24*3; // 3 days
    
    @Column(name = "pwrecoverykey", unique = true, nullable = false, length = 255)
    @NotNull
    private String key;
    
    @Column(name = "creationdate")
    private Date created;
    
    @Column(name = "timeout")
    private Long timeout;
    
    @ManyToOne(targetEntity=HibUser.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "userid")
    private User user;
    
    public HibPasswordRecovery(){
        this(null, null);
    }
    
    /**
     */
    public HibPasswordRecovery(User user, String key) {
        this(user, key, DEFAULT_TIMEOUT);
    }
    
    /**
     * 
     */
    public HibPasswordRecovery(User user, String key, long timeout) {
        this.user = user;
        this.key = key;
        this.timeout = timeout;
        this.created = new Date();
    }
   
    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }
   
    @Override
    public long getTimeout() {
        return timeout;
    }
  
    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    
    public Date getCreated() {
        return created;
    }
   
    @Override
    public void setCreated(Date created) {
        this.created = created;
    }
    
    @Override
    public User getUser() {
        return user;
    }
    
    @Override
    public void setUser(User user) {
        this.user = user;
    }
    
    @Override
    public boolean hasExpired() {
        Date now = new Date();
        return now.after(new Date(created.getTime() + timeout));
    }

    /**
     */
    public boolean equals(Object o) {
        if (! (o instanceof HibPasswordRecovery)) {
            return false;
        }
        HibPasswordRecovery it = (HibPasswordRecovery) o;
        return new EqualsBuilder().
            append(key, it.key).
            append(user, it.user).
            append(created, it.created).
            append(timeout, it.timeout).
            isEquals();
    }

    /**
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
