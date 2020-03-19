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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavContent;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.LockedException;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.acl.report.PrincipalMatchReport;
import org.unitedinternet.cosmo.dav.acl.report.PrincipalPropertySearchReport;
import org.unitedinternet.cosmo.dav.caldav.report.FreeBusyReport;
import org.unitedinternet.cosmo.dav.caldav.report.MultigetReport;
import org.unitedinternet.cosmo.dav.caldav.report.QueryReport;
import org.unitedinternet.cosmo.dav.property.ExcludeFreeBusyRollup;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionLockedException;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.util.ContentTypeUtil;
import org.unitedinternet.cosmo.util.DomWriter;
import org.w3c.dom.Element;

/**
 * Extends <code>DavResourceBase</code> to adapt the Cosmo
 * <code>CollectionItem</code> to the DAV resource model.
 * 
 * This class defines the following live properties:
 *
 * <ul>
 * <li><code>DAV:supported-report-set</code> (protected)</li>
 * <li><code>cosmo:exclude-free-busy-rollup</code></li>
 * </ul>
 *
 * @see DavResourceBase
 * @see CollectionItem
 */
public class DavCollectionBase extends DavItemResourceBase implements DavItemCollection {
    
    private static final Logger LOG = LoggerFactory.getLogger(DavCollectionBase.class);
    
    private static final Set<String> DEAD_PROPERTY_FILTER = new HashSet<String>();
    private static final Set<ReportType> REPORT_TYPES = new HashSet<ReportType>();

    private List<org.apache.jackrabbit.webdav.DavResource> members;

    static {
        registerLiveProperty(EXCLUDEFREEBUSYROLLUP);

        REPORT_TYPES.add(FreeBusyReport.REPORT_TYPE_CALDAV_FREEBUSY);
        REPORT_TYPES.add(MultigetReport.REPORT_TYPE_CALDAV_MULTIGET);
        REPORT_TYPES.add(QueryReport.REPORT_TYPE_CALDAV_QUERY);
        REPORT_TYPES.add(PrincipalMatchReport.REPORT_TYPE_PRINCIPAL_MATCH);
        REPORT_TYPES
                .add(PrincipalPropertySearchReport.REPORT_TYPE_PRINCIPAL_PROPERTY_SEARCH);

        DEAD_PROPERTY_FILTER.add(CollectionItem.class.getName());
    }

    public DavCollectionBase(CollectionItem collection,
            DavResourceLocator locator, DavResourceFactory factory,
            EntityFactory entityFactory) throws CosmoDavException {
        super(collection, locator, factory, entityFactory);
        members = new ArrayList<org.apache.jackrabbit.webdav.DavResource>();
    }

    public DavCollectionBase(DavResourceLocator locator,
            DavResourceFactory factory, EntityFactory entityFactory)
            throws CosmoDavException {
        this(entityFactory.createCollection(), locator, factory, entityFactory);
    }

    // Jackrabbit WebDavResource

    public String getSupportedMethods() {
        // If resource doesn't exist, then options are limited
        if (!exists()) {
            return "OPTIONS, TRACE, PUT, MKCOL, MKCALENDAR";
        } else {
            return "OPTIONS, GET, HEAD, PROPFIND, PROPPATCH, TRACE, COPY, DELETE, MOVE, MKTICKET, DELTICKET, REPORT";
        }
    }

    public boolean isCollection() {
        return true;
    }

    public void spool(OutputContext outputContext) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void addMember(org.apache.jackrabbit.webdav.DavResource member,
            InputContext inputContext)
            throws org.apache.jackrabbit.webdav.DavException {
        throw new UnsupportedOperationException();
    }

    public DavResourceIterator getMembers() {
        try {
            for (Item memberItem : ((CollectionItem) getItem()).getChildren()) {
                WebDavResource resource = memberToResource(memberItem);
                if (resource != null) {
                    members.add(resource);
                }
            }

            return new DavResourceIteratorImpl(members);
        } catch (CosmoDavException e) {
            throw new CosmoException(e);
        }
    }

    public DavResourceIterator getCollectionMembers() {
        try {
            Set<CollectionItem> collectionItems = getContentService().findCollectionItems((CollectionItem) getItem());
            for (Item memberItem : collectionItems) {
                WebDavResource resource = memberToResource(memberItem);
                if (resource != null) {
                    members.add(resource);
                }
            }
            return new DavResourceIteratorImpl(members);
        } catch (CosmoDavException e) {
            throw new CosmoException(e);
        }
    }
    
