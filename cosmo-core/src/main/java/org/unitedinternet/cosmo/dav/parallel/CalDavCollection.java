package org.unitedinternet.cosmo.dav.parallel;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.unitedinternet.cosmo.dav.CosmoDavException;

public interface CalDavCollection extends CalDavContentResource{
	  /**
     * Adds a new content item to this resource.
     */
    void addContent(CalDavFile content, InputContext input) throws CosmoDavException;

    /**
     * Adds a new collection to this resource.
     */
    MultiStatusResponse addCollection(CalDavCollection collection, DavPropertySet properties) throws CosmoDavException;

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
