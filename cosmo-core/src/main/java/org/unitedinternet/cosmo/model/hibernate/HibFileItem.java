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
import java.io.IOException;
import java.io.InputStream;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
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
   
    public byte[] getContent() {
        if (this.contentData != null) {
            return this.contentData.getContent();
        }
        return null;
    }

    @Override
    public void setContent(byte[] content) {
        if (content == null) {
            return;
        }
        if (content.length > MAX_CONTENT_SIZE) {
            throw new DataSizeException("Item content too large");
        }        
        try {
            setContent(new ByteArrayInputStream(content));
        } catch (IOException e) {
            throw new CosmoIOException("Error setting content", e);
        }
    }
    
    @Override
    public void setContent(InputStream is) throws IOException {
        if (contentData == null) {
            contentData = new HibContentData();
        }
        
        byte[] content = IOUtils.toByteArray(is);
        if (content.length > MAX_CONTENT_SIZE) {
            throw new DataSizeException("Item content too large");
        }
        this.contentData.setContent(content);
        this.setContentLength(contentData.getSize());
    }
    
    public void clearContent() {
        this.contentData = null;
    }
    
    @Override
    public InputStream getContentInputStream() {
        if (contentData == null) {
            return null;
        } else {
            return new ByteArrayInputStream(contentData.getContent());
        }
    }
    
    @Override
    public String getContentEncoding() {
        return contentEncoding;
    }

    @Override
    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    @Override
    public String getContentLanguage() {
        return contentLanguage;
    }

    @Override
    public void setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
    }

    @Override
    public Long getContentLength() {
        return contentLength;
    }


    @Override
    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }


    @Override
    public String getContentType() {
        return contentType;
    }


    @Override
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
