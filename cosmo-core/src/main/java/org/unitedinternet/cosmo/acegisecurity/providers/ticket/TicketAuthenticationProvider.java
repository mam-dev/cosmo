/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.acegisecurity.providers.ticket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.server.CollectionPath;
import org.unitedinternet.cosmo.server.ItemPath;

/**
 */
@Component
@Transactional
public class TicketAuthenticationProvider implements AuthenticationProvider {
    
    private static final Logger LOG = LoggerFactory.getLogger(TicketAuthenticationProvider.class);

    @Autowired
    private ContentDao contentDao;

    // AuthenticationProvider methods

    /**
     * Authenticate.
     * @param authentication The authentication.
     * @return authentication.
     * @throws AuthenticationException - if something is wrong this exception is thrown.
     */
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (! supports(authentication.getClass()) 
           || !(authentication instanceof TicketAuthenticationToken) ) {
            return null;
        }

        TicketAuthenticationToken token =
            (TicketAuthenticationToken) authentication;
        for (String key : token.getKeys()) {
            Ticket ticket = findTicket(token.getPath(), key);
            if (ticket != null) {
                token.setTicket(ticket);
                token.setAuthenticated(true);
                return token;
            }
        }

        throw new TicketException("No valid tickets found for resource at " + token.getPath());
    }

    /**
     * Supports.
     * {@inheritDoc}
     * @param authentication The authentication.
     * @return The result.
     */
    public boolean supports(Class<?> authentication) {
        return TicketAuthenticationToken.class.
            isAssignableFrom(authentication);
    }

    // our methods

    /**
     * Gets content dao.
     * @return The content dao.
     */
    public ContentDao getContentDao() {
        return contentDao;
    }

    /**
    * Sets content dao. 
    * @param contentDao The content dao.
    */
    public void setContentDao(ContentDao contentDao) {
        this.contentDao = contentDao;
    }

    /**
     * Find tickets.
     * @param path The path.
     * @param key The key.
     * @return  The ticket.
     */
    private Ticket findTicket(String path, String key) {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("authenticating ticket " + key + " for resource at path " + path);
            }

            Item item = findItem(path);
            Ticket ticket = contentDao.getTicket(item, key);
            if (ticket == null) {
                return null;
            }

            if (ticket.hasTimedOut()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("removing timed out ticket " + ticket.getKey());
                }
                contentDao.removeTicket(item, ticket);
                return null;
            }

            return ticket;
        } catch (DataAccessException e) {
            throw new AuthenticationServiceException(e.getMessage(), e);
        }
    }

    /**
     * Finds item.
     * @param path The path.
     * @return The item.
     */
    private Item findItem(String path) {
        CollectionPath cp = CollectionPath.parse(path, true);
        Item item = null;

        if (cp != null) {
            item = contentDao.findItemByUid(cp.getUid());
            // a ticket cannot be used to access a collection that
            // does not already exist
            if (item == null) {
                throw new TicketedItemNotFoundException("Item with uid " + cp.getUid() + " not found");
            }

            return item;
        }

        ItemPath ip = ItemPath.parse(path, true);
        if (ip != null) {
            item = contentDao.findItemByUid(ip.getUid());
            if (item != null) {
                return item;
            }
            else {
                LOG.debug("no item found for uid: " + ip.getUid());
            }
        }
        
        item = contentDao.findItemByPath(path);
        if (item == null) {
            // if the item's parent exists, the ticket may be good for
            // that
            item = contentDao.findItemParentByPath(path);
        }
        if (item == null) {
            LOG.debug("no item found for path: " + path);
            throw new TicketedItemNotFoundException("Resource at " + path + " not found");
        }

        return item;
    }
}
