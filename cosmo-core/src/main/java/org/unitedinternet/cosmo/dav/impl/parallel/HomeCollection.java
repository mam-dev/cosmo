package org.unitedinternet.cosmo.dav.impl.parallel;

import java.util.ArrayList;
import java.util.List;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceFactory;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceLocator;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.Item;

public class HomeCollection extends CalDavCollectionBase {

	public HomeCollection(HomeCollectionItem item, CalDavResourceLocator locator,
			CalDavResourceFactory calDavResourceFactory, EntityFactory entityFactory) {
		super(item, locator, calDavResourceFactory, entityFactory);
	}

	@Override
	public WebDavResource findMember(String href) throws CosmoDavException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DavResourceIterator getMembers() {
		List<org.apache.jackrabbit.webdav.DavResource> members = new ArrayList<org.apache.jackrabbit.webdav.DavResource>();
		try {
			for (Item memberItem : ((CollectionItem) getItem()).getChildren()) {
				DavResource resource = memberToResource(memberItem);
				if (resource != null) {
					members.add(resource);
				}
			}

			// for now scheduling is an option
			members.add(
					memberToResource(TEMPLATE_USER_INBOX.bindAbsolute(getLocator().getHref(true), getResourcePath())));
			members.add(
					memberToResource(TEMPLATE_USER_OUTBOX.bindAbsolute(getLocator().getHref(true), getResourcePath())));

			return new DavResourceIteratorImpl(members);
		} catch (DavException e) {
			throw new CosmoException(e);
		}
	}

	@Override
	public String getSupportedMethods() {
		return "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, MKTICKET, DELTICKET";
	}

	@Override
	public DavResourceIterator getCollectionMembers() {
		// TODO Auto-generated method stub
		return null;
	}
}
