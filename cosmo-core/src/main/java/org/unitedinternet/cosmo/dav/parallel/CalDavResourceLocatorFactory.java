package org.unitedinternet.cosmo.dav.parallel;

import java.net.URL;

import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.model.User;

public interface CalDavResourceLocatorFactory extends DavLocatorFactory{
	/**
	 * <p>
	 * Returns a locator for the resource at the specified path relative to the
	 * given context URL. The context URL includes enough path information to
	 * identify the dav namespace.
	 * </p>
	 *
	 * @param context
	 *            the URL specifying protocol, authority and unescaped base path
	 * @param path
	 *            the unescaped path of the resource
	 * @return The locator for the resource at the specified path relative to
	 *         the given context URL.
	 */
	DavResourceLocator createResourceLocatorByPath(URL context, String path);
	
	
	/**
	 * <p>
	 * Returns a locator for the resource at the specified URI relative to the
	 * given context URL. The context URL includes enough path information to
	 * identify the dav namespace.
	 * </p>
	 * <p>
	 * If the URI is absolute, its scheme and authority must match those of the
	 * context URL. The URI path must begin with the context URL's path.
	 * </p>
	 *
	 * @param context
	 *            the URL specifying protocol, authority and unescaped base path
	 * @param uri
	 *            the unescaped path of the resource
	 * @throws CosmoDavException
	 *             - if something is wrong this exception is thrown.
	 */
	DavResourceLocator createResourceLocatorByUri(URL context, String uri) throws CosmoDavException;
	
	/**
	 * <p>
	 * Returns a locator for the home collection of the given user relative to
	 * the given context URL.
	 * </p>
	 *
	 * @param context
	 *            the URL specifying protocol, authority and unescaped base path
	 * @param user
	 *            the user
	 * @throws CosmoDavException
	 *             - if something is wrong this exception is thrown.
	 */
	DavResourceLocator createHomeLocator(URL context, User user) throws CosmoDavException;

	/**
	 * <p>
	 * Returns a locator for the principal resource of the given user relative
	 * to the given context URL.
	 * </p>
	 *
	 * @param context
	 *            the URL specifying protocol, authority and unescaped base path
	 * @param user
	 *            the user
	 * @throws CosmoDavException
	 *             - if something is wrong this exception is thrown.
	 */
	DavResourceLocator createPrincipalLocator(URL context, User user) throws CosmoDavException;
}
