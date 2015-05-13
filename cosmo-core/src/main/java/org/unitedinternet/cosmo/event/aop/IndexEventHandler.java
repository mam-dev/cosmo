/*
 * IndexEventHandler.java Jul 2, 2014
 * 
 * Copyright (c) 2014 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.event.aop;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitedinternet.cosmo.service.interceptors.EventAddHandler;
import org.unitedinternet.cosmo.service.interceptors.EventRemoveHandler;
import org.unitedinternet.cosmo.service.interceptors.EventUpdateHandler;
import org.unitedinternet.cosmo.spi.search.SearchException;
import org.unitedinternet.cosmo.spi.search.SearchService;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;


/**
 * Interceptor for operations on events which updated the search index.
 * @author izidaru
 *
 */
public class IndexEventHandler implements EventAddHandler, EventUpdateHandler, EventRemoveHandler{
    
    private static final Log LOG =
            LogFactory.getLog(IndexEventHandler.class);
    
    private SearchService searchService;
    
    private CosmoSecurityManager securityManager = null;
    
    //if reliable is set to true, if the index 
    private boolean reliable = false;
    
    @Override
    public void beforeRemove(CollectionItem parent, Set<Item> items) {
        // do nothing        
    }

    @Override
    public void afterRemove(CollectionItem parent, Set<Item> items) {        
        try{
            for(Item item : items){
                LOG.info("afterRemove" + securityManager.getSecurityContext().getUser()+ 
                        " " + parent.getUid() + " " + item.getUid());
                searchService.deleteIndexForContentItem(securityManager.getSecurityContext().getUser(), parent, item);
            }
        } catch (Exception e){
            LOG.error("Cannot process request for removing index", e);
            if(reliable){
                throw new SearchException("Cannot process request for removing index", e);
            }
        }
        
    }

    @Override
    public void beforeUpdate(Set<CollectionItem> parents, Set<ContentItem> contentItems) {
        //do nothing        
    }

    @Override
    public void afterUpdate(Set<CollectionItem> parents, Set<ContentItem> contentItems) {
        try{
            for(CollectionItem parent: parents){                
                indexItem(parent, contentItems);
            }
        } catch (Exception e){
            LOG.error("Cannot process request for updating index", e);
            if(reliable){
                throw new SearchException("Cannot process request for updating index", e);
            }
        }
        
    }

    private void indexItem(CollectionItem parent, Set<ContentItem> contentItems) {  
        try{
            for(ContentItem item: contentItems){
                LOG.info("afterRemove" + securityManager.getSecurityContext().getUser()+ 
                        " " + parent.getUid() + " " + item.getUid());
                searchService.indexContentItem(securityManager.getSecurityContext().getUser(), parent, item);
            }    
        } catch (Exception e){
            LOG.error("Cannot process request for updating index", e);
            if(reliable){
                throw new SearchException("Cannot process request for updating index", e);
            }
        }
    }

    @Override
    public void beforeAdd(CollectionItem parent, Set<ContentItem> contentItems) {
        //do nothing   
    }

    @Override
    public void afterAdd(CollectionItem parent, Set<ContentItem> contentItems) {
        indexItem(parent, contentItems);        
    }

    public void setReliable(boolean reliable) {
        this.reliable = reliable;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setSecurityManager(CosmoSecurityManager securityManager) {
        this.securityManager = securityManager;
    }


    
}
