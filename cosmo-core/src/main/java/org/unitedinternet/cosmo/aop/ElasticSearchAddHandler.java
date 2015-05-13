/*
 * ElasticSearchAddHandler.java Apr 30, 2014
 * 
 * Copyright (c) 2014 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.aop;

import java.util.Set;

import org.unitedinternet.cosmo.service.interceptors.EventAddHandler;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;

/**
 * Index events during add operation.
 * @author izidaru
 *
 */
public class ElasticSearchAddHandler implements EventAddHandler{
    
    @Override
    public void beforeAdd(CollectionItem parent, Set<ContentItem> contentItems) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void afterAdd(CollectionItem parent, Set<ContentItem> contentItems) {
        // TODO Auto-generated method stub
        
    }

}
