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
package org.unitedinternet.cosmo.dav.impl;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavContent;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.ForbiddenException;
import org.unitedinternet.cosmo.dav.property.ContentLanguage;
import org.unitedinternet.cosmo.dav.property.ContentLength;
import org.unitedinternet.cosmo.dav.property.ContentType;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.model.DataSizeException;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.FileItem;
import org.unitedinternet.cosmo.util.ContentTypeUtil;

/**
 * Extends <code>DavResourceBase</code> to adapt the Cosmo
 * <code>FileItem</code> to the DAV resource model.
 *
 * This class defines the following live properties:
 *
 * <ul>
 * <li><code>DAV:getcontentlanguage</code></li>
 * <li><code>DAV:getcontentlength</code> (protected)</li>
 * <li><code>DAV:getcontenttype</code></li>
 * </ul>
 *
 * @see DavContent
 * @see FileItem
 */
public class DavFile extends DavContentBase {
    private static final Logger LOG = LoggerFactory.getLogger(DavFile.class);

    static {
        registerLiveProperty(DavPropertyName.GETCONTENTLANGUAGE);
        registerLiveProperty(DavPropertyName.GETCONTENTLENGTH);
        registerLiveProperty(DavPropertyName.GETCONTENTTYPE);
    }

    /** */
    public DavFile(FileItem item,
                   DavResourceLocator locator,
                   DavResourceFactory factory,
                   EntityFactory entityFactory)
        throws CosmoDavException {
        super(item, locator, factory, entityFactory);
    }

    /** */
    public DavFile(DavResourceLocator locator,
                   DavResourceFactory factory,
                   EntityFactory entityFactory)
        throws CosmoDavException {
        this(entityFactory.createFileItem(), locator, factory, entityFactory);
    }

    // WebDavResource

    public void writeTo(OutputContext outputContext)
        throws CosmoDavException, IOException {
        if (! exists()) {
            throw new IllegalStateException("cannot spool a nonexistent resource");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Spooling file {}", getResourcePath());
        }

        FileItem content = (FileItem) getItem();

        String contentType =
            ContentTypeUtil.buildContentType(content.getContentType(),
                                    content.getContentEncoding());
        outputContext.setContentType(contentType);

        if (content.getContentLanguage() != null) {
            outputContext.setContentLanguage(content.getContentLanguage());
        }

        long len = content.getContentLength() != null ?
            content.getContentLength().longValue() : 0;
        outputContext.setContentLength(len);
        outputContext.setModificationTime(getModificationTime());
        outputContext.setETag(getETag());

        if (! outputContext.hasStream()) {
            return;
        }
        if (content.getContentInputStream() == null) {
            return;
        }

        FileCopyUtils.copy(content.getContentInputStream(),
                     outputContext.getOutputStream());
    }

    
    /** */
    protected void populateItem(InputContext inputContext)
        throws CosmoDavException {
        super.populateItem(inputContext);

        FileItem file = (FileItem) getItem();

        try {
            InputStream content = inputContext.getInputStream();
            if (content != null) {
                file.setContent(content);
            }

            if (inputContext.getContentLanguage() != null) {
                file.setContentLanguage(inputContext.getContentLanguage());
            }

            String contentType = inputContext.getContentType();
            if (contentType != null) {
                file.setContentType(ContentTypeUtil.getMimeType(contentType));
            }
            else {
                file.setContentType(ContentTypeUtil.getMimeType(file.getName()));
            }
            String contentEncoding = ContentTypeUtil.getEncoding(contentType);
            if (contentEncoding != null) {
                file.setContentEncoding(contentEncoding);
            }
        } catch (IOException e) {
            throw new CosmoDavException(e);
        } catch (DataSizeException e) {
            throw new ForbiddenException(e.getMessage());
        }
    }

    /** */
    protected void loadLiveProperties(DavPropertySet properties) {
        super.loadLiveProperties(properties);

        FileItem content = (FileItem) getItem();
        if (content == null) {
            return;
        }

        if (content.getContentLanguage() != null) {
            properties.add(new ContentLanguage(content.getContentLanguage()));
        }
        properties.add(new ContentLength(content.getContentLength()));
        properties.add(new ContentType(content.getContentType(),
                                       content.getContentEncoding()));
    }

    /** */
    protected void setLiveProperty(WebDavProperty property, boolean create)
        throws CosmoDavException {
        super.setLiveProperty(property, create);

        FileItem content = (FileItem) getItem();
        if (content == null) {
            return;
        }

        DavPropertyName name = property.getName();
        String text = property.getValueText();

        if (name.equals(DavPropertyName.GETCONTENTLANGUAGE)) {
            content.setContentLanguage(text);
            return;
        }

        if (name.equals(DavPropertyName.GETCONTENTTYPE)) {
            String type = ContentTypeUtil.getMimeType(text);
            if (StringUtils.isBlank(type)) {
                throw new BadRequestException("Property " + name + " requires a valid media type");
            }
            content.setContentType(type);
            content.setContentEncoding(ContentTypeUtil.getEncoding(text));
        }
    }

    /** */
    protected void removeLiveProperty(DavPropertyName name)
        throws CosmoDavException {
        super.removeLiveProperty(name);

        FileItem content = (FileItem) getItem();
        if (content == null) {
            return;
        }

        if (name.equals(DavPropertyName.GETCONTENTLANGUAGE)) {
            content.setContentLanguage(null);
            return;
        }
    }

    @Override
    public boolean isCollection() {
        return false;
    }
}
