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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.ProtectedPropertyModificationException;
import org.unitedinternet.cosmo.dav.caldav.InvalidCalendarLocationException;
import org.unitedinternet.cosmo.dav.caldav.UidConflictException;
import org.unitedinternet.cosmo.dav.caldav.report.FreeBusyReport;
import org.unitedinternet.cosmo.dav.caldav.report.MultigetReport;
import org.unitedinternet.cosmo.dav.caldav.report.QueryReport;
import org.unitedinternet.cosmo.dav.io.DavInputContext;
import org.unitedinternet.cosmo.dav.property.ContentLength;
import org.unitedinternet.cosmo.dav.property.ContentType;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.ICalendarItem;
import org.unitedinternet.cosmo.model.IcalUidInUseException;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.util.ContentTypeUtil;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VFreeBusy;

/**
 * Abstract calendar resource.
 */
public abstract class DavCalendarResource extends DavContentBase implements ICalendarConstants {
    
    private static final Logger LOG = LoggerFactory.getLogger(DavCalendarResource.class);
    
    private static final Set<ReportType> REPORT_TYPES =
        new HashSet<ReportType>();
    
    static {
        registerLiveProperty(DavPropertyName.GETCONTENTLENGTH);
        registerLiveProperty(DavPropertyName.GETCONTENTTYPE);

        REPORT_TYPES.add(FreeBusyReport.REPORT_TYPE_CALDAV_FREEBUSY);
        REPORT_TYPES.add(MultigetReport.REPORT_TYPE_CALDAV_MULTIGET);
        REPORT_TYPES.add(QueryReport.REPORT_TYPE_CALDAV_QUERY);
    }

    public DavCalendarResource(ContentItem item,
                               DavResourceLocator locator,
                               DavResourceFactory factory,
                               EntityFactory entityFactory)
        throws CosmoDavException {
        super(item, locator, factory, entityFactory);
    }
       
    // WebDavResource methods

    public String getSupportedMethods() {
        if(exists()) {
            return "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, COPY, PUT, DELETE, MOVE, MKTICKET, DELTICKET, REPORT";
        }
        else {
            return "OPTIONS, TRACE, PUT, MKCOL";
        }
    }

    @Override
    public void copy(org.apache.jackrabbit.webdav.DavResource destination,
            boolean shallow) throws org.apache.jackrabbit.webdav.DavException {
        validateDestination(destination);
        try {
            super.copy(destination, shallow);
        } catch (IcalUidInUseException e) {
            throw new UidConflictException(e);
        }
    }

    @Override
    public void move(org.apache.jackrabbit.webdav.DavResource destination)
            throws org.apache.jackrabbit.webdav.DavException {
        try {
            super.move(destination);
        } catch (IcalUidInUseException e) {
            throw new UidConflictException(e);
        }
    }

    // DavResourceBase methods

    @Override
    protected void populateItem(InputContext inputContext)
        throws CosmoDavException {
        super.populateItem(inputContext);

        DavInputContext dic = (DavInputContext) inputContext;
        Calendar calendar = dic.getCalendar();

        setCalendar(calendar);
    }

    // our methods

    /**
     * @return true if this resource matches the given filter.
     */
    public boolean matches(CalendarFilter filter)
        throws CosmoDavException {
        return getCalendarQueryProcesor().filterQuery((NoteItem)getItem(), filter);
    }

    /**
     * Returns a VFREEBUSY component containing
     * the freebusy periods for the resource for the specified time range.
     * @param period time range for freebusy information
     * @return VFREEBUSY component containing FREEBUSY periods for
     *         specified timerange
     */
    public VFreeBusy generateFreeBusy(Period period) {
        return getCalendarQueryProcesor().
            freeBusyQuery((ICalendarItem)getItem(), period);
    }

    /**
     * @return The calendar object associated with this resource.
     */
    public abstract Calendar getCalendar();
    
    /**
     * Set the calendar object associated with this resource.
     * @param calendar calendar object parsed from inputcontext
     */
    protected abstract void setCalendar(Calendar calendar)
        throws CosmoDavException;

    private void validateDestination(org.apache.jackrabbit.webdav.DavResource destination)
        throws CosmoDavException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Validating destination {}", destination.getResourcePath());
        }

        // XXX: we should allow items to be moved/copied out of
        // calendar collections into regular collections, but they
        // need to be stripped of their calendar-ness
        if ( destination instanceof DavItemResource && 
             ! (((DavItemResource)destination).getParent() instanceof DavCalendarCollection)) {
            throw new InvalidCalendarLocationException("Destination collection must be a calendar collection");
        }
    }

    public void writeTo(OutputContext outputContext)
        throws CosmoDavException, IOException {
        if (! exists()) {
            throw new IllegalStateException("cannot spool a nonexistent resource");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Spooling file {}", getResourcePath());
        }

        String contentType =
            ContentTypeUtil.buildContentType(ICALENDAR_MEDIA_TYPE, "UTF-8");
        outputContext.setContentType(contentType);
  
        // Get calendar
        Calendar calendar = getCalendar();
        
        // convert Calendar object to String, then to bytes (UTF-8)    
        byte[] calendarBytes = calendar.toString().getBytes("UTF-8");
        outputContext.setContentLength(calendarBytes.length);
        outputContext.setModificationTime(getModificationTime());
        outputContext.setETag(getETag());
        
        if (! outputContext.hasStream()) {
            return;
        }

        // spool calendar bytes
        ByteArrayInputStream bois = new ByteArrayInputStream(calendarBytes);
        FileCopyUtils.copy(bois, outputContext.getOutputStream());
    }

    public Set<ReportType> getReportTypes() {
        return REPORT_TYPES;
    }

    /** */
    protected void loadLiveProperties(DavPropertySet properties) {
        super.loadLiveProperties(properties);

        try {
            byte[] calendarBytes = getCalendar().toString().getBytes("UTF-8");
            properties.add(new ContentLength(Long.valueOf(calendarBytes.length)));
        } catch (Exception e) {
            throw new CosmoException("Can't convert calendar", e);
        }

        properties.add(new ContentType(ICALENDAR_MEDIA_TYPE, "UTF-8"));
    }

    /** */
    protected void setLiveProperty(WebDavProperty property, boolean create)
        throws CosmoDavException {
        super.setLiveProperty(property, create);

        DavPropertyName name = property.getName();
        if (name.equals(DavPropertyName.GETCONTENTTYPE)) {
            throw new ProtectedPropertyModificationException(name);
        }
    }
}
