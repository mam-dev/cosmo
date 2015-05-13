/*
 * Copyright 2008 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.model.event;

import java.util.Date;

import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;


/**
 * Base class for event log entry.
 *
 */
public abstract class EventLogEntry { 
    private Date date = null;
    private User user;
    private Ticket ticket;
    
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public Ticket getTicket() {
        return ticket;
    }
    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }
    public Date getDate() {
        return date != null? (Date)date.clone() : null;
    }
    public void setDate(Date date) {
        if(date != null){
            this.date = (Date)date.clone();
        }
    }
}
