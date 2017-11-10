package org.unitedinternet.cosmo.dao.subscription;

import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.User;

/**
 * Component that hides the details of an item by replacing text fields with specific text. Used by
 * <code>ContentDaoSubscriptionImpl</code> to hide details when subscription is of type free-busy.
 * 
 * @author daniel grigore
 *
 */
public interface FreeBusyObfuscater {

    public void apply(User owner, ContentItem contentItem);

}