    public void removeMember(org.apache.jackrabbit.webdav.DavResource member)
            throws org.apache.jackrabbit.webdav.DavException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removing resource{} from {}", member.getDisplayName(), getDisplayName());
        }
        if(!(member instanceof DavItemResource)){
            throw new IllegalArgumentException("Expected 'member' as instance of: [" + DavItemResource.class.getName() +"]");
        }
        CollectionItem collection = (CollectionItem) getItem();
        Item item = ((DavItemResource) member).getItem();

        try {
            if (item instanceof CollectionItem) {
                getContentService().removeCollection((CollectionItem) item);
            } else {
                getContentService().removeItemFromCollection(item, collection);
            }
        } catch (CollectionLockedException e) {
            throw new LockedException();
        }

        members.remove(member);
    }

    // WebDavResource

    public void writeTo(OutputContext out) throws CosmoDavException,
            IOException {
        writeHtmlDirectoryIndex(out);
    }

    // DavCollection

    public void addContent(DavContent content, InputContext context) throws CosmoDavException {
        if(!(content instanceof DavContentBase)) {
            throw new IllegalArgumentException("Expected instance of : [" + DavContentBase.class.getName() + "]");
        }
        
        DavContentBase base = (DavContentBase) content;
        base.populateItem(context);
        saveContent(base);
        members.add(base);
    }

    public MultiStatusResponse addCollection(DavCollection collection, DavPropertySet properties) throws CosmoDavException {
        if(!(collection instanceof DavCollectionBase)){
            throw new IllegalArgumentException("Expected instance of :[" + DavCollectionBase.class.getName() + "]");
        }
        
        DavCollectionBase base = (DavCollectionBase) collection;
        base.populateItem(null);
        MultiStatusResponse msr = base.populateAttributes(properties);
        if (!hasNonOK(msr)) {
            saveSubcollection(base);
            members.add(base);
        }
        return msr;
    }

    public WebDavResource findMember(String href) throws CosmoDavException {
        return memberToResource(href);
    }

    // DavItemCollection

    public boolean isCalendarCollection() {
        return false;
    }

    public boolean isHomeCollection() {
        return false;
    }

    public boolean isExcludedFromFreeBusyRollups() {
        return ((CollectionItem) getItem()).isExcludeFreeBusyRollup();
    }

    // our methods

    protected Set<QName> getResourceTypes() {
        HashSet<QName> rt = new HashSet<QName>(1);
        rt.add(RESOURCE_TYPE_COLLECTION);
        return rt;
    }

    public Set<ReportType> getReportTypes() {
        return REPORT_TYPES;
    }

    /** */
    protected void loadLiveProperties(DavPropertySet properties) {
        super.loadLiveProperties(properties);

        CollectionItem cc = (CollectionItem) getItem();
        if (cc == null) {
            return;
        }

        properties.add(new ExcludeFreeBusyRollup(cc.isExcludeFreeBusyRollup()));
    }

    /** */
    protected void setLiveProperty(WebDavProperty property, boolean create)
            throws CosmoDavException {
        super.setLiveProperty(property, create);

        CollectionItem cc = (CollectionItem) getItem();
        if (cc == null) {
            return;
        }

        DavPropertyName name = property.getName();
        if (property.getValue() == null) {
            throw new UnprocessableEntityException("Property " + name
                    + " requires a value");
        }

        if (name.equals(EXCLUDEFREEBUSYROLLUP)) {
            Boolean flag = Boolean.valueOf(property.getValueText());
            cc.setExcludeFreeBusyRollup(flag);
        }
    }

    /** */
    protected void removeLiveProperty(DavPropertyName name)
            throws CosmoDavException {
        super.removeLiveProperty(name);

        CollectionItem cc = (CollectionItem) getItem();
        if (cc == null) {
            return;
        }

        if (name.equals(EXCLUDEFREEBUSYROLLUP)) {
            cc.setExcludeFreeBusyRollup(false);
        }
    }

    /** */
    protected Set<String> getDeadPropertyFilter() {
        return DEAD_PROPERTY_FILTER;
    }

    /**
     * Saves the given collection resource to storage.
     */
    protected void saveSubcollection(DavItemCollection member)
            throws CosmoDavException {
        CollectionItem collection = (CollectionItem) getItem();
        CollectionItem subcollection = (CollectionItem) member.getItem();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating collection {}", member.getResourcePath());
        }

        try {
            subcollection = getContentService().createCollection(collection,
                    subcollection);
            member.setItem(subcollection);
        } catch (CollectionLockedException e) {
            throw new LockedException();
        }
    }

    /**
     * Saves the given content resource to storage.
     */
    protected void saveContent(DavItemContent member) throws CosmoDavException {
        CollectionItem collection = (CollectionItem) getItem();
        ContentItem content = (ContentItem) member.getItem();

        try {
            if (content.getCreationDate() != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Updating member {} ", member.getResourcePath());
                }

                content = getContentService().updateContent(content);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Creating member {}", member.getResourcePath());
                }

                content = getContentService()
                        .createContent(collection, content);
            }
        } catch (CollectionLockedException e) {
            throw new LockedException();
        }

        member.setItem(content);
    }

    protected WebDavResource memberToResource(Item item) throws CosmoDavException {
        String path;
        try {
            path = getResourcePath() + "/" + URLEncoder.encode(item.getName(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new CosmoDavException(e);
        }
        DavResourceLocator locator = getResourceLocator().getFactory()
                .createResourceLocatorByPath(getResourceLocator().getContext(),
                        path);
        return getResourceFactory().createResource(locator, item);
    }

    protected WebDavResource memberToResource(String uri) throws CosmoDavException {
        DavResourceLocator locator = getResourceLocator().getFactory()
                .createResourceLocatorByUri(getResourceLocator().getContext(),
                        uri);
        return getResourceFactory().resolve(locator);
    }

    private void writeHtmlDirectoryIndex(OutputContext context)
            throws CosmoDavException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Writing html directory index for {}", getDisplayName());
        }

        context.setContentType(ContentTypeUtil.buildContentType("text/html", "UTF-8"));
        context.setModificationTime(getModificationTime());
        context.setETag(getETag());

        if (!context.hasStream()) {
            return;
        }

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                context.getOutputStream(), "utf8"));
        try{
            writer.write("<html>\n<head><title>");
            String colName = getDisplayName() != null ? getDisplayName()
                    : "no name";
            writer.write(StringEscapeUtils.escapeHtml(colName));
    
            writer.write("</title></head>\n");
            writer.write("<body>\n");
            writer.write("<h1>");
    
            writer.write(StringEscapeUtils.escapeHtml(colName));
    
            writer.write("</h1>\n");
    
            WebDavResource parent = getParent();
            if (parent.exists()) {
                writer.write("Parent: <a href=\"");
                writer.write(parent.getResourceLocator().getHref(true));
                writer.write("\">");
                if (parent.getDisplayName() != null) {
                    writer.write(StringEscapeUtils.escapeHtml(parent
                            .getDisplayName()));
                } else {
                    writer.write("no name");
                }
                writer.write("</a></li>\n");
            }
    
            writer.write("<h2>Members</h2>\n");
            writer.write("<ul>\n");
            for (DavResourceIterator i = getMembers(); i.hasNext();) {
                WebDavResource child = (WebDavResource) i.nextResource();
                writer.write("<li><a href=\"");
                writer.write(child.getResourceLocator().getHref(
                        child.isCollection()));
                writer.write("\">");
                if (child.getDisplayName() != null) {
                    writer.write(StringEscapeUtils.escapeHtml(child
                            .getDisplayName()));
                } else {
                    writer.write("no name");
                }
                writer.write("</a></li>\n");
            }
            writer.write("</ul>\n");
    
            writer.write("<h2>Properties</h2>\n");
            writer.write("<dl>\n");
            for (DavPropertyIterator i = getProperties().iterator(); i.hasNext();) {
                WebDavProperty prop = (WebDavProperty) i.nextProperty();
                Object value = prop.getValue();
                String text = null;
                if (value instanceof Element) {
                    try {
                        text = DomWriter.write((Element) value);
                    } catch (XMLStreamException e) {
                        LOG.warn("Error serializing value for property "
                                + prop.getName());
                    }
                }
                if (text == null) {
                    text = prop.getValueText();
                }
                if (text == null) {
                    text = "-- no value --";
                }
                writer.write("<dt>");
                writer.write(StringEscapeUtils
                        .escapeHtml(prop.getName().toString()));
                writer.write("</dt><dd>");
                writer.write(StringEscapeUtils.escapeHtml(text));
                writer.write("</dd>\n");
            }
            writer.write("</dl>\n");
    
            User user = getSecurityManager().getSecurityContext().getUser();
            if (user != null) {
                writer.write("<p>\n");
                if (!isHomeCollection()) {
                    DavResourceLocator homeLocator = getResourceLocator()
                            .getFactory().createHomeLocator(
                                    getResourceLocator().getContext(), user);
                    writer.write("<a href=\"");
                    writer.write(homeLocator.getHref(true));
                    writer.write("\">");
                    writer.write("Home collection");
                    writer.write("</a><br>\n");
                }
    
                DavResourceLocator principalLocator = getResourceLocator()
                        .getFactory().createPrincipalLocator(
                                getResourceLocator().getContext(), user);
                writer.write("<a href=\"");
                writer.write(principalLocator.getHref(false));
                writer.write("\">");
                writer.write("Principal resource");
                writer.write("</a><br>\n");
            }
    
            writer.write("</body>");
            writer.write("</html>\n");
        }finally{
            writer.close();
        }
    }

    @Override
    protected void updateItem() throws CosmoDavException {
        try {
            getContentService().updateCollection((CollectionItem) getItem());
        } catch (CollectionLockedException e) {
            throw new LockedException();
        }
    }

}