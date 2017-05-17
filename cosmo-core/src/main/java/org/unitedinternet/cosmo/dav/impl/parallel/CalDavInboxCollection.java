package org.unitedinternet.cosmo.dav.impl.parallel;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceFactory;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceLocator;
import org.unitedinternet.cosmo.model.Ticket;

public class CalDavInboxCollection extends CalDavSchedulingCollection{
	private static final Set<ReportType> REPORT_TYPES = new HashSet<ReportType>();
    static {
        registerLiveProperty(DavPropertyName.DISPLAYNAME);
        registerLiveProperty(DavPropertyName.ISCOLLECTION);
        registerLiveProperty(DavPropertyName.RESOURCETYPE);
        registerLiveProperty(DavPropertyName.GETETAG);
    }

	public CalDavInboxCollection(CalDavResourceLocator calDavResourceLocator,
			CalDavResourceFactory calDavResourceFactory) {
		super(calDavResourceLocator, calDavResourceFactory);
		
	}
	public String getSupportedMethods() {
        return "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, REPORT";
    }
	
	public String getDisplayName() {
        return "Inbox";
    }
	
	protected Set<QName> getResourceTypes() {
        HashSet<QName> rt = new HashSet<QName>(2);
        rt.add(RESOURCE_TYPE_COLLECTION);
        rt.add(RESOURCE_TYPE_SCHEDULE_INBOX);
        return rt;
    }
	
	public Set<ReportType> getReportTypes() {
        return REPORT_TYPES;
    }
}