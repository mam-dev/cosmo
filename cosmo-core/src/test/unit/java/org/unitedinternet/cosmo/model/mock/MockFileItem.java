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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.unitedinternet.cosmo.CosmoIOException;
import org.unitedinternet.cosmo.model.DataSizeException;
import org.unitedinternet.cosmo.model.FileItem;
import org.unitedinternet.cosmo.model.Item;

/**
 * Extends {@link Item} to represent an item containing binary content.
 */
public class MockFileItem extends MockContentItem implements FileItem {

    
    private String contentType = null;
    
   
    private String contentLanguage = null;
    

    private String contentEncoding = null;
    
   
    private Long contentLength = null;
    
    private MockContentData contentData = null;
    
    /**
     * Contructor.
     */
    public MockFileItem() {
    }

   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceFileItem#getContent()
     */
    /**
     * Gets content.
     * @return The content.
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
     * @see org.unitedinternet.cosmo.model.copy.InterfaceFileItem#setContent(byte[])
     */
    /**
     * Sets content.
     * @param content The content.
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
     * @see org.unitedinternet.cosmo.model.copy.InterfaceFileItem#clearContent()
     */
    /**
     * Clears content.
     */
    public void clearContent() {
        contentData = null;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceFileItem#setContent(java.io.InputStream)
     */
    /**
     * Sets content.
     * @param is The input stream.
     * @throws IOException - if something is wrong this exception is thrown.
     */
    public void setContent(InputStream is) throws IOException {
        if(contentData==null) {
            contentData = new MockContentData(); 
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
     * @see org.unitedinternet.cosmo.model.copy.InterfaceFileItem#getContentInputStream()
     */
    /**
     * Gets content input stream.
     * @return input stream.
     */
    public InputStream getContentInputStream() {
        if (contentData == null) {
            return null;
        }
        else {
            return contentData.getContentInputStream();
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceFileItem#getContentEncoding()
     */
    /**
     * Gets content encoding.
     * @return The content encoding.
     */
    public String getContentEncoding() {
        return contentEncoding;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceFileItem#setContentEncoding(java.lang.String)
     */
    /**
     * Sets content encoding.
     * @param contentEncoding The encoding content.
     */
    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceFileItem#getContentLanguage()
     */
    /**
     * Gets content language.
     * @return The content language.
     */
    public String getContentLanguage() {
        return contentLanguage;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceFileItem#setContentLanguage(java.lang.String)
     */
    /**
     * Sets content language.
     * @param contentLanguage The language content.
     */
    public void setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceFileItem#getContentLength()
     */
    /**
     * Gets content length.
     * @return The content length.
     */
    public Long getContentLength() {
        return contentLength;
    }


    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceFileItem#setContentLength(java.lang.Long)
     */
    /**
     * Sets content length.
     * @param contentLength The content length.
     */
    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }


    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceFileItem#getContentType()
     */
    /**
     * Gets content type.
     * @return The content type.
     */
    public String getContentType() {
        return contentType;
    }


    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceFileItem#setContentType(java.lang.String)
     */
    /**
     * Sets content type.
     * @param contentType The content type.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    /**
     * Copy.
     * {@inheritDoc}
     * @return The item.
     */
    public Item copy() {
        FileItem copy = new MockFileItem();
        copyToItem(copy);
        return copy;
    }
    
    /**
     * Copy to item.
     * {@inheritDoc}
     * @param item The item.
     */
    @Override
    protected void copyToItem(Item item) {
        if (!(item instanceof FileItem)) {
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
     * toString.
     * @return The string.
     */
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append(
                "contentLength", getContentLength()).append("contentType",
                getContentType()).append("contentEncoding",
                getContentEncoding()).append("contentLanguage",
                getContentLanguage()).toString();
    }
}
