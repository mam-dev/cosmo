/*
 * Copyright 2006-2007 Open Source Applications Foundation
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavContent;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.LockedException;
import org.unitedinternet.cosmo.dav.ProtectedPropertyModificationException;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.model.CollectionLockedException;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.MessageStamp;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.TriageStatusUtil;
import org.unitedinternet.cosmo.model.User;

/**
 * Extends <code>DavItemResourceBase</code> to adapt the Cosmo
 * <code>ContentItem</code> to the DAV resource model.
 *
 * This class defines the following live properties:
 *
 * <ul>
 * <li><code>DAV:getetag</code> (protected)</li>
 * <li><code>DAV:getlastmodified</code> (protected)</li>
 * </ul>
 *
 * @see DavContent
 * @see DavResourceBase
 * @see ContentItem
 */
public abstract class DavContentBase extends DavItemResourceBase
    implements DavItemContent {
    
    private static final Set<String> DEAD_PROPERTY_FILTER =
        new HashSet<String>();

    static {

        DEAD_PROPERTY_FILTER.add(NoteItem.class.getName());
        DEAD_PROPERTY_FILTER.add(MessageStamp.class.getName());
    }

    /** */
    public DavContentBase(ContentItem item,
                          DavResourceLocator locator,
                          DavResourceFactory factory,
                          EntityFactory entityFactory)
        throws CosmoDavException {
        super(item, locator, factory, entityFactory);
    }

    // Jackrabbit WebDavResource

    /** */
    public abstract boolean isCollection();

    /** */
    public String getSupportedMethods() {
        return "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, COPY, PUT, DELETE, MOVE, MKTICKET, DELTICKET";
    }
    

    public void addMember(org.apache.jackrabbit.webdav.DavResource member,
                          InputContext inputContext)
        throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public DavResourceIterator getMembers() {
        // while it would be ideal to throw an UnsupportedOperationException,
        // MultiStatus tries to add a MultiStatusResponse for every member
        // of a WebDavResource regardless of whether or not it's a collection,
        // so we need to return an empty iterator.
        return new DavResourceIteratorImpl(new ArrayList<DavResource>());
    }

    public void removeMember(org.apache.jackrabbit.webdav.DavResource member)
        throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    // our methods

    protected Set<QName> getResourceTypes() {
        return new HashSet<QName>();
    }

    /** */
    protected void populateItem(InputContext inputContext)
        throws CosmoDavException {
        super.populateItem(inputContext);

        ContentItem content = (ContentItem) getItem();
        
        User user = getSecurityManager().getSecurityContext().getUser();
        content.setLastModifiedBy(user != null ? user.getEmail() : "");

        if (content.getUid() == null) {
            content.setTriageStatus(TriageStatusUtil.initialize(content
                    .getFactory().createTriageStatus()));
            content.setLastModification(ContentItem.Action.CREATED);
            content.setSent(Boolean.FALSE);
            content.setNeedsReply(Boolean.FALSE);
        } else {
            content.setLastModification(ContentItem.Action.EDITED);
        }
    }

    /** */
    protected void setLiveProperty(WebDavProperty property, boolean create)
        throws CosmoDavException {
        super.setLiveProperty(property, create);

        ContentItem content = (ContentItem) getItem();
        if (content == null) {
            return;
        }

        DavPropertyName name = property.getName();
        if (name.equals(DavPropertyName.GETCONTENTLENGTH)) {
            throw new ProtectedPropertyModificationException(name);
        }

        // content type is settable by subclasses
    }

    /** */
    protected void removeLiveProperty(DavPropertyName name, boolean create)
        throws CosmoDavException {
        super.removeLiveProperty(name);

        ContentItem content = (ContentItem) getItem();
        if (content == null) {
            return;
        }

        if (name.equals(DavPropertyName.GETCONTENTLENGTH) ||
            name.equals(DavPropertyName.GETCONTENTTYPE)) {
            throw new ProtectedPropertyModificationException(name);
        }
    }

    /** */
    protected Set<String> getDeadPropertyFilter() {
        return DEAD_PROPERTY_FILTER;
    }

    @Override
    protected void updateItem() throws CosmoDavException {
        try {
            getContentService().updateContent((ContentItem) getItem());
        } catch (CollectionLockedException e) {
            throw new LockedException();
        }

    }
    
    
}
