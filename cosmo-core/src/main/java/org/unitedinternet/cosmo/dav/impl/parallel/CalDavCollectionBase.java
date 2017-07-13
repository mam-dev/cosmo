package org.unitedinternet.cosmo.dav.impl.parallel;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.unitedinternet.cosmo.dav.LockedException;
import org.unitedinternet.cosmo.dav.parallel.CalDavCollection;
import org.unitedinternet.cosmo.dav.parallel.CalDavContentResource;
import org.unitedinternet.cosmo.dav.parallel.CalDavFile;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceFactory;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceLocator;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionLockedException;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.util.CosmoQName;

public class CalDavCollectionBase extends CalDavContentResourceBase implements CalDavCollection, ExtendedDavConstants {

	public CalDavCollectionBase(Item item, CalDavResourceLocator calDavResourceLocator,
			CalDavResourceFactory calDavResourceFactory, EntityFactory entityFactory) {
		super(item, calDavResourceLocator, calDavResourceFactory, entityFactory);
	}

	public static final CosmoQName RESOURCE_TYPE_COLLECTION = new CosmoQName(NAMESPACE.getURI(), XML_COLLECTION,
			NAMESPACE.getPrefix());

	private List<DavResource> members;

	protected Set<QName> getResourceTypes() {
		HashSet<QName> rt = new HashSet<QName>(1);
		rt.add(RESOURCE_TYPE_COLLECTION);
		return rt;
	}

	protected Set<String> getDeadPropertyFilter() {
		return DEAD_PROPERTY_FILTER;
	}

	@Override
	protected void updateItem() throws CosmoDavException {
		try {
			getContentService().updateCollection((CollectionItem) getItem());
		} catch (CollectionLockedException e) {
			throw new LockedException();
		}
	}

	@Override
	public boolean isCollection() {
		return true;
	}

	public DavResource findMember(String href) throws DavException {
		return memberToResource(href);
	}

	protected DavResource memberToResource(String uri) throws DavException {
		return calDavResourceFactory.createResource(calDavResourceLocator, getItem());
	}

	protected DavResource memberToResource(Item item) throws DavException {
		String path;
		try {
			path = getResourcePath() + "/" + URLEncoder.encode(item.getName(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new CosmoDavException(e);
		}

		DavResourceLocator locator = calDavLocatorFactory.createResourceLocator(null, path);
		return calDavResourceFactory.createResource(locator, getSession());
	}

	public DavResourceIterator getCollectionMembers() {
		try {
			Set<CollectionItem> collectionItems = getContentService().findCollectionItems((CollectionItem) getItem());
			for (Item memberItem : collectionItems) {
				DavResource resource = memberToResource(memberItem);
				if (resource != null) {
					members.add(resource);
				}
			}
			return new DavResourceIteratorImpl(members);
		} catch (DavException e) {
			throw new CosmoException(e);
		}
	}

	protected void saveContent(CalDavFile member) throws CosmoDavException {
		CollectionItem collection = (CollectionItem) getItem();
		ContentItem content =  (ContentItem)member.getItem();

		try {
			if (content.getCreationDate() != null) {

				content = getContentService().updateContent(content);
			} else {

				content = getContentService().createContent(collection, content);
			}
		} catch (CollectionLockedException e) {
			throw new LockedException();
		}

		member.setItem(content);
	}

	@Override
	public MultiStatusResponse addCollection(CalDavCollection collection, DavPropertySet properties)
			throws CosmoDavException {
		if (!(collection instanceof CalDavCollectionBase)) {
			throw new IllegalArgumentException("Expected instance of :[" + CalDavCollectionBase.class.getName() + "]");
		}

		CalDavCollectionBase base = (CalDavCollectionBase) collection;
		base.populateItem(null);
		MultiStatusResponse msr = base.populateAttributes(properties);
		if (!hasNonOK(msr)) {
			saveSubcollection(base);
			members.add(base);
		}
		return msr;
	}

	protected void saveSubcollection(CalDavCollection member) throws CosmoDavException {
		CollectionItem collection = (CollectionItem) getItem();
		CollectionItem subcollection = (CollectionItem) member.getItem();

		try {
			subcollection = getContentService().createCollection(collection, subcollection);
			member.setItem(subcollection);
		} catch (CollectionLockedException e) {
			throw new LockedException();
		}
	}

	@Override
	public void addContent(CalDavContentResource content, InputContext context) throws CosmoDavException {
		if (!(content instanceof CalDavFileBase)) {
			throw new IllegalArgumentException("Expected instance of : [" + CalDavFileBase.class.getName() + "]");
		}

		CalDavFileBase base = (CalDavFileBase) content;
		base.populateItem(context);
		saveContent(base);
		members.add(base);
	}

	@Override
	public String getSupportedMethods() {
		// If resource doesn't exist, then options are limited
		if (!exists()) {
			return "OPTIONS, TRACE, PUT, MKCOL, MKCALENDAR";
		}
		return "OPTIONS, GET, HEAD, PROPFIND, PROPPATCH, TRACE, COPY, DELETE, MOVE, MKTICKET, DELTICKET, REPORT";

	}
}