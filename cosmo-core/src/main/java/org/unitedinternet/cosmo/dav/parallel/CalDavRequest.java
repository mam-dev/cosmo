package org.unitedinternet.cosmo.dav.parallel;

import java.util.Date;

import org.apache.abdera.util.EntityTag;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.WebdavRequest;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.model.Ticket;

public interface CalDavRequest extends WebdavRequest {

	EntityTag[] getIfMatch();

	Date getIfModifiedSince();

	EntityTag[] getIfNoneMatch();

	Date getIfUnmodifiedSince();

	DavPropertySet getProppatchSetProperties() throws CosmoDavException;

	DavPropertyNameSet getProppatchRemoveProperties() throws CosmoDavException;

	CalDavResourceLocator getResourceLocator();

	CalDavResourceLocator getDestinationResourceLocator() throws CosmoDavException;

	/**
	 * Return the list of 'set' entries in the MKCALENDAR request body.
	 * 
	 * @return The list of set entries in the MKCALENDAR request.
	 * @throws CosmoDavException
	 *             if the request body could not be parsed
	 */
	DavPropertySet getMkCalendarSetProperties() throws CosmoDavException;

	/**
	 * Return the report information, if any, included in the request.
	 *
	 * @throws DavException
	 *             if there is no report information in the request or if the
	 *             report information is invalid
	 */
	ReportInfo getReportInfo() throws DavException;

	/**
	 * Return a {@link Ticket} representing the information about a ticket to be
	 * created by a <code>MKTICKET</code> request.
	 *
	 * @throws CosmoDavException
	 *             if there is no ticket information in the request or if the
	 *             ticket information exists but is invalid
	 */
	Ticket getTicketInfo() throws CosmoDavException;

	/**
	 * Return the ticket key included in this request, if any. If different
	 * ticket keys are included in the headers and URL, the one from the URL is
	 * used.
	 *
	 * @throws CosmoDavException
	 *             if there is no ticket key in the request.
	 */
	String getTicketKey() throws CosmoDavException;
}
