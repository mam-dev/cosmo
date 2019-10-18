package org.unitedinternet.cosmo.dav.impl;

import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.carddav.CarddavConstants;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.EntityFactory;

/**
 * Extends <code>DavCollection</code> to adapt the Cosmo <code>CalendarCollectionItem</code> to the DAV resource model.
 *
 * This class defines the following live properties:
 *
 * <ul>
 * <li><code>CARDDAV:addressbook-descripion</code></li>
 * <li><code>CARDDAV:supported-address-data (protected)</code></li>
 * <li><code>CARDDAV:max-resource-size</code> (protected)</li>
 * <li><code>CARDDAV:principal-address</code></li>
 * <li><code>CARDDAV:supported-collation-set(maybe protected)</code></li>
 * </ul>
 *
 * @see DavCollection
 * @see CalendarCollectionItem
 */
public class DavAddressbookCollection extends DavCollectionBase implements CarddavConstants {

    static {
        registerLiveProperty(ADDRESSBOOKDESCRIPTION);
        registerLiveProperty(SUPPORTEDADDRESSDATA);
        registerLiveProperty(MAXRESOURCESIZE);
        registerLiveProperty(ADDRESSBOOKHOMESET);
        registerLiveProperty(PRINCIPALADDRESS);
        registerLiveProperty(SUPPORTEDCOLLATIONSET);


    }

    public DavAddressbookCollection(CollectionItem collection, DavResourceLocator locator, DavResourceFactory factory, EntityFactory entityFactory) throws CosmoDavException {
        super(collection, locator, factory, entityFactory);
    }

    public DavAddressbookCollection(DavResourceLocator locator, DavResourceFactory factory, EntityFactory entityFactory) throws CosmoDavException {
        super(locator, factory, entityFactory);
        getItem().addStamp(entityFactory.createAddressbookCollectionStamp((CollectionItem) getItem()));
    }
}
