package org.unitedinternet.cosmo.dav.parallel;

import java.net.URL;

import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;

/**
 * <p>
 * The interface for classes that encapsulate information about the location of
 * a {@link CalDavResource}.
 * </p>
 * <p>
 * The URL of a resource is defined as
 * <code>{prefix}{base path}{dav path}</code>. The prefix is the leading portion
 * of the URL that includes the scheme and authority sections of the URL. The
 * base path is the leading portion of the URL path that locates the root dav
 * namespace. The dav path is the trailing portion of the URL path that locates
 * the resource within the dav namespace.
 * </p>
 */
public interface CalDavResourceLocator extends DavResourceLocator{
	/**
	 * Returns the escaped URL of the resource that can be used as the value for
	 * a <code>DAV:href</code> property. The href is either absolute or
	 * absolute-path relative (i.e. including the base path but not the prefix).
	 * Appends a trailing slash if <code>isCollection</code>.
	 */
	String getHref(boolean absolute, boolean isCollection);
	
	/**
	 * Returns a URL representing the href as per {@link getHref(boolean,
	 * boolean)}.
	 */
	URL getUrl(boolean absolute, boolean isCollection);
	
	/**
	 * Returns the absolute escaped URL corresponding to the base path that can
	 * be used as the value for a <code>DAV:href</code> property.
	 */
	String getBaseHref();
	
	/**
	 * Returns the escaped URL corresponding to the base path that can be used
	 * as the value for a <code>DAV:href</code> property. The href is either
	 * absolute or absolute-path relative (i.e. including the base path but not
	 * the prefix).
	 */
	String getBaseHref(boolean absolute);

	/**
	 * Returns the trailing portion of the path that identifies the resource
	 * within the dav namespace.
	 */
	String getPath();

	/**
	 * Returns a URL that provides context for resolving other locators. The
	 * context URL has the scheme and authority defined by this locator's
	 * prefix, and its path is the same as this locator's base path.
	 */
	URL getContext();

	/**
	 * Returns a locator identifying the parent resource.
	 */
	CalDavResourceLocator getParentLocator();

	@Override
	CalDavResourceLocatorFactory getFactory();
}
