package org.unitedinternet.cosmo.dav.parallel;

import org.apache.jackrabbit.webdav.version.DeltaVResource;
import org.unitedinternet.cosmo.dav.CosmoDavException;

/**
 * This is the extension point between WEBDAV and CALDAV
 * @author cdobrota
 *
 */

public interface CalDavResource extends DeltaVResource {

	String COMPLIANCE_CLASS = "1, 3, access-control, calendar-access, ticket";

	String COMPLIANCE_CLASS_SCHEDULING = "1, 3, access-control, calendar-access, calendar-schedule, calendar-auto-schedule, ticket";

	String getETag();
	
	CalDavResource getParent() throws CosmoDavException;
}
