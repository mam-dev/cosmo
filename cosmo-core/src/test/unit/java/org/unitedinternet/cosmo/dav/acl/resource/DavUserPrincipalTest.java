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
package org.unitedinternet.cosmo.dav.acl.resource;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.webdav.property.DavPropertyName;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.unitedinternet.cosmo.dav.BaseDavTestCase;
import org.unitedinternet.cosmo.dav.acl.AclConstants;
import org.unitedinternet.cosmo.dav.acl.property.AlternateUriSet;
import org.unitedinternet.cosmo.dav.acl.property.GroupMembership;
import org.unitedinternet.cosmo.dav.acl.property.PrincipalUrl;
import org.unitedinternet.cosmo.dav.property.DisplayName;
import org.unitedinternet.cosmo.dav.property.ResourceType;

import javax.xml.namespace.QName;

/**
 * Test case for <code>DavUserPrincipal</code>.
 */
public class DavUserPrincipalTest extends BaseDavTestCase implements AclConstants {
    // all section references are from RFC 3744

    /**
     * Tests load properties.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testLoadProperties() throws Exception {
        //initialize it on order to be able to run test
        testHelper.getSecurityManager().initiateSecurityContext("test", "test");

        DavUserPrincipal p = testHelper.getPrincipal(testHelper.getUser());


        // section 4

        DisplayName displayName = (DisplayName)
            p.getProperty(DavPropertyName.DISPLAYNAME);
        assertNotNull(displayName, "No displayname property");
        assertTrue(! StringUtils.isBlank(displayName.getDisplayName()), "Empty displayname ");

        ResourceType resourceType = (ResourceType)
            p.getProperty(DavPropertyName.RESOURCETYPE);
        assertNotNull(resourceType, "No resourcetype property");
        boolean foundPrincipalQname = false;
        for (QName qname : resourceType.getQnames()) {
            if (qname.equals(RESOURCE_TYPE_PRINCIPAL)) {
                foundPrincipalQname = true;
                break;
            }
        }
        assertTrue(foundPrincipalQname, "Principal qname not found");

        // 4.1
        AlternateUriSet alternateUriSet = (AlternateUriSet)
            p.getProperty(ALTERNATEURISET);
        assertNotNull(alternateUriSet, "No alternate-uri-set property");
        assertTrue(alternateUriSet.getHrefs().isEmpty(), "Found hrefs for alternate-uri-set");

        // 4.2
        PrincipalUrl principalUrl = (PrincipalUrl)
            p.getProperty(PRINCIPALURL);
        assertNotNull(principalUrl, "No principal-URL property");
        assertEquals(p.getResourceLocator().getHref(false),
                     principalUrl.getHref(),
                     "principal-URL value not the same as locator href");

        // 4.4
        GroupMembership groupMembership = (GroupMembership)
            p.getProperty(GROUPMEMBERSHIP);
        assertNotNull(groupMembership, "No group-membership property");
        assertTrue(groupMembership.getHrefs().isEmpty(), "Found hrefs for group-membership");
    }
}
