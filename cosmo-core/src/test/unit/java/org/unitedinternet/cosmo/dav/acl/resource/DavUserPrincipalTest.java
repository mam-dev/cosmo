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
import org.junit.Assert;
import org.junit.Test;
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
        Assert.assertNotNull("No displayname property", displayName);
        Assert.assertTrue("Empty displayname ",
                    ! StringUtils.isBlank(displayName.getDisplayName()));

        ResourceType resourceType = (ResourceType)
            p.getProperty(DavPropertyName.RESOURCETYPE);
        Assert.assertNotNull("No resourcetype property", resourceType);
        boolean foundPrincipalQname = false;
        for (QName qname : resourceType.getQnames()) {
            if (qname.equals(RESOURCE_TYPE_PRINCIPAL)) {
                foundPrincipalQname = true;
                break;
            }
        }
        Assert.assertTrue("Principal qname not found", foundPrincipalQname);

        // 4.1
        AlternateUriSet alternateUriSet = (AlternateUriSet)
            p.getProperty(ALTERNATEURISET);
        Assert.assertNotNull("No alternate-uri-set property", alternateUriSet);
        Assert.assertTrue("Found hrefs for alternate-uri-set",
                   alternateUriSet.getHrefs().isEmpty());

        // 4.2
        PrincipalUrl principalUrl = (PrincipalUrl)
            p.getProperty(PRINCIPALURL);
        Assert.assertNotNull("No principal-URL property", principalUrl);
        Assert.assertEquals("principal-URL value not the same as locator href",
                     p.getResourceLocator().getHref(false),
                     principalUrl.getHref());

        // 4.4
        GroupMembership groupMembership = (GroupMembership)
            p.getProperty(GROUPMEMBERSHIP);
        Assert.assertNotNull("No group-membership property", groupMembership);
        Assert.assertTrue("Found hrefs for group-membership",
                   groupMembership.getHrefs().isEmpty());
    }
}
