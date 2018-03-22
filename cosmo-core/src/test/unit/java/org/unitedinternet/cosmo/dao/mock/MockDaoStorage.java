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
package org.unitedinternet.cosmo.dao.mock;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.springframework.security.core.token.KeyBasedPersistenceTokenService;
import org.unitedinternet.cosmo.dao.DuplicateItemNameException;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.Stamp;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.mock.MockCollectionItem;
import org.unitedinternet.cosmo.model.mock.MockHomeCollectionItem;
import org.unitedinternet.cosmo.model.mock.MockItem;
import org.unitedinternet.cosmo.util.VersionFourGenerator;

/**
 * Simple in-memory storage system for mock data access objects.
 */
public class MockDaoStorage {
    private HashMap<String, Item> itemsByPath;
    private HashMap<String, Item> itemsByUid;
    private HashMap<String, String> rootUidsByUsername;
    @SuppressWarnings("rawtypes")
    private HashMap<String, Set> tickets;
    private VersionFourGenerator idGenerator;
    private KeyBasedPersistenceTokenService ticketIdGenerator;

    /**
     * Constructor.
     */
    @SuppressWarnings("rawtypes")
    public MockDaoStorage() {
        itemsByPath = new HashMap<String, Item>();
        itemsByUid = new HashMap<String, Item>();
        rootUidsByUsername = new HashMap<String, String>();
        tickets = new HashMap<String, Set>();
        idGenerator = new VersionFourGenerator();
        ticketIdGenerator = new KeyBasedPersistenceTokenService();
        ticketIdGenerator.setServerSecret("cosmossecret");
        ticketIdGenerator.setServerInteger(123);
        ticketIdGenerator.setSecureRandom(new SecureRandom());
    }

    /**
     * Gets item by uid.
     * @param uid The id. The item.
     * @return The item.
     */
    public <STAMP_TYPE extends Stamp> STAMP_TYPE getItemStampByUid(String uid, Class<STAMP_TYPE> clazz) {
        return clazz.cast(itemsByUid.get(uid).getStamp(clazz));
    }
    
    /**
     * Gets item by uid.
     * @param uid The id. The item.
     * @return The item.
     */
    public Item getItemByUid(String uid) {
        return itemsByUid.get(uid);
    }
    
    /**
     * Sets item bu uid.
     * @param uid The id.
     * @param item The item.
     */
    public void setItemByUid(String uid, Item item) {
        itemsByUid.put(uid, item);
    }

    /**
     * Removes item by uid.
     * @param uid The uid.
     */
    public void removeItemByUid(String uid) {
        itemsByUid.remove(uid);
    }

    /**
     * Gets item by path.
     * @param path The path.
     * @return The item.
     */
    public Item getItemByPath(String path) {
        return itemsByPath.get(path);
    }

    /**
     * Sets item by path.
     * @param path The path.
     * @param item The item.
     */
    public void setItemByPath(String path, Item item) {
        itemsByPath.put(path, item);
    }

    /**
     * Removes item by path.
     * @param path The path.
     */
    public void removeItemByPath(String path) {
        itemsByPath.remove(path);
    }

    /**
     * Gets root uid.
     * @param username The username.
     * @return The root id.
     */
    public String getRootUid(String username) {
        return rootUidsByUsername.get(username);
    }

    /**
     * Sets root id.
     * @param userName The username.
     * @param uid The id.
     */
    public void setRootUid(String userName, String uid) {
        rootUidsByUsername.put(userName, uid);
    }

    /**
     * Removes root uid.
     * @param userName The username.
     */
    public void removeRootUid(String userName) {
        rootUidsByUsername.remove(userName);
    }

    /**
     * Gets all items.
     * @return All items.
     */
    public Collection<Item> getAllItems() {
        return itemsByUid.values();
    }
    
    /**
     * Gets root item.
     * @param userName The username
     * @return Home collection item.
     */
    public HomeCollectionItem getRootItem(String userName) {
        String rootUid = rootUidsByUsername.get(userName);
        if (rootUid == null) {
            throw new IllegalStateException("user does not have a root item");
        }
        return (HomeCollectionItem) itemsByUid.get(rootUid);
    }

    /**
     * Creates root item.
     * @param user The user.
     * @return Home collection item.
     */
    public HomeCollectionItem createRootItem(User user) {
        MockHomeCollectionItem rootCollection = new MockHomeCollectionItem();
        rootCollection.setName(user.getUsername());
        rootCollection.setOwner(user);
        rootCollection.setUid(calculateUid());
        rootCollection.setCreationDate(new Date());
        rootCollection.setModifiedDate(rootCollection.getCreationDate());
        rootCollection.setEntityTag(getMockItem(rootCollection).calculateEntityTag());

        itemsByUid.put(rootCollection.getUid(), rootCollection);
        itemsByPath.put("/" + rootCollection.getName(), rootCollection);
        rootUidsByUsername.put(user.getUsername(), rootCollection.getUid());

        return rootCollection;
    }

