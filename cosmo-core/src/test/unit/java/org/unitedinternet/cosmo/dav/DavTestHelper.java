/*
 * Copyright 2006-2007 Open Source Applications Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.dav;

import java.net.URL;

import org.junit.Before;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.MockHelper;
import org.unitedinternet.cosmo.dav.impl.parallel.CalDavCollectionBase;
import org.unitedinternet.cosmo.dav.impl.parallel.CalDavUserPrincipal;
import org.unitedinternet.cosmo.dav.impl.parallel.CalendarCollection;
import org.unitedinternet.cosmo.dav.impl.parallel.DefaultCalDavResourceFactory;
import org.unitedinternet.cosmo.dav.impl.parallel.DefaultCalDavResourceLocatorFactory;
import org.unitedinternet.cosmo.dav.impl.parallel.EventFile;
import org.unitedinternet.cosmo.dav.impl.parallel.HomeCollection;
import org.unitedinternet.cosmo.dav.parallel.CalDavCollection;
import org.unitedinternet.cosmo.dav.parallel.CalDavResource;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceFactory;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceLocator;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceLocatorFactory;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.util.UriTemplate;

/**
 * DavTestHelper.
 */
public class DavTestHelper extends MockHelper implements ExtendedDavConstants {

    private URL baseUrl;
    
    
    //PARALLEL
    private CalDavResourceFactory calDavResourceFactory;
    private CalDavResourceLocatorFactory calDavResourceLocatorFactory;
    private CalDavResourceLocator calDavHomeLocator;

    /**
     * Constructor.
     */
    public DavTestHelper() {
        super();

        
        
        //PARALLEL
        calDavResourceFactory = new DefaultCalDavResourceFactory(getContentService(), getUserService(), getSecurityManager(), getEntityFactory(), getCalendarQueryProcessor(), getClientFilterManager(), getUserIdentitySupplier(), true);
        calDavResourceLocatorFactory = new DefaultCalDavResourceLocatorFactory();
        
        try {
            baseUrl = new URL("http", "localhost", -1, "/test");
        } catch (Exception e) {
            throw new CosmoException(e);
        }
    }

    /**
     * SetUp.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();

        String path = TEMPLATE_HOME.bind(false, getUser().getUsername());
        
        calDavHomeLocator = calDavResourceLocatorFactory.createResourceLocatorByUri(baseUrl, path);
    }

    /**
     * Gets resource factory.
     * @return Dav resource factory.
     */
    public CalDavResourceFactory getResourceFactory() {
        return this.calDavResourceFactory;
    }

    /**
     * Gets resource locator factory.
     * @return The dav resource locator factory.
     */
    public CalDavResourceLocatorFactory getResourceLocatorFactory() {
        return this.calDavResourceLocatorFactory;
    }

    /**
     * Gets home locator.
     * @return The dav resource locator.
     */
    public CalDavResourceLocator getHomeLocator() {
        return this.calDavHomeLocator;
    }

    /**
     * Initializes home resource.
     * @return The dav home collection.
     * @throws CosmoDavException - if something is wrong this exception is thrown.
     */
    public CalDavCollectionBase initializeHomeResource()
        throws CosmoDavException {
        return new HomeCollection(getHomeCollection(), calDavHomeLocator,
                                     calDavResourceFactory, getEntityFactory());
    }
    
    public HomeCollection newCalDavHomeCollection(){
    	return new HomeCollection(getHomeCollection(), calDavHomeLocator, calDavResourceFactory, getEntityFactory());
    }

    /**
     * Gets principal.
     * @param user The user.
     * @return The dav user principal.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public CalDavUserPrincipal getPrincipal(User user)
        throws Exception {
        String path = TEMPLATE_USER.bind(false, user.getUsername());
        CalDavResourceLocator locator =
            calDavResourceLocatorFactory.createResourceLocatorByPath(baseUrl, path);
        return new CalDavUserPrincipal(user, locator, calDavResourceFactory, getUserIdentitySupplier());
    }

    /**
     * Creates test context.
     * @return The dav test context.
     */
    public DavTestContext createTestContext() {
        return new DavTestContext(calDavResourceLocatorFactory);
    }

    /**
     * Creates locator.
     * @param path The path.
     * @return The dav resource locator.
     */
    public CalDavResourceLocator createLocator(String path) {
        return this.calDavResourceLocatorFactory.createResourceLocatorByPath(this.calDavHomeLocator.getContext(), path);
            
    }
    
    public CalDavResourceLocator createCalDavResourceLocator(String path){
    	return (CalDavResourceLocator) calDavResourceLocatorFactory.createResourceLocator(calDavHomeLocator.getBaseHref(), path);
    }

    /**
     * Creates member locator.
     * @param locator The locator.
     * @param segment The segment.
     * @return The dav resource locator.
     */
    public CalDavResourceLocator createMemberLocator(CalDavResourceLocator locator, String segment) {
        String path = locator.getPath() + "/" + segment;
        return calDavResourceLocatorFactory.
            createResourceLocatorByPath(locator.getContext(), path);
    }
       

    /**
     * Finds member.
     * @param collection The collection.
     * @param segment The segment.
     * @return The dav resource.
     * @throws CosmoDavException - if something is wrong this exception is thrown.
     */
    public CalDavResource findMember(CalDavCollection collection,
                                  String segment)
        throws CosmoDavException {
        String href = collection.getCalDavResourceLocator().getHref(false) + "/" +
            UriTemplate.escapeSegment(segment);
        return collection.findMember(href);
    }

    /**
     * Initializes dav calendar collection.
     * @param name The name.
     * @return The dav calendar collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public CalendarCollection initializeDavCalendarCollection(String name)
        throws Exception {
        CollectionItem collection = (CollectionItem)
            getHomeCollection().getChildByName(name);
        if (collection == null) {
            collection = makeAndStoreDummyCalendarCollection(name);
        }
        CalDavResourceLocator locator =  createMemberLocator(calDavHomeLocator, collection.getName());
        return new CalendarCollection(collection, locator, calDavResourceFactory, getEntityFactory());
        
    }

    /**
     * Initializes dav event.
     * @param parent The parent.
     * @param name The name.
     * @return Dav event.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public EventFile initializeDavEvent(CalendarCollection parent,
                                       String name)
        throws Exception {
        CollectionItem collection = (CollectionItem) parent.getItem();
        NoteItem item = (NoteItem) collection.getChildByName(name);
        if (item == null) {
            item = makeAndStoreDummyItem(collection, name);
        }
        CalDavResourceLocator locator =
            createMemberLocator(parent.getCalDavResourceLocator(), item.getName());
        return new EventFile(item, locator, calDavResourceFactory, getEntityFactory());
    }

	public CalDavResourceFactory getCalDavResourceFactory() {
		return calDavResourceFactory;
	}

	public CalDavResourceLocatorFactory getCalDavResourceLocatorFactory() {
		return calDavResourceLocatorFactory;
	}

	public CalDavResourceLocator getCalDavHomeLocator() {
		return calDavHomeLocator;
	}
}
