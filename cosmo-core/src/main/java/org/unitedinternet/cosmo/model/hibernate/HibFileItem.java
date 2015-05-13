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
package org.unitedinternet.cosmo.model.hibernate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.unitedinternet.cosmo.CosmoIOException;
import org.unitedinternet.cosmo.model.DataSizeException;
import org.unitedinternet.cosmo.model.FileItem;
import org.unitedinternet.cosmo.model.Item;

/**
 * Hibernate persistent FileItem.
 */
@Entity
@DiscriminatorValue("file")
public class HibFileItem extends HibContentItem implements FileItem {

    
    /**
     * 
     */
    private static final long serialVersionUID = -3829504638044059875L;

    @Column(name = "contentType", length=64)
    private String contentType = null;
    
    @Column(name = "contentLanguage", length=32)
    private String contentLanguage = null;
    
    @Column(name = "contentEncoding", length=32)
    private String contentEncoding = null;
    
    @Column(name = "contentLength")
    private Long contentLength = null;
    
    @OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
    @JoinColumn(name="contentdataid")
    private HibContentData contentData = null;
    
    public HibFileItem() {
    }

   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.FileItem#getContent()
     */
    public byte[] getContent() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            InputStream contentStream = contentData.getContentInputStream();
            IOUtils.copy(contentStream, bos);
            contentStream.close();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new CosmoIOException("Error getting content", e);
        }
    }


    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.FileItem#setContent(byte[])
     */
    public void setContent(byte[] content) {
        if (content.length > MAX_CONTENT_SIZE) {
            throw new DataSizeException("Item content too large");
        }
        
        try {
            setContent(new ByteArrayInputStream(content));
        } catch (IOException e) {
            throw new CosmoIOException("Error setting content", e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.FileItem#clearContent()
     */
    public void clearContent() {
        contentData = null;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.FileItem#setContent(java.io.InputStream)
     */
    public void setContent(InputStream is) throws IOException {
        if(contentData==null) {
            contentData = new HibContentData(); 
        }
        
        contentData.setContentInputStream(is);
        
        // Verify size is not greater than MAX.
        // TODO: do this checking in ContentData.setContentInputStream()
        if (contentData.getSize() > MAX_CONTENT_SIZE) {
            throw new DataSizeException("Item content too large");
        }
        
        setContentLength(contentData.getSize());
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.FileItem#getContentInputStream()
     */
    public InputStream getContentInputStream() {
        if(contentData==null) {
            return null;
        }
        else {
            return contentData.getContentInputStream();
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.FileItem#getContentEncoding()
     */
    public String getContentEncoding() {
        return contentEncoding;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.FileItem#setContentEncoding(java.lang.String)
     */
    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.FileItem#getContentLanguage()
     */
    public String getContentLanguage() {
        return contentLanguage;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.FileItem#setContentLanguage(java.lang.String)
     */
    public void setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.FileItem#getContentLength()
     */
    public Long getContentLength() {
        return contentLength;
    }


    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.FileItem#setContentLength(java.lang.Long)
     */
    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }


    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.FileItem#getContentType()
     */
    public String getContentType() {
        return contentType;
    }


    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.FileItem#setContentType(java.lang.String)
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public Item copy() {
        FileItem copy = new HibFileItem();
        copyToItem(copy);
        return copy;
    }
    
    @Override
    protected void copyToItem(Item item) {
        if(!(item instanceof FileItem)) {
            return;
        }
        
        super.copyToItem(item);
        
        FileItem contentItem = (FileItem) item;
        
        try {
            InputStream contentStream = getContentInputStream();
            if(contentStream!=null) {
                contentItem.setContent(contentStream);
                contentStream.close();
            }
            contentItem.setContentEncoding(getContentEncoding());
            contentItem.setContentLanguage(getContentLanguage());
            contentItem.setContentType(getContentType());
            contentItem.setContentLength(getContentLength());
        } catch (IOException e) {
            throw new CosmoIOException("Error copying content", e);
        }
    }

    /**
     */
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append(
                "contentLength", getContentLength()).append("contentType",
                getContentType()).append("contentEncoding",
                getContentEncoding()).append("contentLanguage",
                getContentLanguage()).toString();
    }
}