    /**
     * Stores item.
     * @param item The item.
     */
    @SuppressWarnings("deprecation")
    public void storeItem(Item item) {
        if (item.getOwner() == null) {
            throw new IllegalArgumentException("owner cannot be null");
        }

        if (item.getUid()==null) {
            item.setUid(calculateUid());
        }
        if (item.getName() == null) {
            item.setName(item.getUid());
        }
        ((MockItem) item).setCreationDate(new Date());
        ((MockItem) item).setModifiedDate(item.getCreationDate());
        ((MockItem) item).setEntityTag(getMockItem(item).calculateEntityTag());
        
        if(item.getParent()!=null) {
            for (Item sibling : item.getParent().getChildren()) {
                if (sibling.getName().equals(item.getName())) {
                    throw new DuplicateItemNameException(item);
                }
            }
            
            ((MockCollectionItem) item.getParent()).addChild(item);
            itemsByPath.put(getItemPath(item.getParent()) + "/" + item.getName(),
                    item);
        }
        
        // handle NoteItem modifications
        if(item instanceof NoteItem) {
            NoteItem note = (NoteItem) item;
            if (note.getModifies() != null) {
                note.getModifies().addModification(note);
            }
        }
        
        // handle tickets
        for (Ticket ticket: item.getTickets()) {
            this.createTicket(item, ticket);
        }
        
        itemsByUid.put(item.getUid(), item);
    }

    /**
     * Updates item.
     * @param item The item.
     */
    public void updateItem(Item item) {
        Item stored = itemsByUid.get(item.getUid());
        if (stored == null) {
            throw new IllegalArgumentException("item to be updated is not already stored");
        }
        if (! stored.equals(item)) {
            throw new IllegalArgumentException("item to be updated does not match stored item");
        }
        if (item.getName() == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        if (item.getOwner() == null) {
            throw new IllegalArgumentException("owner cannot be null");
        }

        @SuppressWarnings("deprecation")
        CollectionItem parentItem = item.getParent();

        if (parentItem != null) {
            for (Item sibling : parentItem.getChildren()) {
                if (sibling.getName().equals(item.getName()) &&
                    ! (sibling.getUid().equals(item.getUid()))) {
                    throw new DuplicateItemNameException(item);
                }
            }
        }

        ((MockItem) item).setModifiedDate(new Date());
        ((MockItem) item).setEntityTag(getMockItem(item).calculateEntityTag());
        ((MockItem) item).setVersion(getMockItem(item).getVersion()+1);

        String path = "";
        if (parentItem != null) {
            path += getItemPath(parentItem);
        }
        path += "/" + item.getName();

        // XXX if the item name changed during the update, then we
        // leave a dangling map entry
        itemsByPath.put(path, item);
    }

    /**
     * Finds item children.
     * @param item The item.
     * @return The set.
     */
    @SuppressWarnings({ "rawtypes", "deprecation", "unchecked" })
    public Set findItemChildren(Item item) {
        HashSet children = new HashSet();

        for (Item child : itemsByUid.values()) {
            if (child.getParent().getUid().equals(item.getUid())) {
                children.add(child);
            }
        }

        return Collections.unmodifiableSet(children);
    }

    /**
     * Finds item tickets.
     * @param item The item.
     * @return The set.
     */
    public Set<Ticket> findItemTickets(Item item) {
        @SuppressWarnings("unchecked")
        Set<Ticket> itemTickets = (Set<Ticket>) tickets.get(item.getUid());
        if (itemTickets == null) {
            itemTickets = new HashSet<Ticket>();
            tickets.put(item.getUid(), itemTickets);
        }
        return itemTickets;
    }
    
    /**
     * Finds ticket.
     * @param key The key.
     * @return The ticket.
     */
    @SuppressWarnings("unchecked")
    public Ticket findTicket(String key) {
        for(@SuppressWarnings("rawtypes") Set ticketSet: tickets.values()) {
            for(Ticket ticket: (Set<Ticket>) ticketSet) {
                if(ticket.getKey().equals(key)) {
                    return ticket;
                }
            }
        }
        
        return null;
    }

    /**
     * Creates ticket.
     * @param item The item.
     * @param ticket The ticket.
     */
    public void createTicket(Item item, Ticket ticket) {
        if (ticket.getKey() == null) { 
            ticket.setKey(calculateTicketKey());
        }
        findItemTickets(item).add(ticket);
    }

    /**
     * Removes ticket.
     * @param item The item.
     * @param ticket The ticket.
     */
    public void removeTicket(Item item, Ticket ticket) {
        findItemTickets(item).remove(ticket);
    }

    /**
     * Gets item path.
     * @param item The item.
     * @return The item path.
     */
    @SuppressWarnings("deprecation")
    public String getItemPath(Item item) {
        StringBuilder path = new StringBuilder();
        LinkedList<String> hierarchy = new LinkedList<String>();
        hierarchy.addFirst(item.getName());

        Item currentItem = item;
        while (currentItem.getParent() != null) {
            currentItem = itemsByUid.get(currentItem.getParent().getUid());
            hierarchy.addFirst(currentItem.getName());
        }

        // hierarchy
        for (String part : hierarchy) {
            path.append("/" + part);
        }

        return path.toString();
    }

    /**
     * Gets item path.
     * @param uid The uid.
     * @return The item path.
     */
    public String getItemPath(String uid) {
        return getItemPath(getItemByUid(uid));
    }

    /**
     * Calculates uid.
     * @return The uid.
     */
    public String calculateUid() {
        return idGenerator.nextStringIdentifier();
    }

    /**
     * Calculates the ticket key.
     * @return The ticket key.
     */
    private String calculateTicketKey() {
        return ticketIdGenerator.allocateToken("").getKey();
    }
    
    /**
     * Gets mock item.
     * @param item The item.
     * @return The mock item.
     */
    private MockItem getMockItem(Item item) {
        return (MockItem) item;
    }
}
