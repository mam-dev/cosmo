/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dav.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;
import org.unitedinternet.cosmo.dao.external.UuidExternalGenerator;
import org.unitedinternet.cosmo.dao.subscription.UuidSubscriptionGenerator;
import org.unitedinternet.cosmo.dav.BaseDavTestCase;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.unitedinternet.cosmo.dav.acl.DavPrivilege;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.caldav.property.SupportedCollationSet;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionSubscription;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.TicketType;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionSubscription;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionSubscriptionItem;
import org.unitedinternet.cosmo.model.hibernate.HibTicket;

/**
 * 
 * @author daniel grigore
 *
 */
public class DavCalendarCollectionTest extends BaseDavTestCase implements ExtendedDavConstants, CaldavConstants {

    private DavCalendarCollection instance;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.testHelper.logIn();
        CollectionItem col = this.testHelper.makeAndStoreDummyCalendarCollection();
        DavHomeCollection home = this.testHelper.initializeHomeResource();
        this.instance = (DavCalendarCollection) this.testHelper.findMember(home, col.getName());
        assertNotNull(this.instance);
    }

    @Test
    public void shouldSupportDefaultCollationSet() throws Exception {
        SupportedCollationSet prop = (SupportedCollationSet) this.instance.getProperty(SUPPORTEDCOLLATIONSET);
        assertNotNull(prop);
        assertTrue(prop.isProtected());
        Set<String> collations = prop.getCollations();
        assertNotNull(collations);
        assertTrue(collations.size() == 2);
        for (String c : collations) {
            assertTrue(CalendarUtils.isSupportedCollation(c));
        }
    }

    @Test
    public void shouldHaveOnlyReadPrivilegesForExternalItem() throws Exception {
        this.instance.getItem().setUid(UuidExternalGenerator.get().getNext());
        
        Set<DavPrivilege> privileges = this.instance.getCurrentPrincipalPrivileges();
        assertFalse(privileges.isEmpty());
        assertTrue(privileges.contains(DavPrivilege.READ));
        assertFalse(privileges.contains(DavPrivilege.WRITE));
    }
    
    @Test
    public void shouldHavePrivilegesFromTicketForSubscriptionItem() throws Exception {
        
        HibCollectionSubscriptionItem item = new HibCollectionSubscriptionItem();
        item.setUid(UuidSubscriptionGenerator.get().getNext());
        CollectionSubscription subscription = new HibCollectionSubscription();
        subscription.setTargetCollection(new HibCollectionItem());
        item.setSubscription(subscription);
        Ticket ticket = new HibTicket(TicketType.READ_ONLY);        
        subscription.setTicket(ticket);
        
        this.instance.setItem(item);                
        
        Set<DavPrivilege> privileges = this.instance.getCurrentPrincipalPrivileges();
        assertFalse(privileges.isEmpty());
        assertTrue(privileges.contains(DavPrivilege.READ));
        assertFalse(privileges.contains(DavPrivilege.WRITE));
        
        subscription.setTicket(new HibTicket(TicketType.READ_WRITE));
        privileges = this.instance.getCurrentPrincipalPrivileges();
        assertFalse(privileges.isEmpty());
        assertTrue(privileges.contains(DavPrivilege.READ));
        assertTrue(privileges.contains(DavPrivilege.WRITE));
    }
}
