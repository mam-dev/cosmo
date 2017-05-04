package org.unitedinternet.cosmo.dav.impl.parallel;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavServletRequest;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSession;
import org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.unitedinternet.cosmo.dav.NotFoundException;
import org.unitedinternet.cosmo.dav.parallel.CalDavRequest;
import org.unitedinternet.cosmo.dav.parallel.CalDavResource;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceFactory;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceLocator;
import org.unitedinternet.cosmo.icalendar.ICalendarClientFilterManager;
import org.unitedinternet.cosmo.model.AvailabilityItem;
import org.unitedinternet.cosmo.model.CalendarCollectionStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.FileItem;
import org.unitedinternet.cosmo.model.FreeBusyItem;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.UserIdentitySupplier;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.service.UserService;
import org.unitedinternet.cosmo.util.UriTemplate;

public class DefaultCalDavResourceFactory implements CalDavResourceFactory, ExtendedDavConstants{
	private ContentService contentService;
    private UserService userService;
    private CosmoSecurityManager securityManager;
    private EntityFactory entityFactory;
    private CalendarQueryProcessor calendarQueryProcessor;
    private ICalendarClientFilterManager clientFilterManager;
    private boolean schedulingEnabled = false;
    private UserIdentitySupplier userIdentitySupplier;
    
    
    public DefaultCalDavResourceFactory(ContentService contentService,
            UserService userService,
            CosmoSecurityManager securityManager,
            EntityFactory entityFactory,
            CalendarQueryProcessor calendarQueryProcessor,
            ICalendarClientFilterManager clientFilterManager,
            UserIdentitySupplier userIdentitySupplier,
            boolean schedulingEnabled) {

		this.contentService = contentService;
		this.userService = userService;
		this.securityManager = securityManager;
		this.entityFactory = entityFactory;
		this.calendarQueryProcessor = calendarQueryProcessor;
		this.clientFilterManager = clientFilterManager;
		this.userIdentitySupplier = userIdentitySupplier;
		this.schedulingEnabled = schedulingEnabled;
}


	@Override
	public DavResource createResource(DavResourceLocator locator, DavServletRequest request,
			DavServletResponse response) throws DavException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public DavResource createResource(DavResourceLocator locator, DavSession session) throws DavException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ContentService getContentService() {
        return contentService;
    }


	@Override
	public ICalendarClientFilterManager getClientFilterManager() {
        return clientFilterManager;
    }


	@Override
	public CalendarQueryProcessor getCalendarQueryProcessor() {
        return calendarQueryProcessor;
    }


	@Override
	public UserService getUserService() {
        return userService;
    }


	@Override
	public CosmoSecurityManager getSecurityManager() {
        return securityManager;
	}


	@Override
	public CalDavResource resolve(CalDavResourceLocator locator, CalDavRequest request) throws CosmoDavException {
		
		CalDavResource resource = resolve(locator);
		
        if (resource != null) {
            return resource;
        }

        // we didn't find an item in storage for the resource, so either
        // the request is creating a resource or the request is targeting a
        // nonexistent item.
        if (request.getMethod().equals("MKCALENDAR")) {
            return new CalendarCollection(entityFactory.createCollection(), locator, this, entityFactory);
        }
        if (request.getMethod().equals("MKCOL")) {
            return new DavCollectionBase(locator, this, entityFactory);
        }
        if (request.getMethod().equals("PUT")) {
            // will be replaced by the provider if a different resource
            // type is required
            CalDavResource parent = resolve(locator.getParentLocator());
            if (parent instanceof CalendarCollection) {
                return new EventFile(entityFactory.createNote(), locator, this, entityFactory);
            }
            return new CustomFile(entityFactory.createFileItem(), locator, this, entityFactory);
        }
        
        // handle OPTIONS for non-existent resource
        if(request.getMethod().equals("OPTIONS")) { 
            // ensure parent exists first
            CalDavResource parent = resolve(locator.getParentLocator());
            if(parent!=null && parent.exists()) {
                if(parent instanceof CalendarCollection) {
                    return new EventFile(entityFactory.createNote(),locator, this, entityFactory);
                }
                else {
                    return new DavCollectionBase(locator, this, entityFactory);
                }
            }
        }
    
        throw new NotFoundException();
	}


	@Override
	public CalDavResource resolve(CalDavResourceLocator locator) throws CosmoDavException {
		
		String uri = locator.getPath();
       

        UriTemplate.Match match = null;

        match = TEMPLATE_COLLECTION.match(uri);
        if (match != null) {
            return createUidResource(locator, match);
        }

        match = TEMPLATE_ITEM.match(uri);
        if (match != null) {
            return createUidResource(locator, match);
        }

        match = TEMPLATE_USERS.match(uri);
        if (match != null) {
            return new DavUserPrincipalCollection(locator, this);
        }

        match = TEMPLATE_USER.match(uri);
        if (match != null) {
            return createUserPrincipalResource(locator, match);
        }

        if(schedulingEnabled) {
            match = TEMPLATE_USER_INBOX.match(uri);
            if (match != null) {
                return new InboxCollection(locator, this);
            }
            
            match = TEMPLATE_USER_OUTBOX.match(uri);
            if (match != null) {
                return new OutboxCollection(locator, this);
            }
        }

        return createUnknownResource(locator, uri);
	}
	
	protected CalDavResource createUnknownResource(CalDavResourceLocator locator, String uri) throws CosmoDavException {
		Item item = contentService.findItemByPath(uri);
		return item != null ? createResource(locator, item) : null;
	}
	
	protected CalDavResource createUidResource(CalDavResourceLocator locator, UriTemplate.Match match) throws CosmoDavException {
		String uid = match.get("uid");
		String path = match.get("*");
		Item item = path != null ? contentService.findItemByPath(path, uid) : contentService.findItemByUid(uid);
		return item != null ? createResource(locator, item) : null;
	}

	 protected CalDavResource createUserPrincipalResource(CalDavResourceLocator locator, UriTemplate.Match match) throws CosmoDavException {
     User user = userService.getUser(match.get("username"));
     return user != null ? new DavUserPrincipal(user, locator, this, userIdentitySupplier) : null;
 }

	@Override
	public CalDavResource createResource(CalDavResourceLocator locator, Item item) throws CosmoDavException {
		if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        if (item instanceof HomeCollectionItem) {
            return new HomeCollection((HomeCollectionItem) item, locator, this, entityFactory);
        }

        if (item instanceof CollectionItem) {
            if (item.getStamp(CalendarCollectionStamp.class) != null) {
                return new CalendarCollection((CollectionItem) item, locator, this,entityFactory);
            }
            //TODO: see if this is used 
            /*else {
                return new DavCollectionBase((CollectionItem) item, locator, this, entityFactory);
            }*/
        }

        if (item instanceof NoteItem) {
            NoteItem note = (NoteItem) item;
            // don't expose modifications
            if(note.getModifies()!=null) {
                return null;
            }
            else if (item.getStamp(EventStamp.class) != null) {
                return new EventFile(note, locator, this, entityFactory);
            }
            else {
                return new TaskFile(note, locator, this, entityFactory);
            }
        }
        
        if(item instanceof FreeBusyItem) {
            return new FreeBusyFile((FreeBusyItem) item, locator, this, entityFactory);
        }
        if(item instanceof AvailabilityItem) {
            return new AvailabilityFile((AvailabilityItem) item, locator, this, entityFactory);
        }

        return new CustomFile((FileItem) item, locator, this, entityFactory);
	}
}
