package org.unitedinternet.cosmo.dav.impl.parallel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.springframework.util.FileCopyUtils;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.LockedException;
import org.unitedinternet.cosmo.dav.ProtectedPropertyModificationException;
import org.unitedinternet.cosmo.dav.caldav.InvalidCalendarLocationException;
import org.unitedinternet.cosmo.dav.caldav.UidConflictException;
import org.unitedinternet.cosmo.dav.caldav.report.FreeBusyReport;
import org.unitedinternet.cosmo.dav.caldav.report.MultigetReport;
import org.unitedinternet.cosmo.dav.caldav.report.QueryReport;
import org.unitedinternet.cosmo.dav.impl.DavCalendarCollection;
import org.unitedinternet.cosmo.dav.impl.DavItemResource;
import org.unitedinternet.cosmo.dav.io.DavInputContext;
import org.unitedinternet.cosmo.dav.parallel.CalDavFile;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceFactory;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceLocator;
import org.unitedinternet.cosmo.dav.property.ContentLength;
import org.unitedinternet.cosmo.dav.property.ContentType;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.model.CollectionLockedException;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.ICalendarItem;
import org.unitedinternet.cosmo.model.IcalUidInUseException;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.util.ContentTypeUtil;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VFreeBusy;

public abstract class CalDavFileBase extends CalDavContentResourceBase implements CalDavFile {
	
	 public CalDavFileBase(Item item, 
			 				CalDavResourceLocator calDavResourceLocator,
			 				CalDavResourceFactory calDavResourceFactory, 
			 				EntityFactory entityFactory) {
		super(item, calDavResourceLocator, calDavResourceFactory, entityFactory);
	}


	private static final Log LOG = LogFactory.getLog(CalDavFileBase.class);
	 
	 public static final String ICALENDAR_MEDIA_TYPE = "text/calendar";
	 private static final Set<ReportType> REPORT_TYPES = new HashSet<ReportType>();
	    
	    static {
	        registerLiveProperty(DavPropertyName.GETCONTENTLENGTH);
	        registerLiveProperty(DavPropertyName.GETCONTENTTYPE);

	        REPORT_TYPES.add(FreeBusyReport.REPORT_TYPE_CALDAV_FREEBUSY);
	        REPORT_TYPES.add(MultigetReport.REPORT_TYPE_CALDAV_MULTIGET);
	        REPORT_TYPES.add(QueryReport.REPORT_TYPE_CALDAV_QUERY);
	    }

	    private CalendarQueryProcessor calendarQueryProcessor;
	    
	       
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
	    public void copy(DavResource destination,
	            boolean shallow) throws org.apache.jackrabbit.webdav.DavException {
	        validateDestination(destination);
	        try {
	            super.copy(destination, shallow);
	        } catch (IcalUidInUseException e) {
	            throw new UidConflictException(e);
	        }
	    }

	    @Override
	    public void move(DavResource destination) throws DavException {
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
	        return getCalendarQueryProcessor().filterQuery((NoteItem)getItem(), filter);
	    }

	    /**
	     * Returns a VFREEBUSY component containing
	     * the freebusy periods for the resource for the specified time range.
	     * @param period time range for freebusy information
	     * @return VFREEBUSY component containing FREEBUSY periods for
	     *         specified timerange
	     */
	    public VFreeBusy generateFreeBusy(Period period) {
	        return getCalendarQueryProcessor().freeBusyQuery((ICalendarItem)getItem(), period);
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
	            LOG.debug("validating destination " + destination.getResourcePath());
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
	            LOG.debug("spooling file " + getResourcePath());
	        }

	        String contentType = ContentTypeUtil.buildContentType(ICALENDAR_MEDIA_TYPE, "UTF-8");
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
	    
	    protected void removeLiveProperty(DavPropertyName name, boolean create) throws CosmoDavException {
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

	    public Set<ReportType> getReportTypes() {
	        return REPORT_TYPES;
	    }
	    
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
	    public void setLiveProperty(WebDavProperty property, boolean create)
	        throws CosmoDavException {
	        super.setLiveProperty(property, create);

	        DavPropertyName name = property.getName();
	        if (name.equals(DavPropertyName.GETCONTENTTYPE)) {
	            throw new ProtectedPropertyModificationException(name);
	        }
	    }
	    public CalendarQueryProcessor getCalendarQueryProcessor() {
	        return calendarQueryProcessor;
	    }
	    
	    protected Set<QName> getResourceTypes() {
	        return new HashSet<QName>();
	    }
	    

	    @Override
		public boolean isCollection() {
			return false;
		}
}
