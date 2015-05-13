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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.model.AuditableObject;
import org.unitedinternet.cosmo.model.EntityFactory;

/**
 * Extends BaseModelObject and adds creationDate, modifiedDate
 * to track when Object was created and modified.
 */
public abstract class MockAuditableObject implements AuditableObject {

    private static final ThreadLocal<MessageDigest> ETAG_DIGEST_LOCAL = new ThreadLocal<MessageDigest>();
    private static final EntityFactory FACTORY = new MockEntityFactory();
    
    private Date creationDate;
    private Date modifiedDate;
    private String etag = "";
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceAuditableObject#getCreationDate()
     */
    /**
     * Gets creation date.
     * @return The date.
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceAuditableObject#setCreationDate(java.util.Date)
     */
    /**
     * Sets creation date.
     * @param creationDate The creation date.
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceAuditableObject#getModifiedDate()
     */
    /**
     * Gets modified date.
     * @return the date.
     */
    public Date getModifiedDate() {
        return modifiedDate;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceAuditableObject#setModifiedDate(java.util.Date)
     */
    /**
     * Sets modified date.
     * @param modifiedDate Modified date.
     */
    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceAuditableObject#updateTimestamp()
     */
    /**
     * Updates timestamp.
     */
    public void updateTimestamp() {
        modifiedDate = new Date();
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceAuditableObject#getEntityTag()
     */
    /**
     * Gets entity tag.
     * @return tag.
     */
    public String getEntityTag() {
        return etag;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceAuditableObject#setEntityTag(java.lang.String)
     */
    /**
     * Sets entity tag.
     * @param etag The tag.
     */
    public void setEntityTag(String etag) {
        this.etag = etag;
    }
    
    /**
     * <p>
     * Calculates updates object's entity tag.
     * Returns calculated entity tag.  
     * </p>
     * <p>
     * This implementation simply returns the empty string. Subclasses should
     * override it when necessary.
     * </p>
     * 
     * Subclasses should override
     * this.
     * @return ETAG
     */
    public abstract String calculateEntityTag();

    /**
     * <p>
     * Returns a Base64-encoded SHA-1 digest of the provided bytes.
     * </p>
     * @param bytes 
     * @return Encode entity tag.
     */
    protected static String encodeEntityTag(byte[] bytes) {
        
        // Use MessageDigest stored in threadlocal so that each
        // thread has its own instance.
        MessageDigest md = ETAG_DIGEST_LOCAL.get();
        
        if(md==null) {
            try {
                // initialize threadlocal
                md = MessageDigest.getInstance("sha1");
                ETAG_DIGEST_LOCAL.set(md);
            } catch (NoSuchAlgorithmException e) {
                throw new CosmoException("Platform does not support sha1?", e);
            }
        }
        
        return Base64.encodeBase64String(md.digest(bytes));
    }
    
    /**
     * Gets factory.
     * {@inheritDoc}
     * @return entity factory.
     */
    public EntityFactory getFactory() {
        return FACTORY;
    }
}
