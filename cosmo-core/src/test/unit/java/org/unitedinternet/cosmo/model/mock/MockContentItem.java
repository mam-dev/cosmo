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
package org.unitedinternet.cosmo.model.mock;

import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.TriageStatus;

/**
 * Extends {@link Item} to represent an abstract
 * content item.
 */
public abstract class MockContentItem extends MockItem implements ContentItem {

    private String lastModifiedBy = null;
  
    private Integer lastModification = null;
    
   
    private TriageStatus triageStatus = new MockTriageStatus();
    
    private Boolean sent = null;
    
   
    private Boolean needsReply = null;
    
    /**
     * Constructor.
     */
    public MockContentItem() {
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceContentItem#getLastModifiedBy()
     */
    /**
     * Gets last modified by.
     * @return lastModifiedBy
     */
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceContentItem#setLastModifiedBy(java.lang.String)
     */
    /**
     * Sets last modified by.
     * @param lastModifiedBy Last modified by.
     */
    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceContentItem#getLastModification()
     */
    /**
     * Gets last modification.
     * @return The last modification.
     */
    public Integer getLastModification() {
        return lastModification;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceContentItem#setLastModification(java.lang.Integer)
     */
    /**
     * Sets last modification.
     * @param lastModification The last modification.
     */
    public void setLastModification(Integer lastModification) {
        this.lastModification = lastModification;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceContentItem#getTriageStatus()
     */
    /**
     * Gets triageStatus.
     * @return triageStatus TriageStatus.
     */
    public TriageStatus getTriageStatus() {
        return triageStatus;
    }
  
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceContentItem#setTriageStatus(org.unitedinternet.cosmo.model.copy.TriageStatus)
     */
    /**
     * Sets triageStatus.
     * @param ts triageStatus.
     */
    public void setTriageStatus(TriageStatus ts) {
        triageStatus = ts;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceContentItem#getSent()
     */
    /**
     * Gets sent.
     * @return boolean.
     */
    public Boolean getSent() {
        return sent;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceContentItem#setSent(java.lang.Boolean)
     */
    /**
     * Sets sent.
     * @param sent sent.
     */
    public void setSent(Boolean sent) {
        this.sent = sent;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceContentItem#getNeedsReply()
     */
    /**
     * Gets needs reply.
     * @return boolean.
     */
    public Boolean getNeedsReply() {
        return needsReply;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceContentItem#setNeedsReply(java.lang.Boolean)
     */
    /**
     * Sets need reply.
     * @param needsReply Needs reply.
     */
    public void setNeedsReply(Boolean needsReply) {
        this.needsReply = needsReply;
    }
    
    /**
     * Copy to item.
     * {@inheritDoc}
     * @param item The item.
     */
    @Override
    protected void copyToItem(Item item) {
        if (!(item instanceof ContentItem)) {
            return;
        }
        
        super.copyToItem(item);
        
        ContentItem contentItem = (ContentItem) item;
        
        contentItem.setLastModifiedBy(getLastModifiedBy());
        contentItem.setLastModification(getLastModification());
        contentItem.setTriageStatus(getTriageStatus());
        contentItem.setSent(getSent());
        contentItem.setNeedsReply(getNeedsReply());
    }
}
