package org.unitedinternet.cosmo.dav.parallel;

import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.icalendar.ICalendarClientFilterManager;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.service.UserService;

import javassist.NotFoundException;

public interface CalDavResourceFactory extends DavResourceFactory {
	/**
	 * <p>
	 * Resolves a {@link DavResourceLocator} into a {@link WebDavResource}.
	 * </p>
	 * <p>
	 * If the identified resource does not exist and the request method
	 * indicates that one is to be created, returns a resource backed by a
	 * newly-instantiated item that has not been persisted or assigned a UID.
	 * Otherwise, if the resource does not exists, then a
	 * {@link NotFoundException} is thrown.
	 * </p>
	 */
	CalDavResource resolve(CalDavResourceLocator locator, CalDavRequest request) throws CosmoDavException;

	/**
	 * <p>
	 * Resolves a {@link DavResourceLocator} into a {@link WebDavResource}.
	 * </p>
	 * <p>
	 * If the identified resource does not exists, returns <code>null</code>.
	 * </p>
	 */
	CalDavResource resolve(CalDavResourceLocator locator) throws CosmoDavException;

	/**
	 * <p>
	 * Instantiates a <code>WebDavResource</code> representing the
	 * <code>Item</code> located by the given <code>DavResourceLocator</code>.
	 * </p>
	 */
	CalDavResource createResource(CalDavResourceLocator locator, Item item) throws CosmoDavException;

	ContentService getContentService();

	ICalendarClientFilterManager getClientFilterManager();

	CalendarQueryProcessor getCalendarQueryProcessor();

	UserService getUserService();

	CosmoSecurityManager getSecurityManager();
}
