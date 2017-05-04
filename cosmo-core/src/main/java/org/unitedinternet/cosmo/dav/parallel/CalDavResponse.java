package org.unitedinternet.cosmo.dav.parallel;

import java.io.IOException;

import org.apache.jackrabbit.webdav.WebdavResponse;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.impl.DavItemResource;

public interface CalDavResponse extends WebdavResponse {
	
	void sendDavError(CosmoDavException e) throws IOException;

	/**
	 * Send the <code>ticketdiscovery</code> response to a <code>MKTICKET</code>
	 * request.
	 *
	 * @param resource
	 *            the resource on which the ticket was created
	 * @param ticketId
	 *            the id of the newly created ticket
	 */
	void sendMkTicketResponse(DavItemResource resource, String ticketId) throws CosmoDavException, IOException;
}
