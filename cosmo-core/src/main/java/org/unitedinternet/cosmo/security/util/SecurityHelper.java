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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.*;
import org.unitedinternet.cosmo.model.filter.ItemFilter;
import org.unitedinternet.cosmo.model.filter.NoteItemFilter;
import org.unitedinternet.cosmo.security.CosmoSecurityContext;
import org.unitedinternet.cosmo.security.Permission;
import org.unitedinternet.cosmo.security.PermissionDeniedException;

/**
 * Contains methods that help determine if a
 * security context has sufficient privileges for certain
 * resources.
 */
public class SecurityHelper {
    
    private UserDao userDao;

    private static final  Log LOG =
        LogFactory.getLog(SecurityHelper.class);

    public boolean hasReadAccess(CosmoSecurityContext securityContext, Item item) {
        return hasAccess(securityContext, item, Permission.READ);
    }

    public boolean hasWriteAccess(CosmoSecurityContext securityContext, Item item) {
        return hasAccess(securityContext, item, Permission.WRITE);
    }


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
        if(context.getUser()!=null && context.getUser().getAdmin()) {
            return true;
        }
        
        // Otherwise require read access to parent or note
        if(filter.getParent()!=null) {
            return hasAccess(context, filter.getParent(), Permission.READ);
        }
        
        if (filter instanceof NoteItemFilter) {
            NoteItemFilter nif = (NoteItemFilter) filter;
            if (nif.getMasterNoteItem() != null) {
                return hasAccess(context, nif.getMasterNoteItem(), Permission.READ);
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
    public boolean hasAccess(CosmoSecurityContext context, Item item, Permission perm) {
        if(context.getUser()!=null) {
            return hasAccess(context.getUser(), item, context.getTickets(), perm);
        }
        else if(context.getTicket()!=null) {
            return  hasAccess(context.getTicket(), item, context.getTickets(), perm);
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
                if (hasAccess(ticket, item, Permission.WRITE)) {
                    return true;
                }
            }
        }

        return false;
    }
    


    private boolean hasAccess(User user, Item item, Set<Ticket> tickets, Permission permission) {
        LOG.debug("Checking access " + permission + " for item " + item.getUid() + " for user " + user.getUsername());
        // SecurityHelperUtils provides non-ticket authorization logic
        if (SecurityHelperUtils.canAccess(user, item, permission)) {
            return true;
        }


        // ticket for item present
        if (hasAccess(item, tickets, permission))
            return true;

        /*
         * heck subscriptions. Refresh user to prevent lazy init exceptions
         */
        user = this.userDao.getUser(user.getUsername());
        if (user != null) {
             for(CollectionSubscription cs: user.getSubscriptions()) {
                 Ticket ticket = cs.getTicket();
                 if(ticket == null) {
                     continue;
                 }
                 if(hasAccess(ticket, item, permission)) {
                     return true;
                 }
             }
        }

        // Otherwise no access
        return false;
    }

    private boolean hasAccess(Item item, Set<Ticket> tickets, Permission permission) {
        if (tickets != null) {
            for (Ticket ticket : tickets) {
                if (hasAccess(ticket, item, permission)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check access for a principal ticket or a set of other tickets
     * @param ticket
     * @param item
     * @param tickets
     * @param permission
     * @return
     */
    private boolean hasAccess(Ticket ticket, Item item, Set<Ticket> tickets, Permission permission) {
        if (hasAccess(ticket, item, permission)) { //principal
            return true;
        }
        return hasAccess(item, tickets, permission); //other tickets
    }

    /**
     * Ticket permission logic
     * @param ticket
     * @param item
     * @param permission
     * @return
     */
    private boolean hasAccess(Ticket ticket, Item item, Permission permission) {
        LOG.debug("Checking access " + permission + " for item  " + item.getUid() + " for ticket " + ticket);
        return isValidTicket(ticket, item) && ticket.getPermissions().contains(permission);
    }



    public void throwIfNoAccess(CosmoSecurityContext ctx, Item item, Permission permission) throws PermissionDeniedException {
        if (ctx.isAnonymous()) {
            LOG.warn("Anonymous access attempted to item " + item.getUid());
            throw new PermissionDeniedException("Anonymous principals have no permissions");
        }

        if (!hasAccess(ctx, item, permission)) {
            LOG.warn("Insufficient privileges while accessing item " + item.getUid() );
            throw new PermissionDeniedException("Principal has insufficient privileges");
        }

    }





    private boolean isValidTicket(Ticket ticket, Item item) {
        return ticket.isGranted(item) && !ticket.hasTimedOut();
    }



}
