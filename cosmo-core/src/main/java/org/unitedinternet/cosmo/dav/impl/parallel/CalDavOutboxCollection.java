package org.unitedinternet.cosmo.dav.impl.parallel;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceFactory;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceLocator;
import org.unitedinternet.cosmo.model.Ticket;

public class CalDavOutboxCollection extends CalDavSchedulingCollection{


	private static final Set<ReportType> REPORT_TYPES = new HashSet<ReportType>();

	
	public CalDavOutboxCollection(CalDavResourceLocator calDavResourceLocator,
									CalDavResourceFactory calDavResourceFactory) {
		super(calDavResourceLocator, calDavResourceFactory);
	}

	@Override
	public String getSupportedMethods() {
		return "OPTIONS, GET, HEAD, POST, DELETE, TRACE, PROPFIND, PROPPATCH, LOCK, UNLOCK, REPORT, ACL";
	}

	public String getDisplayName() {
		return "Outbox";
	}

	@Override
	public DavResourceIterator getMembers() {
		return DavResourceIteratorImpl.EMPTY;
	}


	@Override
	public void removeMember(DavResource member) throws DavException {
		throw new UnsupportedOperationException();
	}

	@Override
	public DavResource getCollection() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void move(DavResource destination) throws DavException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void copy(DavResource destination, boolean shallow) throws DavException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Set<QName> getResourceTypes() {
		HashSet<QName> rt = new HashSet<QName>(2);
		rt.add(RESOURCE_TYPE_COLLECTION);
		rt.add(RESOURCE_TYPE_SCHEDULE_OUTBOX);
		return rt;
	}

	@Override
	protected Set<ReportType> getReportTypes() {
		return REPORT_TYPES;
	}

	
}