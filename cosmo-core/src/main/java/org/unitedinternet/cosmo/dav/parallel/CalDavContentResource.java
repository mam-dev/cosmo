package org.unitedinternet.cosmo.dav.parallel;

import java.util.Set;

import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.Ticket;

public interface CalDavContentResource extends CalDavResource{
	Item getItem();

	void setItem(Item item) throws CosmoDavException;
	
	/**
	 * Associates a ticket with this resource and saves it into persistent
	 * storage.
	 */
	void saveTicket(Ticket ticket) throws CosmoDavException;

	/**
	 * Removes the association between the ticket and this resource and deletes
	 * the ticket from persistent storage.
	 */
	void removeTicket(Ticket ticket) throws CosmoDavException;

	/**
	 * @return the ticket with the given id on this resource.
	 */
	Ticket getTicket(String id);

	/**
	 * @return all visible tickets (those owned by the currently authenticated
	 *         user) on this resource, or an empty <code>Set</code> if there are
	 *         no visible tickets.
	 */
	Set<Ticket> getTickets();
}
