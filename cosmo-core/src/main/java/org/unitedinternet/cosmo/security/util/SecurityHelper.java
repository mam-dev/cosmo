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
package org.unitedinternet.cosmo.security.util;

import java.util.Set;

import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.dav.acl.DavPrivilege;
import org.unitedinternet.cosmo.model.*;
import org.unitedinternet.cosmo.model.filter.ItemFilter;
import org.unitedinternet.cosmo.model.filter.NoteItemFilter;
import org.unitedinternet.cosmo.security.CosmoSecurityContext;
import org.unitedinternet.cosmo.security.Permission;

/**
 * Contains methods that help determine if a
 * security context has sufficient privileges for certain
 * resources.
 */
public class SecurityHelper {
    
    private UserDao userDao;
    
    public SecurityHelper(ContentDao contentDao, UserDao userDao) {
        this.userDao = userDao;
    }
    
    /**
     * Determines if the current security context has access to
     * User.  The context must either be the user, or have admin access.
     * @param context security context
     * @param user user
     * @return true if the security context has sufficient privileges
     *         to view user
     */
    public boolean hasUserAccess(CosmoSecurityContext context, UserBase user) {
        if(context.getUser()==null) {
            return false;
        }
        
        return SecurityHelperUtils.canAccessPrincipal(context.getUser(), user);
    }
    
    /**
     * @param context security context
     * @param filter item filter
     * @return true if the 
     */
    public boolean hasAccessToFilter(CosmoSecurityContext context, ItemFilter filter) {
        // admin has access to everything
        if(context.getUser()!=null && context.getUser().getAdmin().booleanValue()) {
            return true;
        }
        
        // Otherwise require read access to parent or note
        if(filter.getParent()!=null) {
            return hasReadAccess(context, filter.getParent());
        }
        
        if (filter instanceof NoteItemFilter) {
            NoteItemFilter nif = (NoteItemFilter) filter;
            if (nif.getMasterNoteItem() != null) {
                return hasReadAccess(context, nif.getMasterNoteItem());
            }
        }
        
        // otherwise no access
        return false;
    }
    
    /**
     * @param context security context
     * @param item existing item
     * @return true if the security context has sufficient privileges
     *         to view the item
     */
    public boolean hasReadAccess(CosmoSecurityContext context, Item item) {
        if(context.getUser()!=null) {
            return hasReadAccess(context.getUser(), item, context.getTickets());
        }
        else if(context.getTicket()!=null) {
            return  hasReadAccess(context.getTicket(), item, context.getTickets());
        }
        
        return false;
    }
    
   
    public boolean hasWriteTicketAccess(CosmoSecurityContext context, Item item) {
        if (context.getUser() == null) {
            return false;
        }

        if (item.getOwner().equals(context.getUser())) {
            return true;
        }
        
        // Case 4: check subscriptions refresh user to prevent lazy init exceptions
        User user = userDao.getUser(context.getUser().getUsername());
        if (user != null) {
            for (CollectionSubscription cs : user.getSubscriptions()) {
                Ticket ticket = cs.getTicket();
                if (ticket == null) {
                    continue;
                }
                if (hasWriteAccess(ticket, item)) {
                    return true;
                }
            }
        }

        return false;
    }
    
    /**
     * @param context security context
     * @param item existing item
     * @return true if the security context has sufficient privileges
     *         to update the item
     */
    public boolean hasWriteAccess(CosmoSecurityContext context, Item item) {
        if(context.getUser()!=null) {
            return hasWriteAccess(context.getUser(), item, context.getTickets());
        }
        else if(context.getTicket()!=null) {
            return  hasWriteAccess(context.getTicket(), item, context.getTickets());
        }
        
        return false;
    }
    
    private boolean hasReadAccess(User user, Item item, Set<Ticket> tickets) {
        // SecurityHelperUtils provides non-ticket authorization logic

        if (SecurityHelperUtils.canAccess(user, item, Permission.READ)) {
            return true;
        }


        // Case 3: ticket for item present
        if (tickets != null) {
            for (Ticket ticket : tickets) {
                if (hasReadAccess(ticket, item)) {
                    return true;
                }
            }
        }

        /*
         * Case 4: check subscriptions. Refresh user to prevent lazy init exceptions
         */
        user = this.userDao.getUser(user.getUsername());
        if (user != null) {
             for(CollectionSubscription cs: user.getSubscriptions()) {
                 Ticket ticket = cs.getTicket();
                 if(ticket == null) {
                     continue;
                 }
                 if(hasReadAccess(ticket, item)) {
                     return true;
                 }
             }
        }

        // Otherwise no access
        return false;
    }
    
    private boolean hasReadAccess(Ticket ticket, Item item, Set<Ticket> tickets) {
        // check principal ticket
        if(hasReadAccess(ticket,item)) {
            return true;
        }
        
        // check other tickets
        if(tickets!=null) {
            for(Ticket t: tickets) {
                if(hasReadAccess(t, item)) {
                    return true;
                }
            }
        }
        
        // otherwise no access
        return false;
    }
    
    private boolean hasReadAccess(Ticket ticket, Item item) {
        // ticket must be valid
        if(ticket.isGranted(item) && !ticket.hasTimedOut()) {
            return true;
        }
       
        // otherwise no access
        return false;
    }
    
    private boolean hasWriteAccess(User user, Item item, Set<Ticket> tickets) {

        if (SecurityHelperUtils.canAccess(user, item, Permission.WRITE)) {
            return true;
        }
        
        // Case 3: ticket for item present
        if (tickets != null) {
            for(Ticket ticket: tickets) {
                if(hasWriteAccess(ticket, item)) {
                    return true;
                }
            }
        }
        
        // Case 4: check subscriptions refresh user to prevent lazy init exceptions
        user = userDao.getUser(user.getUsername());
        if (user != null) {
            for (CollectionSubscription cs : user.getSubscriptions()) {
                Ticket ticket = cs.getTicket();
                if (ticket == null) {
                    continue;
                }
                if (hasWriteAccess(ticket, item)) {
                    return true;
                }
            }
        }
        
        // otherwise no access
        return false;
    }
    
    private boolean hasWriteAccess(Ticket ticket, Item item, Set<Ticket> tickets) {
        // check principal ticket
        if(hasWriteAccess(ticket,item)) {
            return true;
        }
        
        // check other tickets
        if(tickets!=null) {
            for(Ticket t: tickets) {
                if(hasWriteAccess(t, item)) {
                    return true;
                }
            }
        }
            
        // otherwise no access
        return false;
    }
    
    private boolean hasWriteAccess(Ticket ticket, Item item) {
        // ticket must be valid
        if(ticket.isGranted(item) && !ticket.hasTimedOut() && ticket.isReadWrite()) {
            return true;
        }
       
        // otherwise no access
        return false;
    }
   
    
}
