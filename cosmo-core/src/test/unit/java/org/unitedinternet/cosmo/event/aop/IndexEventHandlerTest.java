/*
 * IndexEventHandlerTest.java Jul 2, 2014
 * 
 * Copyright (c) 2014 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.event.aop;

import java.io.IOException;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.unitedinternet.cosmo.spi.search.SearchService;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.security.CosmoSecurityContext;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;

/**
 * 
 * @author izidaru
 *
 */
public class IndexEventHandlerTest {

    private IndexEventHandler indexEventHandler = new IndexEventHandler();
    
    private SearchService searchService;
    
    private CosmoSecurityManager securityManager;
    
    private CollectionItem collectionMock = Mockito.mock(CollectionItem.class);
    
    private Item itemMock = Mockito.mock(Item.class);
    
    private ContentItem contentItemMock = Mockito.mock(ContentItem.class); 
    
    private User user = Mockito.mock(User.class);
    
    @Before
    public void setup(){
        searchService = Mockito.mock(SearchService.class);
        securityManager = Mockito.mock(CosmoSecurityManager.class);
                
        indexEventHandler.setSearchService(searchService);
        indexEventHandler.setSecurityManager(securityManager);
        
        CosmoSecurityContext cosmoSecurityContextMock = Mockito.mock(CosmoSecurityContext.class);
        Mockito.when(securityManager.getSecurityContext()).thenReturn(cosmoSecurityContextMock);
        
        Mockito.when(cosmoSecurityContextMock.getUser()).thenReturn(user);
    }
    

    
    @Test
    public void afterRemove_removesFromSearchSystem(){
        Set<Item> itemSet = new java.util.HashSet<Item>();
        itemSet.add(itemMock);
        indexEventHandler.afterRemove(collectionMock, itemSet);
        Mockito.verify(searchService).deleteIndexForContentItem(user, collectionMock, itemMock);
    }
    
    
    @Test(expected = RuntimeException.class)
    public void afterRemove_throwException(){
        Set<Item> itemSet = new java.util.HashSet<Item>();
        itemSet.add(itemMock);
        indexEventHandler.setReliable(true);
        Mockito.doThrow(IOException.class).when(searchService).deleteIndexForContentItem(user, collectionMock, itemMock);
        indexEventHandler.afterRemove(collectionMock, itemSet);
    }
    
    @Test
    public void afterRemove_DoNotthrowException(){
        Set<Item> itemSet = new java.util.HashSet<Item>();
        itemSet.add(itemMock);
        indexEventHandler.setReliable(false);
        Mockito.doThrow(IOException.class).when(searchService).deleteIndexForContentItem(user, collectionMock, itemMock);
        indexEventHandler.afterRemove(collectionMock, itemSet);
    }
    
    
    @Test
    public void afterUpdate_updatesSearchSystem(){
        Set<ContentItem> itemSet = new java.util.HashSet<ContentItem>();
        itemSet.add(contentItemMock);        
        Set<CollectionItem> collectionItemSet = new java.util.HashSet<CollectionItem>();
        collectionItemSet.add(collectionMock);
        indexEventHandler.afterUpdate(collectionItemSet, itemSet);        
        
        Mockito.verify(searchService).indexContentItem(user, collectionMock, contentItemMock);
    }
    
    
    @Test(expected = RuntimeException.class)
    public void afterUpdate_throwException(){
        Set<ContentItem> itemSet = new java.util.HashSet<ContentItem>();
        itemSet.add(contentItemMock);        
        Set<CollectionItem> collectionItemSet = new java.util.HashSet<CollectionItem>();   
        collectionItemSet.add(collectionMock);
        indexEventHandler.setReliable(true);
        
        Mockito.doThrow(IOException.class).when(searchService).indexContentItem(user, collectionMock, contentItemMock);
        indexEventHandler.afterUpdate(collectionItemSet, itemSet);        
    }
    
    @Test
    public void afterUpdate_DoNotthrowException(){
        Set<ContentItem> itemSet = new java.util.HashSet<ContentItem>();
        itemSet.add(contentItemMock);        
        Set<CollectionItem> collectionItemSet = new java.util.HashSet<CollectionItem>();
        collectionItemSet.add(collectionMock);
        indexEventHandler.setReliable(false);
        
        Mockito.doThrow(IOException.class).when(searchService).indexContentItem(user, collectionMock, contentItemMock);
        indexEventHandler.afterUpdate(collectionItemSet, itemSet);  
    }
    
    @Test
    public void afterAdd_updatesSearchSystem(){
        Set<ContentItem> itemSet = new java.util.HashSet<ContentItem>();
        itemSet.add(contentItemMock);        

        indexEventHandler.afterAdd(collectionMock, itemSet);        
        
        Mockito.verify(searchService).indexContentItem(user, collectionMock, contentItemMock);
    }
    
    
    @Test(expected = RuntimeException.class)
    public void afterAdd_throwException(){
        Set<ContentItem> itemSet = new java.util.HashSet<ContentItem>();
        itemSet.add(contentItemMock);        
        indexEventHandler.setReliable(true);
        
        Mockito.doThrow(IOException.class).when(searchService).indexContentItem(user, collectionMock, contentItemMock);
        indexEventHandler.afterAdd(collectionMock, itemSet);        
    }
    
    @Test
    public void afterAdd_DoNotthrowException(){
        Set<ContentItem> itemSet = new java.util.HashSet<ContentItem>();
        itemSet.add(contentItemMock);        
        indexEventHandler.setReliable(false);
        
        Mockito.doThrow(IOException.class).when(searchService).indexContentItem(user, collectionMock, contentItemMock);
        indexEventHandler.afterAdd(collectionMock, itemSet);  
    }
    
    
    
    
}
