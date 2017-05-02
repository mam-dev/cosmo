package org.unitedinternet.cosmo.dav.parallel;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavContent;

public interface CalDavCollection extends CalDavResource{
	  /**
     * Adds a new content item to this resource.
     */
    void addContent(DavContent content, InputContext input) throws CosmoDavException;

    /**
     * Adds a new collection to this resource.
     */
    MultiStatusResponse addCollection(DavCollection collection, DavPropertySet properties) throws CosmoDavException;

    /**
     * Returns the member resource at the given absolute href.
     * @throws DavException 
     */
    DavResource findMember(String href) throws CosmoDavException, DavException;
    
    /**
     * Returns an iterator over all internal members which are themselves collection.
     *
     * @return a {@link DavResourceIterator} over all internal members.
     */
    DavResourceIterator getCollectionMembers();
}
